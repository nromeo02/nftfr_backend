package org.nftfr.backend.persistence.model;

public class PaymentMethod {
    public static final int TYPE_ETH = 0;
    public static final int TYPE_EUR = 1;

    private String address;
    private User user;
    private int type;
    private double balance;

    public PaymentMethod() {}

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
        this.type = type;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
