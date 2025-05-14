// src/main/java/com/example/providercomparison/dto/provider/servusspeed/ServusSpeedMapper.java
package com.example.providercomparison.dto.provider.servusspeed;

import com.example.providercomparison.dto.provider.servusspeed.model.DetailedResponseData;
import com.example.providercomparison.dto.provider.servusspeed.model.ServusSpeedProduct;
import com.example.providercomparison.dto.provider.servusspeed.model.OfferProductInfo;
import com.example.providercomparison.dto.provider.servusspeed.model.OfferPricingDetails;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.OfferResponseDto.ContractInfo;
import com.example.providercomparison.dto.ui.OfferResponseDto.CostInfo;
import com.example.providercomparison.dto.ui.OfferResponseDto.TvInfo;

public class ServusSpeedMapper {

    public static OfferResponseDto toDto(String productId, DetailedResponseData detailed) {
        if (detailed == null || detailed.servusSpeedProduct() == null) {
            // defaults when no data is present
            ContractInfo defaultContract = new ContractInfo("", 0, 0, 0, 0);
            CostInfo     defaultCost     = new CostInfo(0, 0, null, null, 0, false);
            TvInfo       defaultTv       = new TvInfo(false, null);

            return new OfferResponseDto(
                    "",             // productId
                    "",             // provider
                    defaultContract,
                    defaultCost,
                    defaultTv
            );
        }

        ServusSpeedProduct p = detailed.servusSpeedProduct();
        OfferProductInfo    info    = p.productInfo();
        OfferPricingDetails pricing = p.pricingDetails();

        // provider & IDs
        String provider = p.providerName() != null ? p.providerName() : "";

        // -- ContractInfo --
        String connectionType = info != null && info.connectionType() != null
                ? info.connectionType()
                : "";
        int speed       = info != null ? info.speed() : 0;
        int limitFrom   = (info != null && info.limitFrom() != null)
                ? info.limitFrom()
                : 0;
        int duration    = info != null ? info.contractDurationInMonths() : 0;
        int maxAge      = (info != null && info.maxAge() != null)
                ? info.maxAge()
                : 0;
        ContractInfo contractInfo = new ContractInfo(
                connectionType,
                speed,
                limitFrom,
                duration,
                maxAge
        );

        // -- CostInfo --
        int monthlyCost = pricing != null ? pricing.monthlyCostInCent() : 0;

        // discount
        Integer monthlyDiscountValue = p.discount();
        if (info != null && info.contractDurationInMonths() > 0) {
            monthlyDiscountValue = p.discount() / info.contractDurationInMonths();
        }
        int discountedMonthlyCost = monthlyCost - monthlyDiscountValue;

        boolean installation = pricing != null && pricing.installationService();

        CostInfo costInfo = new CostInfo(
                discountedMonthlyCost,    // discountedMonthlyCostInCent
                monthlyCost,    // monthlyCostInCent
                null,
                monthlyDiscountValue,
                0,
                installation
        );

        // -- TvInfo --
        boolean tvIncluded = info != null
                && info.tv() != null
                && !info.tv().isBlank();
        String tvBrand = tvIncluded ? info.tv() : null;
        TvInfo tvInfo = new TvInfo(tvIncluded, tvBrand);

        return new OfferResponseDto(
                productId,
                provider,
                contractInfo,
                costInfo,
                tvInfo
        );
    }
}
