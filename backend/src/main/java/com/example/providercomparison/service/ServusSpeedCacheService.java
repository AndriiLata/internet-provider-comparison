package com.example.providercomparison.service;

import com.example.providercomparison.dto.provider.servusspeed.ServusSpeedClient;
import com.example.providercomparison.dto.provider.servusspeed.ServusSpeedMapper;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import com.example.providercomparison.entity.ServusSpeedProductEntity;
import com.example.providercomparison.repository.ServusSpeedProductRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServusSpeedCacheService {

    /* ------------------------------------------------------------------
     *  Dependencies
     * ---------------------------------------------------------------- */
    private final ServusSpeedProductRepository prodRepo;
    private final ServusSpeedClient           client;

    /* ------------------------------------------------------------------
     *  City-level cache  (24 h TTL, memory only)
     * ---------------------------------------------------------------- */
    private final Cache<String, Mono<List<String>>> cityCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofHours(24))
                    .maximumSize(500)               // ~500 cities ⇒ < 1 MB
                    .build();

    private static final Retry RETRY_POLICY =
            Retry.backoff(2, Duration.ofSeconds(1)).jitter(0.2);

    /* ================================================================
     *  Public API
     * ================================================================ */

    /** -------- ① get productIds for a city (cached 24 h) ------------ */
    public Mono<List<String>> productIdsForAddress(SearchCriteria c) {
        String key = canonicalCity(c.city());
        return cityCache.get(key,
                __ -> client.getAvailableProducts(c)
                        .timeout(Duration.ofSeconds(30))
                        .retryWhen(RETRY_POLICY)
                        .cache());       // memoise the Mono itself
    }

    /** -------- ② details for a single productId -------------------- */
    public Mono<OfferResponseDto> productDetails(String id, SearchCriteria c) {

        return prodRepo.findById(id)
                .map(ServusSpeedCacheService::toDto)             // hit
                .switchIfEmpty(                                  // miss
                        client.getProductDetails(id, c)
                                .map(d -> ServusSpeedMapper.toDto(id, d))
                                .flatMap(dto -> prodRepo
                                        .save(toEntity(dto))
                                        .thenReturn(dto)));
    }

    /** -------- ③ batch helper (cached first, API for misses) ------- */
    public Flux<OfferResponseDto> productDetailsBatch(List<String> ids,
                                                      SearchCriteria c) {

        Flux<ServusSpeedProductEntity> cached = prodRepo.findAllById(ids);

        Mono<Set<String>> foundIds = cached
                .map(ServusSpeedProductEntity::getProductId)
                .collect(Collectors.toSet());

        Flux<OfferResponseDto> cachedDtos = cached.map(ServusSpeedCacheService::toDto);

        Flux<OfferResponseDto> apiDtos = foundIds.flatMapMany(found -> {
            List<String> missing = ids.stream()
                    .filter(id -> !found.contains(id))
                    .toList();

            return Flux.fromIterable(missing)
                    .flatMap(id -> productDetails(id, c));
        });

        return Flux.concat(cachedDtos, apiDtos);
    }

    /* ================================================================
     *  Helpers
     * ================================================================ */

    private static String canonicalCity(String city) {
        return city == null ? "" : city.trim().toLowerCase(Locale.ROOT);
    }

    private static ServusSpeedProductEntity toEntity(OfferResponseDto dto) {
        ServusSpeedProductEntity e = new ServusSpeedProductEntity();
        e.setProductId(dto.productId());
        e.setProvider(dto.provider());

        var ci = dto.contractInfo();
        e.setConnectionType(ci.connectionType());
        e.setSpeed(ci.speed());
        e.setSpeedLimitFrom(ci.speedLimitFrom());
        e.setContractDuration(ci.contractDurationInMonths());
        e.setMaxAge(ci.maxAge());

        var co = dto.costInfo();
        e.setDiscountedMonthlyCost(co.discountedMonthlyCostInCent());
        e.setMonthlyCost(co.monthlyCostInCent());
        e.setMonthlyCostAfter24m(co.monthlyCostAfter24mInCent());
        e.setMonthlyDiscountValue(co.monthlyDiscountValueInCent());
        e.setMaxDiscount(co.maxDiscountInCent());
        e.setInstallationService(co.installationService());

        var tv = dto.tvInfo();
        e.setTvIncluded(tv.tvIncluded());
        e.setTvBrand(tv.tvBrand());

        e.setLastUpdated(LocalDateTime.now());
        return e;
    }

    private static OfferResponseDto toDto(ServusSpeedProductEntity e) {
        var ci = new OfferResponseDto.ContractInfo(
                e.getConnectionType(), e.getSpeed(), e.getSpeedLimitFrom(),
                e.getContractDuration(), e.getMaxAge());

        var co = new OfferResponseDto.CostInfo(
                e.getDiscountedMonthlyCost(), e.getMonthlyCost(),
                e.getMonthlyCostAfter24m(), e.getMonthlyDiscountValue(),
                e.getMaxDiscount(), Boolean.TRUE.equals(e.getInstallationService()));

        var tv = new OfferResponseDto.TvInfo(
                Boolean.TRUE.equals(e.getTvIncluded()), e.getTvBrand());

        return new OfferResponseDto(
                e.getProductId(), e.getProvider(), ci, co, tv, 0.0);
    }
}
