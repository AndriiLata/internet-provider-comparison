package com.example.providercomparison.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class KeepAliveConfig {

    private final DatabaseClient db;

    public KeepAliveConfig(DatabaseClient db) { this.db = db; }

    @Scheduled(fixedRateString = "PT4M")   // every 4 min
    public void keepAlive() {
        db.sql("SELECT 1").fetch().rowsUpdated().subscribe();
    }
}
