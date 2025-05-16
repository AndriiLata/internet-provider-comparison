package com.example.providercomparison.entity;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table("search_session")
public class SearchSessionEntity implements Persistable<UUID> {

    @Id
    private UUID sessionId;

    private Json criteria;     // raw JSON (SearchCriteria)
    private LocalDateTime createdAt;

    @Override public UUID getId()        { return sessionId; }
    @Override public boolean isNew()     { return true; }   // ← always INSERT
}
