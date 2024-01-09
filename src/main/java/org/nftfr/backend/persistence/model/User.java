package org.nftfr.backend.persistence.model;

public class User {
    private String username;
    private String name;
    private String surname;
    private String password;
    private Long value;
    private int rank;
    private boolean admin;
    public User(){}
    public User(String username, String name, String surname, String password, Long value, int rank, boolean admin) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.password = password;
        this.value = value;
        this.rank = rank;
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isAdmin() {
        return admin;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String s){
        this.username = s;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

}
