package com.example.nestbudget;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setContentView(R.layout.activity_main);



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
                // Open Settings
                return true;
            } else if (itemId == R.id.nav_settings) {
                // Open Settings
                return true;
            } else if (itemId == R.id.nav_logout) {
                // Open Logout
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
        });

        // Initialize Pie Chart
        pieChart = findViewById(R.id.pieChart);
        setupPieChart();

        // Bottom Navigation Setup
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_dashboard) {
                // Open Dashboard
                return true;
            } else if (itemId == R.id.menu_transactions) {
                // Open Transactions
                return true;
            } else if (itemId == R.id.menu_insights) {
                // Open Insights
                return true;
            } else if (itemId == R.id.menu_journal) {
                // Open Help
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
}