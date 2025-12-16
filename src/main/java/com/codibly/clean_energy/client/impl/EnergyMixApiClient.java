package com.codibly.clean_energy.client.impl;

import com.codibly.clean_energy.client.EnergyMixClient;
import com.codibly.clean_energy.dto.external.GenerationResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;

@Component
public class EnergyMixApiClient implements EnergyMixClient {
    WebClient webClient;

    public EnergyMixApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public GenerationResponse getEnergyMix(Instant from, Instant to) {
        String url = UriComponentsBuilder.fromPath("/generation/{from}/{to}")
                .build(false)// disables encoding, so that ":" is not encoded as "%3A"
                .expand(
                        from,
                        to
                ).toUriString();

        return webClient
                .get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(GenerationResponse.class)
                .block();
    }
}
