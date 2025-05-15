package com.example.providercomparison.service;

import com.example.providercomparison.dto.provider.servusspeed.ServusSpeedClient;
import com.example.providercomparison.dto.provider.servusspeed.ServusSpeedMapper;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import com.example.providercomparison.entity.AddressProductEntity;
import com.example.providercomparison.entity.ServusSpeedProductEntity;
import com.example.providercomparison.repository.AddressProductRepository;
import com.example.providercomparison.repository.ServusSpeedProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServusSpeedCacheService {

    private final AddressProductRepository addrRepo;
    private final ServusSpeedProductRepository prodRepo;
    private final ServusSpeedClient client;

    public Flux<OfferResponseDto> productDetailsBatch(List<String> ids,
                                                      SearchCriteria c) {

        /* 1) fetch everything we already have */
        Flux<ServusSpeedProductEntity> cached = prodRepo.findAllById(ids);

        /* 2) remember which came back */
        Mono<Set<String>> foundIds = cached.map(ServusSpeedProductEntity::getProductId)
                .collect(Collectors.toSet());

        /* 3) turn cached entities into DTOs */
        Flux<OfferResponseDto> cachedDtos = cached.map(ServusSpeedCacheService::toDto);

        /* 4) when we know the cached set, call API only for the missing ones */
        Flux<OfferResponseDto> apiDtos = foundIds.flatMapMany(found -> {
            List<String> missing = ids.stream()
                    .filter(id -> !found.contains(id))
                    .toList();

            return Flux.fromIterable(missing)
                    .flatMap(id -> productDetails(id, c));   // uses 1-by-1 method
        });

        /* 5) emit cached first, then API results as they arrive */
        return Flux.concat(cachedDtos, apiDtos);
    }


    /* ---------- address → product ids -------------------------------- */

    public Mono<List<String>> productIdsForAddress(SearchCriteria c) {

        return addrRepo.findByStreetAndHouseNumberAndPostalCodeAndCity(
                        c.street(), c.houseNumber(), c.postalCode(), c.city())
                .map(AddressProductEntity::getProductId)
                .collectList()
                .flatMap(ids -> ids.isEmpty()
                        ? fetchAndPersistAddress(c)   // miss → ask API
                        : Mono.just(ids));           // hit  → return
    }

    private Mono<List<String>> fetchAndPersistAddress(SearchCriteria c) {
        return client.getAvailableProducts(c)
                .flatMapMany(Flux::fromIterable)
                .flatMap(id -> addrRepo.save(toAddressEntity(id, c)).thenReturn(id))
                .collectList();
    }

    private static AddressProductEntity toAddressEntity(String id, SearchCriteria c) {
        AddressProductEntity e = new AddressProductEntity();
        e.setStreet(c.street());
        e.setHouseNumber(c.houseNumber());
        e.setPostalCode(c.postalCode());
        e.setCity(c.city());
        e.setProductId(id);
        return e;
    }

    /* ---------- product details -------------------------------------- */

    public Mono<OfferResponseDto> productDetails(String id, SearchCriteria c) {

        return prodRepo.findById(id)
                .map(ServusSpeedCacheService::toDto)           // hit
                .switchIfEmpty(
                        client.getProductDetails(id, c)        // miss
                                .map(d -> ServusSpeedMapper.toDto(id, d))
                                .flatMap(dto -> prodRepo
                                        .save(toEntity(dto))
                                        .thenReturn(dto)));
    }

    /* ---------- mappers ---------------------------------------------- */

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
