package com.example.providercomparison.dto.provider.pingperfect.model;

public record CompareProductsRequestData(
        String street,
        String plz,
        String houseNumber,
        String city,
        boolean wantsFiber
) {
}
