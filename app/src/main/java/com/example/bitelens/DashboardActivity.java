package com.example.bitelens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FirebaseFirestore firebaseFirestore;
    private TextView usernameTextView;
    private TextView caloriesGoal;
    private TextView caloriesConsumed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        firebaseFirestore = FirebaseFirestore.getInstance();
        usernameTextView = findViewById(R.id.welcomeText);
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
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }


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

        CustomCircularProgressBar progressBar = findViewById(R.id.circular_progress_bar);
        TextView progressText = findViewById(R.id.progress_text);
        //LinearLayout linearLayout = findViewById(R.id.linearCircle);
        // Update progress and text values
        String consumedStr = caloriesConsumed.getText().toString();
        String goalStr = caloriesGoal.getText().toString();

        int consumed = Integer.parseInt(consumedStr);
        int goal = Integer.parseInt(goalStr);

        float progress = (float) 1500 / goal * 100;
        progressBar.setProgress(progress);
        progressText.setText(consumedStr + " / " + goalStr);


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
                Intent mealplanningIntent = new Intent(DashboardActivity.this, LoginActivity.class);
                startActivity(mealplanningIntent);
                finish();
                break;
            case R.id.nav_history:
                Intent nav_history = new Intent(DashboardActivity.this, LoginActivity.class);
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