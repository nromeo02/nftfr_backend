package org.nftfr.backend.persistence.model;

public class PaymentMethod {
    private String address;
    private String username;
    private int type;
    private double balance;
    public PaymentMethod(){}
    public PaymentMethod(String address, String username, int type,double balance) {
        this.address = address;
        this.username = username;
        this.type = type;
        this.balance = balance;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String user) {
        this.username = user;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
