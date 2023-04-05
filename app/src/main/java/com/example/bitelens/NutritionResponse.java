package com.example.bitelens;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NutritionResponse {

    @SerializedName("foods")
    private List<Food> foods;

    public List<Food> getFoods() {
        return foods;
    }

    public static class Food {

        @SerializedName("food_name")
        private String foodName;

        @SerializedName("serving_qty")
        private double servingQuantity;

        @SerializedName("serving_unit")
        private String servingUnit;

        @SerializedName("calories")
        private double calories;

        @SerializedName("total_fat")
        private double totalFat;

        @SerializedName("saturated_fat")
        private double saturatedFat;

        @SerializedName("cholesterol")
        private double cholesterol;

        @SerializedName("sodium")
        private double sodium;

        @SerializedName("total_carbohydrate")
        private double totalCarbohydrate;

        @SerializedName("dietary_fiber")
        private double dietaryFiber;

        @SerializedName("sugars")
        private double sugars;

        @SerializedName("protein")
        private double protein;

        // Add more fields if needed

        public String getFoodName() {
            return foodName;
        }

        public double getServingQuantity() {
            return servingQuantity;
        }

        public String getServingUnit() {
            return servingUnit;
        }

        public double getCalories() {
            return calories;
        }

        public double getTotalFat() {
            return totalFat;
        }

        public double getSaturatedFat() {
            return saturatedFat;
        }

        public double getCholesterol() {
            return cholesterol;
        }

        public double getSodium() {
            return sodium;
        }

        public double getTotalCarbohydrate() {
            return totalCarbohydrate;
        }

        public double getDietaryFiber() {
            return dietaryFiber;
        }

        public double getSugars() {
            return sugars;
        }

        public double getProtein() {
            return protein;
        }

        // Add getter methods for any additional fields
    }
}
