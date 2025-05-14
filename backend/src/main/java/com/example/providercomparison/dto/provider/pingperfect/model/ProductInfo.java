package com.example.providercomparison.dto.provider.pingperfect.model;

public record ProductInfo(
        int speed,
        int contractDurationInMonths,
        ConnectionType connectionType,
        String tv,                     // "INCLUDED" | "NONE" …
        Integer limitFrom,
        Integer maxAge
) {
    public enum ConnectionType { DSL, CABLE, FIBER, MOBILE }
}
