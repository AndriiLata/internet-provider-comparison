package com.example.providercomparison.dto.provider.webwunder;

import com.example.providercomparison.dto.provider.webwunder.model.LegacyGetInternetOffers;
import com.example.providercomparison.dto.provider.webwunder.model.Output;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
class WebWunderMapper {

    /* UI ➟ SOAP (unchanged) */
    LegacyGetInternetOffers from(SearchCriteria c,
                                 LegacyGetInternetOffers.ConnectionType connection) {
        return new LegacyGetInternetOffers(
                new LegacyGetInternetOffers.Input(
                        c.includeInstallation(),
                        connection,
                        new LegacyGetInternetOffers.Input.Address(
                                c.street(),
                                c.houseNumber(),
                                c.city(),
                                c.postalCode(),
                                LegacyGetInternetOffers.SupportedCountry.DE
                        )
                )
        );
    }

    /* SOAP ➟ UI (nested DTO, with maxDiscountInCent) */
    List<OfferResponseDto> toDtos(Output output) {
        if (output.products() == null || output.products().isEmpty()) {
            return List.of();
        }

        return output.products().stream()
                .map(p -> {
                    var info = p.productInfo();

                    // --- ContractInfo ---
                    String connection = info.connectionType() == null
                            ? "UNKNOWN"
                            : info.connectionType().name();
                    OfferResponseDto.ContractInfo contract = new OfferResponseDto.ContractInfo(
                            connection,
                            info.speed(),       // speed (mbps)
                            null,       // speedLimitFrom
                            info.contractDurationInMonths(),
                            null                   // maxAge (not used)
                    );

                    // --- Base costs ---
                    int monthlyCost = info.monthlyCostInCent();
                    Integer monthlyCostAfter24 = info.monthlyCostInCentFrom25thMonth() == 0
                            ? null
                            : info.monthlyCostInCentFrom25thMonth();

                    // --- Voucher parsing ---
                    Output.Voucher v = info.voucher();
                    String voucherType   = null;
                    Integer rawVoucher   = null;
                    int computedDiscount = 0;
                    int declaredMaxDiscount = 0;

                    if (v != null) {
                        // declared maximum discount cap
                        if (v.maxDiscountInCent() != null) {
                            declaredMaxDiscount = v.maxDiscountInCent();
                        }

                        if (v.percentage() != null) {
                            voucherType     = "PERCENTAGE";
                            rawVoucher      = v.percentage();
                            // compute absolute discount from percentage
                            computedDiscount = monthlyCost * rawVoucher / 100;
                        } else if (v.discountInCent() != null) {
                            voucherType     = "ABSOLUTE";
                            rawVoucher      = v.discountInCent();
                            computedDiscount = rawVoucher;
                        }
                    }

                    // choose the true discount: respect the provider’s cap if present
                    int discountInCent = declaredMaxDiscount > 0
                            ? Math.min(computedDiscount, declaredMaxDiscount)
                            : computedDiscount;

                    int discountedMonthly = monthlyCost - discountInCent;
                    Integer monthlyDiscountValue = (voucherType == null)
                            ? null
                            : discountInCent;

                    // --- CostInfo ---
                    OfferResponseDto.CostInfo cost = new OfferResponseDto.CostInfo(
                            discountedMonthly,              // discountedMonthlyCostInCent
                            monthlyCost,                    // monthlyCostInCent
                            monthlyCostAfter24,             // monthlyCostAfter24mInCent
                            monthlyDiscountValue,           // monthlyDiscountValueInCent
                            declaredMaxDiscount > 0
                                    ? declaredMaxDiscount
                                    : discountInCent,          // maxDiscountInCent
                            true                            // installationService
                    );

                    // --- TvInfo (none on WebWunder) ---
                    OfferResponseDto.TvInfo tv = new OfferResponseDto.TvInfo(false, "");

                    // --- Assemble final DTO ---
                    return new OfferResponseDto(
                            String.valueOf(p.productId()),
                            p.providerName(),
                            contract,
                            cost,
                            tv,
                            0.0
                    );
                })
                .collect(Collectors.toList());
    }
}
