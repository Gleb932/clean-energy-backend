package com.codibly.clean_energy.dto.external;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record GenerationIntervalDTO(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mmX", timezone = "UTC")
        Instant from,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mmX", timezone = "UTC")
        Instant to,
        @JsonProperty("generationmix")
        List<GenerationMixEntryDTO> entries
) {
}
