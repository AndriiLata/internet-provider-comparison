package com.example.providercomparison.dto.provider.verbyndich;

import com.example.providercomparison.dto.provider.verbyndich.model.VerbynDichResponse;
import com.example.providercomparison.dto.ui.OfferResponseDto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerbynDichMapper {
    private static final Pattern PRICE_PATTERN          = Pattern.compile("Für nur (\\d+)€ im Monat");
    private static final Pattern SPEED_PATTERN          = Pattern.compile("Geschwindigkeit von (\\d+) Mbit/s");
    private static final Pattern DURATION_PATTERN       = Pattern.compile("Mindestvertragslaufzeit (\\d+) Monate");
    private static final Pattern PRICE_AFTER24_PATTERN  = Pattern.compile("Ab dem 24\\. Monat beträgt der monatliche Preis (\\d+)€");
   // private static final Pattern CONNECTION_PATTERN   = Pattern.compile("erhalten Sie eine (\\d+)-Verbindung");

    public static OfferResponseDto toDto(VerbynDichResponse resp) {
        String desc = resp.description();
        int price       = parse(find(PRICE_PATTERN, desc));
        int speed       = parse(find(SPEED_PATTERN, desc));
        int duration    = parse(find(DURATION_PATTERN, desc));
        int after24     = parse(find(PRICE_AFTER24_PATTERN, desc));
        return new OfferResponseDto(
                resp.product(),               // productId
                "VerbynDich",                // provider
                speed,                        // speed
                price * 100,                  // monthlyCostInCent
                after24 * 100,                // monthlyCostAfter24mInCent
                duration,                     // durationInMonths
                "DSL",                       // connectionType
                false,                        // tvIncluded
                false,                        // installationService
                null,                         // voucherValueInCent
                null,                         // voucherType
                null                          // discountInCent
        );
    }

    private static String find(Pattern p, String text) {
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1);
        return "0";
    }

    private static int parse(String s) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return 0; }
    }
}
