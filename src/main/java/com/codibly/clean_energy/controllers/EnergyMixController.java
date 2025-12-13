package com.codibly.clean_energy.controllers;

import com.codibly.clean_energy.dto.DayEnergyMixDTO;
import com.codibly.clean_energy.dto.response.DayEnergyMixResponse;
import com.codibly.clean_energy.services.EnergyMixService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/energy-mix")
public class EnergyMixController {
    private final EnergyMixService energyMixService;

    public EnergyMixController(EnergyMixService energyMixService) {
        this.energyMixService = energyMixService;
    }

    @GetMapping("/summary")
    List<DayEnergyMixResponse> getSummary() {
        List<DayEnergyMixDTO> result = energyMixService.getSummary();
        return result.stream()
                .map(dayMix -> new DayEnergyMixResponse(
                        dayMix.date(),
                        dayMix.entries(),
                        energyMixService.getCleanEnergyPercentage(dayMix))
                ).toList();
    }
}
