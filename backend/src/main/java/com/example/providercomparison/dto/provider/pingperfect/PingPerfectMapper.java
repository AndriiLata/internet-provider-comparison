// src/main/java/com/example/providercomparison/dto/provider/pingperfect/PingPerfectMapper.java
package com.example.providercomparison.dto.provider.pingperfect;

import com.example.providercomparison.dto.provider.pingperfect.model.InternetProduct;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PingPerfectMapper {

    public OfferResponseDto toDto(InternetProduct p) {
        var info  = p.productInfo();
        var price = p.pricingDetails();

        // basic IDs & names
        String productId = UUID.randomUUID().toString();
        String provider  = p.providerName();

        // contract info (use limitFrom/maxAge only if non-null AND > 0)
        Integer rawLimitFrom = info.limitFrom();
        Integer limitFrom    = (rawLimitFrom != null && rawLimitFrom > 0)
                ? rawLimitFrom
                : null;

        Integer rawMaxAge = info.maxAge();
        Integer maxAge    = (rawMaxAge != null && rawMaxAge > 0)
                ? rawMaxAge
                : null;

        OfferResponseDto.ContractInfo contractInfo = new OfferResponseDto.ContractInfo(
                info.connectionType().name(),
                info.speed(),
                limitFrom,
                info.contractDurationInMonths(),
                maxAge
        );

        // cost info (no discounted rate or vouchers in PingPerfect)
        boolean installation = price.installationService() != null
                && price.installationService().equalsIgnoreCase("yes");
        OfferResponseDto.CostInfo costInfo = new OfferResponseDto.CostInfo(
                price.monthlyCostInCent(),
                price.monthlyCostInCent(),
                null,
                null,
                null,
                installation
        );

        // TV info: present only if non-blank
        String rawTv = info.tv();
        boolean tvIncluded = rawTv != null && !rawTv.isBlank();
        OfferResponseDto.TvInfo tvInfo = new OfferResponseDto.TvInfo(
                tvIncluded,
                tvIncluded ? rawTv : null
        );

        return new OfferResponseDto(
                productId,
                provider,
                contractInfo,
                costInfo,
                tvInfo
        );
    }
}
