package com.example.providercomparison.controller;

import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import com.example.providercomparison.service.OfferService;
import com.example.providercomparison.service.OfferServiceReactive;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class OfferController {
    private final OfferService offerService;

    private final OfferServiceReactive svc;


    public OfferController(OfferService offerService, OfferServiceReactive offerServiceReactive) {
        this.offerService = offerService;
        this.svc = offerServiceReactive;
    }


    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOffers(@RequestBody SearchCriteria criteria) {
        return offerService.streamOffers(criteria);
    }

    @GetMapping(value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<OfferResponseDto>> streamOfferFlux(@RequestBody SearchCriteria criteria) {

        return Flux.merge(svc.offersFromAllProviders(criteria))
                // .filter(criteria::matches) // your TODO
                .map(dto -> ServerSentEvent.builder(dto).build());
        // When the client closes the connection, WebFlux
        // cancels the subscription for you.
    }

}
