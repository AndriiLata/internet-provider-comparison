package com.example.providercomparison.dto.ui;

// AFTER
import java.util.List;

public record SearchCriteria(
        String street,
        String houseNumber,
        String city,
        String postalCode,
        List<String> connectionTypes,
        Integer maxPriceInCent,
        boolean includeTv,
        boolean includeInstallation) {

    public boolean matches(OfferResponseDto dto) {

        /* 1. Connection type filter (skip if none sent) */
        if (connectionTypes != null && !connectionTypes.isEmpty()
                && connectionTypes.stream()
                .noneMatch(t -> t.equalsIgnoreCase(dto.connectionType()))) {
            return false;
        }

        /* 2-4 unchanged … */
        if (maxPriceInCent != null && dto.monthlyCostInCent() > maxPriceInCent) return false;
        if (includeTv && !dto.tvIncluded()) return false;
        if (includeInstallation && !dto.installationService()) return false;

        return true;
    }
}

