package com.example.nestbudget;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Handle Navigation Drawer Clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_settings:
                    // Open Settings
                    break;
                case R.id.nav_logout:
                    // Handle Logout
                    break;
            }
            drawerLayout.closeDrawers();
            return true;
        });

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
            switch (item.getItemId()) {
                case R.id.menu_dashboard:
                    // Open Dashboard
                    return true;
                case R.id.menu_transactions:
                    // Open Transactions
                    return true;
                case R.id.menu_insights:
                    // Open Insights
                    return true;
                case R.id.menu_journal:
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
        pieChart.setData(pieData);
        pieChart.invalidate();
    }
}