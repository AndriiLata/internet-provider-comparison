package com.example.providercomparison.service;


import com.example.providercomparison.dto.provider.servusspeed.ServusSpeedClient;
import com.example.providercomparison.dto.provider.servusspeed.ServusSpeedMapper;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class OfferServiceImpl implements OfferService {
    private final ServusSpeedClient servusSpeedClient;

    public OfferServiceImpl(ServusSpeedClient servusSpeedClient) {
        this.servusSpeedClient = servusSpeedClient;
    }

    @Override
    public List<OfferResponseDto> searchOffers(SearchCriteria criteria) {
        var ids = servusSpeedClient.getAvailableProducts(criteria);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .map(id -> {
                    var detail = servusSpeedClient.getProductDetails(id, criteria);
                    return ServusSpeedMapper.toDto(id, detail);
                })
                .toList();
    }
}
