package com.example.providercomparison.service;

import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ShareLinkService {

    /** create DB row & return the generated UUID */
    Mono<UUID> createSession(SearchCriteria criteria);

    /** persists every offer but leaves the original Flux unmodified */
    Flux<OfferResponseDto> saveOffers(UUID sessionId, Flux<OfferResponseDto> offers);

    /** lazy-load all offers stored for that session */
    Flux<OfferResponseDto> offersForSession(UUID sessionId);
}
