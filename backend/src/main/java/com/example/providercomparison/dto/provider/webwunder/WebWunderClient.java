package com.example.providercomparison.dto.provider.webwunder;

import com.example.providercomparison.dto.provider.webwunder.model.LegacyGetInternetOffers;
import com.example.providercomparison.dto.provider.webwunder.model.Output;
import reactor.core.publisher.Mono;

public interface WebWunderClient {
    Mono<Output> fetchOffers(LegacyGetInternetOffers request);
}
