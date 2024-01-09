package org.nftfr.backend.persistenza.model;

public class User {
    private String username;
    private String name;
    private String surname;
    private String password;
    private Long value;
    private int rank;
    private int isAdmin;
    public User(){}
    public User(String username, String name, String surname, String password, Long value, int rank, int isAdmin) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.password = password;
        this.value = value;
        this.rank = rank;
        this.isAdmin = isAdmin;
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

    public int getIsAdmin() {
        return isAdmin;
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

    public void setIsAdmin(int isAdmin) {
        this.isAdmin = isAdmin;
    }

}
