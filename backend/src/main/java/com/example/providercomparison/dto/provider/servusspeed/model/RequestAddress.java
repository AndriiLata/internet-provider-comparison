package com.example.providercomparison.dto.provider.servusspeed.model;
/*
 Here is the information which is passed inside "address": {} for JSON
 */
public record RequestAddress(
        String strasse,
        String hausnummer,
        String postleitzahl,
        String stadt,
        String land
) {
}
