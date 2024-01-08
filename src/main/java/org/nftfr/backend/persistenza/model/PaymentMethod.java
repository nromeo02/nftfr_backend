package org.nftfr.backend.persistenza.model;

public class PaymentMethod {
    private String address;
    private User user;
    private int Type;

    public PaymentMethod(String address, User user, int type) {
        this.address = address;
        this.user = user;
        Type = type;
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
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }
}
