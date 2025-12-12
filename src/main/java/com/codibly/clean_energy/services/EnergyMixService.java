package com.codibly.clean_energy.services;

import com.codibly.clean_energy.dto.DayEnergyMixDTO;
import com.codibly.clean_energy.dto.EnergyMixEntryDTO;
import com.codibly.clean_energy.dto.api.response.GenerationIntervalDTO;
import com.codibly.clean_energy.dto.api.response.GenerationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class EnergyMixService {
    private final WebClient webClient;

    public EnergyMixService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<DayEnergyMixDTO> getSummary() {
        LocalDate now = LocalDate.now();
        ZoneId utc = ZoneId.of("UTC");
        ZonedDateTime todayStartUtc = now.atStartOfDay().plusSeconds(1).atZone(utc);
        ZonedDateTime todayEndUtc = now.plusDays(3).atStartOfDay().atZone(utc);

        String url = UriComponentsBuilder.fromPath("/generation/{from}/{to}")
                .build(false)// disables encoding, so that ":" is not encoded as "%3A"
                .expand(
                        todayStartUtc.format(DateTimeFormatter.ISO_INSTANT),
                        todayEndUtc.format(DateTimeFormatter.ISO_INSTANT)
                ).toUriString();

        GenerationResponse response = webClient
                .get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(GenerationResponse.class)
                .block();
        if (response == null) {
            return null;
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

}
