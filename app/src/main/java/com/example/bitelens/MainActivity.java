package com.example.bitelens;

import static android.content.ContentValues.TAG;

import static java.lang.Double.parseDouble;

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
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import api.ApiHelper;

public class MainActivity extends AppCompatActivity implements LocationListener{

    private static final int SELECT_IMAGE_REQUEST_CODE = 1;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 2;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 3;
    LocationManager locationManager;
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
    private Uri foodImageUri;
    String location = "";
    private boolean addedMeal = false;
    private boolean selectedLocation = false;
    AutoCompleteTextView editTextFilledExposedDropdown;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Meal Planning");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.bitelenslogo);
        }
        String[] locations = new String[] {"Home", "Work", "Other"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.dropdown_menu_popup_item,
                        locations);
        editTextFilledExposedDropdown = findViewById(R.id.location_autocomplete);
        editTextFilledExposedDropdown.setAdapter(adapter);

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
        editTextFilledExposedDropdown.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No need to do anything here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty() && !editTextFilledExposedDropdown.getText().toString().isEmpty()) {
                    selectedLocation = true;
                } else {
                    selectedLocation = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No need to do anything here
            }
        });
        db = FirebaseFirestore.getInstance();

        Button addMealButton = findViewById(R.id.add_meal_button);
        addMealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) MainActivity.this);
                    Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc != null) {
                        location = loc.getLatitude() + "," + loc.getLongitude();
                    }
                    if (addedMeal) {
                        if(selectedLocation){
                        String foodName = "";  // replace with actual food name
                        String calories = "";  // replace with actual calories
                        String[] nutritionalInfoLines = nutritionInfo.getText().toString().split("\n");

                        // Create a HashMap to store the meal data
                        Map<String, Object> meal = new HashMap<>();

                        // Parse each line and store the data in the HashMap
                        for (String line : nutritionalInfoLines) {
                            String[] parts = line.split(": ");
                            if (parts.length == 2) {
                                String key = parts[0].trim();
                                String value = parts[1].trim();
                                if (key.equals("Calories") || key.contains("fat") || key.contains("Cholesterol") || key.contains("Sodium")
                                        || key.contains("carbohydrate") || key.contains("fiber") || key.contains("Sugars") || key.contains("Protein")) {
                                    // Remove the units from the value (g, mg, etc.)
                                    value = value.replaceAll("[^\\d.]", "").trim();
                                    if (key.equals("Calories")) {
                                        calories = value;
                                    }
                                    meal.put(key, Double.parseDouble(value));
                                } else {
                                    if (key.contains("name")) {
                                        foodName = value;
                                    }
                                    meal.put(key, value);
                                }
                            }
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.dialog_add_meal, null);
                        builder.setView(dialogView);

                        TextView foodNameTextView = dialogView.findViewById(R.id.food_name_textview);
                        TextView caloriesTextView = dialogView.findViewById(R.id.calories_textview);
                        TextView dateTextView = dialogView.findViewById(R.id.date);
                        TextView locationTextView = dialogView.findViewById(R.id.location_textview);
                        foodNameTextView.setText("Food: " + foodName);
                        caloriesTextView.setText("Calories: " + calories);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

                        dateTextView.setText("Date: " + dateFormat.format(Calendar.getInstance().getTime()));
                        getLocation();
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        String city;
                        try {
                            city = geocoder.getFromLocation(parseDouble(location.substring(0,location.indexOf(","))),parseDouble(location.substring(location.indexOf(",")+1,location.length())),1).get(0).getLocality();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        locationTextView.setText("Location: " + city + " (" + editTextFilledExposedDropdown.getText() + ")");

                        // Add the current date as a Timestamp
                        meal.put("Date", com.google.firebase.Timestamp.now());
                        meal.put("StatDate", com.google.firebase.Timestamp.now());
                        meal.put("Location", city);
                        meal.put("Place", editTextFilledExposedDropdown.getText().toString());

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User confirmed the dialog
                                        // Replace uid with the actual user id
                                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                        // Create a reference to the storage bucket
                                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                                        // Create a timestamp for the file name
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault());
                                        String fileName = sdf.format(new Date());

                                        // Create a reference to the file location
                                        StorageReference fileRef = storageRef.child(fileName);

                                        // Upload the file
                                        fileRef.putFile(foodImageUri)
                                                .addOnSuccessListener(taskSnapshot -> {
                                                    // Get the download URL of the uploaded file
                                                    fileRef.getDownloadUrl()
                                                            .addOnSuccessListener(uri -> {
                                                                // Add the URL to the meal data
                                                                meal.put("ImageURL", fileName);

                                                                // Add the meal data to Firestore
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
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                // Handle any errors
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Handle any errors
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
                    } else {
                        Toast.makeText(MainActivity.this, "Please select Current Location.", Toast.LENGTH_SHORT).show();
                    }
                    }else {
                            Toast.makeText(MainActivity.this, "No meal information in activity.", Toast.LENGTH_SHORT).show();
                        }
                }else{
                    askForLocationPermission();
                }
            }

        });

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bmp = null;
            try {
                InputStream in = new URL(urldisplay).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bmp;
        }

        @SuppressLint("WrongThread")
        protected void onPostExecute(Bitmap result) {
            // Save the bitmap to the cache directory
            File file = new File(getCacheDir(), "image.png");
            try (FileOutputStream out = new FileOutputStream(file)) {
                result.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Create a Uri from the file path
            foodImageUri = Uri.fromFile(file);
        }
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
            case R.id.nav_history:
                Intent nav_history = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(nav_history);
                finish();
                break;
            case R.id.nav_statistics:
                Intent nav_statistics = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(nav_statistics);
                finish();
                break;
            case R.id.nav_logout:
                Intent nav_logout = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(nav_logout);
                FirebaseAuth.getInstance().signOut();
                finish();
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
            searchNutritionInfo(foodConfidences);
        }else if(food.equals("")){
            nutritionInfo.setText("Please input a food in the search bar.");
            addedMeal = false;
        }
        else{
            nutritionInfo.setText("Error recognizing food.");
            addedMeal = false;
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
                foodImageUri = selectedImage;
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
                            addedMeal = false;
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
                        addedMeal = false;
                        Log.e("RECOGNITION_ERROR", e.getMessage());
                    }
                });
    }
    private boolean isFoodRelated(String label) {

        // A list of common food-related keywords
        String[] foodKeywords = {
                "fruit", "vegetable", "meat",
                "bread", "cereal", "cheese", "pizza", "pasta",
                "rice", "salad", "sandwich", "soup", "seafood", "fish",
                "cake", "cookie", "pastry", "chocolate", "ice cream", "sauce",
                "coffee", "tea", "wine", "beer", "cocktail", "juice", "smoothie","icing",
                "tomato", "cupcake", "banana", "burger"
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
            addedMeal = false;
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
                                    addedMeal = true;
                                } else {
                                    loadingIndicator.setVisibility(View.GONE);
                                    nutritionInfo.setText("No data found.");
                                    addedMeal = false;
                                }
                            } else {
                                loadingIndicator.setVisibility(View.GONE);
                                nutritionInfo.setText("No data found.");
                                addedMeal = false;
                            }
                        } else {
                            loadingIndicator.setVisibility(View.GONE);
                            addedMeal = false;
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
                        addedMeal = false;
                        Log.e("NETWORK_ERROR", t.getMessage());
                    }
                });
    }
    //If user has given permission to Location then it gets the user's current location
    public void getLocation(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                // Build the alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location Services Not Enabled!");
                builder.setMessage("Please enable Location Services.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Show location settings when the user acknowledges the alert dialog
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
                Dialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
            }
        }else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location loc) {
                    // Update the location variable every time the device's location changes
                    location = loc.getLatitude() + "," + loc.getLongitude();
                }
            });
        }
    }
    //Asks user for Location Permission
    private void askForLocationPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
        }
    }
    private void searchNutritionInfo(List<FoodConfidence> foodConfidences) {
        if (foodConfidences == null || foodConfidences.isEmpty()) {
            nutritionInfo.setText("No data found.");
            addedMeal = false;
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
                                    nutritionInfo.setText(currentFood.getFormattedNutritionInfo());
                                    addedMeal = true;
                                    Glide.with(MainActivity.this)
                                            .load(currentFood.getPhoto().getThumb())
                                            .into(foodImage);
                                    new DownloadImageTask().execute(currentFood.getPhoto().getThumb());

                                } else {
                                    loadingIndicator.setVisibility(View.GONE);
                                    nutritionInfo.setText("No data found.");
                                    addedMeal = false;
                                }
                            } else {
                                loadingIndicator.setVisibility(View.GONE);
                                nutritionInfo.setText("No data found.");
                                addedMeal = false;
                            }
                        } else {
                            loadingIndicator.setVisibility(View.GONE);
                            addedMeal = false;
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
                        addedMeal = false;
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

