package com.example.providercomparison.service;

import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import reactor.core.publisher.Flux;

public interface OfferServiceReactive {
    Flux<OfferResponseDto> offersFromAllProviders(SearchCriteria criteria);
}
