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

@Component
@RequiredArgsConstructor
public class WebWunderProvider implements OfferProvider {

    private static final Logger log =
            LoggerFactory.getLogger(WebWunderProvider.class);

    private final WebWunderClient client;
    private final WebWunderMapper mapper;

    @Override
    public Flux<OfferResponseDto> offers(SearchCriteria criteria) {

        /* 1 ─ decide which connectionEnums to request */
        List<LegacyGetInternetOffers.ConnectionType> types = mapTypes(criteria.connectionType());

        /* 2 ─ call WebWunder once per type and merge the fluxes */
        return Flux.merge(
                types.stream().map(t ->
                        client.fetchOffers(mapper.from(criteria, t))
                                .doOnError(e ->
                                        log.warn("WebWunder {} failed after retries: {}", t, e.toString()))
                                //.onErrorResume(e -> Mono.empty())          // graceful fallback
                                .flatMapMany(out -> Flux.fromIterable(mapper.toDtos(out)))
                ).toList()
        );
    }

    /* helper: blank → all four, otherwise parse the enum */
    private static List<LegacyGetInternetOffers.ConnectionType> mapTypes(String sel) {
        if (sel == null || sel.isBlank()) {
            return List.of(LegacyGetInternetOffers.ConnectionType.values());
        }
        try {
            return List.of(LegacyGetInternetOffers.ConnectionType
                    .valueOf(sel.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {           // unknown string
            return List.of(LegacyGetInternetOffers.ConnectionType.values());
        }
    }
}
