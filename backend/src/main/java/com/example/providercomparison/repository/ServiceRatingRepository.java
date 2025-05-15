package com.example.providercomparison.repository;

import com.example.providercomparison.entity.ServiceRating;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ServiceRatingRepository extends ReactiveCrudRepository<ServiceRating, Long> {

    @Query("SELECT AVG(ranking) FROM service_rating WHERE service_name = :serviceName")
    Mono<Double> averageRating(String serviceName);

    /** fetch every rating, newest first */
    Flux<ServiceRating> findAllByServiceNameOrderByCreatedAtDesc(String serviceName);
}
