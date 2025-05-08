package com.example.providercomparison.dto.provider.verbyndich;

import com.example.providercomparison.dto.provider.OfferProvider;
import com.example.providercomparison.dto.provider.verbyndich.model.VerbynDichResponse;
import com.example.providercomparison.dto.ui.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class VerbynDichProvider implements OfferProvider {

    private final VerbynDichClient client;

    private static final int MAX_PARALLEL_REQUESTS = 8;   // make fan‑out configurable

    @Override
    public Flux<OfferResponseDto> offers(SearchCriteria criteria) {

        AtomicBoolean stop = new AtomicBoolean(false);

        /*  Produce an unbounded sequence of page numbers, but stop() the generator
            once somebody sets stop = true (i.e. when we have seen last == true).  */
        Flux<Integer> pageNumbers = Flux.generate(() -> 0, (page, sink) -> {
            if (stop.get()) {
                sink.complete();
            } else {
                sink.next(page);
            }
            return page + 1;     // next state
        });

        return pageNumbers
                /* fetch() pages in parallel but with a hard cap                */
                .flatMap(p -> client.fetchRawPage(criteria, p)
                                .doOnNext(resp -> { if (resp.last()) stop.set(true); }),
                        MAX_PARALLEL_REQUESTS)
                /* filter out invalid offers                                     */
                .filter(VerbynDichResponse::valid)
                /* change the API model → DTO used by your UI                    */
                .map(VerbynDichMapper::toDto);
    }
}
