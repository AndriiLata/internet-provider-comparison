package com.example.providercomparison.dto.provider.servusspeed.model;

public record OfferProductInfo(
        int speed,
        int contractDurationInMonths,
        String connectionType,
        String tv,
        Integer limitFrom,
        Integer maxAge
) {
}
