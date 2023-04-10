package com.example.bitelens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import api.ApiHelper;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_IMAGE_REQUEST_CODE = 1;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 2;

    private Button selectImageButton;
    private ImageView foodImage;
    private TextView nutritionInfo;
    private LinearLayout loadingIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Initialize UI components
        selectImageButton = findViewById(R.id.select_image_button);
        foodImage = findViewById(R.id.food_image);
        nutritionInfo = findViewById(R.id.nutrition_info);
        loadingIndicator = findViewById(R.id.loading_layout);

        // Set click listener for the select image button
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAPTURE_IMAGE_REQUEST_CODE);
    }

    // Launches the image picker to allow the user to select an image
    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Choose a photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which].equals("Take Photo")) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestCameraPermission();
                    } else {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST_CODE);
                        }
                    }
                } else if (options[which].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
                } else if (options[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    // Processes the selected image and recognizes food using the ML Kit Image Labeling library
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = null;

            if (requestCode == SELECT_IMAGE_REQUEST_CODE) {
                Uri selectedImage = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    bitmap = (Bitmap) extras.get("data");
                }
            }

            if (bitmap != null) {
                foodImage.setImageBitmap(bitmap);
                recognizeFood(bitmap);
            }
        }
    }


    // Recognizes food items in the input image using ML Kit Image Labeling
    private void recognizeFood(Bitmap bitmap) {
        loadingIndicator.setVisibility(View.VISIBLE);
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

                        List<FoodConfidence> foodConfidences = new ArrayList<>();

                        for (ImageLabel label : labels) {
                            String text = label.getText();
                            System.out.println("FOOD: " + text);
                            float confidence = label.getConfidence();
                            // If the recognized label is related to food, add it to the foodConfidences list
                            if (isFoodRelated(text)) {
                                foodConfidences.add(new FoodConfidence(text, confidence));
                            }
                        }

                        if (foodConfidences.isEmpty()) {
                            loadingIndicator.setVisibility(View.GONE);
                            nutritionInfo.setText("Nutrition Info:\nNo food recognized.");
                            return;
                        }

                        // Fetch nutritional information using the list of FoodConfidence objects
                        fetchNutritionInfo(foodConfidences);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingIndicator.setVisibility(View.GONE);
                        // Handle any errors in the labeling process
                        nutritionInfo.setText("Nutrition Info:\nError recognizing food.");
                        Log.e("RECOGNITION_ERROR", e.getMessage());
                    }
                });
    }



    private boolean isFoodRelated(String label) {

        // A list of common food-related keywords
        String[] foodKeywords = {
                "fruit", "vegetable", "meat", "drink", "beverage", "snack",
                "bread", "cereal", "cheese", "sweets", "dessert", "pizza", "pasta",
                "rice", "salad", "sandwich", "soup", "seafood", "fish", "poultry",
                "cake", "cookie", "pastry", "chocolate", "ice cream", "sauce", "fast food",
                "coffee", "tea", "wine", "beer", "cocktail", "juice", "smoothie","icing",
                "tomato", "cupcake", "banana"
        };

        // Convert the label to lowercase for comparison
        String lowercaseLabel = label.toLowerCase();

        // Check if any food keyword is present in the label
        for (String keyword : foodKeywords) {
            if (lowercaseLabel.contains(keyword)) {
                return true;
            }
        }

        return false; // Return false if none of the keywords were found
    }


    private void fetchNutritionInfo(List<FoodConfidence> foodConfidences) {
        if (foodConfidences == null || foodConfidences.isEmpty()) {
            nutritionInfo.setText("Nutrition Info:\nNo data found.");
            return;
        }

        // Sort the foodConfidences list in descending order of confidence
        Collections.sort(foodConfidences, (o1, o2) -> Float.compare(o2.getConfidence(), o1.getConfidence()));

        FoodConfidence highestConfidenceFood = foodConfidences.get(0);
        String foodName = highestConfidenceFood.getName();
        float confidence = highestConfidenceFood.getConfidence();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("query", foodName);

        ApiHelper.getInstance().getNutritionixApi().getNutritionInfo(requestBody)
                .enqueue(new Callback<NutritionResponse>() {
                    @Override
                    public void onResponse(Call<NutritionResponse> call, Response<NutritionResponse> response) {
                        if (response.isSuccessful()) {
                            NutritionResponse nutritionResponse = response.body();
                            if (nutritionResponse != null) {
                                NutritionResponse.Food currentFood = nutritionResponse.getFood(0);
                                if (currentFood != null) {
                                    // Update the UI with the nutritional information and confidence value
                                    loadingIndicator.setVisibility(View.GONE);
                                    nutritionInfo.setText("Nutrition Info:\n" + currentFood.getFormattedNutritionInfo()
                                            + "\n\nConfidence: " + String.format("%.2f", confidence * 100) + "%");
                                } else {
                                    loadingIndicator.setVisibility(View.GONE);
                                    nutritionInfo.setText("Nutrition Info:\nNo data found.");
                                }
                            } else {
                                loadingIndicator.setVisibility(View.GONE);
                                nutritionInfo.setText("Nutrition Info:\nNo data found.");
                            }
                        } else {
                            loadingIndicator.setVisibility(View.GONE);
                            // Handle API errors
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
                        loadingIndicator.setVisibility(View.GONE);
                        nutritionInfo.setText("Nutrition Info:\nNetwork error.");
                        Log.e("NETWORK_ERROR", t.getMessage());
                    }
                });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, open the camera
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST_CODE);
                }
            } else {
                // Permission was denied, show a message
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

