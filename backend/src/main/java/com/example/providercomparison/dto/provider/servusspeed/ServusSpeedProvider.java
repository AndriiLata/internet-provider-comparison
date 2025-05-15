package com.example.providercomparison.dto.provider.servusspeed;

import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import com.example.providercomparison.service.ServusSpeedCacheService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServusSpeedProvider implements OfferProvider {

    private final ServusSpeedCacheService cache;

    @Value("${provider.servusspeed.delay-ms:100}")  private long delayMs;
    @Value("${provider.servusspeed.parallelism:6}") private int  parallelism;

    @Override
    public Flux<OfferResponseDto> offers(SearchCriteria criteria) {

        return cache.productIdsForAddress(criteria)          // Mono<List<String>>
                .flatMapMany(ids -> Flux.fromIterable(ids))
                .delayElements(Duration.ofMillis(delayMs))    // ① spread requests
                .flatMap(id -> cache.productDetails(id, criteria),
                        parallelism)                         // ② limit concurrency
                .onErrorContinue((e, __) ->
                        log.warn("ServusSpeed stream error: {}", e.toString()));
    }
}

