package com.example.bitelens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import api.ApiHelper;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_IMAGE_REQUEST_CODE = 1;

    private Button selectImageButton;
    private ImageView foodImage;
    private TextView nutritionInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Initialize UI components
        selectImageButton = findViewById(R.id.select_image_button);
        foodImage = findViewById(R.id.food_image);
        nutritionInfo = findViewById(R.id.nutrition_info);

        // Set click listener for the select image button
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    // Launches the image picker to allow the user to select an image
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
    }

    // Processes the selected image and recognizes food using the ML Kit Image Labeling library
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                foodImage.setImageBitmap(bitmap);
                recognizeFood(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Recognizes food items in the input image using ML Kit Image Labeling
    private void recognizeFood(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build();
        ImageLabeler labeler = ImageLabeling.getClient(options);

        // Processes the image and retrieves a list of recognized labels
        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        for (ImageLabel label : labels) {
                            String text = label.getText();
                            System.out.println("FOOD: "+text);
                            float confidence = label.getConfidence();
                            // If the recognized label is related to food, fetch the nutritional information
                            if (isFoodRelated(text)) {
                                fetchNutritionInfo(text);
                                break;
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors in the labeling process
                    }
                });
    }

    private boolean isFoodRelated(String label) {

        /*private boolean isFoodRelated(String label) {
            // A list of common food-related keywords
            String[] foodKeywords = {"food", "fruit", "vegetable", "meat", "drink", "beverage",
                    "snack", "bread", "cereal", "cheese", "sweets", "dessert",
                    "pizza", "pasta", "rice", "salad", "sandwich", "soup"};

            // Convert the label to lowercase for comparison
            String lowercaseLabel = label.toLowerCase();

            // Check if any food keyword is present in the label
            for (String keyword : foodKeywords) {
                if (lowercaseLabel.contains(keyword)) {
                    return true;
                }
            }

            return false; // Return false if none of the keywords were found
        }*/

        return true; // For simplicity, assume all labels are food-related
    }

    private void fetchNutritionInfo(String foodName) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("query", foodName);

        ApiHelper.getInstance().getNutritionixApi().getNutritionInfo(requestBody)
                .enqueue(new Callback<NutritionResponse>() {
                    @Override
                    public void onResponse(Call<NutritionResponse> call, Response<NutritionResponse> response) {
                        if (response.isSuccessful()) {
                            NutritionResponse nutritionResponse = response.body();
                            if (nutritionResponse != null) {
                                NutritionResponse.Food firstFood = nutritionResponse.getFirstFood();
                                if (firstFood != null) {
                                    // Update the UI with the nutritional information
                                    nutritionInfo.setText("Nutrition Info:\n" + firstFood.getFormattedNutritionInfo());
                                } else {
                                    // Handle cases where the response is null
                                    nutritionInfo.setText("Nutrition Info:\nNo data found.");
                                }
                            } else {
                                // Handle cases where the response is null
                                nutritionInfo.setText("Nutrition Info:\nNo data found.");
                            }
                        } else {
                            // Handle API errors
                            nutritionInfo.setText("Nutrition Info:\nError retrieving data.");
                            Log.e("API_ERROR", "Status code: " + response.code() + ", Message: " + response.message());
                            try {
                                Log.e("API_ERROR", "Error response: " + response.errorBody().string());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<NutritionResponse> call, Throwable t) {
                        // Handle network errors
                        nutritionInfo.setText("Nutrition Info:\nNetwork error.");
                        Log.e("NETWORK_ERROR", t.getMessage());
                    }
                });
    }

}

