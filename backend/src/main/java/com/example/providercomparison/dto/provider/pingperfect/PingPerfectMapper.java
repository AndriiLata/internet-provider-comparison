package com.example.providercomparison.dto.provider.pingperfect;

import com.example.providercomparison.dto.provider.pingperfect.model.InternetProduct;
import com.example.providercomparison.dto.provider.pingperfect.model.PricingDetails;
import com.example.providercomparison.dto.provider.pingperfect.model.ProductInfo;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PingPerfectMapper {

    public OfferResponseDto toDto(InternetProduct p) {

        ProductInfo    info  = p.productInfo();     // may be null
        PricingDetails price = p.pricingDetails();  // may be null

        boolean tvIncluded = info != null
                && info.tv() != null
                && !"NONE".equalsIgnoreCase(info.tv());

        boolean installationIncluded = price != null
                && price.installationService() != null
                && !"NONE".equalsIgnoreCase(price.installationService());

        return new OfferResponseDto(
                UUID.randomUUID().toString(),                 // productId
                p.providerName(),                             // provider
                info != null ? info.speed() : 0,              // speed
                price != null ? price.monthlyCostInCent() : 0,
                null,                                         // monthlyCostAfter24mInCent
                info != null ? info.contractDurationInMonths() : 0,
                info != null ? info.connectionType().name() : null,
                tvIncluded,
                installationIncluded,
                null, null, null                              // voucher/discount not in API
        );
    }
}
