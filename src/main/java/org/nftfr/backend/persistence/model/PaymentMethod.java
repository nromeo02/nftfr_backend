package org.nftfr.backend.persistence.model;

public class PaymentMethod {
    private String address;
    private User user;
    private int type;
    private double balance;

    public PaymentMethod(String address, User user, int type,double balance) {
        this.address = address;
        this.user = user;
        this.type = type;
        this.balance = balance;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        type = type;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
