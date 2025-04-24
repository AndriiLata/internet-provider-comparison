package com.example.providercomparison.dto.provider.servusspeed.model;

import java.util.List;
/*
* Here I get the responses which product IDs are available for the given address
 */
public record InternetOfferResponse(
        List<String> availableProducts
) {
}
