package com.example.providercomparison.service;

import com.example.providercomparison.dto.ui.RatingRequestDto;
import com.example.providercomparison.dto.ui.RatingResponseDto;
import com.example.providercomparison.entity.ServiceRating;
import com.example.providercomparison.repository.ServiceRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final ServiceRatingRepository repo;

    @Override
    public Mono<Void> saveRating(RatingRequestDto dto) {
        return repo.findByServiceNameAndEmail(dto.serviceName(), dto.email())
                // check if the user has already rated this service
                .flatMap(existing -> {
                    existing.setUserName(dto.userName());
                    existing.setRanking(dto.ranking());
                    existing.setComment(dto.comment());
                    existing.setCreatedAt(LocalDateTime.now());
                    return repo.save(existing);
                })
                // if this was the first rating for this service by this user
                .switchIfEmpty(Mono.defer(() -> {
                    ServiceRating rating = new ServiceRating();
                    rating.setServiceName(dto.serviceName());
                    rating.setUserName(dto.userName());
                    rating.setEmail(dto.email());
                    rating.setRanking(dto.ranking());
                    rating.setComment(dto.comment());
                    rating.setCreatedAt(LocalDateTime.now());
                    return repo.save(rating);
                }))
                .then();   // return Mono<Void>
    }

    @Override
    public Mono<Double> averageRating(String serviceName) {
        return repo.averageRating(serviceName)
                .defaultIfEmpty(0.0);
    }

    @Override
    public Flux<RatingResponseDto> ratingsForService(String serviceName) {
        return repo.findAllByServiceNameOrderByCreatedAtDesc(serviceName)
                .map(r -> new RatingResponseDto(
                        r.getUserName(),
                        r.getRanking(),
                        r.getComment(),
                        r.getCreatedAt()
                ));
    }
}
