package com.example.providercomparison.controller;

import com.example.providercomparison.dto.ui.RatingRequestDto;
import com.example.providercomparison.dto.ui.RatingResponseDto;
import com.example.providercomparison.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> submitRating(@Valid @RequestBody RatingRequestDto dto) {
        return ratingService.saveRating(dto);
    }

    @GetMapping("/{serviceName}")
    public Flux<RatingResponseDto> allRatings(@PathVariable String serviceName) {
        return ratingService.ratingsForService(serviceName);
    }

}
