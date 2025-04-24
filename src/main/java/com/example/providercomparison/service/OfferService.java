package com.example.providercomparison.service;

import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;

import java.util.List;

public interface OfferService {
    List<OfferResponseDto> searchOffers(SearchCriteria criteria);
    org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamOffers(SearchCriteria criteria);
}
