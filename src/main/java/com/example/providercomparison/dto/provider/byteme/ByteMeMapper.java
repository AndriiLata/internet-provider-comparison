package com.example.providercomparison.dto.provider.byteme;

import com.example.providercomparison.dto.provider.byteme.model.ByteMeCsvOffer;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ByteMeMapper {

    public OfferResponseDto toDto(ByteMeCsvOffer o) {

        //clean title
        String title = o.providerName() == null
                ? null
                : o.providerName().split(",", 2)[0].trim();

        return new OfferResponseDto(
                o.productId(),
                title,
                o.speed(),
                o.monthlyCostInCent(),
                o.monthlyCostAfter24mInCent(),
                o.durationInMonths(),
                o.connectionType(),
                o.tvIncluded(),
                o.installationService(),
                o.voucherValueInCent(),
                o.voucherType(),
                null                     // ByteMe never returns ServusSpeed discount
        );
    }
}
