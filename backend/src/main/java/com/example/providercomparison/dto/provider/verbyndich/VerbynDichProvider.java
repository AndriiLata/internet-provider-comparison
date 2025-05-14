package com.example.providercomparison.dto.provider.verbyndich;

import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.provider.verbyndich.model.VerbynDichResponse;
import com.example.providercomparison.dto.ui.*;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class VerbynDichProvider implements OfferProvider {

    private static final Logger log = LoggerFactory.getLogger(VerbynDichProvider.class);
    private final VerbynDichClient client;
    private static final int MAX_PARALLEL_REQUESTS = 8;

    @Override
    public Flux<OfferResponseDto> offers(SearchCriteria criteria) {
        AtomicBoolean stop = new AtomicBoolean(false);

        Flux<Integer> pageNumbers = Flux.generate(() -> 0, (page, sink) -> {
            if (stop.get()) {
                sink.complete();
            } else {
                sink.next(page);
            }
            return page + 1;
        });

        return pageNumbers
                .flatMap(page ->
                                client.fetchRawPage(criteria, page)
                                        .doOnNext(resp -> {
                                            if (resp.last()) stop.set(true);
                                        })
                                        .onErrorResume(ex -> {
                                            // log and swallow
                                            log.warn("Error fetching page {}: {} – skipping", page, ex.toString());
                                            return Mono.empty();
                                        })
                        , MAX_PARALLEL_REQUESTS)
                .filter(VerbynDichResponse::valid)
                .map(VerbynDichMapper::toDto);
    }

}

