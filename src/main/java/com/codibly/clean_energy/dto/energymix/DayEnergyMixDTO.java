package com.codibly.clean_energy.dto.energymix;

import java.time.LocalDate;
import java.util.List;

public record DayEnergyMixDTO(
        LocalDate date,
        List<EnergyMixEntryDTO> entries
) {

}