package com.example.bitelens;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NutritionResponse {

    @SerializedName("foods")
    private List<Food> foods;

    public List<Food> getFoods() {
        return foods;
    }

    public Food getFirstFood() {
        if (foods != null && !foods.isEmpty()) {
            return foods.get(0);
        } else {
            return null;
        }
    }

    public Food getFood(int index) {
        if (foods != null && !foods.isEmpty()) {
            return foods.get(index);
        } else {
            return null;
        }
    }

    public static class Food {

        @SerializedName("food_name")
        private String foodName;

        @SerializedName("serving_qty")
        private double servingQuantity;

        @SerializedName("serving_unit")
        private String servingUnit;

        @SerializedName("nf_calories")
        private double calories;

        @SerializedName("nf_total_fat")
        private double totalFat;

        @SerializedName("nf_saturated_fat")
        private double saturatedFat;

        @SerializedName("nf_cholesterol")
        private double cholesterol;

        @SerializedName("nf_sodium")
        private double sodium;

        @SerializedName("nf_total_carbohydrate")
        private double totalCarbohydrate;

        @SerializedName("nf_dietary_fiber")
        private double dietaryFiber;

        @SerializedName("nf_sugars")
        private double sugars;

        @SerializedName("nf_protein")
        private double protein;

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

        public String getFormattedNutritionInfo() {
            return "Food name: " + foodName + "\n" +
                    "Serving quantity: " + servingQuantity + "\n" +
                    "Serving unit: " + servingUnit + "\n" +
                    "Calories: " + calories + "\n" +
                    "Total fat: " + totalFat + "g" + "\n" +
                    "Saturated fat: " + saturatedFat + "g" + "\n" +
                    "Cholesterol: " + cholesterol + "mg" + "\n" +
                    "Sodium: " + sodium + "mg" + "\n" +
                    "Total carbohydrate: " + totalCarbohydrate + "g" + "\n" +
                    "Dietary fiber: " + dietaryFiber + "g" + "\n" +
                    "Sugars: " + sugars + "g" + "\n" +
                    "Protein: " + protein + "g";
        }
    }
}
