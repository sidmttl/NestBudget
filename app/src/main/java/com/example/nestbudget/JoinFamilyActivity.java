package com.example.nestbudget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.*;
import java.util.UUID;

public class JoinFamilyActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FAMILYCODE = "familyCode";

    private SharedPreferences sharedPreferences;

    private TextView tvInstructions;
    private EditText etGroupCode;
    private Button btnJoin;

    private DatabaseReference database;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_family);

        Toolbar toolbar = findViewById(R.id.family_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get username from intent first (during registration flow)
        username = getIntent().getStringExtra("username");

        // If not in intent, try SharedPreferences (normal usage)
        if (username == null) {
            sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            username = sharedPreferences.getString(KEY_USERNAME, null);
        }

        // Initialize UI elements
        tvInstructions = findViewById(R.id.tv_instructions);
        etGroupCode = findViewById(R.id.et_group_code);
        btnJoin = findViewById(R.id.btn_join);

        // Set instructions
        tvInstructions.setText("Enter the family code shared with you to join an existing family group.");

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().getReference();

        // Set click listener for Join button
        btnJoin.setOnClickListener(v -> {
            String enteredCode = etGroupCode.getText().toString().trim();
            if (!enteredCode.isEmpty()) {
                verifyAndJoinGroup(enteredCode);
            } else {
                Toast.makeText(this, "Please enter a group code.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyAndJoinGroup(String groupCode) {
        // First check if group code exists
        database.child("Groups").child(groupCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Group exists, join it
                    joinGroup(groupCode);
                } else {
                    // Group doesn't exist
                    etGroupCode.setError("Incorrect Family Code");
                    Toast.makeText(JoinFamilyActivity.this,
                            "This family code doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JoinFamilyActivity.this,
                        "Failed to verify code: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinGroup(String groupCode) {
        // Skip if username is null
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Error: Username not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user to group members list
        database.child("Groups").child(groupCode).child("members").child(username).setValue(true);

        // Save joined group in user's data
        database.child("Users").child(username).child("familyCode").setValue(groupCode);

        // Also save to SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_FAMILYCODE, groupCode).apply();

        Toast.makeText(this, "Joined group: " + groupCode, Toast.LENGTH_SHORT).show();

        // Navigate to MainActivity or FamilySetupActivity
        Intent intent = new Intent(JoinFamilyActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Closes this activity and returns to previous
        return true;
    }
}