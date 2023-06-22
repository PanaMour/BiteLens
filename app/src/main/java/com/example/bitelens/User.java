package com.example.bitelens;

public class User {
    private String uid;
    private String name;
    private String surname;
    private int calories_consumed;
    private int calories_goal;

    public User() {
    }

    public User(String uid, String name, String surname, int calories_consumed, int calories_goal) {
        this.uid = uid;
        this.name = name;
        this.surname = surname;
        this.calories_consumed = calories_consumed;
        this.calories_goal = calories_goal;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public int getCalories_consumed() { return calories_consumed; }
    public int getCalories_goal() { return calories_goal; }

}
