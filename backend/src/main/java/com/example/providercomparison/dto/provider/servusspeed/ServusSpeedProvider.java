package com.example.providercomparison.dto.provider.servusspeed;

import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
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
public class ServusSpeedProvider {

    private final ServusSpeedClient client;

    /* ---------- retry policy used everywhere ------------------------ */
    private static final Retry RETRY_POLICY =
            Retry.backoff(2, Duration.ofSeconds(1))      // 2 retries  ➜ 3 attempts
                    .maxBackoff(Duration.ofSeconds(10))
                    .jitter(0.2)
                    .doBeforeRetry(sig ->
                            log.debug("ServusSpeed retry #{} after {} – {}",
                                    sig.totalRetries() + 1,
                                    sig.failure().toString()));

    /* ---------- caching (unchanged) --------------------------------- */
    private final Cache<AddressKey, Mono<List<String>>> addressCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofHours(12))
                    .maximumSize(5_000)
                    .build();

    private final Cache<String, Mono<OfferResponseDto>> detailCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofHours(12))
                    .maximumSize(10_000)
                    .build();

    /* ---------- tuning knobs (unchanged) ---------------------------- */
    @Value("${provider.servusspeed.delay-ms:100}")  private long delayMs;
    @Value("${provider.servusspeed.parallelism:6}") private int  parallelism;
    @Value("${provider.servusspeed.timeout-sec:45}") private long timeoutSec;

    /* ================================================================ */
   // @Override
    public Flux<OfferResponseDto> offers(SearchCriteria criteria) {

        return productsForAddress(criteria)               // Mono<List<String>>
                .flatMapMany(Flux::fromIterable)          // Flux<String>
                .delayElements(Duration.ofMillis(delayMs))
                .flatMap(id -> detailsForId(id, criteria), parallelism)
                .onErrorContinue((e, __) ->
                        log.warn("ServusSpeed stream error: {}", e.toString()));
    }

    /* ---------------- address‑level cache --------------------------- */
    private Mono<List<String>> productsForAddress(SearchCriteria c) {
        AddressKey key = new AddressKey(
                c.street(), c.houseNumber(), c.postalCode(), c.city());

        return addressCache.get(key,
                k -> client.getAvailableProducts(c)
                        .timeout(Duration.ofSeconds(timeoutSec))
                        .retryWhen(RETRY_POLICY)
                        .onErrorResume(e -> {
                            log.warn("ServusSpeed addr‑lookup failed for {}: {}",
                                    k, e.toString());
                            return Mono.just(Collections.emptyList());
                        })
                        .doOnNext(list ->
                                log.debug("→ cached {} IDs for {}", list.size(), k))
                        .cache());
    }

    /* ---------------- product‑detail cache -------------------------- */
    private Mono<OfferResponseDto> detailsForId(String id, SearchCriteria c) {
        return detailCache.get(id,
                key -> fetchOneFromApi(key, c)
                        .doOnNext(__ ->
                                log.debug("→ cached details for id={}", key))
                        .cache());
    }

    /* ---------------- single detail call (+ retry) ------------------ */
    private Mono<OfferResponseDto> fetchOneFromApi(String id, SearchCriteria c) {
        return client.getProductDetails(id, c)
                .timeout(Duration.ofSeconds(timeoutSec))
                .retryWhen(RETRY_POLICY)
                .map(d -> ServusSpeedMapper.toDto(id, d))
                .onErrorResume(e -> {
                    log.warn("ServusSpeed detail‑call failed (id={}): {}", id,
                            e.toString());
                    return Mono.empty();             // skip this ID, continue
                });
    }

    /* ------------ value object for the address cache ---------------- */
    private static record AddressKey(String street,
                                     String houseNumber,
                                     String postalCode,
                                     String city) {}
}
