package com.example.providercomparison.dto.provider.pingperfect.model;

public record ProductInfo(
        int speed,
        int contractDurationInMonths,
        ConnectionType connectionType,
        String tv                      // "INCLUDED" | "NONE" …
) {
    public enum ConnectionType { DSL, CABLE, FIBER, MOBILE }
}
