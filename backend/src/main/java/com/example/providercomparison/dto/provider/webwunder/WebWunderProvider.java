package com.example.providercomparison.dto.provider.webwunder;

import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.provider.webwunder.model.LegacyGetInternetOffers;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class WebWunderProvider implements OfferProvider {

    private static final Logger log = LoggerFactory.getLogger(WebWunderProvider.class);

    private final WebWunderClient client;
    private final WebWunderMapper mapper;

    @Override
    public Flux<OfferResponseDto> offers(SearchCriteria criteria) {

        /* 1 ─ decide which connectionEnums to request */
        List<LegacyGetInternetOffers.ConnectionType> types =
                mapTypes(criteria.connectionTypes());                     // ← changed

        /* 2 ─ call WebWunder once per type and merge the fluxes */
        return Flux.merge(
                types.stream().map(t ->
                        client.fetchOffers(mapper.from(criteria, t))
                                .doOnError(e ->
                                        log.warn("WebWunder {} failed after retries: {}", t, e.toString()))
                                .onErrorResume(e -> Mono.empty())          // graceful fallback
                                .flatMapMany(out -> Flux.fromIterable(mapper.toDtos(out)))
                ).toList()
        );
    }

    /* helper: empty/null → all four, otherwise parse every selected string */
    private static List<LegacyGetInternetOffers.ConnectionType> mapTypes(List<String> sel) {   // ← changed
        if (sel == null || sel.isEmpty()) {
            return List.of(LegacyGetInternetOffers.ConnectionType.values());
        }

        List<LegacyGetInternetOffers.ConnectionType> parsed = sel.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .map(s -> {
                    try {
                        return LegacyGetInternetOffers.ConnectionType.valueOf(s);
                    } catch (IllegalArgumentException ex) {
                        return null;                  // ignore unknown values
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        /* if nothing valid remained, fall back to “all” */
        return parsed.isEmpty()
                ? List.of(LegacyGetInternetOffers.ConnectionType.values())
                : parsed;
    }
}
