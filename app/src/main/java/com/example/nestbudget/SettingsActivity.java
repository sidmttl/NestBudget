package com.example.nestbudget;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);  // This references the XML layout we'll define next

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // Show back arrow
            getSupportActionBar().setDisplayShowHomeEnabled(true);  // Enable back navigation
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Closes this activity and returns to the previous screen
        return true;
    }
}
