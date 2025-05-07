package com.example.providercomparison.dto.provider.pingperfect;

import com.example.providercomparison.config.PingPerfectProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class PingPerfectSigner {

    private final PingPerfectProperties props;   // injected

    public SignedHeaders sign(String jsonPayload) {
        try {
            long ts = Instant.now().getEpochSecond();
            String toSign = ts + ":" + jsonPayload;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(props.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(toSign.getBytes(StandardCharsets.UTF_8));
            String sig = HexFormat.of().formatHex(raw);

            return new SignedHeaders(ts, sig);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot compute PingPerfect signature", ex);
        }
    }

    public record SignedHeaders(long timestamp, String signature) { }
}

