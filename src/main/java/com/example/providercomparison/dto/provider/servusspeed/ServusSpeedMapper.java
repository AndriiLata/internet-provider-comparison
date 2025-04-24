package com.example.providercomparison.dto.provider.servusspeed;

import com.example.providercomparison.dto.provider.servusspeed.model.DetailedResponseData;
import com.example.providercomparison.dto.provider.servusspeed.model.ServusSpeedProduct;
import com.example.providercomparison.dto.ui.OfferResponseDto;

import java.util.Optional;

public class ServusSpeedMapper {
    public static OfferResponseDto toDto(String productId, DetailedResponseData detailed){
        if (detailed == null || detailed.servusSpeedProduct() == null) {
            // return a DTO with default or empty values if no data was found
            return new OfferResponseDto(
                    "", "", 0, 0, 0, 0, "", false, false, 0, "", 0
            );
        }
        ServusSpeedProduct p = detailed.servusSpeedProduct();
        var info = p.productInfo();
        var pricing = p.pricingDetails();

        int costAfter24 = Optional.ofNullable(pricing)
                .map(pr -> pr.monthlyCostInCent() - p.discount())
                .filter(c -> c >= 0)
                .orElse(0);

        boolean tvIncluded = info != null && info.tv() != null && !info.tv().isEmpty();

        return new OfferResponseDto(
                productId,
                p.providerName(),
                Optional.ofNullable(info).map(i -> i.speed()).orElse(0),
                Optional.ofNullable(pricing).map(pr -> pr.monthlyCostInCent()).orElse(0),
                costAfter24,
                Optional.ofNullable(info).map(i -> i.contractDurationInMonths()).orElse(0),
                Optional.ofNullable(info).map(i -> i.connectionType()).orElse(""),
                tvIncluded,
                Optional.ofNullable(pricing).map(pr -> pr.installationService()).orElse(false),
                p.discount(),
                "ABSOLUTE",
                p.discount()
        );
    }
}
