package com.example.providercomparison.dto.ui;

import jakarta.validation.constraints.*;

public record RatingRequestDto(
        @NotBlank String serviceName,
        @NotBlank String userName,
        @Email @NotBlank String email,
        @Min(1) @Max(5) Integer ranking,
        String comment
) { }
