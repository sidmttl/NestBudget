package com.example.nestbudget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AccountsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AccountAdapter adapter;
    private List<Account> accountList;
    private TabLayout tabLayout;
    private TextView totalBalanceTextView;
    private FloatingActionButton addAccountFab;
    private ImageView backButton;

    private String familyCode;
    private String userId;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        // Get login info
        LoginManager loginManager = new LoginManager(this);
        familyCode = loginManager.getFamilyCode();
        userId = loginManager.getLoggedInUser();

        // Initialize Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Initialize UI Components
        recyclerView = findViewById(R.id.accounts_recycler_view);
        tabLayout = findViewById(R.id.accounts_tab_layout);
        totalBalanceTextView = findViewById(R.id.accounts_total_balance);
        addAccountFab = findViewById(R.id.add_account_fab);
        backButton = findViewById(R.id.back_button);

        // Set up RecyclerView
        accountList = new ArrayList<>();
        adapter = new AccountAdapter(this, accountList, familyCode);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set up tabs
        setupTabs();

        // Load accounts from Firebase
        loadAccounts();

        // Set up Add Account FAB
        addAccountFab.setOnClickListener(v -> showAddAccountDialog());

        // Set up back button
        backButton.setOnClickListener(v -> finish());
    }

    private void setupTabs() {
        // Add tabs
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Cash"));
        tabLayout.addTab(tabLayout.newTab().setText("Savings"));
        tabLayout.addTab(tabLayout.newTab().setText("Credit Card"));
        tabLayout.addTab(tabLayout.newTab().setText("Investment"));

        // Handle tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedType = tab.getText().toString();
                // Extract the type without the count if it exists
                if (selectedType.contains(" (")) {
                    selectedType = selectedType.substring(0, selectedType.indexOf(" ("));
                }

                if (selectedType.equals("All")) {
                    adapter.setFilterType(null);
                } else {
                    adapter.setFilterType(selectedType);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // When a tab is reselected, refresh the same filter
                onTabSelected(tab);
            }
        });
    }

    private void loadAccounts() {
        databaseRef.child("Groups").child(familyCode).child("accounts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear existing accounts
                        accountList.clear();

                        // Check if accounts exist
                        if (!snapshot.exists()) {
                            updateBalanceSummary();
                            adapter.updateAccounts(accountList);
                            return;
                        }

                        // Add each account
                        for (DataSnapshot accountSnapshot : snapshot.getChildren()) {
                            Account account = accountSnapshot.getValue(Account.class);
                            if (account != null) {
                                accountList.add(account);
                            }
                        }

                        // Update the total balance display
                        updateBalanceSummary();

                        // Update the adapter with the new accounts list
                        adapter.updateAccounts(accountList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AccountsActivity.this, "Failed to load accounts: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateBalanceSummary() {
        // Calculate totals for different account types
        double cashTotal = 0;
        double savingsTotal = 0;
        double creditCardTotal = 0;
        double investmentTotal = 0;

        for (Account account : accountList) {
            switch (account.getType()) {
                case "Cash":
                    cashTotal += account.getBalance();
                    break;
                case "Savings":
                    savingsTotal += account.getBalance();
                    break;
                case "Credit Card":
                    creditCardTotal += account.getBalance();
                    break;
                case "Investment":
                    investmentTotal += account.getBalance();
                    break;
            }
        }

        // Calculate total across all accounts (subtract credit card debt)
        double totalBalance = cashTotal + savingsTotal - creditCardTotal + investmentTotal;

        // Update UI
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        totalBalanceTextView.setText(currencyFormat.format(totalBalance));

        // Update account counts in tab titles
        if (tabLayout.getTabCount() >= 5) {
            int cashCount = 0;
            int savingsCount = 0;
            int creditCardCount = 0;
            int investmentCount = 0;

            for (Account account : accountList) {
                switch (account.getType()) {
                    case "Cash":
                        cashCount++;
                        break;
                    case "Savings":
                        savingsCount++;
                        break;
                    case "Credit Card":
                        creditCardCount++;
                        break;
                    case "Investment":
                        investmentCount++;
                        break;
                }
            }

            tabLayout.getTabAt(0).setText("All (" + accountList.size() + ")");
            tabLayout.getTabAt(1).setText("Cash (" + cashCount + ")");
            tabLayout.getTabAt(2).setText("Savings (" + savingsCount + ")");
            tabLayout.getTabAt(3).setText("Credit Card (" + creditCardCount + ")");
            tabLayout.getTabAt(4).setText("Investment (" + investmentCount + ")");
        }
    }

    private void showAddAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Account");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null);
        final EditText editTextName = view.findViewById(R.id.et_account_name);
        final Spinner spinnerType = view.findViewById(R.id.spinner_account_type);
        final EditText editTextBalance = view.findViewById(R.id.et_account_balance);
        final EditText editTextInstitution = view.findViewById(R.id.et_account_institution);
        final EditText editTextNumber = view.findViewById(R.id.et_account_number);

        // Set up the account type spinner
        String[] accountTypes = {"Cash", "Savings", "Credit Card", "Investment"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, accountTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        builder.setView(view);

        // Setup dialog buttons
        builder.setPositiveButton("Add", null); // We'll set this later to prevent auto-dismiss
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override the positive button to validate input
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String type = spinnerType.getSelectedItem().toString();
            String balanceStr = editTextBalance.getText().toString().trim();
            String institution = editTextInstitution.getText().toString().trim();
            String number = editTextNumber.getText().toString().trim();

            // Validate input
            if (name.isEmpty()) {
                editTextName.setError("Please enter a name");
                return;
            }

            if (balanceStr.isEmpty()) {
                editTextBalance.setError("Please enter a balance");
                return;
            }

            double balance;
            try {
                balance = Double.parseDouble(balanceStr);
            } catch (NumberFormatException e) {
                editTextBalance.setError("Invalid amount");
                return;
            }

            if (institution.isEmpty()) {
                editTextInstitution.setError("Please enter an institution");
                return;
            }

            // Create new account
            String accountId = databaseRef.child("Groups").child(familyCode).child("accounts").push().getKey();
            if (accountId != null) {
                Account newAccount = new Account(
                        accountId,
                        name,
                        type,
                        balance,
                        institution,
                        number,
                        userId
                );

                // Save to Firebase
                databaseRef.child("Groups").child(familyCode).child("accounts").child(accountId)
                        .setValue(newAccount)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(AccountsActivity.this, "Account added successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AccountsActivity.this, "Failed to add account: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}