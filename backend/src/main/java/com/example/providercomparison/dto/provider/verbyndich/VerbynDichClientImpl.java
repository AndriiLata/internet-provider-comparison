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


    // Fetch exactly one page, or null if invalid
    @Override
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
                .retrieve()
                .bodyToMono(VerbynDichResponse.class)
                // 429 / 5xx are retried with exponential back‑off
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetriable));
    }

    // define a tiny marker exception
    private boolean isRetriable(Throwable t) {
        return t instanceof WebClientResponseException w &&
                (w.getStatusCode().is5xxServerError() || w.getRawStatusCode() == 429);
    }


}
