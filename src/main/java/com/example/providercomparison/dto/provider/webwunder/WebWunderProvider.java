package com.example.providercomparison.dto.provider.webwunder;

import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.provider.webwunder.model.LegacyGetInternetOffers;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component          // gets auto‑registered in OfferServiceImpl
@RequiredArgsConstructor
public class WebWunderProvider implements OfferProvider {

    private final WebWunderClient client;
    private final WebWunderMapper mapper;

    @Override
    public Flux<OfferResponseDto> offers(SearchCriteria criteria) {

        // decide which connectionEnums to ask WebWunder for
        List<LegacyGetInternetOffers.ConnectionType> types =
                criteria.wantsFiber()
                        ? List.of(LegacyGetInternetOffers.ConnectionType.FIBER)
                        : List.of(
                        LegacyGetInternetOffers.ConnectionType.DSL,
                        LegacyGetInternetOffers.ConnectionType.CABLE,
                        LegacyGetInternetOffers.ConnectionType.MOBILE
                );
        // build a Flux for each connection type and merge them

        return Flux.merge(
                types.stream().map(t ->
                        client.fetchOffers( mapper.from(criteria, t) )
                                .flatMapMany(out -> Flux.fromIterable(mapper.toDtos(out)))
                                .onErrorContinue((e,v) -> System.err.println("WebWunder "+t+" failed: "+e))
                ).toList()
        );
    }
}
