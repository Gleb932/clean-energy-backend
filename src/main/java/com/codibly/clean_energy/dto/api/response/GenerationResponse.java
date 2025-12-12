package com.codibly.clean_energy.dto.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GenerationResponse(
        @JsonProperty("data")
        List<GenerationIntervalDTO> generationIntervals
) {
}
