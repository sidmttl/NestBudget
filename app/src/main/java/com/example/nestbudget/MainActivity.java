package com.example.nestbudget;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.util.TypedValue;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private PieChart pieChart;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hide the default title that appears before the menu icon
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        ImageView menuIcon = findViewById(R.id.menu_icon);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Handle Navigation Drawer Clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_add_family) {
                Intent intent = new Intent(MainActivity.this, JoinFamilyActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_settings) {
                // Open Settings
                return true;
            } else if (itemId == R.id.nav_logout) {
                // Open Logout
                LoginManager loginManager = new LoginManager(MainActivity.this);
                loginManager.logout(MainActivity.this);
                return true;
            }
            drawerLayout.closeDrawers();
            return true;
        });

        menuIcon.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        // Handle Notification and Profile Clicks
        ImageView notificationIcon = findViewById(R.id.notification_icon);
        ImageView profileIcon = findViewById(R.id.profile_icon);

        notificationIcon.setOnClickListener(v -> {
            // Show notifications
        });

        profileIcon.setOnClickListener(v -> {
            // Open Profile Details
            Intent intent = new Intent(MainActivity.this, SetProfileActivity.class);
            startActivity(intent);
        });

        // Initialize Pie Chart
        pieChart = findViewById(R.id.pieChart);
        setupPieChart();

        // Bottom Navigation Setup
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // Ensure dashboard is selected when this activity is shown
        bottomNavigationView.setSelectedItemId(R.id.menu_dashboard);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_dashboard) {
                // Already on Dashboard, no action needed
                return true;
            } else if (itemId == R.id.menu_transactions) {
                // For now, just show a toast that this feature is coming soon
                return true;
            } else if (itemId == R.id.menu_insights) {
                // For now, just show a toast that this feature is coming soon
                return true;
            } else if (itemId == R.id.menu_journal) {
                Intent intent = new Intent(MainActivity.this, ToBuyListActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(40, "Food"));
        entries.add(new PieEntry(20, "Housing"));
        entries.add(new PieEntry(15, "Investing"));
        entries.add(new PieEntry(15, "Healthcare"));
        entries.add(new PieEntry(10, "Misc"));

        PieDataSet dataSet = new PieDataSet(entries, "Expenses");
        dataSet.setColors(new int[]{R.color.red, R.color.blue, R.color.green, R.color.yellow, R.color.purple}, this);

        PieData pieData = new PieData(dataSet);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;
        pieChart.getLegend().setTextColor(colorPrimary);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Make sure Dashboard is selected when returning to this activity
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.menu_dashboard);
        }
    }
}