package com.example.providercomparison.dto.ui;

public record SearchCriteria(
        String street,
        String houseNumber,
        String city,
        String postalCode,
        String connectionType,
        Integer maxPriceInCent,
        boolean includeTv,
        boolean includeInstallation
) {
    public boolean matches(OfferResponseDto dto) {



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
