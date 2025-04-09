package com.example.nestbudget;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.Toast;

public class LoginManager {
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FAMILYCODE  = "familyCode";


    private SharedPreferences sharedPreferences;
    private DatabaseReference databaseReference;

    public LoginManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    // Logs in the user by searching for the username inside users child
    public void loginUser(String inputUsername, Context context) {

        databaseReference.child("Users").child(inputUsername).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show();
                    sharedPreferences.edit().putString(KEY_USERNAME, inputUsername).apply();
                    String familyCode = snapshot.child("familyCode").getValue().toString();
                    sharedPreferences.edit().putString(KEY_FAMILYCODE, familyCode).apply();
                    //context.startActivity(new Intent(context, HistoryActivity.class));
                    context.startActivity(new Intent(context, MainActivity.class));
                } else {
                    Toast.makeText(context, "User not found!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Failed to connect to database!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Checks if a user is already logged in
    public String getLoggedInUser() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }
    public String getFamilyCode() {
        return sharedPreferences.getString(KEY_FAMILYCODE, null);
    }

    public void logout(Context context) {
        sharedPreferences.edit().clear().apply();
        Intent intent = new Intent(context, LoginActivity.class);

        startActivity(context, intent,null);
    }
}