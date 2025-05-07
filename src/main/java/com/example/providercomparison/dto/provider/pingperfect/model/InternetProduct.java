package com.example.providercomparison.dto.provider.pingperfect.model;

public record InternetProduct(
        String providerName,
        ProductInfo productInfo,
        PricingDetails pricingDetails
) {
}
