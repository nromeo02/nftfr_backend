package org.nftfr.backend.application;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public class MoneyConverter {
    private static final String URL = "https://api.etherscan.io";
    private static final String ENDPOINT = "/api?module=stats&action=ethprice";
    private static MoneyConverter instance = null;
    private final WebClient client;
    private LocalDateTime lastUpdated = null;

    private record Result(Double ethbtc, Long ethbtc_timestamp, Double ethusd, Long ethusd_timestamp) {}

    private record Response(String status, String message, Result result) {}

    private Response lastResponse = null;

    private MoneyConverter() {
        client = WebClient.builder().baseUrl(URL).build();
    }

    public static MoneyConverter getInstance() {
        if (instance == null)
            instance = new MoneyConverter();

        return instance;
    }

    private double getRateChange() {
        // The rate is updated every 15 minutes.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime then = now.minusMinutes(15);
        if (lastResponse == null || then.isAfter(lastUpdated)) {
            final Mono<Response> mono = client.get().uri(ENDPOINT).retrieve().bodyToMono(Response.class);
            lastResponse = mono.block();
            lastUpdated = now;
        }

        assert lastResponse != null;
        return lastResponse.result().ethusd();
    }

    public double convertUsdToEth(double usd) {
        return usd / getRateChange();
    }

    public double convertEthToUsd(double eth) {
        return eth * getRateChange();
    }
}
