package org.nftfr.backend.persistence.model;
import java.time.*;
import java.time.Duration;
public class Sale {
    private int id;
    private String idNft;
    private double price;
    private LocalDateTime creationDate;
    private LocalDateTime endTime;

    public Sale(int id, String idNft, double price,  LocalDateTime creationDate, LocalDateTime endTime) {
        this.id = id;
        this.idNft = idNft;
        this.price = price;
        this.creationDate = creationDate;
        this.endTime = endTime;
    }
    public void addDurationTime(Duration duration){
        this.endTime = this.endTime.plus(duration);
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

    public String getIdNft() {
        return idNft;
    }

    public void setIdNft(String idNft) {
        this.idNft = idNft;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }



    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
