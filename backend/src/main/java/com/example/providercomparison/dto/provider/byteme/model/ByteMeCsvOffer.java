package com.example.providercomparison.dto.provider.byteme.model;

public record ByteMeCsvOffer(
        String productId,
        String providerName,
        int    speed,
        int    monthlyCostInCent,
        Integer monthlyCostAfter24mInCent,
        int    durationInMonths,
        String connectionType,
        boolean installationService,
        String tvBrand,
        Integer limitFrom,
        Integer maxAge,
        String  voucherType,
        Integer voucherValueInCent
) {
}
