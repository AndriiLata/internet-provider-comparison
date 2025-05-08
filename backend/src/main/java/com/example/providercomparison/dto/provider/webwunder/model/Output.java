package com.example.providercomparison.dto.provider.webwunder.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "output", namespace = Ns.URL)
public record Output(
        @JacksonXmlProperty(localName = "products", namespace = Ns.URL)
        java.util.List<Product> products
) {
    public record Product(
            @JacksonXmlProperty(namespace = Ns.URL) int productId,
            @JacksonXmlProperty(namespace = Ns.URL) String providerName,
            @JacksonXmlProperty(namespace = Ns.URL) ProductInfo productInfo
    ) {}

    public record ProductInfo(
            @JacksonXmlProperty(namespace = Ns.URL) int speed,
            @JacksonXmlProperty(namespace = Ns.URL) int monthlyCostInCent,
            @JacksonXmlProperty(namespace = Ns.URL) int monthlyCostInCentFrom25thMonth,
            @JacksonXmlProperty(namespace = Ns.URL) Voucher voucher,
            @JacksonXmlProperty(namespace = Ns.URL) int contractDurationInMonths,
            @JacksonXmlProperty(namespace = Ns.URL) LegacyGetInternetOffers.ConnectionType connectionType
    ) {}

    /* sealed interface + two concrete records */
    public sealed interface Voucher permits PercentageVoucher, AbsoluteVoucher { }

    public record PercentageVoucher(
            @JacksonXmlProperty(namespace = Ns.URL) int percentage,
            @JacksonXmlProperty(namespace = Ns.URL) int maxDiscountInCent
    ) implements Voucher {}

    public record AbsoluteVoucher(
            @JacksonXmlProperty(namespace = Ns.URL) int discountInCent,
            @JacksonXmlProperty(namespace = Ns.URL) int minOrderValueInCent
    ) implements Voucher {}
}
