package com.example.nestbudget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {

    private EditText budgetEditText, incomeEditText;
    private ImageView editBudgetIcon, editIncomeIcon;
    private Button saveButton, cancelButton;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        budgetEditText = findViewById(R.id.budgetEditText);
        incomeEditText = findViewById(R.id.incomeEditText);
        editBudgetIcon = findViewById(R.id.editBudgetIcon);
        editIncomeIcon = findViewById(R.id.editIncomeIcon);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString(KEY_USERNAME, null);
        database = FirebaseDatabase.getInstance().getReference();

        disableEditText(budgetEditText);
        disableEditText(incomeEditText);

        editBudgetIcon.setOnClickListener(v -> enableEditing(budgetEditText));
        editIncomeIcon.setOnClickListener(v -> enableEditing(incomeEditText));

        budgetEditText.setOnKeyListener((v, keyCode, event) -> handleEnterKey(event, budgetEditText));
        incomeEditText.setOnKeyListener((v, keyCode, event) -> handleEnterKey(event, incomeEditText));

        saveButton.setOnClickListener(v -> {
            if (userId != null) {
                String budget = budgetEditText.getText().toString().trim();
                String income = incomeEditText.getText().toString().trim();
                if (!budget.isEmpty()) {
                    database.child("Users").child(userId).child("monthlyBudget").setValue(budget);
                }
                if (!income.isEmpty()) {
                    database.child("Users").child(userId).child("monthlyIncome").setValue(income);
                }
                Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            finish();
        });
    }

    private void enableEditing(EditText editText) {
        editText.setEnabled(true);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
    }

    private boolean handleEnterKey(KeyEvent event, EditText editText) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            editText.clearFocus();
            disableEditText(editText);
            return true;
        }
        return false;
    }

    private void disableEditText(EditText editText) {
        editText.setEnabled(false);
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
