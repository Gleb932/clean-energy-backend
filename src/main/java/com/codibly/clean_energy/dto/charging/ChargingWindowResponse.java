package com.codibly.clean_energy.dto.charging;

import java.time.Instant;

public record ChargingWindowResponse(
        Instant start,
        Instant end,
        double cleanEnergy
) {
}
