package com.example.providercomparison.dto.provider.pingperfect.model;

public record PricingDetails(
        int monthlyCostInCent,
        String installationService     // "INCLUDED" | "NONE"
) {
}
