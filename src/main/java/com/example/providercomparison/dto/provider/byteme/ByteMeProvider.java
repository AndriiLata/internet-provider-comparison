package com.example.providercomparison.dto.provider.byteme;

import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class ByteMeProvider implements OfferProvider {

    private final ByteMeClient  client;
    private final ByteMeMapper  mapper;

    @Override
    public Flux<OfferResponseDto> offers(SearchCriteria criteria) {
        return client.fetchOffers(criteria)
                .map(mapper::toDto)
                // ByteMe bug: same offer returned multiple times
                .distinct(OfferResponseDto::productId);
    }
}
