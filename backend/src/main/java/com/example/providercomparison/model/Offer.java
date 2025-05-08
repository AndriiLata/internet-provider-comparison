package com.example.providercomparison.model;

import com.example.providercomparison.model.enums.ConnectionType;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class Offer {
    String productId;
    String providerName;
    Speed speed;
    Money monthlyCost;
    Money monthlyCostAfter24m;
    Money installationCost;
    int durationInMonths;
    ConnectionType connectionType;
    boolean tvIncluded;
    boolean installationIncluded;
    Voucher voucher;
    int ageLimitFrom;
    int ageLimitTo;
    Money discount;

}
