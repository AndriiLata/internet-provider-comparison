package com.example.providercomparison.dto.ui;

import java.time.LocalDateTime;

public record RatingResponseDto(
        String userName,
        Integer ranking,           // 1-5
        String comment,
        LocalDateTime createdAt
) { }
