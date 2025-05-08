package com.example.providercomparison.dto.provider.verbyndich;

import com.example.providercomparison.dto.provider.verbyndich.model.VerbynDichResponse;
import com.example.providercomparison.dto.ui.OfferResponseDto;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VerbynDichMapper {

    /* ─────────── 1. unique product ids ─────────── */
    private static final AtomicLong COUNTER = new AtomicLong();

    /* ─────────── 2. patterns (unchanged from previous version) ─────────── */
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

    private static final Pattern TV_PATTERN =
            Pattern.compile("(Fernsehsender\\s+enthalten|RobynTV\\+|TV\\+)", FLAGS);

    /* ────────────────────────────────────────────────────────────────────── */

    public static OfferResponseDto toDto(VerbynDichResponse resp) {
        String desc = resp.description();

        int price     = parseInt(find(PRICE_PATTERN,        desc));
        int speed     = parseInt(find(SPEED_PATTERN,        desc));
        int duration  = parseInt(find(DURATION_PATTERN,     desc));
        int after24   = parseInt(find(PRICE_AFTER24_PATTERN, desc));

        if (after24 == 0) after24 = price;           // same price if clause missing

        String connection = find(CONNECTION_PATTERN, desc).toUpperCase();
        if (connection.isBlank()) connection = "UNKNOWN";

        boolean tvIncluded = TV_PATTERN.matcher(desc).find();

        /* --------- CHANGES HERE --------- */
        String productId = "verbyndich" + COUNTER.incrementAndGet();  // unique id
        String provider  = resp.product();                            // provider field
        /* -------------------------------- */

        return new OfferResponseDto(
                productId,
                provider,
                speed,
                price   * 100,
                after24 * 100,
                duration,
                connection,
                tvIncluded,
                false,   // installationService
                null,
                null,
                null
        );
    }

    /* ───────────────────── helpers ───────────────────── */

    private static String find(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1) : "";
    }

    private static int parseInt(String s) {
        if (s == null || s.isBlank()) return 0;
        try { return Integer.parseInt(s.replace(".", "")); }
        catch (NumberFormatException e) { return 0; }
    }

    private VerbynDichMapper() {}
}
