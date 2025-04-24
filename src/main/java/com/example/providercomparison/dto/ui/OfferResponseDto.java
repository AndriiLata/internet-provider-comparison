package com.example.providercomparison.dto.ui;

public record OfferResponseDto(
        String productId,
        String provider,
        int speed,               // mbps
        int monthlyCostInCent,
        Integer monthlyCostAfter24mInCent,
        int durationInMonths,
        String connectionType,   // "DSL" …
        boolean tvIncluded,
        boolean installationService,
        Integer voucherValueInCent,      // expose absolute value only
        String voucherType,              // "ABSOLUTE" or "PERCENTAGE"
        Integer discountInCent           // ServusSpeed
) {
}
