package com.example.bitelens;

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
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StatisticsActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setTitle("Statistics");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.bitelenslogo);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        setupDrawerContent(navigationView);

        List<Integer> data = new ArrayList<>();
        data.add(2000);
        data.add(2500);
        data.add(2300);
        data.add(1800);
        data.add(2400);
        data.add(2000);
        data.add(2300);

        int totalValues = 30;
        int zeroPercentage = 10;
        int zeroCount = (int) Math.round(totalValues * zeroPercentage / 100.0);

        Random random = new Random();

        for (int i = 0; i < totalValues - zeroCount; i++) {
            int randomValue = random.nextInt(1000) + 1800; // Generate random values between 1800 and 2800
            data.add(randomValue);
        }

        for (int i = 0; i < zeroCount; i++) {
            data.add(0);
        }
        LinearLayout layout = findViewById(R.id.barGraphContainer);
        BarGraphView graph = new BarGraphView(this, data);

        // Set custom width and height
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
        graph.setLayoutParams(params);

        layout.addView(graph);
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
                Intent dashboardIntent = new Intent(StatisticsActivity.this, DashboardActivity.class);
                startActivity(dashboardIntent);
                finish();
                break;
            case R.id.nav_mealplanning:
                Intent mealplanningIntent = new Intent(StatisticsActivity.this, MainActivity.class);
                startActivity(mealplanningIntent);
                finish();
                break;
            case R.id.nav_history:
                Intent nav_history = new Intent(StatisticsActivity.this, HistoryActivity.class);
                startActivity(nav_history);
                finish();
                break;
            case R.id.nav_logout:
                Intent nav_logout = new Intent(StatisticsActivity.this, LoginActivity.class);
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
}