package com.example.providercomparison.dto.provider.verbyndich;

import com.example.providercomparison.dto.provider.verbyndich.model.VerbynDichResponse;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import reactor.core.publisher.Mono;

import java.util.List;

public interface VerbynDichClient {

    Mono<VerbynDichResponse> fetchRawPage(SearchCriteria criteria, int page);
}
