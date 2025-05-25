// src/main/java/com/example/providercomparison/dto/provider/verbyndich/VerbynDichMapper.java
package com.example.providercomparison.dto.provider.verbyndich;

import com.example.providercomparison.dto.provider.verbyndich.model.VerbynDichResponse;
import com.example.providercomparison.dto.ui.OfferResponseDto;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VerbynDichMapper {

    // product ID
    private static final AtomicLong COUNTER = new AtomicLong();

    // patterns
    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

    private static final Pattern PRICE_PATTERN =
            Pattern.compile("Für\\s+nur\\s+(\\d{1,3}(?:\\.\\d{3})*)€\\s+im\\s+Monat", FLAGS);

    private static final Pattern SPEED_PATTERN =
            Pattern.compile("Geschwindigkeit\\s+von\\s+(\\d{1,4}(?:\\.\\d{3})*)\\s+Mbit/s", FLAGS);

    private static final Pattern DURATION_PATTERN =
            Pattern.compile("Mindestvertragslaufzeit\\s+(\\d{1,2})\\s+Monate?", FLAGS);

    private static final Pattern PRICE_AFTER24_PATTERN =
            Pattern.compile("Ab\\s+dem\\s+24\\.?\\s+Monat\\s+beträgt\\s+der\\s+monatliche\\s+Preis\\s+(\\d{1,3}(?:\\.\\d{3})*)€", FLAGS);

    private static final Pattern CONNECTION_PATTERN =
            Pattern.compile("erhalten\\s+Sie\\s+eine\\s+(Cable|Fiber|DSL)-Verbindung", FLAGS);

    // tv brand
    private static final Pattern TV_BRAND_PATTERN =
            Pattern.compile(
                    "Zusätzlich\\s+sind\\s+folgende\\s+Fernsehsender\\s+enthalten\\s+([^\\.\\n]+)",
                    FLAGS
            );
    // data cap limit
    private static final Pattern DATA_CAP_PATTERN =
            Pattern.compile(
                    "Ab\\s+(\\d{1,4})GB\\s+pro\\s+Monat\\s+wird\\s+die\\s+Geschwindigkeit\\s+gedrosselt",
                    FLAGS
            );

    private VerbynDichMapper() {}

    public static OfferResponseDto toDto(VerbynDichResponse resp) {
        String desc = resp.description();

        //  parse basic numbers
        int price    = parseInt(find(PRICE_PATTERN, desc));
        int after24  = parseInt(find(PRICE_AFTER24_PATTERN, desc));
        int speed    = parseInt(find(SPEED_PATTERN, desc));
        int duration = parseInt(find(DURATION_PATTERN, desc));
        if (after24 == 0) after24 = price;

        //  extract data-cap limit (speedLimitFrom)
        Integer dataCapLimit = parseIntNull(find(DATA_CAP_PATTERN, desc));
        // if not found, leave as 0

        // extract TV brand
        Matcher tvMatcher = TV_BRAND_PATTERN.matcher(desc);
        boolean tvIncluded = tvMatcher.find();
        String tvBrand = tvIncluded
                ? tvMatcher.group(1).trim()
                : null;

        // "unique" product ID
        String productId = "verbyndich" + COUNTER.incrementAndGet();
        String provider  = resp.product();

        //  nested DTOs
        OfferResponseDto.ContractInfo contract = new OfferResponseDto.ContractInfo(
                find(CONNECTION_PATTERN, desc).toUpperCase(),
                speed,
                dataCapLimit,
                duration,
                null        // maxAge not used here
        );

        OfferResponseDto.CostInfo cost = new OfferResponseDto.CostInfo(
                price * 100,
                price * 100,
                after24 * 100,
                null,
                null,
                false
        );

        OfferResponseDto.TvInfo tv = new OfferResponseDto.TvInfo(
                tvIncluded,
                tvBrand
        );

        return new OfferResponseDto(
                productId,
                provider,
                contract,
                cost,
                tv,
                0.0
        );
    }

    // helpers
    private static String find(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : "";
    }

    private static int parseInt(String s) {
        if (s == null || s.isBlank()) return 0;
        try {
            return Integer.parseInt(s.replace(".", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Integer parseIntNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Integer.parseInt(s.replace(".", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
