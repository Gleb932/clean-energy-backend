package com.codibly.clean_energy.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record EnergyMixEntryDTO(
        @JsonAlias("fuel") String fuel,
        @JsonAlias("perc") double percentage
) {

}
