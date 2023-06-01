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
    private TextView place;
    private LinearLayout layout;
    private LinearLayout layoutPlace;
    private Calendar calendar;
    private FirebaseUser user;
    private int currentMonth;
    private int currentYear;
    private FirebaseFirestore db;
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
        place = findViewById(R.id.placeView);
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        setupDrawerContent(navigationView);

        layout = findViewById(R.id.monthGraphContainer);
        layoutPlace = findViewById(R.id.placeGraphContainer);
        user = FirebaseAuth.getInstance().getCurrentUser();
        calendar = Calendar.getInstance();
        currentMonth = calendar.get(Calendar.MONTH);
        String monthName = new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());
        String monthText = "Month: " + monthName;
        month.setText(monthText);
        currentYear = calendar.get(Calendar.YEAR);
        Calendar start = Calendar.getInstance();
        start.set(currentYear, currentMonth, 1);
        Calendar end = Calendar.getInstance();
        end.set(currentYear, currentMonth, end.getActualMaximum(Calendar.DAY_OF_MONTH));
        db = FirebaseFirestore.getInstance();
        /*Query query = db.collection("users").document(user.getUid()).collection("meals")
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
        });*/

        // Get the Spinner from your layout
        Spinner monthSpinner = findViewById(R.id.month_spinner);
        Spinner placeSpinner = findViewById(R.id.place_spinner);

        // Create an ArrayAdapter using the string array (months) and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.months, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,R.array.places, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        monthSpinner.setAdapter(adapter);
        placeSpinner.setAdapter(adapter2);

        // Set the spinner's default selection to the current month
        monthSpinner.setSelection(currentMonth);
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPlace = placeSpinner.getSelectedItem().toString(); // Get the currently selected place
                updateMonthGraph(position);
                updatePlaceGraph(position,selectedPlace);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        placeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int selectedMonth = monthSpinner.getSelectedItemPosition(); // Get the currently selected month
                String selectedPlace = adapterView.getItemAtPosition(i).toString(); // Get the selected place
                place.setText("Place " + selectedPlace);
                updatePlaceGraph(selectedMonth, selectedPlace);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });




    }

    void updateMonthGraph(int selectedMonth) {
        // The start and end dates of the selected month
        Calendar start = Calendar.getInstance();
        start.set(Calendar.YEAR, currentYear);
        start.set(Calendar.MONTH, selectedMonth);
        start.set(Calendar.DAY_OF_MONTH, 1);

        String monthName = new SimpleDateFormat("MMMM", Locale.getDefault()).format(start.getTime());
        String monthText = "Month: " + monthName;
        month.setText(monthText);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.YEAR, currentYear);
        end.set(Calendar.MONTH, selectedMonth);
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));

        // Query for meals within the date range
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

                // Convert the array to a list and pass it to your BarGraphView.
                List<Integer> data = new ArrayList<>();
                for (int calories : caloriesPerDay) {
                    data.add(calories);
                }

                // Add padding for displaying last few days of the month
                data.add(0);
                data.add(0);
                data.add(0);

                // Update the bar graph for the layout
                View oldGraph = layout.findViewWithTag("BarGraphView");
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

    void updatePlaceGraph(int selectedMonth, String selectedPlace) {
        // The start and end dates of the selected month
        Calendar start = Calendar.getInstance();
        start.set(Calendar.YEAR, currentYear);
        start.set(Calendar.MONTH, selectedMonth);
        start.set(Calendar.DAY_OF_MONTH, 1);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.YEAR, currentYear);
        end.set(Calendar.MONTH, selectedMonth);
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));

        // Query for meals within the date range and place
        Query query = db.collection("users").document(user.getUid()).collection("meals")
                .whereGreaterThanOrEqualTo("StatDate", start.getTime())
                .whereLessThanOrEqualTo("StatDate", end.getTime())
                .whereEqualTo("Place",selectedPlace);

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

                // Convert the array to a list and pass it to your BarGraphView.
                List<Integer> data = new ArrayList<>();
                for (int calories : caloriesPerDay) {
                    data.add(calories);
                }

                // Add padding for displaying last few days of the month
                data.add(0);
                data.add(0);
                data.add(0);

                // Update the bar graph for the layoutPlace
                View oldGraphPlace = layoutPlace.findViewWithTag("BarGraphView");
                if (oldGraphPlace != null) layoutPlace.removeView(oldGraphPlace);
                BarGraphView barGraphViewPlace = new BarGraphView(StatisticsActivity.this, data);
                barGraphViewPlace.setTag("BarGraphView");
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 750);
                barGraphViewPlace.setLayoutParams(params);
                layoutPlace.addView(barGraphViewPlace);

            } else {
                Log.d("TAG", "Error getting documents: ", task.getException());
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