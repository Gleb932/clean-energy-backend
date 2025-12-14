package com.codibly.clean_energy.services;

import com.codibly.clean_energy.client.EnergyMixClient;
import com.codibly.clean_energy.dto.DayEnergyMixDTO;
import com.codibly.clean_energy.dto.EnergyMixEntryDTO;
import com.codibly.clean_energy.dto.api.response.GenerationIntervalDTO;
import com.codibly.clean_energy.dto.api.response.GenerationResponse;
import com.codibly.clean_energy.dto.response.ChargingWindowResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EnergyMixServiceTest {
    private final Clock clock = Clock.fixed(Instant.parse("2025-12-13T00:00:00Z"), ZoneOffset.UTC);
    @Mock
    private EnergyMixClient energyMixClient;
    private EnergyMixService energyMixService;

    @BeforeEach
    void setup() {
        energyMixService = new EnergyMixService(energyMixClient, clock);
    }

    @Test
    void shouldAverageIntervals() {
        GenerationResponse response = new GenerationResponse(List.of(
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-13T00:00:00Z"),
                        Instant.parse("2025-12-13T00:30:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 50),
                                new EnergyMixEntryDTO("solar", 5),
                                new EnergyMixEntryDTO("other", 5),
                                new EnergyMixEntryDTO("hydro", 10),
                                new EnergyMixEntryDTO("nuclear", 30)
                        )
                ),
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-13T00:30:00Z"),
                        Instant.parse("2025-12-13T01:00:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 25),
                                new EnergyMixEntryDTO("solar", 5),
                                new EnergyMixEntryDTO("other", 5),
                                new EnergyMixEntryDTO("hydro", 20),
                                new EnergyMixEntryDTO("nuclear", 45)
                        )
                ),
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-13T01:30:00Z"),
                        Instant.parse("2025-12-13T02:00:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("other", 8)
                        )
                )
        ));
        given(energyMixClient.getEnergyMix(any(), any())).willReturn(response);
        List<EnergyMixEntryDTO> expectedEntries = List.of(
                new EnergyMixEntryDTO("coal", 37.5),
                new EnergyMixEntryDTO("solar", 5),
                new EnergyMixEntryDTO("other", 6),
                new EnergyMixEntryDTO("hydro", 15),
                new EnergyMixEntryDTO("nuclear", 37.5)
        );

        List<DayEnergyMixDTO> energyMixes = energyMixService.getSummary();

        assertThat(energyMixes).hasSize(1);
        DayEnergyMixDTO dayMix = energyMixes.getFirst();
        assertThat(dayMix.date()).isEqualTo(LocalDate.of(2025, 12, 13));
        assertThat(dayMix.entries()).hasSize(expectedEntries.size());
        expectedEntries.forEach(expectedEntry ->
                assertThat(dayMix.entries())
                        .anySatisfy(actualEntry -> {
                            assertThat(actualEntry.fuel()).isEqualTo(expectedEntry.fuel());
                            assertThat(actualEntry.percentage()).isCloseTo(expectedEntry.percentage(), within(0.01));
                        })
        );
    }

    @Test
    void shouldAverageSeveralDaysIntervals() {
        GenerationResponse response = new GenerationResponse(List.of(
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-13T00:00:00Z"),
                        Instant.parse("2025-12-13T00:30:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 50),
                                new EnergyMixEntryDTO("solar", 5),
                                new EnergyMixEntryDTO("other", 5),
                                new EnergyMixEntryDTO("hydro", 10),
                                new EnergyMixEntryDTO("nuclear", 30)
                        )
                ),
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-13T00:30:00Z"),
                        Instant.parse("2025-12-13T01:00:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 25),
                                new EnergyMixEntryDTO("solar", 5),
                                new EnergyMixEntryDTO("other", 5),
                                new EnergyMixEntryDTO("hydro", 20),
                                new EnergyMixEntryDTO("nuclear", 45)
                        )
                ),
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-14T00:00:00Z"),
                        Instant.parse("2025-12-14T00:30:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("other", 20)
                        )
                )
                ,
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-14T00:30:00Z"),
                        Instant.parse("2025-12-14T01:00:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("other", 40)
                        )
                )
        ));
        given(energyMixClient.getEnergyMix(any(), any())).willReturn(response);
        List<LocalDate> expectedDates = List.of(
                LocalDate.of(2025, 12, 13),
                LocalDate.of(2025, 12, 14)
        );
        List<List<EnergyMixEntryDTO>> expectedEntriesPerDay = List.of(
                List.of(
                        new EnergyMixEntryDTO("coal", 37.5),
                        new EnergyMixEntryDTO("solar", 5),
                        new EnergyMixEntryDTO("other", 5),
                        new EnergyMixEntryDTO("hydro", 15),
                        new EnergyMixEntryDTO("nuclear", 37.5)
                ),
                List.of(
                        new EnergyMixEntryDTO("other", 30)
                )
        );

        List<DayEnergyMixDTO> energyMixes = energyMixService.getSummary();

        assertThat(energyMixes).hasSize(2);
        for (int i = 0; i < expectedDates.size(); i++) {
            DayEnergyMixDTO dayMix = energyMixes.get(i);
            assertThat(dayMix.date()).isEqualTo(expectedDates.get(i));
            List<EnergyMixEntryDTO> expectedEntries = expectedEntriesPerDay.get(i);
            assertThat(dayMix.entries()).hasSize(expectedEntries.size());
            expectedEntries.forEach(expectedEntry ->
                    assertThat(dayMix.entries())
                            .anySatisfy(actualEntry -> {
                                assertThat(actualEntry.fuel()).isEqualTo(expectedEntry.fuel());
                                assertThat(actualEntry.percentage()).isCloseTo(expectedEntry.percentage(), within(0.01));
                            })
            );
        }
    }

    @Test
    void shouldPickLastTwoIntervals() {
        GenerationResponse response = new GenerationResponse(List.of(
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-13T23:00:00Z"),
                        Instant.parse("2025-12-13T23:30:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 10),
                                new EnergyMixEntryDTO("gas", 20),
                                new EnergyMixEntryDTO("nuclear", 70)
                        )
                ),
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-13T23:30:00Z"),
                        Instant.parse("2025-12-14T00:00:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 20),
                                new EnergyMixEntryDTO("gas", 50),
                                new EnergyMixEntryDTO("nuclear", 30)
                        )
                ),
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-14T00:00:00Z"),
                        Instant.parse("2025-12-14T00:30:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 20),
                                new EnergyMixEntryDTO("gas", 20),
                                new EnergyMixEntryDTO("solar", 60)
                        )
                ),
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-14T00:30:00Z"),
                        Instant.parse("2025-12-14T01:00:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 20),
                                new EnergyMixEntryDTO("gas", 20),
                                new EnergyMixEntryDTO("nuclear", 50)
                        )
                )
        ));
        given(energyMixClient.getEnergyMix(any(), any())).willReturn(response);

        ChargingWindowResponse result = energyMixService.calculateCleanestChargingWindow(1);

        assertThat(result.start())
                .isEqualTo(Instant.parse("2025-12-14T00:00:00Z"));

        assertThat(result.end())
                .isEqualTo(Instant.parse("2025-12-14T01:00:00Z"));

        assertThat(result.cleanEnergy())
                .isCloseTo(55.0, within(0.01));
    }

    @Test
    void shouldPickCleanestIntervalsAcrossMidnight() {
        GenerationResponse response = new GenerationResponse(List.of(
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-13T23:00:00Z"),
                        Instant.parse("2025-12-13T23:30:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 10),
                                new EnergyMixEntryDTO("gas", 20),
                                new EnergyMixEntryDTO("nuclear", 30)
                        )
                ),
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-13T23:30:00Z"),
                        Instant.parse("2025-12-14T00:00:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 20),
                                new EnergyMixEntryDTO("gas", 10),
                                new EnergyMixEntryDTO("nuclear", 30)
                        )
                ),
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-14T00:00:00Z"),
                        Instant.parse("2025-12-14T00:30:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 30),
                                new EnergyMixEntryDTO("gas", 60),
                                new EnergyMixEntryDTO("solar", 10)
                        )
                ),
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-14T00:30:00Z"),
                        Instant.parse("2025-12-14T01:00:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 30),
                                new EnergyMixEntryDTO("gas", 50),
                                new EnergyMixEntryDTO("nuclear", 50)
                        )
                ),
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-14T01:00:00Z"),
                        Instant.parse("2025-12-14T01:30:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 30),
                                new EnergyMixEntryDTO("gas", 50),
                                new EnergyMixEntryDTO("nuclear", 50)
                        )
                ),
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-14T01:30:00Z"),
                        Instant.parse("2025-12-14T02:00:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 10),
                                new EnergyMixEntryDTO("gas", 70),
                                new EnergyMixEntryDTO("nuclear", 10)
                        )
                ),
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-14T02:00:00Z"),
                        Instant.parse("2025-12-14T02:30:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 30),
                                new EnergyMixEntryDTO("gas", 50),
                                new EnergyMixEntryDTO("nuclear", 20)
                        )
                ),
                new GenerationIntervalDTO(
                        Instant.parse("2025-12-14T02:30:00Z"),
                        Instant.parse("2025-12-14T03:00:00Z"),
                        List.of(
                                new EnergyMixEntryDTO("coal", 40),
                                new EnergyMixEntryDTO("gas", 50),
                                new EnergyMixEntryDTO("nuclear", 10)
                        )
                )
        ));
        given(energyMixClient.getEnergyMix(any(), any())).willReturn(response);

        ChargingWindowResponse result = energyMixService.calculateCleanestChargingWindow(2);

        assertThat(result.start())
                .isEqualTo(Instant.parse("2025-12-13T23:30:00Z"));

        assertThat(result.end())
                .isEqualTo(Instant.parse("2025-12-14T01:30:00Z"));

        assertThat(result.cleanEnergy())
                .isCloseTo(35.0, within(0.01));
    }
}