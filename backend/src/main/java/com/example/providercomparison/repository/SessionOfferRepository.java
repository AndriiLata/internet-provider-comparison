package com.example.providercomparison.repository;

import com.example.providercomparison.entity.SessionOfferEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface SessionOfferRepository
        extends ReactiveCrudRepository<SessionOfferEntity, Long> {

    Flux<SessionOfferEntity> findAllBySessionIdOrderByIdAsc(UUID sessionId);
}
