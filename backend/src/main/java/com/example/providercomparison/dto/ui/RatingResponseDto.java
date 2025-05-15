package com.example.providercomparison.dto.ui;

import java.time.LocalDateTime;

/**
 * What the client receives when it asks for all ratings of a service.
 */
public record RatingResponseDto(
        String userName,
        Integer ranking,           // 1-5
        String comment,
        LocalDateTime createdAt
) { }
