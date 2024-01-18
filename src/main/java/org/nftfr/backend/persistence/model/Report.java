package org.nftfr.backend.persistence.model;

public class Report {
    private String nft_id;
    private int counter;
    public Report(){}

    public Report(String nft_id, int counter) {
        this.nft_id = nft_id;
        this.counter = counter;
    }

    public String getNft_id() {
        return nft_id;
    }
    public void setNft_id(String nft_id) {
        this.nft_id = nft_id;
    }
    public int getCounter() {
        return counter;
    }
    public void setCounter(int counter) {
        this.counter = counter;
    }
}
