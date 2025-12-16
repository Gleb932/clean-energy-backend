package com.codibly.clean_energy.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerationMixEntryDTO(
        @JsonProperty("fuel") String fuel,
        @JsonProperty("perc") double percentage
) {

}
