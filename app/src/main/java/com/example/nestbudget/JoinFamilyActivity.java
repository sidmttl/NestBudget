package com.example.nestbudget;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.*;
import java.util.UUID;

public class JoinFamilyActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";

    private SharedPreferences sharedPreferences;

    private TextView tvOwnCode;
    private EditText etGroupCode;
    private Button btnJoin;

    private DatabaseReference database;
    private String userId;
    private String ownCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_join_family);

        Toolbar toolbar = findViewById(R.id.family_toolbar);
        setSupportActionBar(toolbar);

        tvOwnCode = findViewById(R.id.tv_own_code);
        etGroupCode = findViewById(R.id.et_group_code);
        btnJoin = findViewById(R.id.btn_join);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        database = FirebaseDatabase.getInstance().getReference();

        userId = sharedPreferences.getString(KEY_USERNAME, null);

        // Load or generate user's own code
        loadOrCreateUserCode();

        btnJoin.setOnClickListener(v -> {
            String enteredCode = etGroupCode.getText().toString().trim();
            if (!enteredCode.isEmpty()) {
                joinGroup(enteredCode);
            } else {
                Toast.makeText(this, "Please enter a group code.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrCreateUserCode() {
        database.child("Users").child(userId).child("familyCode").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ownCode = snapshot.getValue(String.class);
                } else {
                    ownCode = generateRandomCode();
                    database.child("Users").child(userId).child("familyCode").setValue(ownCode);
                    database.child("Groups").child(ownCode).child("members").child(userId).setValue(true);
                }
                tvOwnCode.setText("Your Code: " + ownCode);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JoinFamilyActivity.this, "Failed to load code", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinGroup(String groupCode) {
        // Add user to group members list
        database.child("Groups").child(groupCode).child("members").child(userId).setValue(true);

        // Save joined group in user's data
        database.child("Users").child(userId).child("familyCode").setValue(groupCode);

        Toast.makeText(this, "Joined group: " + groupCode, Toast.LENGTH_SHORT).show();
    }

    private String generateRandomCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Closes this activity and returns to previous
        return true;
    }
}