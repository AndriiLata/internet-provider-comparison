package com.example.providercomparison.dto.provider.servusspeed.model;

public record RequestAddress(
        String strasse,
        String hausnummer,
        String postleitzahl,
        String stadt,
        String land
) {
}
