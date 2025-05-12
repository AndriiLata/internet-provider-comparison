package com.example.providercomparison.dto.provider.pingperfect;

import com.example.providercomparison.config.PingPerfectProperties;
import com.example.providercomparison.dto.provider.pingperfect.model.CompareProductsRequestData;
import com.example.providercomparison.dto.provider.pingperfect.model.InternetProduct;
import com.example.providercomparison.dto.ui.SearchCriteria;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PingPerfectClientImpl implements PingPerfectClient {

    private final WebClient.Builder builder;
    private final ObjectMapper mapper;
    private final PingPerfectProperties props;
    private final PingPerfectSigner signer;

    @Override
    public Flux<InternetProduct> getProducts(SearchCriteria c) {

        // 1. build request body ------------------------------------------------
        CompareProductsRequestData body = new CompareProductsRequestData(
                c.street(),
                c.postalCode(),
                c.houseNumber(),
                c.city(),
                Optional.ofNullable(c.connectionTypes())
                        .orElse(Collections.emptyList())
                        .stream()
                        .anyMatch(type -> type.equalsIgnoreCase("FIBER"))

        );

        String json = toJson(body);

        // 2. sign it -----------------------------------------------------------
        var headers = signer.sign(json);

        // 3. call the API ------------------------------------------------------
        return builder.baseUrl(props.getBaseUrl()).build()
                .post()
                .uri("/internet/angebote/data")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Client-Id", props.getClientId())
                .header("X-Timestamp", String.valueOf(headers.timestamp()))
                .header("X-Signature", headers.signature())
                .bodyValue(json)
                .retrieve()
                .bodyToFlux(InternetProduct.class);
    }

    private String toJson(Object obj) {
        try { return mapper.writeValueAsString(obj); }
        catch (Exception ex) { throw new IllegalStateException(ex); }
    }
}
