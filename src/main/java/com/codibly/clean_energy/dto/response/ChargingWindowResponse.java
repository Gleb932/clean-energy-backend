package com.codibly.clean_energy.dto.response;

import java.time.Instant;

public record ChargingWindowResponse(
        Instant start,
        Instant end,
        double cleanEnergy
) {
}
