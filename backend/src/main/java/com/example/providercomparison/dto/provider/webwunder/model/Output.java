package com.example.providercomparison.dto.provider.webwunder.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
            @JacksonXmlProperty(namespace = Ns.URL) Integer speed,
            @JacksonXmlProperty(namespace = Ns.URL) Integer monthlyCostInCent,
            @JacksonXmlProperty(namespace = Ns.URL) Integer monthlyCostInCentFrom25thMonth,
            @JacksonXmlProperty(namespace = Ns.URL) Voucher voucher,          // ← just one record
            @JacksonXmlProperty(namespace = Ns.URL) Integer contractDurationInMonths,
            @JacksonXmlProperty(namespace = Ns.URL) LegacyGetInternetOffers.ConnectionType connectionType
    ) {}



    // WebWunder sometimes sends a percentage voucher, sometimes an absolute one -> Jackson just fills whichever fields exist
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Voucher(
            @JacksonXmlProperty(namespace = Ns.URL) Integer percentage,
            @JacksonXmlProperty(namespace = Ns.URL) Integer maxDiscountInCent,
            @JacksonXmlProperty(namespace = Ns.URL) Integer discountInCent,
            @JacksonXmlProperty(namespace = Ns.URL) Integer minOrderValueInCent
    ) {}




}
