package org.nftfr.backend.application;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class AuctionData {
    private final SseEmitter emitter;
    private Double currentOffer;

    public AuctionData(SseEmitter emitter, Double currentOffer) {
        this.emitter = emitter;
        this.currentOffer = currentOffer;
    }

    public SseEmitter getEmitter() {
        return emitter;
    }

    public Double getCurrentOffer() {
        return currentOffer;
    }

    public void setCurrentOffer(Double currentOffer) {
        this.currentOffer = currentOffer;
    }
}
