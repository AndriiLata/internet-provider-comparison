package com.example.providercomparison.dto.provider.pingperfect;

import com.example.providercomparison.dto.provider.pingperfect.model.InternetProduct;
import com.example.providercomparison.dto.ui.SearchCriteria;
import reactor.core.publisher.Flux;

public interface PingPerfectClient {
    Flux<InternetProduct> getProducts(SearchCriteria criteria);
}
