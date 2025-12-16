package com.codibly.clean_energy.controller;

import com.codibly.clean_energy.dto.charging.ChargingWindowResponse;
import com.codibly.clean_energy.service.EnergyMixService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/charging")
public class ChargingController {
    private final EnergyMixService energyMixService;

    public ChargingController(EnergyMixService energyMixService) {
        this.energyMixService = energyMixService;
    }

    @GetMapping("/window")
    ChargingWindowResponse getWindow(@NotNull @RequestParam Integer hours) {
        if(hours < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Charging window must be at least a hour");
        return energyMixService.calculateCleanestChargingWindow(hours);
    }
}
