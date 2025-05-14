package com.example.providercomparison.dto.provider.verbyndich;

import com.example.providercomparison.dto.provider.verbyndich.model.VerbynDichResponse;
import com.example.providercomparison.dto.ui.OfferResponseDto;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VerbynDichMapper {

    /* ─────────── 1. unique product ids ─────────── */
    private static final AtomicLong COUNTER = new AtomicLong();

    /* ─────────── 2. patterns ─────────── */
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

    private VerbynDichMapper() {}

    public static OfferResponseDto toDto(VerbynDichResponse resp) {
        String desc = resp.description();

        // parse raw numbers
        int price       = parseInt(find(PRICE_PATTERN, desc));
        int after24     = parseInt(find(PRICE_AFTER24_PATTERN, desc));
        int speed       = parseInt(find(SPEED_PATTERN, desc));
        int duration    = parseInt(find(DURATION_PATTERN, desc));
        if (after24 == 0) after24 = price;  // if no 24-month price, assume flat

        // product/meta
        String productId = "verbyndich" + COUNTER.incrementAndGet();
        String provider  = resp.product();

        // connection
        String connection = find(CONNECTION_PATTERN, desc).toUpperCase();
        if (connection.isBlank()) connection = "UNKNOWN";

        // TV
        Matcher tvM = TV_PATTERN.matcher(desc);
        boolean tvIncluded = false;
        String tvBrand = "";
        if (tvM.find()) {
            tvIncluded = true;
            String match = tvM.group(1);
            // only treat actual brand tags as brand
            if (match.equalsIgnoreCase("RobynTV+") || match.equalsIgnoreCase("TV+")) {
                tvBrand = match;
            }
        }

        // cost info (all in cents)
        int discountedMonthlyCost = price * 100; // (since there is no discount)
        int nominalMonthlyCost     = price * 100;
        Integer costAfter24        = after24 * 100;


        // build nested DTOs
        OfferResponseDto.ContractInfo contract = new OfferResponseDto.ContractInfo(
                connection,               // connectionType
                speed,                    // speed (mbps)
                speed,                    // speedLimitFrom (fixed)
                duration,                 // contractDurationInMonths
                0                         // maxAge (not used)
        );

        OfferResponseDto.CostInfo cost = new OfferResponseDto.CostInfo(
                discountedMonthlyCost,    // discountedMonthlyCostInCent
                nominalMonthlyCost,       // monthlyCostInCent (regular)
                costAfter24,              // monthlyCostAfter24mInCent
                null,     // monthlyDiscountValueInCent
                0,              // maxDiscountInCent
                false           // installationService
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
                tv
        );
    }

    /* ───────────────────── helpers ───────────────────── */

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
}
