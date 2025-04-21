package com.example.nestbudget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
    private List<Account> accounts;
    private List<Account> filteredAccounts;
    private Context context;
    private String familyCode;
    private DatabaseReference databaseRef;
    private String filterType;

    public AccountAdapter(Context context, List<Account> accounts, String familyCode) {
        this.context = context;
        this.accounts = accounts;
        this.filteredAccounts = new ArrayList<>(accounts); // Create a copy for filtering
        this.familyCode = familyCode;
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
        this.filterType = null; // No filter initially
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        ImageView accountIcon;
        TextView accountName;
        TextView accountInstitution;
        TextView accountNumber;
        TextView accountBalance;

        public AccountViewHolder(View itemView) {
            super(itemView);
            accountIcon = itemView.findViewById(R.id.account_icon);
            accountName = itemView.findViewById(R.id.account_name);
            accountInstitution = itemView.findViewById(R.id.account_institution);
            accountNumber = itemView.findViewById(R.id.account_number);
            accountBalance = itemView.findViewById(R.id.account_balance);
        }
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AccountViewHolder holder, int position) {
        if (position < 0 || position >= filteredAccounts.size()) {
            return;
        }

        Account account = filteredAccounts.get(position);

        holder.accountName.setText(account.getName());
        holder.accountInstitution.setText(account.getInstitution());

        // Format account number
        if (account.getAccountNumber() != null && !account.getAccountNumber().isEmpty()) {
            holder.accountNumber.setText("xxxx-" + account.getAccountNumber());
        } else {
            holder.accountNumber.setText("");
        }

        // Format balance with currency symbol
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        holder.accountBalance.setText(currencyFormat.format(account.getBalance()));

        // Set appropriate icon and color for different account types
        switch (account.getType()) {
            case "Cash":
                holder.accountIcon.setImageResource(android.R.drawable.ic_menu_compass); // Use better icon when available
                holder.accountBalance.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "Savings":
                holder.accountIcon.setImageResource(android.R.drawable.ic_menu_save); // Use better icon when available
                holder.accountBalance.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case "Credit Card":
                holder.accountIcon.setImageResource(android.R.drawable.ic_menu_send); // Use better icon when available
                // For credit cards, negative balance is actually good (you don't owe money)
                if (account.getBalance() > 0) {
                    holder.accountBalance.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    holder.accountBalance.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                }
                break;
            case "Investment":
                holder.accountIcon.setImageResource(android.R.drawable.ic_menu_report_image); // Use better icon when available
                holder.accountBalance.setTextColor(context.getResources().getColor(android.R.color.holo_purple));
                break;
            default:
                holder.accountIcon.setImageResource(android.R.drawable.ic_menu_info_details);
                holder.accountBalance.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                break;
        }

        // Edit account on click
        holder.itemView.setOnClickListener(v -> {
            showEditAccountDialog(account);
        });
    }

    @Override
    public int getItemCount() {
        return filteredAccounts.size();
    }

    // Method to update the filter type
    public void setFilterType(String filterType) {
        this.filterType = filterType;
        filterAccounts();
        notifyDataSetChanged();
    }

    // Method to update accounts list with new data
    public void updateAccounts(List<Account> newAccounts) {
        this.accounts = new ArrayList<>(newAccounts);
        filterAccounts();
        notifyDataSetChanged();
    }

    // Method to filter accounts based on the current filterType
    private void filterAccounts() {
        filteredAccounts.clear();

        if (filterType == null || filterType.equals("All")) {
            // If no filter or "All" filter, include all accounts
            filteredAccounts.addAll(accounts);
        } else {
            // Otherwise, only include accounts of the specified type
            for (Account account : accounts) {
                if (account.getType().equals(filterType)) {
                    filteredAccounts.add(account);
                }
            }
        }
    }

    private void showEditAccountDialog(Account account) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Account");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_account, null);
        final EditText editTextName = view.findViewById(R.id.et_account_name);
        final Spinner spinnerType = view.findViewById(R.id.spinner_account_type);
        final EditText editTextBalance = view.findViewById(R.id.et_account_balance);
        final EditText editTextInstitution = view.findViewById(R.id.et_account_institution);
        final EditText editTextNumber = view.findViewById(R.id.et_account_number);

        // Set up the account type spinner
        String[] accountTypes = {"Cash", "Savings", "Credit Card", "Investment"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, accountTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Fill the fields with account data
        editTextName.setText(account.getName());
        editTextBalance.setText(String.format(Locale.US, "%.2f", account.getBalance()));
        editTextInstitution.setText(account.getInstitution());
        editTextNumber.setText(account.getAccountNumber());

        // Set the spinner to the current account type
        for (int i = 0; i < accountTypes.length; i++) {
            if (accountTypes[i].equals(account.getType())) {
                spinnerType.setSelection(i);
                break;
            }
        }

        builder.setView(view);

        // Add option to delete
        builder.setNeutralButton("Delete", (dialog, which) -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this account?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        // Delete from Firebase
                        databaseRef.child("Groups").child(familyCode).child("accounts").child(account.getId())
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to delete account",
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Setup dialog buttons
        builder.setPositiveButton("Save", null); // We'll set this later to prevent auto-dismiss
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

            // Create updated account
            Account updatedAccount = new Account(
                    account.getId(),
                    name,
                    type,
                    balance,
                    institution,
                    number,
                    account.getCreatedBy()
            );

            // Save to Firebase
            databaseRef.child("Groups").child(familyCode).child("accounts").child(account.getId())
                    .setValue(updatedAccount)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Account updated successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update account: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });
    }
}