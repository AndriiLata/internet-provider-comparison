package com.example.providercomparison.entity;

import io.r2dbc.postgresql.codec.Json;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Table("session_offer")
public class SessionOfferEntity {

    @Id
    private Long id;

    private UUID   sessionId;
    private Json offer;    // raw JSON (OfferResponseDto)
}