package com.example.nestbudget;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
    private String savedUsername;

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
            etFirstName.setError("First Name is required");
            return;
        }
        if (TextUtils.isEmpty(age)) {
            etAge.setError("Age is required");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        databaseReference.child("Users").child(username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Toast.makeText(SignupActivity.this, "Username already taken. Choose another one.", Toast.LENGTH_SHORT).show();
            } else {
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

        savedUsername = username;

        databaseReference.child("Users").child(username).setValue(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Proceed to income/budget pop-up
                showIncomeBudgetDialog(username);
            } else {
                Toast.makeText(SignupActivity.this, "Failed to save data!", Toast.LENGTH_SHORT).show();
            }
        });

        databaseReference.child("Groups").child(ownCode).child("members").child(username).setValue(true);
    }

    private String generateRandomCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private void showIncomeBudgetDialog(String username) {
        Dialog dialog = new Dialog(SignupActivity.this);
        dialog.setContentView(R.layout.dialog_income_budget);
        dialog.setCancelable(false);

        EditText etIncome = dialog.findViewById(R.id.etIncome);
        EditText etBudget = dialog.findViewById(R.id.etBudget);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> {
            String income = etIncome.getText().toString().trim();
            String budget = etBudget.getText().toString().trim();

            if (TextUtils.isEmpty(income) || TextUtils.isEmpty(budget)) {
                Toast.makeText(SignupActivity.this, "Both fields are required", Toast.LENGTH_SHORT).show();
            } else {
                databaseReference.child("Users").child(username).child("monthlyIncome").setValue(income);
                databaseReference.child("Users").child(username).child("monthlyBudget").setValue(budget);
                dialog.dismiss();
                Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                finish(); // Finish SignupActivity so the user cannot go back to it
            }
        });

        btnCancel.setOnClickListener(v -> {
            String income = etIncome.getText().toString().trim();
            String budget = etBudget.getText().toString().trim();

            if (TextUtils.isEmpty(income) || TextUtils.isEmpty(budget)) {
                Toast.makeText(SignupActivity.this, "Both fields are required", Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
