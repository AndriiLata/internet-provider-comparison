package com.example.providercomparison.service;


import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.ui.OfferResponseDto;
import com.example.providercomparison.dto.ui.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final List<OfferProvider> providers;

    @Override
    public SseEmitter streamOffers(SearchCriteria criteria) {
        SseEmitter emitter = new SseEmitter(0L);

        Flux.merge(providers.stream().map(p -> p.offers(criteria)).toList())
                .doOnNext(dto -> safeSend(emitter, dto))
                .doOnError(emitter::completeWithError)
                .doOnComplete(emitter::complete)
                .subscribe();

        return emitter;
    }

    private void safeSend(SseEmitter emitter, OfferResponseDto dto) {
        try { emitter.send(dto, MediaType.APPLICATION_JSON); }
        catch (IOException ex) { emitter.completeWithError(ex); }
    }
}
