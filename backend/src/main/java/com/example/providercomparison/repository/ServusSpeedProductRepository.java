package com.example.providercomparison.repository;

import com.example.providercomparison.entity.ServusSpeedProductEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ServusSpeedProductRepository
        extends ReactiveCrudRepository<ServusSpeedProductEntity, String> { }
