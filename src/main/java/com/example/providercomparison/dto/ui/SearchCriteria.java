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
    public boolean matches(OfferResponseDto dto) {

        /* 1. Fibre preference ------------------------------------------------ */
        if (wantsFiber && !"FIBER".equalsIgnoreCase(dto.connectionType())) {
            return false;
        }

        /* 2. Hard price ceiling (null ⇒ no limit) --------------------------- */
        if (maxPriceInCent != null && dto.monthlyCostInCent() > maxPriceInCent) {
            return false;
        }

        /* 3. Wants TV? Then offer *must* contain TV. ------------------------ */
        if (includeTv && !dto.tvIncluded()) {
            return false;
        }

        /* 4. Wants installation service? ------------------------------------ */
        if (includeInstallation && !dto.installationService()) {
            return false;
        }

        return true;            // everything checked → ok
    }
}
