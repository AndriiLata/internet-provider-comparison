package com.example.providercomparison.dto.provider.pingperfect;

import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.provider.pingperfect.exceptions.PingPerfectServerException;
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

    private static final Retry RETRY_3 = Retry.backoff(3, Duration.ofMillis(900))
            .maxBackoff(Duration.ofSeconds(2))
            .jitter(0.5)
            // only retry on server errors (our custom) or network timeouts, not on 4xx
            .filter(ex -> ex instanceof PingPerfectServerException
                    || ex instanceof java.io.IOException);

    @Override
    public Flux<OfferResponseDto> offers(SearchCriteria criteria) {
        return Flux.defer(() ->
                        client.getProducts(criteria)
                                .filter(p -> p.productInfo() != null && p.pricingDetails() != null)
                                .map(mapper::toDto)
                )
                .retryWhen(RETRY_3)
                .onErrorResume(ex -> {
                    log.warn("Ping Perfect ultimately failed: {}", ex.toString());
                    return Flux.empty();
                });
    }
}
