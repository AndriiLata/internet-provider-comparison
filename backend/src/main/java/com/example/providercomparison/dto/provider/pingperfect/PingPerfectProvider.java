package com.example.providercomparison.dto.provider.pingperfect;

import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class PingPerfectProvider implements OfferProvider {

    private final PingPerfectClient client;
    private final PingPerfectMapper mapper;

    private static final Retry RETRY_3 =
            Retry.backoff(3, Duration.ofMillis(500))          // 1st retry after 0.5 s, 2nd after 1 s, 3rd after 2 s
                    .maxBackoff(Duration.ofSeconds(2))
                    .jitter(0.3)                                 // add 30 % randomness to avoid thundering herd
                    .filter(ex -> !(ex instanceof IllegalArgumentException));

    @Override
    public Flux<OfferResponseDto> offers(SearchCriteria criteria) {

        return client.getProducts(criteria)
                .filter(p -> p.productInfo()   != null
                        && p.pricingDetails() != null)
                .map(mapper::toDto)
                .retryWhen(RETRY_3)
                .onErrorResume(ex -> {
                    log.warn("Ping Perfect failed after 3 retries: {}", ex.toString());
                    return Flux.empty();                      // => OfferServiceImpl.merge keeps running
                });
    }
}
