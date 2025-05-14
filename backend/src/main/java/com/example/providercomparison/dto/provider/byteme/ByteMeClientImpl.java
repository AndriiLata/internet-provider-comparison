// src/main/java/com/example/providercomparison/dto/provider/byteme/ByteMeClientImpl.java
package com.example.providercomparison.dto.provider.byteme;

import com.example.providercomparison.dto.provider.byteme.model.ByteMeCsvOffer;
import com.example.providercomparison.dto.ui.SearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ByteMeClientImpl implements ByteMeClient {

    private static final String BASE_URL =
            "https://byteme.gendev7.check24.fun/app/api/products/data";

    private final WebClient webClient;

    public ByteMeClientImpl(
            @Value("${provider.byteme.api-key}") String apiKey,
            WebClient.Builder builder
    ) {
        this.webClient = builder
                .baseUrl(BASE_URL)
                .defaultHeader("X-Api-Key", apiKey)
                .build();
    }

    @Override
    public Flux<ByteMeCsvOffer> fetchOffers(SearchCriteria c) {
        return webClient.get()
                .uri(uri -> uri
                        .queryParam("street",      c.street())
                        .queryParam("houseNumber", c.houseNumber())
                        .queryParam("city",        c.city())
                        .queryParam("plz",         c.postalCode())
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(
                        Retry.backoff(3, Duration.ofMillis(250))
                                .filter(this::isRetriable)
                                .doBeforeRetry(rs -> log.warn(
                                        "ByteMe retry {}/3 – {}",
                                        rs.totalRetries() + 1,
                                        rs.failure().toString()
                                ))
                )
                .onErrorResume(ex -> {
                    log.error("ByteMe provider failed after retries: {}", ex.toString());
                    return Mono.empty();
                })
                .flatMapMany(this::parseCsv)
                .distinct();   // drop exact duplicate lines
    }

    private boolean isRetriable(Throwable t) {
        return t instanceof IOException ||
                t instanceof WebClientResponseException;
    }

    private Flux<ByteMeCsvOffer> parseCsv(String rawCsv) {
        return Flux.fromStream(
                rawCsv.lines()
                        .filter(line -> !line.isBlank())
                        .filter(line -> !line.toLowerCase().startsWith("productid"))
                        .filter(this::hasRequiredColumns)
                        .map(this::toModel)
        );
    }

    private boolean hasRequiredColumns(String line) {
        String[] c = splitCsv(line);
        return  c.length > 5 &&
                !c[2].isBlank() &&     // speed
                !c[3].isBlank() &&     // monthlyCostInCent
                !c[5].isBlank();       // contractDurationInMonths
    }

    private static final Pattern CSV_SPLIT =
            Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    private ByteMeCsvOffer toModel(String line) {
        String[] t = splitCsv(line);

        return new ByteMeCsvOffer(
                unquote(t[0]),                    // productId
                unquote(t[1]),                    // providerName
                Integer.parseInt(t[2].trim()),    // speed
                Integer.parseInt(t[3].trim()),    // monthlyCostInCent
                parseNullableInt(t[4]),           // afterTwoYearsMonthlyCost
                Integer.parseInt(t[5].trim()),    // contractDurationInMonths
                unquote(t[6]),                    // connectionType
                parseBoolean(t[7]),               // installationService
                unquote(t[8]),                    // tvBrand (raw CSV “tv” column)
                parseNullableInt(t[9]),           // limitFrom
                parseNullableInt(t[10]),          // maxAge
                unquote(t[11]),                   // voucherType
                parseNullableInt(t[12])           // voucherValueInCent
        );
    }

    private static Integer parseNullableInt(String s) {
        return (s == null || s.isBlank())
                ? null
                : Integer.valueOf(s.trim());
    }

    private static boolean parseBoolean(String s) {
        return Boolean.parseBoolean((s == null ? "" : s).trim());
    }

    private String[] splitCsv(String line) {
        return CSV_SPLIT.split(line, -1);
    }

    private static String unquote(String s) {
        return s == null ? null : s.replaceAll("^\"|\"$", "").trim();
    }
}
