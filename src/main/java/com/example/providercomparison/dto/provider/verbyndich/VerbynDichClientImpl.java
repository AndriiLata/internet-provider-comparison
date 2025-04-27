package com.example.providercomparison.dto.provider.verbyndich;

import com.example.providercomparison.dto.provider.verbyndich.model.VerbynDichResponse;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class VerbynDichClientImpl implements VerbynDichClient {
    private final WebClient webClient;
    private final String apiKey;

    public VerbynDichClientImpl(
            @Value("${provider.verbyndich.base-url}") String baseUrl,
            @Value("${provider.verbyndich.api-key}")   String apiKey
    ) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    @Override
    public List<OfferResponseDto> getOffers(SearchCriteria criteria) {
        String address = String.format("%s;%s;%s;%s",
                criteria.street(),
                criteria.houseNumber(),
                criteria.city(),
                criteria.postalCode()
        );
        List<OfferResponseDto> offers = new ArrayList<>();
        int page = 0;
        while (true) {
            int finalPage = page;
            VerbynDichResponse resp = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/check24/data")
                            .queryParam("apiKey", apiKey)
                            .queryParam("page", finalPage)
                            .build()
                    )
                    .contentType(MediaType.TEXT_PLAIN)
                    .bodyValue(address)
                    .retrieve()
                    .bodyToMono(VerbynDichResponse.class)
                    .block();
            if (resp == null) {
                page++;
                continue;
            }
            offers.add(VerbynDichMapper.toDto(resp));
            if (resp.last()) break;
            page++;
        }
        return offers;
    }

    /** Fetch exactly one page, or null if invalid */
    @Override
    // in VerbynDichClientImpl
    public Mono<VerbynDichResponse> fetchRawPage(SearchCriteria criteria, int page) {
        String address = String.format("%s;%s;%s;%s",
                criteria.street(),
                criteria.houseNumber(),
                criteria.city(),
                criteria.postalCode()
        );

        return webClient.post()
                .uri(uri -> uri
                        .path("/check24/data")
                        .queryParam("apiKey", apiKey)
                        .queryParam("page", page)
                        .build()
                )
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(address)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                        // look for Retry-After header (in seconds), default to 1s
                        String ra = resp.headers().asHttpHeaders()
                                .getFirst("Retry-After");
                        long delay = ra != null ? Long.parseLong(ra) : 1L;
                        return Mono.error(new RateLimitException(Duration.ofSeconds(delay)));
                    }
                    return resp.bodyToMono(VerbynDichResponse.class);
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        // only retry on our marker exception
                        .filter(throwable -> throwable instanceof RateLimitException)
                        // extract server’s suggested delay if present
                        .doBeforeRetry(retrySignal -> {
                            if (retrySignal.failure() instanceof RateLimitException rle) {
                                retrySignal.getClass();
                            }
                        })
                )
                .onErrorResume(WebClientResponseException.class, ex -> {
                    // if after retries you still get an error, swallow it and return empty
                    return Mono.empty();
                });
    }

    // define a tiny marker exception
    public class RateLimitException extends RuntimeException {
        private final Duration retryAfter;
        public RateLimitException(Duration retryAfter) { this.retryAfter = retryAfter; }
        public Duration getRetryAfter() { return retryAfter; }
    }


}
