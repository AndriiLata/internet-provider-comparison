package com.example.providercomparison.service;

import com.example.providercomparison.dto.ui.RatingRequestDto;
import com.example.providercomparison.dto.ui.RatingResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RatingService {
    Mono<Void> saveRating(RatingRequestDto dto);
    Mono<Double> averageRating(String serviceName);

    Flux<RatingResponseDto> ratingsForService(String serviceName);
}
