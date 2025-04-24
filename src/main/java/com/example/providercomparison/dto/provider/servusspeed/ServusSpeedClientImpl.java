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

import java.util.Collections;
import java.util.List;

@Component
public class ServusSpeedClientImpl implements ServusSpeedClient {
    private final WebClient webClient;

    public ServusSpeedClientImpl(
            @Value("${servusspeed.base-url}") String baseUrl,
            @Value("${servusspeed.username}") String user,
            @Value("${servusspeed.password}") String pw
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(h -> h.setBasicAuth(user, pw))
                .build();
    }

    @Override
    public List<String> getAvailableProducts(SearchCriteria criteria) {
        var req = new InternetOfferRequest(
                new RequestAddress(
                        criteria.street(),
                        criteria.houseNumber(),
                        criteria.postalCode(),
                        criteria.city(),
                        "DE"
                )
        );
        var resp = webClient.post()
                .uri("/api/external/available-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(InternetOfferResponse.class)
                .block();
        if(resp == null || resp.availableProducts() == null || resp.availableProducts().isEmpty()) {
            System.out.println("ServusSpeedClientImpl: No products found for the given address.");
            return Collections.emptyList();
        }
        return resp.availableProducts();
    }

    @Override
    public DetailedResponseData getProductDetails(String productId, SearchCriteria criteria) {
        var req = new InternetOfferRequest(
                new RequestAddress(
                        criteria.street(),
                        criteria.houseNumber(),
                        criteria.postalCode(),
                        criteria.city(),
                        "DE"
                )
        );
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/external/product-details/{product}")
                        .build(productId)
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(DetailedResponseData.class)
                .block();
    }
}

