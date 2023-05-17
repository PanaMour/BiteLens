package com.example.bitelens;

import java.util.Date;

public class Meal {
    private String name;
    private Double calories;
    private Double fat;
    private Double cholesterol;
    private Double sodium;
    private Double carbohydrate;
    private Double fiber;
    private Double sugars;
    private Double protein;
    private Date date;

    public Meal() {

    }

    public Meal(String name, Double calories, Double fat, Double cholesterol, Double sodium, Double carbohydrate, Double fiber, Double sugars, Double protein, Date date) {
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

