package com.example.bitelens;

public class User {
    private String uid;
    private String name;
    private String surname;
    private String location;
    private String token;

    public User() {}

    public User(String uid, String name, String surname, String location, String token) {
        this.uid = uid;
        this.name = name;
        this.surname = surname;
        this.location = location;
        this.token = token;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getLocation() { return location; }
    public String getToken() { return token; }
}
