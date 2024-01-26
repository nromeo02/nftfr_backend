package org.nftfr.backend.persistence.model;

public class Report {
    private String nftId;
    private int counter;
    public Report() {}

    public String getNftId() {
        return nftId;
    }

    public void setNftId(String nftId) {
        this.nftId = nftId;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

}
