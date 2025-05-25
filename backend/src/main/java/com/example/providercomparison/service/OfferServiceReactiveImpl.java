package com.example.providercomparison.service;

import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferServiceReactiveImpl implements OfferServiceReactive {

    private final List<OfferProvider> providers;
    private final RatingService ratingService;

    @Override
    public Flux<OfferResponseDto> offersFromAllProviders(SearchCriteria criteria) {
        return Flux.merge(
                        providers.stream()
                                .map(p -> p.offers(criteria))
                                .toList()
                )
                //place ranking
                .flatMap(offer ->
                        ratingService.averageRating(offer.provider())   // lookup by provider name
                                .map(offer::withAverageRating)     // replace the 0.0
                );
    }
}


