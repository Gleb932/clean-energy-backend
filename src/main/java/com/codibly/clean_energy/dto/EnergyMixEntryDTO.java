package com.codibly.clean_energy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EnergyMixEntryDTO(
        @JsonProperty("fuel") String fuel,
        @JsonProperty("perc") double percentage
) {

}
