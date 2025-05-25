package com.example.providercomparison.dto.provider.servusspeed;

import com.example.providercomparison.dto.provider.servusspeed.model.DetailedResponseData;
import com.example.providercomparison.dto.ui.SearchCriteria;
import reactor.core.publisher.Mono;

import java.util.List;


public interface ServusSpeedClient {
    Mono<List<String>> getAvailableProducts(SearchCriteria criteria);
    Mono<DetailedResponseData> getProductDetails   (String productId,
                                                    SearchCriteria criteria);
}

