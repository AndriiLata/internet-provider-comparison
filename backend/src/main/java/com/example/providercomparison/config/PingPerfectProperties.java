package com.example.providercomparison.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "provider.pingperfect")
public class PingPerfectProperties {
    private String baseUrl;
    private String clientId;
    private String secret;
}
