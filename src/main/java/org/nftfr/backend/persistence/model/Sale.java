package org.nftfr.backend.persistence.model;
import java.time.*;
import java.time.Duration;
public class Sale {
    private int id;
    private String idNft;
    private double price;
    private LocalDateTime timeLeft;
    private LocalDateTime creationDate;

    public Sale(int id, String idNft, double price, LocalDateTime timeLeft, LocalDateTime creationDate) {
        this.id = id;
        this.idNft = idNft;
        this.price = price;
        this.timeLeft = timeLeft;
        this.creationDate = creationDate;
    }
    public void addDurationTime(Duration duration){
        this.timeLeft = this.timeLeft.plus(duration);
    }
    public void addDurationDate(Duration duration){
        this.creationDate = this.creationDate.plus(duration);
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getidNft() {
        return idNft;
    }

    public void setidNft(String idNft) {
        this.idNft = idNft;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDateTime getTimeLeft() {
        return timeLeft;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }
}
