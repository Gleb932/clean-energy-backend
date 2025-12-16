package com.codibly.clean_energy.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GenerationResponse(
        @JsonProperty("data")
        List<GenerationIntervalDTO> generationIntervals
) {
}
