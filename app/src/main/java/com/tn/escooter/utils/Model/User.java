package com.tn.escooter.utils.Model;

public class User {

    public String token;
    public int id;
    public String serial_number;
    public String name;

    public User(String token, int id, String serial_number, String name) {
        this.token = token;
        this.id = id;
        this.serial_number = serial_number;
        this.name = name;
    }
}