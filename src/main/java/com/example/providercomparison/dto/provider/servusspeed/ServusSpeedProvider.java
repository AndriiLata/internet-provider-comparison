package com.example.providercomparison.dto.provider.servusspeed;

import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.provider.servusspeed.model.DetailedResponseData;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.cache.CacheMono;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServusSpeedProvider implements OfferProvider {

    private final ServusSpeedClient client;

    /* ---- tuning knobs (default values, override in application.yml) ---- */
    @Value("${provider.servusspeed.delay-ms:100}")    // gap between *starts*
    private long delayMs;

    @Value("${provider.servusspeed.parallelism:8}")  // max in‑flight calls
    private int parallelism;

    @Value("${provider.servusspeed.timeout-sec:45}") // per‑request hard limit
    private long timeoutSec;
    /* ------------------------------------------------------------------- */

    @Override
    public Flux<OfferResponseDto> offers(SearchCriteria criteria) {

        return client.getAvailableProducts(criteria)      // Mono<List<String>>
                .flatMapMany(Flux::fromIterable)          // Flux<String>
                .delayElements(Duration.ofMillis(delayMs))// orderly pacing
                .flatMap(id -> fetchOne(id, criteria),    // fan‑out
                        parallelism)
                .onErrorContinue((e, __) ->
                        log.warn("ServusSpeed stream error: {}", e.toString()));
    }

    /* ---- detail call wrapped with timeout + 429‑aware retry ------------ */
    private Mono<OfferResponseDto> fetchOne(String id, SearchCriteria c) {

        return client.getProductDetails(id, c)
                .timeout(Duration.ofSeconds(timeoutSec))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .filter(this::is429))
                .map(d -> ServusSpeedMapper.toDto(id, d))
                .onErrorResume(e -> {
                    log.warn("ServusSpeed error (id={}): {}", id, e.toString());
                    return Mono.empty();                 // skip this ID, continue
                });
    }

    private boolean is429(Throwable t) {
        return t instanceof WebClientResponseException w &&
                w.getStatusCode().value() == 429;
    }
}



