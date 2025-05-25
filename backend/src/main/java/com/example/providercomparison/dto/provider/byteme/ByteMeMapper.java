package com.example.providercomparison.dto.provider.byteme;

import com.example.providercomparison.dto.provider.byteme.model.ByteMeCsvOffer;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ByteMeMapper {

    public OfferResponseDto toDto(ByteMeCsvOffer o) {
        // clean provider name
        String title = o.providerName() == null
                ? null
                : o.providerName().split(",", 2)[0].trim();

        // build ContractInfo, defaulting any null Integer → 0
        int contractDuration = Math.max(1, o.durationInMonths());
        OfferResponseDto.ContractInfo contractInfo = new OfferResponseDto.ContractInfo(
                o.connectionType(),
                o.speed()      != 0 ? o.speed()      : 0,
                o.limitFrom()  != null ? o.limitFrom()  : 0,
                contractDuration,
                o.maxAge()     != null ? o.maxAge()     : null
        );

        // compute the per-month voucher value (prorate if ABSOLUTE)
        int voucherValue = voucherValueInCent(
                o.voucherType(),
                o.voucherValueInCent(),
                o.monthlyCostInCent(),
                contractDuration
        );

        // build CostInfo, using voucherValue for both discount and maxDiscount
        OfferResponseDto.CostInfo costInfo = new OfferResponseDto.CostInfo(
    o.monthlyCostInCent() - voucherValue,
                            o.monthlyCostInCent(),
                            o.monthlyCostAfter24mInCent(),
                            voucherValue,
                             null,
                            o.installationService()
        );

        // TV info
        boolean tvIncluded = o.tvBrand() != null && !o.tvBrand().isBlank();
        OfferResponseDto.TvInfo tvInfo = new OfferResponseDto.TvInfo(
                tvIncluded,
                tvIncluded ? o.tvBrand().trim() : ""
        );

        return new OfferResponseDto(
                o.productId(),
                title,
                contractInfo,
                costInfo,
                tvInfo,
                0.0
        );
    }

    /**
     * @param voucherType          "ABSOLUTE" or "PERCENTAGE"
     * @param voucherValue         raw Integer value (may be null!)
     * @param monthlyCostInCent    the normal cost in cents
     * @param durationInMonths     contract length used to prorate absolute vouchers
     * @return the per-month discount in cents
     */
    private static int voucherValueInCent(String  voucherType,
                                          Integer voucherValue,
                                          int     monthlyCostInCent,
                                          int     durationInMonths) {

        if (voucherType == null
                || voucherType.isBlank()
                || voucherValue == null) {
            return 0;
        }

        if ("PERCENTAGE".equalsIgnoreCase(voucherType)) {          // already “per-month”
            return monthlyCostInCent * voucherValue / 100;
        }

        return voucherValue / durationInMonths;
    }
}
