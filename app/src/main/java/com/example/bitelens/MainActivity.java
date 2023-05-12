package com.example.bitelens;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
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
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 3;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Button selectImageButton;
    private Button searchButton;
    private Button addMealButton;
    private EditText searchBar;
    private ImageView foodImage;
    private TextView nutritionInfo;
    private LinearLayout loadingIndicator;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.bitelenslogo);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);
        setupDrawerContent(navigationView);

        // Initialize UI components
        selectImageButton = findViewById(R.id.select_image_button);
        searchButton = findViewById(R.id.search_button);
        searchBar = findViewById(R.id.search_field);
        foodImage = findViewById(R.id.food_image);
        nutritionInfo = findViewById(R.id.nutinfo);
        loadingIndicator = findViewById(R.id.loading_layout);

        // Set click listener for the select image button
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(searchBar.getText().toString());
            }
        });

        db = FirebaseFirestore.getInstance();

        Button addMealButton = findViewById(R.id.add_meal_button);
        addMealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is assuming you have the food name and calories stored in variables
                String foodName = "food";  // replace with actual food name
                int calories = 100;  // replace with actual calories
                Map<String, Object> meal = new HashMap<>();
                meal.put("foodName", foodName);
                meal.put("calories", calories);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_add_meal, null);
                builder.setView(dialogView);

                TextView foodNameTextView = dialogView.findViewById(R.id.food_name_textview);
                TextView caloriesTextView = dialogView.findViewById(R.id.calories_textview);
                foodNameTextView.setText("Food: " + foodName);
                caloriesTextView.setText("Calories: " + calories);

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User confirmed the dialog
                                // Replace uid with the actual user id
                                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                db.collection("users").document(uid).collection("meals")
                                        .add(meal)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(MainActivity.this, "Meal data added to Firestore.", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, "Failed to add meal data to Firestore.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                // Nothing happens
                            }
                        });
                // Create the AlertDialog object and return it
                builder.create().show();
            }
        });




    }
    private void setupDrawerContent(NavigationView navigationView) {
            navigationView.setNavigationItemSelectedListener(menuItem -> {
                selectDrawerItem(menuItem);
                return true;
            });
        }
    private void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_dashboard:
                // Handle the Dashboard menu item click
                Intent dashboardIntent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(dashboardIntent);
                finish();
                break;
            case R.id.nav_mealplanning:
                Intent mealplanningIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(mealplanningIntent);
                finish();
                break;
            case R.id.nav_history:
                Intent nav_history = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(nav_history);
                finish();
                break;
            case R.id.nav_statistics:
                Intent nav_statistics = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(nav_statistics);
                finish();
                break;
            case R.id.nav_logout:
                //
                break;
            }
            drawerLayout.closeDrawers();
        }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
    }
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SELECT_IMAGE_REQUEST_CODE);
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
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestStoragePermission();
                    }else {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
                    }
                } else if (options[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void search(String food){
        List<FoodConfidence> foodConfidences = new ArrayList<>();
        if (isFoodRelated(food)) {
            foodConfidences.add(new FoodConfidence(food, 1));
            fetchNutritionInfo(foodConfidences);
        }else if(food.equals("")){
            nutritionInfo.setText("Please input a food in the search bar.");
        }
        else{
            nutritionInfo.setText("Error recognizing food.");
        }
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
                            nutritionInfo.setText("No food recognized.");
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
                        nutritionInfo.setText("Error recognizing food.");
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
            nutritionInfo.setText("No data found.");
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
                                    nutritionInfo.setText(currentFood.getFormattedNutritionInfo()
                                            + "\n\nConfidence: " + String.format("%.2f", confidence * 100) + "%");
                                } else {
                                    loadingIndicator.setVisibility(View.GONE);
                                    nutritionInfo.setText("No data found.");
                                }
                            } else {
                                loadingIndicator.setVisibility(View.GONE);
                                nutritionInfo.setText("No data found.");
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
                        nutritionInfo.setText("Network error.");
                        Log.e("NETWORK_ERROR", t.getMessage());
                    }
                });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Permissions were granted, open the camera
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST_CODE);
                }
            } else {
                // Some permissions were denied, show a message
                Toast.makeText(this, "Camera and storage permissions are required.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

