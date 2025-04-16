package com.example.nestbudget;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FamilySetupActivity extends AppCompatActivity {

    private EditText etIncome, etBudget, etSavingsGoal, etInvestmentsCurrent, etInvestmentsGoal, etHasDebt;
    private Button btnSave, btnCancel;
    private String username, familyCode;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_setup);

        etIncome = findViewById(R.id.etIncome);
        etBudget = findViewById(R.id.etBudget);
        etSavingsGoal = findViewById(R.id.etSavingsGoal);
        etInvestmentsCurrent = findViewById(R.id.etInvestmentsCurrent);
        etInvestmentsGoal = findViewById(R.id.etInvestmentsGoal);
        etHasDebt = findViewById(R.id.etHasDebt);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        username = getIntent().getStringExtra("username");
        familyCode = getIntent().getStringExtra("familyCode");

        btnSave.setOnClickListener(v -> saveFamilyDetails());
        btnCancel.setOnClickListener(v -> cancelSetup());
    }

    private void saveFamilyDetails() {
        String income = etIncome.getText().toString().trim();
        String budget = etBudget.getText().toString().trim();
        String savingsGoal = etSavingsGoal.getText().toString().trim();
        String investmentsCurrent = etInvestmentsCurrent.getText().toString().trim();
        String investmentsGoal = etInvestmentsGoal.getText().toString().trim();
        String hasDebt = etHasDebt.getText().toString().trim();

        if (TextUtils.isEmpty(income) || TextUtils.isEmpty(budget) || TextUtils.isEmpty(savingsGoal) ||
                TextUtils.isEmpty(investmentsCurrent) || TextUtils.isEmpty(investmentsGoal) || TextUtils.isEmpty(hasDebt)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save financial details in Firebase under the user's profile
        databaseReference.child("Users").child(username).child("monthlyIncome").setValue(income);
        databaseReference.child("Users").child(username).child("monthlyBudget").setValue(budget);
        databaseReference.child("Users").child(username).child("savingsGoal").setValue(savingsGoal);
        databaseReference.child("Users").child(username).child("investmentsCurrent").setValue(investmentsCurrent);
        databaseReference.child("Users").child(username).child("investmentsGoal").setValue(investmentsGoal);
        databaseReference.child("Users").child(username).child("hasDebt").setValue(hasDebt);

        Toast.makeText(this, "Family setup successful!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(FamilySetupActivity.this, MainActivity.class));
        finish();
    }

    private void cancelSetup() {
        // Return to FamilyGroupActivity
        Intent intent = new Intent(FamilySetupActivity.this, FamilyGroupActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
    }
}
