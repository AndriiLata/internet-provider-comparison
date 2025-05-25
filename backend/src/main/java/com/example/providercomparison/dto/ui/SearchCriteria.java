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

    // search filters
    public boolean matches(OfferResponseDto dto) {
        // Connection type filter (skip if none sent)
        if (connectionTypes != null && !connectionTypes.isEmpty()
                && connectionTypes.stream()
                .noneMatch(t -> t.equalsIgnoreCase(dto.contractInfo().connectionType()))) {
            return false;
        }

        // Max price
        if (maxPriceInCent != null
                && dto.costInfo().discountedMonthlyCostInCent() > maxPriceInCent) {
            return false;
        }

        // TV inclusion
        if (includeTv && !dto.tvInfo().tvIncluded()) {
            return false;
        }

        // Installation service
        if (includeInstallation && !dto.costInfo().installationService()) {
            return false;
        }

        return true;
    }

}

