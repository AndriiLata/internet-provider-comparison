package com.example.providercomparison.repository;

import com.example.providercomparison.entity.SearchSessionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface SearchSessionRepository
        extends ReactiveCrudRepository<SearchSessionEntity, UUID> { }
