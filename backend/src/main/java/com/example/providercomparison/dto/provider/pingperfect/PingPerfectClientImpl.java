package com.example.providercomparison.dto.provider.pingperfect;

import com.example.providercomparison.config.PingPerfectProperties;
import com.example.providercomparison.dto.provider.pingperfect.exceptions.PingPerfectServerException;
import com.example.providercomparison.dto.provider.pingperfect.model.CompareProductsRequestData;
import com.example.providercomparison.dto.provider.pingperfect.model.InternetProduct;
import com.example.providercomparison.dto.provider.pingperfect.exceptions.PingPerfectClientException;
import com.example.providercomparison.dto.ui.SearchCriteria;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PingPerfectClientImpl implements PingPerfectClient {

    private final WebClient pingPerfectWebClient;
    private final ObjectMapper mapper;
    private final PingPerfectProperties props;
    private final PingPerfectSigner signer;

    @Override
    public Flux<InternetProduct> getProducts(SearchCriteria c) {
        return Flux.defer(() -> {
            // build & sign payload on each subscription
            CompareProductsRequestData payload = new CompareProductsRequestData(
                    c.street(),
                    c.postalCode(),
                    c.houseNumber(),
                    c.city(),
                    Optional.ofNullable(c.connectionTypes())
                            .orElse(List.of())
                            .stream()
                            .anyMatch(t -> t.equalsIgnoreCase("FIBER"))
            );
            String json = null;
            try {
                json = mapper.writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            var headers = signer.sign(json);

            // perform request, with explicit 4xx vs 5xx handling
            return pingPerfectWebClient.post()
                    .uri("/internet/angebote/data")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Client-Id", props.getClientId())
                    .header("X-Timestamp", String.valueOf(headers.timestamp()))
                    .header("X-Signature", headers.signature())
                    .bodyValue(json)
                    .exchangeToFlux(response -> {
                        if (response.statusCode().is5xxServerError()) {
                            return response
                                    .bodyToMono(String.class)
                                    .flatMapMany(body ->
                                            Flux.error(new PingPerfectServerException(
                                                    "PingPerfect 5xx: " + response.statusCode() + " - " + body
                                            ))
                                    );
                        } else if (response.statusCode().is4xxClientError()) {
                            return response
                                    .bodyToMono(String.class)
                                    .flatMapMany(body ->
                                            Flux.error(new PingPerfectClientException(
                                                    "PingPerfect 4xx: " + response.statusCode() + " - " + body
                                            ))
                                    );
                        } else {
                            return response.bodyToFlux(InternetProduct.class);
                        }
                    });
        });
    }
}
