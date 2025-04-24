package com.example.providercomparison.util.mapper;

import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.model.Offer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OfferMapper {

    @Mapping(target = "speed",                    source = "speed.downstreamMbps")
    @Mapping(target = "monthlyCostInCent",        source = "monthlyCost.amountInCent")
    @Mapping(target = "discountInCent",           source = "discount.amountInCent")
    @Mapping(target = "monthlyCostAfter24mInCent",source = "monthlyCostAfter24m.amountInCent")
    OfferResponseDto toDto(Offer offer);
}
