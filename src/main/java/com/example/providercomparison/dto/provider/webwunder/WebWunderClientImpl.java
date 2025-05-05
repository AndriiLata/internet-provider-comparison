package com.example.providercomparison.dto.provider.webwunder;

import com.example.providercomparison.dto.provider.webwunder.model.LegacyGetInternetOffers;
import com.example.providercomparison.dto.provider.webwunder.model.Output;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
class WebWunderClientImpl implements WebWunderClient {

    private final XmlMapper xml = new XmlMapper();

    private final WebClient webClient = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                    .build())
            .build();

    @Value("${provider.webwunder.endpoint}")   private String endpoint;
    @Value("${provider.webwunder.api-key}")    private String apiKey;

    @Override
    public Mono<Output> fetchOffers(LegacyGetInternetOffers request) {
        return Mono.fromCallable(() -> xml.writeValueAsString(request))
                .map(this::wrapInEnvelope)
                .flatMap(this::callService)
                .map(this::stripEnvelope)
                .map(this::deserialize)
                // SOAP is blocking; run on a boundedElastic worker
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<String> callService(String soapBody) {
        return webClient.post()
                .uri(endpoint)
                .contentType(MediaType.TEXT_XML)
                .header("X-Api-Key", apiKey)
                .header("SOAPAction", "\"legacyGetInternetOffers\"")   // 👈 usually required
                .bodyValue(soapBody)
                .exchangeToMono(resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(b -> resp.statusCode().isError()
                                        ? Mono.error(new WebWunderException((HttpStatus) resp.statusCode(), b))
                                        : Mono.just(b))
                );
    }



    /* ---------------- small helpers ---------------- */

    private String wrapInEnvelope(String body) {
        return """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
              <soapenv:Body>%s</soapenv:Body>
            </soapenv:Envelope>
            """.formatted(body);
    }

    private static final Pattern OUTPUT_PATTERN = Pattern.compile(
            "<(?:(\\w+):)?output[\\s>][\\s\\S]*?</(?:(\\w+):)?output>",
            Pattern.CASE_INSENSITIVE);

    private String stripEnvelope(String soapResponse) {

        Matcher m = OUTPUT_PATTERN.matcher(soapResponse);
        if (!m.find()) {
            throw new IllegalStateException("""
            Unexpected SOAP response – cannot find <output/> element.
            First 400 bytes were:
            %s""".formatted(
                    soapResponse.substring(0, Math.min(400, soapResponse.length()))
            ));
        }
        return m.group();   // full match (group 0)
    }

    private Output deserialize(String xml) {
        try { return xmlMapper().readValue(xml, Output.class); }
        catch (Exception ex) { throw new RuntimeException("Cannot parse WebWunder response", ex); }
    }

    private XmlMapper xmlMapper() {
        xml.setDefaultUseWrapper(false);
        return xml;
    }

    /** unchecked but rich with info */
    public static final class WebWunderException extends RuntimeException {
        public WebWunderException(HttpStatus status, String body) {
            super("WebWunder " + status + " – body:\n" + body);
        }
    }
}
