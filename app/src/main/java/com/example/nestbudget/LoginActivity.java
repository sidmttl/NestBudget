package com.example.nestbudget;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewCreateAccount;
    private LoginManager loginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize LoginManager
        loginManager = new LoginManager(this);

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewCreateAccount = findViewById(R.id.textViewCreateAccount);

        // Check if a user is already logged in and redirect
        String loggedInUser = loginManager.getLoggedInUser();
        if (loggedInUser != null) {
//            navigateToDashboard(loggedInUser);
        }

        // Handle login button click
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();

                if (!username.isEmpty()) {
                    // Attempt to log in the user
                    loginManager.loginUser(username, LoginActivity.this);
                } else {
                    Toast.makeText(LoginActivity.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle "Create Account" link click
        textViewCreateAccount.setOnClickListener(view -> {
//            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
//            startActivity(intent);
        });
    }

    /**
     * Navigates to the Sticker Dashboard after login.
     *
     * @param username The logged-in username.
     */
//    private void navigateToDashboard(String username) {
//        Intent intent = new Intent(A7StickerActivity.this, StickerDashboardActivity.class);
//        intent.putExtra("USERNAME", username);
//        startActivity(intent);
//        finish(); // Close login activity
//    }
}
