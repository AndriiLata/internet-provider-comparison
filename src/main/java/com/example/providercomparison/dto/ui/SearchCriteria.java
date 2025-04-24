package com.example.providercomparison.dto.ui;

public record SearchCriteria(
        String street,
        String houseNumber,
        String city,
        String postalCode,
        boolean wantsFiber,
        Integer maxPriceInCent,
        boolean includeTv,
        boolean includeInstallation
) {
}
