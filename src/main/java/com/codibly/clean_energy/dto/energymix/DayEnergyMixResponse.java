package com.codibly.clean_energy.dto.energymix;

import java.time.LocalDate;
import java.util.List;

public record DayEnergyMixResponse(
        LocalDate date,
        List<EnergyMixEntryDTO> entries,
        double cleanEnergy
) {
}
