package com.example.bitelens;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Meal implements Serializable {
    private String id;
    private String name;
    private Double calories;
    private Double fat;
    private Double cholesterol;
    private Double sodium;
    private Double carbohydrate;
    private Double fiber;
    private Double sugars;
    private Double protein;
    private String date;
    private String imageurl;
    private String location;
    private String place;
    private Timestamp statdate;
    public Meal() {

    }

    public Meal(String id,String name, Double calories, Double fat, Double cholesterol, Double sodium, Double carbohydrate, Double fiber, Double sugars, Double protein, String date, String imageurl,String location,String place) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.fat = fat;
        this.cholesterol = cholesterol;
        this.sodium = sodium;
        this.carbohydrate = carbohydrate;
        this.fiber = fiber;
        this.sugars = sugars;
        this.protein = protein;
        this.date = date;
        this.imageurl = imageurl;
        this.location = location;
        this.place = place;

    }

    // Getters and setters for all fields
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public Double getFat() {
        return fat;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public Double getFiber() {
        return fiber;
    }

    public void setFiber(Double fiber) {
        this.fiber = fiber;
    }

    public Double getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(Double cholesterol) {
        this.cholesterol = cholesterol;
    }

    public Double getSodium() {
        return sodium;
    }

    public void setSodium(Double sodium) {
        this.sodium = sodium;
    }

    public Double getCarbohydrate() {
        return carbohydrate;
    }

    public void setCarbohydrate(Double carbohydrate) {
        this.carbohydrate = carbohydrate;
    }

    public Double getProtein() {
        return protein;
    }

    public void setProtein(Double protein) {
        this.protein = protein;
    }

    public Double getSugars() {
        return sugars;
    }

    public void setSugars(Double sugars) {
        this.sugars = sugars;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

