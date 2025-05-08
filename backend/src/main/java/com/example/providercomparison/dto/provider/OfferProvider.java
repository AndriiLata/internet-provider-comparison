package com.example.providercomparison.dto.provider;

import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import reactor.core.publisher.Flux;

public interface OfferProvider {
    Flux<OfferResponseDto> offers(SearchCriteria criteria);
}
