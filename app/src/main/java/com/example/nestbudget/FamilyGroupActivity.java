package com.example.nestbudget;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class FamilyGroupActivity extends AppCompatActivity {

    private Button btnCreateFamily, btnJoinFamily;
    private String username;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_group);

        btnCreateFamily = findViewById(R.id.btnCreateFamily);
        btnJoinFamily = findViewById(R.id.btnJoinFamily);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        username = getIntent().getStringExtra("username");

        btnCreateFamily.setOnClickListener(v -> createNewFamily());
        btnJoinFamily.setOnClickListener(v -> showJoinFamilyScreen());
    }

    private void createNewFamily() {
        String newFamilyCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Save familyCode under user
        databaseReference.child("Users").child(username).child("familyCode").setValue(newFamilyCode);
        // Add user to the group
        databaseReference.child("Groups").child(newFamilyCode).child("members").child(username).setValue(true);

        // Navigate to FamilySetupActivity to collect financial details
        Intent intent = new Intent(FamilyGroupActivity.this, FamilySetupActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("familyCode", newFamilyCode);
        startActivity(intent);
    }

    private void showJoinFamilyScreen() {
        // Navigate to a screen where the user can enter the family code
        Intent intent = new Intent(FamilyGroupActivity.this, JoinFamilyActivity.class);
        startActivity(intent);
    }
}
