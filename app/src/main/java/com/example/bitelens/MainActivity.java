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
import java.util.List;

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
        selectImageButton = findViewById(R.id.select_image_button);
        foodImage = findViewById(R.id.food_image);
        nutritionInfo = findViewById(R.id.nutrition_info);

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
    }

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

    private void recognizeFood(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build();
        ImageLabeler labeler = ImageLabeling.getClient(options);

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
        // Implement a method to determine if the recognized label is related to food
        return true; // For simplicity, assume all labels are food-related
    }

    private void fetchNutritionInfo(String foodName) {
        ApiHelper.getInstance().getNutritionixApi().getNutritionInfo(foodName)
                .enqueue(new Callback<NutritionResponse>() {
                    @Override
                    public void onResponse(Call<NutritionResponse> call, Response<NutritionResponse> response) {
                        if (response.isSuccessful()) {
                            NutritionResponse nutritionResponse = response.body();
                            if (nutritionResponse != null) {
                                // Update the UI with the nutritional information
                                nutritionInfo.setText("Nutrition Info:\n" + nutritionResponse.toString());
                            } else {
                                // Handle cases where the response is null
                                nutritionInfo.setText("Nutrition Info:\nNo data found.");
                            }
                        } else {
                            // Handle API errors
                            nutritionInfo.setText("Nutrition Info:\nError retrieving data.");
                            Log.e("API_ERROR", "Status code: " + response.code() + ", Message: " + response.message());
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

