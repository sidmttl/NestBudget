package com.example.nestbudget;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.UUID;

public class SignupActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPassword, etUsername, etAge;
    private Button btnSignup;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference();

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etAge = findViewById(R.id.etAge);
        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(view -> registerUser());

    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String age = etAge.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            return;
        }
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(age)) {
            etFirstName.setError("Age is required");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("firstName", firstName);
        userMap.put("lastName", lastName);
        userMap.put("password", password);
        userMap.put("age", age);

        assert username != null;

        databaseReference.child("Users").child(username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Username already exists
                Toast.makeText(SignupActivity.this, "Username already taken. Choose another one.", Toast.LENGTH_SHORT).show();
            } else {
                // Username is available, proceed with signup
                saveUserToDatabase(username, firstName, lastName, password, age);
            }
        });
    }

    private void saveUserToDatabase(String username, String firstName, String lastName, String password, String age) {

        String ownCode = generateRandomCode();

        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("firstName", firstName);
        userMap.put("lastName", lastName);
        userMap.put("password", password);
        userMap.put("age", age);
        userMap.put("familyCode", ownCode);


        databaseReference.child("Users").child(username).setValue(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
               Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
               finish();
            } else {
                Toast.makeText(SignupActivity.this, "Failed to save data!", Toast.LENGTH_SHORT).show();
            }
        });

        databaseReference.child("Groups").child(ownCode).child("members").child(username).setValue(true);

    }

    private String generateRandomCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}