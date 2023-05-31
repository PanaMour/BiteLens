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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class StatisticsActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private TextView month;

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
        month = findViewById(R.id.monthView);
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        setupDrawerContent(navigationView);

        LinearLayout layout = findViewById(R.id.barGraphContainer);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        String monthName = new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());
        String monthText = "Month: " + monthName;
        month.setText(monthText);
        int currentYear = calendar.get(Calendar.YEAR);
        Calendar start = Calendar.getInstance();
        start.set(currentYear, currentMonth, 1);
        Calendar end = Calendar.getInstance();
        end.set(currentYear, currentMonth, end.getActualMaximum(Calendar.DAY_OF_MONTH));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("users").document(user.getUid()).collection("meals")
                .whereGreaterThanOrEqualTo("StatDate", start.getTime())
                .whereLessThanOrEqualTo("StatDate", end.getTime());
        int[] caloriesPerDay = new int[end.getActualMaximum(Calendar.DAY_OF_MONTH)];
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Date mealDate = document.getDate("StatDate");
                    int calories = document.getLong("Calories").intValue();
                    Calendar mealCalendar = Calendar.getInstance();
                    mealCalendar.setTime(mealDate);
                    int dayOfMonth = mealCalendar.get(Calendar.DAY_OF_MONTH);
                    caloriesPerDay[dayOfMonth - 1] += calories;
                }
                // At this point, you have the calories per day for the current month.
                // You can convert the array to a list and pass it to your BarGraphView.
                List<Integer> data = new ArrayList<>();
                for (int calories : caloriesPerDay) {
                    data.add(calories);
                    System.out.println("Calories " + calories);
                }
                data.add(0);
                data.add(0);
                data.add(0);
                BarGraphView barGraphView = new BarGraphView(this, data);
                barGraphView.setTag("BarGraphView");
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
                barGraphView.setLayoutParams(params);
                layout.addView(barGraphView);
            } else {
                Log.d("TAG", "Error getting documents: ", task.getException());
            }
        });

        // Get the Spinner from your layout
        Spinner monthSpinner = findViewById(R.id.month_spinner);

        // Create an ArrayAdapter using the string array (months) and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.months, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        monthSpinner.setAdapter(adapter);

        // Set the spinner's default selection to the current month
        monthSpinner.setSelection(currentMonth);


        // Set a listener to be invoked when an item in this AdapterView has been selected
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedMonth = position; // The position corresponds to the selected month (0 = January, 1 = February, ...)

                // Now you can use the selected month to update your query
                Calendar start = Calendar.getInstance();
                start.set(Calendar.YEAR, currentYear);
                start.set(Calendar.MONTH, selectedMonth);
                start.set(Calendar.DAY_OF_MONTH, 1);

                Calendar end = Calendar.getInstance();
                end.set(Calendar.YEAR, currentYear);
                end.set(Calendar.MONTH, selectedMonth);
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
                String monthName = new DateFormatSymbols().getMonths()[selectedMonth];
                month.setText("Month: " + monthName);

                Query query = db.collection("users").document(user.getUid()).collection("meals")
                        .whereGreaterThanOrEqualTo("StatDate", start.getTime())
                        .whereLessThanOrEqualTo("StatDate", end.getTime());

                int[] caloriesPerDay = new int[end.getActualMaximum(Calendar.DAY_OF_MONTH)];
                query.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Date mealDate = document.getDate("StatDate");
                            int calories = document.getLong("Calories").intValue();
                            Calendar mealCalendar = Calendar.getInstance();
                            mealCalendar.setTime(mealDate);
                            int dayOfMonth = mealCalendar.get(Calendar.DAY_OF_MONTH);
                            caloriesPerDay[dayOfMonth - 1] += calories;
                        }
                        // At this point, you have the calories per day for the selected month.
                        // You can convert the array to a list and pass it to your BarGraphView.
                        List<Integer> data = new ArrayList<>();
                        for (int calories : caloriesPerDay) {
                            data.add(calories);
                        }
                        data.add(0);
                        data.add(0);
                        data.add(0);
                        View oldGraph = layout.findViewWithTag("BarGraphView");  // Find the BarGraphView using the tag
                        if (oldGraph != null) layout.removeView(oldGraph);
                        BarGraphView barGraphView = new BarGraphView(StatisticsActivity.this, data);
                        barGraphView.setTag("BarGraphView");
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
                        barGraphView.setLayoutParams(params);
                        layout.addView(barGraphView);
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });


        /*List<Integer> data = new ArrayList<>();
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

        BarGraphView graph = new BarGraphView(this, data);*/

        // Set custom width and height



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