package com.example.nestbudget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private PieChart pieChart;
    private BottomNavigationView bottomNavigationView;

    private DatabaseReference database;
    private String userId;  // User ID from SharedPreferences
    private TextView budgetTextView, incomeTextView;  // TextViews to display budget and income

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize drawer layout
        drawerLayout = findViewById(R.id.drawer_layout);
        ImageView menuIcon = findViewById(R.id.menu_icon);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Handle navigation drawer clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_add_family) {
                startActivity(new Intent(MainActivity.this, JoinFamilyActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            } else if (itemId == R.id.nav_logout) {
                new LoginManager(MainActivity.this).logout(MainActivity.this);
                return true;
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // Open the drawer
        menuIcon.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        // Handle profile clicks
        ImageView profileIcon = findViewById(R.id.profile_icon);
        profileIcon.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SetProfileActivity.class));
        });

        // Initialize PieChart
        pieChart = findViewById(R.id.pieChart);
        setupPieChart();

        // Bottom Navigation setup
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_transactions) {
                startActivity(new Intent(MainActivity.this, TransactionActivity.class));
                return true;
            } else if (itemId == R.id.menu_journal) {
                startActivity(new Intent(MainActivity.this, ToBuyListActivity.class));
                return true;
            }
            return false;
        });

        // Get user data (budget and income) using Firebase
        database = FirebaseDatabase.getInstance().getReference();
        userId = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("username", null);

        if (userId != null) {
            // Fetch user's income and budget
            database.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String budget = snapshot.child("monthlyBudget").getValue(String.class);
                        String income = snapshot.child("monthlyIncome").getValue(String.class);

                        // Update UI with the budget and income
                        budgetTextView = findViewById(R.id.budgetTextView);
                        incomeTextView = findViewById(R.id.incomeTextView);

                        if (budget != null) budgetTextView.setText("Budget: " + budget);
                        if (income != null) incomeTextView.setText("Income: " + income);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
