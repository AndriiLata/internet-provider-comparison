// src/main/java/com/example/providercomparison/config/WebClientConfig.java
package com.example.providercomparison.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient pingPerfectWebClient(WebClient.Builder builder,
                                          PingPerfectProperties props) {
        // create a ConnectionProvider that does NOT pool (every request opens its own TCP connection)
        ConnectionProvider provider = ConnectionProvider.newConnection();
        HttpClient httpClient = HttpClient.create(provider);
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        return builder
                .baseUrl(props.getBaseUrl())
                .clientConnector(connector)
                .build();
    }
}
