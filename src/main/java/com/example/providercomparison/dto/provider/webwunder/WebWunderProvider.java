package com.example.providercomparison.dto.provider.webwunder;

import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.provider.webwunder.model.LegacyGetInternetOffers;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebWunderProvider implements OfferProvider {

    private final WebWunderClient  client;
    private final WebWunderMapper  mapper;

    @Override
    public Flux<OfferResponseDto> offers(SearchCriteria criteria) {

        /* Decide which connectionEnums to request */
        List<LegacyGetInternetOffers.ConnectionType> types;

        if (criteria.connectionType() == null || criteria.connectionType().isBlank()) {
            types = List.of(
                    LegacyGetInternetOffers.ConnectionType.DSL,
                    LegacyGetInternetOffers.ConnectionType.CABLE,
                    LegacyGetInternetOffers.ConnectionType.FIBER,
                    LegacyGetInternetOffers.ConnectionType.MOBILE
            );
        } else {
            try {
                types = List.of(
                        LegacyGetInternetOffers.ConnectionType.valueOf(
                                criteria.connectionType().trim().toUpperCase())
                );
            } catch (IllegalArgumentException ex) {
                // unknown string ⇒ ask all four so user still gets results
                types = List.of(
                        LegacyGetInternetOffers.ConnectionType.DSL,
                        LegacyGetInternetOffers.ConnectionType.CABLE,
                        LegacyGetInternetOffers.ConnectionType.FIBER,
                        LegacyGetInternetOffers.ConnectionType.MOBILE
                );
            }
        }

        /* Call WebWunder once per type and merge the fluxes */
        return Flux.merge(
                types.stream().map(t ->
                        client.fetchOffers(mapper.from(criteria, t))
                                .flatMapMany(out -> Flux.fromIterable(mapper.toDtos(out)))
                                .onErrorContinue((e, v) ->
                                        System.err.println("WebWunder " + t + " failed: " + e))
                ).toList()
        );
    }
}

