package com.example.providercomparison.dto.provider.servusspeed.model;

public record ServusSpeedProduct(
        String providerName,
        OfferProductInfo productInfo,
        OfferPricingDetails pricingDetails,
        int discount
) {
}
