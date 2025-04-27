package com.example.providercomparison.service;


import com.example.providercomparison.dto.provider.servusspeed.ServusSpeedClient;
import com.example.providercomparison.dto.provider.servusspeed.ServusSpeedMapper;
import com.example.providercomparison.dto.provider.verbyndich.VerbynDichClient;
import com.example.providercomparison.dto.provider.verbyndich.VerbynDichMapper;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

@Service
public class OfferServiceImpl implements OfferService {
    private final ServusSpeedClient servus;
    private final VerbynDichClient verbyndich;

    public OfferServiceImpl(ServusSpeedClient servusSpeedClient, VerbynDichClient verbynDichClient) {
        this.servus = servusSpeedClient;
        this.verbyndich = verbynDichClient;
    }

    @Override
    public SseEmitter streamOffers(SearchCriteria criteria) {
        SseEmitter emitter = new SseEmitter(0L);
        ExecutorService exec = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        // 1) Servus Speed – parallel fetch (5 at a time) with retry-on-429
        exec.submit(() -> {
            try {
                var ids = servus.getAvailableProducts(criteria);
                if (ids != null && !ids.isEmpty()) {
                    // Reactor chain that:
                    //  - wraps your blocking getProductDetails(...) in a Mono
                    //  - retries up to 3× on 429 with exponential backoff
                    //  - limits concurrency to 5 in-flight
                    //  - sends each DTO as soon as it arrives
                    Flux.fromIterable(ids)
                            .flatMap(id ->
                                            Mono.fromCallable(() -> servus.getProductDetails(id, criteria))
                                                    .subscribeOn(Schedulers.boundedElastic())
                                                    // retry up to 5 times on HTTP 429
                                                    .retryWhen(Retry.backoff(5, Duration.ofSeconds(1))
                                                            .filter(WebClientResponseException.TooManyRequests.class::isInstance))
                                                    .onErrorResume(ex -> {
                                                        // skip failed ID
                                                        return Mono.empty();
                                                    })
                                                    .map(details -> ServusSpeedMapper.toDto(id, details))
                                                    .doOnNext(dto -> {
                                                        try {
                                                            emitter.send(dto, MediaType.APPLICATION_JSON);
                                                        } catch (IOException e) {
                                                            throw new UncheckedIOException(e);
                                                        }
                                                    }),
                                    5  // max 10 concurrent requests
                            )
                            .doOnError(emitter::completeWithError)
                            .blockLast();  // wait until *all* have been processed
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                latch.countDown();
            }
        });

        // 2) VerbynDich – **sequential** fetch & send per page
        exec.submit(() -> {
            AtomicBoolean anyLast = new AtomicBoolean(false);

            Flux.range(0, 20)
                    // limit in-flight requests to 5 at a time
                    .flatMap(page ->
                                    verbyndich
                                            .fetchRawPage(criteria, page)
                                            .doOnNext(resp -> {
                                                try {
                                                    emitter.send(VerbynDichMapper.toDto(resp),
                                                            MediaType.APPLICATION_JSON);
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                                if (resp.last()) anyLast.set(true);
                                            })
                            , 5
                    )
                    .doOnError(emitter::completeWithError)
                    .doFinally(sig -> {
                        // here you could inspect anyLast.get()
                        latch.countDown();
                    })
                    .subscribe();

        });

        // 3) close when both providers done
        exec.submit(() -> {
            try {
                latch.await();
                emitter.complete();
            } catch (InterruptedException e) {
                emitter.completeWithError(e);
            } finally {
                exec.shutdown();
            }
        });

        return emitter;
    }


}
