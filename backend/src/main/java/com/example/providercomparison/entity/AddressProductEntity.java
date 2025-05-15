package com.example.providercomparison.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("servusspeed_address_product")
public class AddressProductEntity {
    @Id
    private Long id;

    private String street;
    private String houseNumber;
    private String postalCode;
    private String city;

    private String productId;
}
