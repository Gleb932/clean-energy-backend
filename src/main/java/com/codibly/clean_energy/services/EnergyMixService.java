package com.codibly.clean_energy.services;

import com.codibly.clean_energy.client.EnergyMixClient;
import com.codibly.clean_energy.dto.DayEnergyMixDTO;
import com.codibly.clean_energy.dto.EnergyMixEntryDTO;
import com.codibly.clean_energy.dto.api.response.GenerationIntervalDTO;
import com.codibly.clean_energy.dto.api.response.GenerationResponse;
import com.codibly.clean_energy.dto.response.ChargingWindowResponse;
import com.codibly.clean_energy.exceptions.EnergyMixClientException;
import com.codibly.clean_energy.exceptions.ForecastWindowException;
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
    private final static int INTERVAL_LENGTH = 30;

    public EnergyMixService(EnergyMixClient energyMixClient, Clock clock) {
        this.energyMixClient = energyMixClient;
        this.clock = clock;
    }

    public List<DayEnergyMixDTO> getSummary() {
        GenerationResponse generationForecast = getGenerationForecast(3);
        return intervalsToDayMixes(generationForecast.generationIntervals());
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

    public double getCleanEnergyPercentage(List<EnergyMixEntryDTO> entries) {
        return entries.stream()
                .filter(entry -> CleanFuels.TYPES.contains(entry.fuel()))
                .map(EnergyMixEntryDTO::percentage)
                .reduce(0d, Double::sum);
    }

    public ChargingWindowResponse calculateCleanestChargingWindow(int hours) {
        GenerationResponse generationForecast = getGenerationForecast(3);
        return calculateCleanestChargingWindow(hours, generationForecast.generationIntervals());
    }

    private GenerationResponse getGenerationForecast(int days) {
        Instant now = Instant.now(clock);
        LocalDate today = LocalDate.ofInstant(now, clock.getZone());
        ZoneOffset offset = clock.getZone().getRules().getOffset(now);
        Instant todayStartUtc = today.atStartOfDay().plusSeconds(1).toInstant(offset);
        Instant lastDayEndUtc = today.plusDays(days).atStartOfDay().toInstant(offset);

        GenerationResponse response;
        try {
            response = energyMixClient.getEnergyMix(todayStartUtc, lastDayEndUtc);
            if (response == null) {
                throw new EnergyMixClientException("EnergyMixClient response was null");
            }
            return response;
        } catch (Exception e) {
            throw new EnergyMixClientException("Failed to fetch generation response from EnergyMixClient", e);
        }
    }

    private ChargingWindowResponse calculateCleanestChargingWindow(int hours, List<GenerationIntervalDTO> generationIntervals) {
        int intervalsPerWindow = hours * 60 / INTERVAL_LENGTH;
        if(generationIntervals.size() < intervalsPerWindow) throw new ForecastWindowException(hours);

        double[] cleanEnergy = new double[generationIntervals.size()];
        for(int i = 0; i < generationIntervals.size(); i++) {
            cleanEnergy[i] = getCleanEnergyPercentage(generationIntervals.get(i).entries());
        }

        //fill the first window to needed hours
        double sum = 0;
        for(int i = 0; i < intervalsPerWindow; i++) {
            sum += cleanEnergy[i];
        }

        //slide the window and check if the sum gets better
        int bestWindowIndex = 0;
        double bestSum = sum;
        for(int i = intervalsPerWindow; i < cleanEnergy.length; i++) {
            sum += cleanEnergy[i] - cleanEnergy[i - intervalsPerWindow];
            if(sum > bestSum) {
                bestWindowIndex = i - intervalsPerWindow + 1;
                bestSum = sum;
            }
        }

        return new ChargingWindowResponse(
                generationIntervals.get(bestWindowIndex).from(),
                generationIntervals.get(bestWindowIndex + intervalsPerWindow - 1).to(),
                bestSum / intervalsPerWindow);
    }
}
