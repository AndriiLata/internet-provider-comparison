package com.example.providercomparison.dto.provider.webwunder.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "legacyGetInternetOffers", namespace = Ns.URL)
public record LegacyGetInternetOffers(
        @JacksonXmlProperty(localName = "input", namespace = Ns.URL)
        Input input
) {
    public record Input(
            @JacksonXmlProperty(namespace = Ns.URL)
            boolean installation,

            @JacksonXmlProperty(localName = "connectionEnum", namespace = Ns.URL)
            ConnectionType connectionEnum,

            @JacksonXmlProperty(namespace = Ns.URL)
            Address address
    ) {
        public record Address(
                @JacksonXmlProperty(namespace = Ns.URL) String street,
                @JacksonXmlProperty(namespace = Ns.URL) String houseNumber,
                @JacksonXmlProperty(namespace = Ns.URL) String city,
                @JacksonXmlProperty(localName = "plz", namespace = Ns.URL) String plz,
                @JacksonXmlProperty(namespace = Ns.URL) SupportedCountry countryCode
        ) {}
    }

    public enum ConnectionType { DSL, CABLE, FIBER, MOBILE }
    public enum SupportedCountry { DE, AT, CH }
}
