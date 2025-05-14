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
                .noneMatch(t -> t.equalsIgnoreCase(dto.contractInfo().connectionType()))) {
            return false;
        }

        /* 2. Max price (uses the nested CostInfo) */
        if (maxPriceInCent != null
                && dto.costInfo().discountedMonthlyCostInCent() > maxPriceInCent) {
            return false;
        }

        /* 3. TV inclusion (uses the nested TvInfo) */
        if (includeTv && !dto.tvInfo().tvIncluded()) {
            return false;
        }

        /* 4. Installation service (uses the nested CostInfo) */
        if (includeInstallation && !dto.costInfo().installationService()) {
            return false;
        }

        return true;
    }

}

