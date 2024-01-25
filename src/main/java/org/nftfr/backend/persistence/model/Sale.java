package org.nftfr.backend.persistence.model;

import java.time.LocalDateTime;

public class Sale {
    private Nft nft;
    private PaymentMethod paymentMethod;
    private double price;
    private LocalDateTime creationDate;
    private LocalDateTime endTime;
    private String offerMaker;

    public Sale() {}

    public Nft getNft() {
        return nft;
    }

    public void setNft(Nft nft) {
        this.nft = nft;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getOfferMaker() {
        return offerMaker;
    }

    public void setOfferMaker(String offerMaker) {
        this.offerMaker = offerMaker;
    }
}
