package com.codibly.clean_energy.dto.energymix;

import java.time.Instant;
import java.util.List;

public record IntervalEnergyMixDTO(
        Instant from,
        Instant to,
        List<EnergyMixEntryDTO> entries
) {
}
