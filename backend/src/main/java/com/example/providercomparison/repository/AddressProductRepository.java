package com.example.providercomparison.repository;

import com.example.providercomparison.entity.AddressProductEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AddressProductRepository
        extends ReactiveCrudRepository<AddressProductEntity, Long> {

    Flux<AddressProductEntity> findByStreetAndHouseNumberAndPostalCodeAndCity(
            String street, String houseNumber, String postalCode, String city);
}
