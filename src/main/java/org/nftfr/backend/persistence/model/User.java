package org.nftfr.backend.persistence.model;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class User {
    private String username;
    private String name;
    private String surname;
    private String encryptedPw;
    private int rank = 0;
    private boolean admin = false;
    public User() {}


    public String getUsername(){
        return username;
    }

    public void setUsername(String s){
        this.username = s;
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

    public String getEncryptedPw() {
        return encryptedPw;
    }

    public void setEncryptedPw(String encryptedPw) {
        this.encryptedPw = encryptedPw;
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

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public static String encryptPassword(String plainText) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(plainText);
    }

    public boolean verifyPassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(password, this.encryptedPw);
    }
}
