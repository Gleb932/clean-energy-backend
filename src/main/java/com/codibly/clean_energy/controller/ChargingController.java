package com.codibly.clean_energy.controller;

import com.codibly.clean_energy.dto.charging.ChargingWindowResponse;
import com.codibly.clean_energy.service.EnergyMixService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/charging")
public class ChargingController {
    private final EnergyMixService energyMixService;

    public ChargingController(EnergyMixService energyMixService) {
        this.energyMixService = energyMixService;
    }

    @GetMapping("/window")
    ChargingWindowResponse getWindow(@NotNull @RequestParam @Min(1) Integer hours) {
        return energyMixService.calculateCleanestChargingWindow(hours);
    }
}
