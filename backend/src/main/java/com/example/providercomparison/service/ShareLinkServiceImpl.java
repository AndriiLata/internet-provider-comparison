package com.example.providercomparison.service;

import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import com.example.providercomparison.entity.SearchSessionEntity;
import com.example.providercomparison.entity.SessionOfferEntity;
import com.example.providercomparison.repository.SearchSessionRepository;
import com.example.providercomparison.repository.SessionOfferRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShareLinkServiceImpl implements ShareLinkService {

    private final SearchSessionRepository   sessionRepo;
    private final SessionOfferRepository    offerRepo;
    private final ObjectMapper              mapper;   // inject the one Spring already has

    @Override
    public Mono<UUID> createSession(SearchCriteria criteria) {
        UUID id = UUID.randomUUID();
        SearchSessionEntity entity = new SearchSessionEntity();
        entity.setSessionId(id);
        entity.setCriteria(writeJsonAsJsonb(criteria));
        entity.setCreatedAt(LocalDateTime.now());
        return sessionRepo.save(entity).map(SearchSessionEntity::getSessionId);
    }

    @Override
    public Flux<OfferResponseDto> saveOffers(UUID sessionId, Flux<OfferResponseDto> offers) {
        return offers.flatMap(offer -> {
            SessionOfferEntity ent = new SessionOfferEntity();
            ent.setSessionId(sessionId);
            ent.setOffer(writeJsonAsJsonb(offer));
            return offerRepo.save(ent).thenReturn(offer);  // pass the object downstream
        });
    }

    @Override
    public Flux<OfferResponseDto> offersForSession(UUID sessionId) {
        return offerRepo.findAllBySessionIdOrderByIdAsc(sessionId)
                .map(ent -> readJson(ent.getOffer(), OfferResponseDto.class));
    }

    /* ---------- helpers ---------- */

    private Json writeJsonAsJsonb(Object o) {
        try { return Json.of(mapper.writeValueAsString(o)); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    private <T> T readJson(Json json, Class<T> type) {
        try { return mapper.readValue(json.asString(), type); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
