package org.nftfr.backend.persistenza.model;
import java.time.*;
import java.time.Duration;
public class Sale {
    private String id;
    private String idnft;
    private double price;
    private LocalTime timeLeft;
    private LocalDate creationDate;

    public Sale(String id, String idnft, double price, LocalTime timeLeft, LocalDate creationDate) {
        this.id = id;
        this.idnft = idnft;
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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdnft() {
        return idnft;
    }

    public void setIdnft(String idnft) {
        this.idnft = idnft;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalTime getTimeLeft() {
        return timeLeft;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }
}
