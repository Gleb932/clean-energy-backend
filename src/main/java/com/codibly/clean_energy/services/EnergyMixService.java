package com.codibly.clean_energy.services;

import com.codibly.clean_energy.CleanFuels;
import com.codibly.clean_energy.client.EnergyMixClient;
import com.codibly.clean_energy.dto.DayEnergyMixDTO;
import com.codibly.clean_energy.dto.EnergyMixEntryDTO;
import com.codibly.clean_energy.dto.api.response.GenerationIntervalDTO;
import com.codibly.clean_energy.dto.api.response.GenerationResponse;
import com.codibly.clean_energy.exceptions.EnergyMixClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class EnergyMixService {
    private final EnergyMixClient energyMixClient;
    private final Clock clock;

    public EnergyMixService(EnergyMixClient energyMixClient, Clock clock) {
        this.energyMixClient = energyMixClient;
        this.clock = clock;
    }

    public List<DayEnergyMixDTO> getSummary() {
        Instant now = Instant.now(clock);
        LocalDate today = LocalDate.ofInstant(now, clock.getZone());
        ZoneOffset offset = clock.getZone().getRules().getOffset(now);
        Instant todayStartUtc = today.atStartOfDay().plusSeconds(1).toInstant(offset);
        Instant todayEndUtc = today.plusDays(3).atStartOfDay().toInstant(offset);

        GenerationResponse response;
        try {
            response = energyMixClient.getEnergyMix(todayStartUtc, todayEndUtc);
            if (response == null) {
                throw new EnergyMixClientException("EnergyMixClient response was null");
            }
        } catch (Exception e) {
            throw new EnergyMixClientException("Failed to fetch generation response from EnergyMixClient", e);
        }
        return intervalsToDayMixes(response.generationIntervals());
    }

    private List<DayEnergyMixDTO> intervalsToDayMixes(List<GenerationIntervalDTO> generationIntervals) {
        //group intervals by date
        Map<LocalDate, List<GenerationIntervalDTO>> generationIntervalsPerDay = generationIntervals.stream().collect(Collectors.groupingBy(
                interval -> LocalDate.ofInstant(interval.from(), ZoneOffset.UTC)
        ));

        //convert list of entries to DayEnergyMixDTO, calculating average
        List<DayEnergyMixDTO> mixes = generationIntervalsPerDay.entrySet().stream().map(entry -> new DayEnergyMixDTO(
                        entry.getKey(),
                        entry.getValue().stream()
                                .flatMap(interval -> interval.entries().stream())
                                .collect(Collectors.groupingBy(
                                        EnergyMixEntryDTO::fuel,
                                        Collectors.averagingDouble(EnergyMixEntryDTO::percentage)
                                )).entrySet().stream().map(e ->
                                        new EnergyMixEntryDTO(e.getKey(), e.getValue())
                                ).toList()
                ))
                .sorted(Comparator.comparing(DayEnergyMixDTO::date))
                .toList();
        return mixes;
    }

    public double getCleanEnergyPercentage(DayEnergyMixDTO dayEnergyMix) {
        return dayEnergyMix.entries().stream()
                .filter(entry -> CleanFuels.TYPES.contains(entry.fuel()))
                .map(EnergyMixEntryDTO::percentage)
                .reduce(0d, Double::sum);
    }

}
