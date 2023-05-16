package com.example.bitelens;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FirebaseFirestore firebaseFirestore;
    private TextView usernameTextView;
    private TextView progressPercentage;
    private TextView caloriesGoal;
    private TextView caloriesConsumed;
    private float consumed = 0;
    private int goal;
    CustomCircularProgressBar progressBar;
    TextView progressText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        firebaseFirestore = FirebaseFirestore.getInstance();
        usernameTextView = findViewById(R.id.welcomeText);
        progressPercentage = findViewById(R.id.progress_percentage);
        caloriesGoal = findViewById(R.id.calories_goal);
        caloriesConsumed = findViewById(R.id.total_calories);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            firebaseFirestore.collection("users").document(uid).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String username = documentSnapshot.getString("name");
                            if (username != null) {
                                usernameTextView.setText("Welcome, " + username);
                            }

                            // Fetch calories_consumed and calories_goal from the database
                            //Integer consumedFromDatabase = documentSnapshot.getLong("calories_consumed").intValue();
                            Integer goalFromDatabase = documentSnapshot.getLong("calories_goal").intValue();

                            // Update the UI with fetched values
                            //caloriesConsumed.setText(String.valueOf(consumedFromDatabase));
                            caloriesGoal.setText(String.valueOf(goalFromDatabase));

                            // Update progress and text values
                            //consumed = consumedFromDatabase;
                            goal = goalFromDatabase;
                            calculateTotalCaloriesForToday();
                            float progress = (float) consumed / goal * 100;
                            String progressString = String.format("%.0f%%", progress);
                            progressPercentage.setText(progressString);
                            progressBar.setProgress(progress);
                            progressText.setText((int)consumed + " / " + goal);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
        Button setNewGoalButton = findViewById(R.id.progress_button);
        setNewGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetNewGoalDialog();
            }
        });


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

        progressBar = findViewById(R.id.circular_progress_bar);
        progressText = findViewById(R.id.progress_text);
        // Update progress and text values
        String consumedStr = caloriesConsumed.getText().toString();
        String goalStr = caloriesGoal.getText().toString();

        consumed = Integer.parseInt(consumedStr);
        goal = Integer.parseInt(goalStr);

        calculateTotalCaloriesForToday();



    }
    public com.google.firebase.Timestamp getCurrentDayTimestamp() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        // Set the time to 00:00:00
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date date = calendar.getTime();
        // Create a timestamp from the Date object
        return new com.google.firebase.Timestamp(date);
    }
    public com.google.firebase.Timestamp getStartOfDayTimestamp(com.google.firebase.Timestamp timestamp) {
        // Convert the timestamp to Date
        Date date = timestamp.toDate();
        // Create a Calendar instance and set the time to the given date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // Set the time to 00:00:00
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // Convert it back to a Date
        date = calendar.getTime();
        // Create a new Timestamp from the Date object
        return new com.google.firebase.Timestamp(date);
    }
    public void calculateTotalCaloriesForToday() {
        com.google.firebase.Timestamp currentDayTimestamp = getCurrentDayTimestamp();
        if (currentDayTimestamp != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            firebaseFirestore.collection("users").document(uid).collection("meals")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                int totalCalories = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Get the Date of the meal as a Timestamp
                                    com.google.firebase.Timestamp mealDateTimestamp = document.getTimestamp("Date");
                                    if (mealDateTimestamp != null) {
                                        // Convert it to the start of the day
                                        com.google.firebase.Timestamp mealDateStartOfDayTimestamp = getStartOfDayTimestamp(mealDateTimestamp);
                                        // Compare only the date parts
                                        if (mealDateStartOfDayTimestamp.equals(currentDayTimestamp)) {
                                            // Assuming the calories are stored as a double
                                            Double mealCalories = document.getDouble("Calories");
                                            if (mealCalories != null) {
                                                totalCalories += mealCalories;
                                            }
                                        }
                                    }
                                }
                                // Update the UI with the total calories
                                consumed = totalCalories;
                                float progress = (float) consumed / goal * 100;
                                String progressString = String.format("%.0f%%", progress);
                                progressPercentage.setText(progressString);
                                progressBar.setProgress(progress);
                                progressText.setText(((int) consumed) + " / " + goal);
                                caloriesConsumed.setText(String.valueOf(totalCalories));
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }



    private void showSetNewGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_new_goal, null);
        builder.setView(dialogView);

        final EditText newGoalEditText = dialogView.findViewById(R.id.new_goal);
        builder.setTitle("Set New Calories Goal")
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newGoal = newGoalEditText.getText().toString().trim();
                        if (!TextUtils.isEmpty(newGoal)) {
                            int newGoalInt = Integer.parseInt(newGoal);
                            if (newGoalInt >= 500 && newGoalInt <= 5000) {
                                updateCaloriesGoal(newGoalInt);
                            } else {
                                Toast.makeText(DashboardActivity.this, "Calories goal must be between 500 and 5,000", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }

    private void updateCaloriesGoal(int newGoal) {
        TextView caloriesGoalTextView = findViewById(R.id.calories_goal);
        caloriesGoalTextView.setText(String.valueOf(newGoal));
        goal = newGoal;
        float progress = (float) consumed / goal * 100;
        String progressString = String.format("%.0f%%", progress);
        progressPercentage.setText(progressString);
        progressBar.setProgress(progress);
        progressText.setText((int)consumed + " / " + goal);

        // Assuming you have the user's UID, for example, from FirebaseAuth
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Update the calories goal in your database here
        // Call the appropriate method depending on which database you are using
        updateCaloriesGoalInDatabase(uid, newGoal);
    }

    private void updateCaloriesGoalInDatabase(String uid, int newGoal) {
        DocumentReference userReference = FirebaseFirestore.getInstance().collection("users").document(uid);
        userReference.update("calories_goal", newGoal);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            selectDrawerItem(menuItem);
            return true;
        });
    }
    private void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            /*case R.id.nav_dashboard:
                // Handle the Dashboard menu item click
                Intent dashboardIntent = new Intent(DashboardActivity.this, MainActivity.class);
                startActivity(dashboardIntent);
                finish();
                break;*/
            case R.id.nav_mealplanning:
                Intent mealplanningIntent = new Intent(DashboardActivity.this, MainActivity.class);
                startActivity(mealplanningIntent);
                finish();
                break;
            case R.id.nav_history:
                Intent nav_history = new Intent(DashboardActivity.this, MainActivity.class);
                startActivity(nav_history);
                finish();
                break;
            case R.id.nav_statistics:
                Intent nav_statistics = new Intent(DashboardActivity.this, LoginActivity.class);
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
}