package com.example.providercomparison.dto.provider.byteme;

import com.example.providercomparison.dto.provider.byteme.model.ByteMeCsvOffer;
import com.example.providercomparison.dto.ui.SearchCriteria;
import reactor.core.publisher.Flux;

public interface ByteMeClient {
    Flux<ByteMeCsvOffer> fetchOffers(SearchCriteria criteria);
}
