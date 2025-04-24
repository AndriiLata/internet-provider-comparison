package com.example.providercomparison.controller;

import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import com.example.providercomparison.service.OfferService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class OfferController {
    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping
    public List<OfferResponseDto> searchOffers(@RequestBody SearchCriteria criteria) {
        return offerService.searchOffers(criteria);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOffers(@RequestBody SearchCriteria criteria) {
        return offerService.streamOffers(criteria);
    }
}
