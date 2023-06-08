package com.example.bitelens;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MealDetailActivity extends AppCompatActivity {

    private TextView mealName, mealCalories, mealFat, mealCholesterol, mealSodium, mealCarbs, mealFiber, mealSugar, mealProtein;
    private ImageView mealImage;
    private Button backButton, deleteButton;
    private Meal meal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        meal = (Meal) getIntent().getSerializableExtra("meal");

        mealImage = findViewById(R.id.meal_image);
        mealName = findViewById(R.id.meal_name);
        mealCalories = findViewById(R.id.meal_calories);
        mealFat = findViewById(R.id.meal_fat);
        mealCholesterol = findViewById(R.id.meal_cholesterol);
        mealSodium = findViewById(R.id.meal_sodium);
        mealCarbs = findViewById(R.id.meal_carbs);
        mealFiber = findViewById(R.id.meal_fiber);
        mealSugar = findViewById(R.id.meal_sugar);
        mealProtein = findViewById(R.id.meal_protein);
        backButton = findViewById(R.id.back_button);
        deleteButton = findViewById(R.id.delete_button);

        // Set values
        mealName.setText("Meal Name: " + meal.getName());
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(meal.getImageurl());
        imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Convert the bytes to a Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                // Set the Bitmap to the ImageView
                mealImage.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        mealCalories.setText("Calories: " + meal.getCalories());
        mealFat.setText("Total Fat: " + meal.getFat() + "g");
        mealCholesterol.setText("Cholesterol: " + meal.getCholesterol() + "mg");
        mealSodium.setText("Sodium: " + meal.getSodium() + "mg");
        mealCarbs.setText("Total Carbohydrates: " + meal.getCarbohydrate() + "g");
        mealFiber.setText("Dietary Fiber: " + meal.getFiber() + "g");
        mealSugar.setText("Sugars: " + meal.getSugars() + "g");
        mealProtein.setText("Protein: " + meal.getProtein() + "g");


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MealDetailActivity.this)
                        .setTitle("Delete Meal")
                        .setMessage("Are you sure you want to delete this meal?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMealFromFirebase();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    private void deleteMealFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("meals").document(meal.getId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MealDetailActivity.this, "Meal Deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(MealDetailActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
