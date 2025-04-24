package com.example.providercomparison.dto.provider.servusspeed.model;
/*
 Here is the information which is passed inside "address": {} for JSON
 */
public record RequestAddress(
        String street,
        String houseNumber,
        String postalCode,
        String city,
        String country
) {
}
