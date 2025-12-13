package com.codibly.clean_energy.dto.response;

import com.codibly.clean_energy.dto.EnergyMixEntryDTO;

import java.time.LocalDate;
import java.util.List;

public record DayEnergyMixResponse(
        LocalDate date,
        List<EnergyMixEntryDTO> entries,
        double cleanEnergy
) {
}
