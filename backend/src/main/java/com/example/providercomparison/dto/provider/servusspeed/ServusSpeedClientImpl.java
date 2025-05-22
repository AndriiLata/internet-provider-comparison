package com.example.providercomparison.dto.provider.servusspeed;

import com.example.providercomparison.dto.provider.servusspeed.model.DetailedResponseData;
import com.example.providercomparison.dto.provider.servusspeed.model.InternetOfferRequest;
import com.example.providercomparison.dto.provider.servusspeed.model.InternetOfferResponse;
import com.example.providercomparison.dto.provider.servusspeed.model.RequestAddress;
import com.example.providercomparison.dto.ui.SearchCriteria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
public class ServusSpeedClientImpl implements ServusSpeedClient {

    private final WebClient web;

    /**
     * Retry 2 × with exponential back-off starting at 1 s (±20 % jitter)<br>
     * – **always** when we get an {@code IOException} or {@code TimeoutException}<br>
     * – **only** when the server responds with an HTTP 5xx status
     */
    private static final Retry RETRY_POLICY =
            Retry.backoff(2, Duration.ofSeconds(1))
                    .jitter(0.2)
                    .filter(t -> t instanceof IOException ||
                            t instanceof TimeoutException ||
                            (t instanceof WebClientResponseException w &&
                                    w.getStatusCode().is5xxServerError()));

    public ServusSpeedClientImpl(
            @Value("${provider.servusspeed.base-url}") String base,
            @Value("${provider.servusspeed.username}")  String user,
            @Value("${provider.servusspeed.password}")  String pw) {

        this.web = WebClient.builder()
                .baseUrl(base)
                .defaultHeaders(h -> h.setBasicAuth(user, pw))
                .build();
    }

    @Override
    public Mono<List<String>> getAvailableProducts(SearchCriteria c) {

        var req = new InternetOfferRequest(
                new RequestAddress(c.street(), c.houseNumber(),
                        c.postalCode(), c.city(), "DE"));

        return web.post()
                .uri("/api/external/available-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(InternetOfferResponse.class)
                .retryWhen(RETRY_POLICY)
                .map(resp -> {
                    if (resp == null || resp.availableProducts() == null) {
                        return Collections.<String>emptyList();
                    }
                    return resp.availableProducts().stream()
                            .map(Object::toString)
                            .toList();
                })
                .defaultIfEmpty(Collections.<String>emptyList());
    }

    @Override
    public Mono<DetailedResponseData> getProductDetails(String id,
                                                        SearchCriteria c) {

        var req = new InternetOfferRequest(
                new RequestAddress(c.street(), c.houseNumber(),
                        c.postalCode(), c.city(), "DE"));

        return web.post()
                .uri("/api/external/product-details/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(DetailedResponseData.class)
                .retryWhen(RETRY_POLICY);
    }
}
