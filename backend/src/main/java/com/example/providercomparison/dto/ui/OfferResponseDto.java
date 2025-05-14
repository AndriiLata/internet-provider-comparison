package com.example.providercomparison.dto.ui;

public record OfferResponseDto(
        String productId,
        String provider,
        ContractInfo contractInfo,
        CostInfo costInfo,
        TvInfo tvInfo
                     // "ABSOLUTE" or "PERCENTAGE"
        // Verbyndich:  FernseherAngebot "RobynTV+"
        // ServusSpeed: FernseherAngebot, limitFrom, maxAge
        // WebWunder: maxDiscountInCent
        // ByteMe: FernseherAngebot, limitFrom, maxAge
        // PingPerfect: FernseherAngebot, limitFrom, maxAge
) {
    public static record CostInfo(
            int discountedMonthlyCostInCent,
            int monthlyCostInCent,
            Integer monthlyCostAfter24mInCent,
            Integer monthlyDiscountValueInCent,
            Integer maxDiscountInCent,
            boolean installationService
    ){}
    public static record TvInfo(
            boolean tvIncluded,
            String tvBrand
    ){}
    public static record ContractInfo(
            String connectionType,
            int speed, // mbps
            Integer speedLimitFrom,// mbps
            Integer contractDurationInMonths,
            Integer maxAge
    ){}

}
