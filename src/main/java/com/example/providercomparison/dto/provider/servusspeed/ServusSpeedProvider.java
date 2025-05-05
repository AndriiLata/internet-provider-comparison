package com.example.providercomparison.dto.provider.servusspeed;

import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ServusSpeedProvider implements OfferProvider {

    private final ServusSpeedClient client;
    @Value("${provider.servusspeed.max-concurrency:5}")
    private int maxConcurrency;           // make fan‑out configurable

    @Override
    public Flux<OfferResponseDto> offers(SearchCriteria criteria) {
        return Mono.fromCallable(() -> client.getAvailableProducts(criteria))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .flatMap(id ->
                                Mono.fromCallable(() -> client.getProductDetails(id, criteria))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .map(details -> ServusSpeedMapper.toDto(id, details))
                                        .retryWhen(Retry.backoff(5, Duration.ofSeconds(1))
                                                .filter(WebClientResponseException.TooManyRequests.class::isInstance))
                                        .onErrorResume(e -> Mono.empty()),
                        maxConcurrency
                )
                .onErrorResume(e -> Flux.empty());
    }
}
