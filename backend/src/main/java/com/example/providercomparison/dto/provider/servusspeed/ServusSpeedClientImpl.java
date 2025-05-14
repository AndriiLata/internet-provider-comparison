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
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class ServusSpeedClientImpl implements ServusSpeedClient {

    private final WebClient web;

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
                .map(resp -> {
                    if (resp == null || resp.availableProducts() == null) {
                        return Collections.<String>emptyList();   // ⇠ generic fixed
                    }
                    // availableProducts() is List<?>, convert to List<String>
                    return resp.availableProducts().stream()
                            .map(Object::toString)             // or p -> p.id()
                            .toList();                          // Java16+ collector
                })
                .defaultIfEmpty(Collections.<String>emptyList());  // ⇠ same here
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
                .bodyToMono(DetailedResponseData.class);
    }
}


