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

    /* UI ➟ SOAP ------------------------------------ */
    LegacyGetInternetOffers from(SearchCriteria c,
                                 LegacyGetInternetOffers.ConnectionType connection) {

        return new LegacyGetInternetOffers(
                new LegacyGetInternetOffers.Input(
                        /* installation flag */
                        c.includeInstallation(),

                        /* the connectionEnum we pass in */
                        connection,

                        /* address */
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

    /* SOAP ➟ UI ------------------------------------ */
    List<OfferResponseDto> toDtos(Output output) {
        if (output.products() == null || output.products().isEmpty()) {
            return List.of();                    // nothing to map
        }

        return output.products().stream().map(p -> {
            var info = p.productInfo();
            Integer voucherValue = null;
            String  voucherType  = null;
            if (info.voucher() instanceof Output.PercentageVoucher pv) {
                voucherType = "PERCENTAGE";
                voucherValue = pv.maxDiscountInCent();
            }
            if (info.voucher() instanceof Output.AbsoluteVoucher av) {
                voucherType = "ABSOLUTE";
                voucherValue = av.discountInCent();
            }
            return new OfferResponseDto(
                    String.valueOf(p.productId()),
                    p.providerName(),
                    info.speed(),
                    info.monthlyCostInCent(),
                    info.monthlyCostInCentFrom25thMonth() == 0
                            ? null : info.monthlyCostInCentFrom25thMonth(),
                    info.contractDurationInMonths(),
                    info.connectionType().name(),
                    /* WebWunder has no TV flag */ false,
                    /* we already passed installation wish, include it if flag set */ true,
                    voucherValue,
                    voucherType,
                    null               // discountInCent – not supplied by WebWunder
            );
        }).collect(Collectors.toList());
    }
}
