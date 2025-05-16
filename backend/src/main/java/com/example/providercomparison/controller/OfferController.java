package com.example.providercomparison.controller;

import com.example.providercomparison.dto.ui.*;
import com.example.providercomparison.service.OfferServiceReactive;
import com.example.providercomparison.service.ShareLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferServiceReactive svc;
    private final ShareLinkService     share;

    /** streaming search + persistence + share-ID */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<?>> streamOffers(@RequestBody SearchCriteria criteria) {

        return share.createSession(criteria)
                .flatMapMany(sessionId -> {

                    /* 1 ── push the UUID first */
                    Flux<ServerSentEvent<ShareLinkDto>> idEvent =
                            Flux.just(ServerSentEvent.<ShareLinkDto>builder(
                                            new ShareLinkDto(sessionId.toString()))
                                    .event("sessionId")
                                    .build());

                    /* 2 ── provider calls + user filters */
                    Flux<OfferResponseDto> filtered =
                            svc.offersFromAllProviders(criteria)
                                    .filter(criteria::matches);          // 🔸 apply filters first

                    /* 3 ── persist only what passed the filters */
                    Flux<OfferResponseDto> persisted =
                            share.saveOffers(sessionId, filtered);

                    /* 4 ── convert to SSE */
                    Flux<ServerSentEvent<OfferResponseDto>> offerEvents =
                            persisted.map(dto -> ServerSentEvent.builder(dto).build());

                    return Flux.concat(idEvent, offerEvents);
                });
    }

    /** retrieve a previously-saved list */
    @GetMapping("/session/{sessionId}")
    public Flux<OfferResponseDto> offersBySession(@PathVariable UUID sessionId) {
        return share.offersForSession(sessionId);
    }
}
