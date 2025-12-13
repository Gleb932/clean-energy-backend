package com.codibly.clean_energy.client;

import com.codibly.clean_energy.dto.api.response.GenerationResponse;

import java.time.Instant;

public interface EnergyMixClient {
    GenerationResponse getEnergyMix(Instant from, Instant to);
}
