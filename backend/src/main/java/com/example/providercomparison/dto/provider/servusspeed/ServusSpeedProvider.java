package com.example.providercomparison.dto.provider.servusspeed;

import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServusSpeedProvider implements OfferProvider {

    private final ServusSpeedClient client;          // <-- direct, no cache

    @Value("${provider.servusspeed.delay-ms:100}")  private long delayMs;
    @Value("${provider.servusspeed.parallelism:6}") private int  parallelism;

    @Override
    public Flux<OfferResponseDto> offers(SearchCriteria criteria) {

        return client.getAvailableProducts(criteria)              // Mono<List<String>>
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(e -> {
                    log.warn("Error fetching product IDs: {}", e.toString());
                    return Mono.empty();
                })
                .flatMapMany(Flux::fromIterable)                  // -> Flux<String>
                .delayElements(Duration.ofMillis(delayMs))        // spread requests
                .flatMap(id -> client.getProductDetails(id, criteria)
                                .map(d -> ServusSpeedMapper.toDto(id, d)),
                        parallelism)                              // limit concurrency
                .onErrorContinue((e, __) ->
                        log.warn("ServusSpeed stream error: {}", e.toString()));
    }
}
