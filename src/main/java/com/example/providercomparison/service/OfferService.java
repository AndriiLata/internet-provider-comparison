package com.example.providercomparison.service;

import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface OfferService {

    SseEmitter streamOffers(SearchCriteria criteria);
}
