package com.example.bitelens;

public class FoodConfidence {
    private String name;
    private float confidence;

    public FoodConfidence(String name, float confidence) {
        this.name = name;
        this.confidence = confidence;
    }

    public String getName() {
        return name;
    }

    public float getConfidence() {
        return confidence;
    }
}
