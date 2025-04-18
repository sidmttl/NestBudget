package com.example.nestbudget;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private DatabaseReference databaseRef;
    private String familyCode;
    private String userId;

    // UI Elements
    private TextView currentDateTextView;
    private TextView totalBalanceTextView;
    private TextView cashAmountTextView;
    private TextView savingsAmountTextView;
    private TextView investmentsAmountTextView;

    // Budget tracker elements
    private TextView budgetSpentTextView;
    private TextView budgetTotalTextView;
    private ProgressBar budgetProgressBar;

    private LinearLayout upcomingBillsContainer;
    private TextView addBillButton;
    private LinearLayout goalsContainer;
    private TextView addGoalButton;

    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get login info
        LoginManager loginManager = new LoginManager(this);
        familyCode = loginManager.getFamilyCode();
        userId = loginManager.getLoggedInUser();

        // Initialize Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference("profile_pics");

        // Initialize UI Components
        initializeUI();

        // Set up navigation
        setupNavigationDrawer();
        setupBottomNavigation();

        // Load data
        loadCurrentDate();
        loadAccountsOverview();
        loadBudgetData();
        loadUpcomingBills();
        loadFinancialGoals();
    }

    private void initializeUI() {
        // Toolbar and navigation
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        // Header information
        currentDateTextView = findViewById(R.id.current_date);

        // Accounts overview
        totalBalanceTextView = findViewById(R.id.total_balance);
        cashAmountTextView = findViewById(R.id.cash_amount);
        savingsAmountTextView = findViewById(R.id.savings_amount);
        investmentsAmountTextView = findViewById(R.id.investments_amount);

        // Budget section
        budgetSpentTextView = findViewById(R.id.budget_spent);
        budgetTotalTextView = findViewById(R.id.budget_total);
        budgetProgressBar = findViewById(R.id.budgetBar);

        // Upcoming bills
        upcomingBillsContainer = findViewById(R.id.upcoming_bills_container);
        addBillButton = findViewById(R.id.add_bill_button);

        // Add bill button click
        addBillButton.setOnClickListener(v -> {
            showAddBillDialog();
        });

        // Financial goals
        goalsContainer = findViewById(R.id.goals_container);
        addGoalButton = findViewById(R.id.add_goal_button);

        // Handle notification and profile clicks
        ImageView notificationIcon = findViewById(R.id.notification_icon);
        ImageView profileIcon = findViewById(R.id.profile_icon);

        notificationIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications coming soon!", Toast.LENGTH_SHORT).show();
        });

        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SetProfileActivity.class);
            startActivity(intent);
        });

        loadProfilePicture(profileIcon);

        // Add goal button click
        addGoalButton.setOnClickListener(v -> {
            showAddGoalDialog();
        });
    }

    private void setupNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_add_family) {
                Intent intent = new Intent(MainActivity.this, JoinFamilyActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_settings) {
                Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_logout) {
                LoginManager loginManager = new LoginManager(MainActivity.this);
                loginManager.logout(MainActivity.this);
                return true;
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_dashboard);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_dashboard) {
                // Already on dashboard
                return true;
            } else if (itemId == R.id.menu_transactions) {
                Intent intent = new Intent(MainActivity.this, TransactionActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_insights) {
                Intent intent = new Intent(MainActivity.this, InsightsActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_journal) {
                Intent intent = new Intent(MainActivity.this, ToBuyListActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void loadCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        currentDateTextView.setText(currentDate);
    }

    private void loadAccountsOverview() {
        // Get accounts data from Firebase
        databaseRef.child("Groups").child(familyCode).child("accounts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double cashAmount = 0;
                        double savingsAmount = 0;
                        double creditCardTotal = 0;
                        double investmentsAmount = 0;

                        // Calculate totals for each account type
                        for (DataSnapshot accountSnapshot : snapshot.getChildren()) {
                            Account account = accountSnapshot.getValue(Account.class);
                            if (account != null) {
                                switch (account.getType()) {
                                    case "Cash":
                                        cashAmount += account.getBalance();
                                        break;
                                    case "Savings":
                                        savingsAmount += account.getBalance();
                                        break;
                                    case "Credit Card":
                                        creditCardTotal += account.getBalance();
                                        break;
                                    case "Investment":
                                        investmentsAmount += account.getBalance();
                                        break;
                                }
                            }
                        }

                        // Calculate total balance (subtract credit card debt)
                        double totalBalance = cashAmount + savingsAmount - creditCardTotal + investmentsAmount;

                        // Update UI
                        totalBalanceTextView.setText(String.format("$%.2f", totalBalance));
                        cashAmountTextView.setText(String.format("$%.0f", cashAmount));
                        savingsAmountTextView.setText(String.format("$%.0f", savingsAmount));
                        investmentsAmountTextView.setText(String.format("$%.0f", investmentsAmount));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Failed to load account data", Toast.LENGTH_SHORT).show();
                    }
                });

        // Make accounts overview card clickable
        View accountsCard = findViewById(R.id.accounts_overview_card);
        accountsCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AccountsActivity.class);
            startActivity(intent);
        });
    }

    private void loadBudgetData() {
        // For demo purposes, we'll use sample data
        double budgetTotal = 1000.00;
        double budgetSpent = 750.00;
        double budgetRemaining = budgetTotal - budgetSpent;

        // Update Budget UI
        budgetSpentTextView.setText(String.format("$%.0f Spent", budgetSpent));
        budgetTotalTextView.setText(String.format("of $%.0f", budgetTotal));

        // Set up budget progress bar - the progress represents amount spent
        budgetProgressBar.setMax((int)budgetTotal);
        budgetProgressBar.setProgress((int)budgetSpent);
    }

    private void loadUpcomingBills() {
        // Get bills from Firebase
        databaseRef.child("Groups").child(familyCode).child("bills")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear container
                        upcomingBillsContainer.removeAllViews();

                        // Check if bills exist
                        if (!snapshot.exists() || !snapshot.hasChildren()) {
                            // No bills found, show a message
                            TextView noBillsText = new TextView(MainActivity.this);
                            noBillsText.setText("No upcoming bills. Add your first bill!");
                            noBillsText.setTextSize(16);
                            noBillsText.setPadding(16, 16, 16, 16);
                            upcomingBillsContainer.addView(noBillsText);
                            return;
                        }

                        // Create bills list
                        List<Bill> bills = new ArrayList<>();
                        for (DataSnapshot billSnapshot : snapshot.getChildren()) {
                            Bill bill = billSnapshot.getValue(Bill.class);
                            if (bill != null && !bill.isPaid()) {
                                bills.add(bill);
                            }
                        }

                        // Sort bills by due date (closest first)
                        Collections.sort(bills, (bill1, bill2) -> bill1.getDueDate().compareTo(bill2.getDueDate()));

                        // Add bill views
                        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

                        for (Bill bill : bills) {
                            View billView = inflater.inflate(R.layout.item_upcoming_bill, upcomingBillsContainer, false);

                            TextView billNameTextView = billView.findViewById(R.id.bill_name);
                            TextView billAmountTextView = billView.findViewById(R.id.bill_amount);
                            TextView billDueDateTextView = billView.findViewById(R.id.bill_due_date);

                            billNameTextView.setText(bill.getName());
                            billAmountTextView.setText(String.format("$%.0f", bill.getAmount()));

                            String dueDate = dateFormat.format(bill.getDueDate());

                            // Check if bill is overdue
                            if (bill.isOverdue()) {
                                billDueDateTextView.setText("Overdue: " + dueDate);
                                billDueDateTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            } else {
                                // Check if bill is due soon (within 7 days)
                                long daysUntilDue = bill.daysUntilDue();

                                if (daysUntilDue <= 7) {
                                    billDueDateTextView.setText("Due: " + dueDate);
                                    billDueDateTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                                } else {
                                    billDueDateTextView.setText("Due: " + dueDate);
                                    billDueDateTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                                }
                            }

                            // Set click listener to edit bill
                            billView.setOnClickListener(v -> showEditBillDialog(bill));

                            upcomingBillsContainer.addView(billView);

                            // Add divider if not the last item
                            if (bills.indexOf(bill) < bills.size() - 1) {
                                View divider = new View(MainActivity.this);
                                divider.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        1
                                ));
                                divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                divider.setAlpha(0.5f);
                                upcomingBillsContainer.addView(divider);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Failed to load bills: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddBillDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Upcoming Bill");

        // Inflate the dialog layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_bill, null);
        builder.setView(view);

        // Get references to dialog views
        EditText etBillName = view.findViewById(R.id.et_bill_name);
        EditText etBillAmount = view.findViewById(R.id.et_bill_amount);
        Button btnPickDate = view.findViewById(R.id.btn_pick_date);
        TextView tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_bill_category);
        CheckBox checkboxPaid = view.findViewById(R.id.checkbox_bill_paid);

        // Create category adapter
        String[] categories = {"Rent/Mortgage", "Utilities", "Internet/Phone", "Insurance", "Subscription", "Credit Card", "Loan", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Date selection
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 7); // Default to 7 days from now
        final Date[] selectedDate = {calendar.getTime()};

        // Format and show the default date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvSelectedDate.setText(dateFormat.format(selectedDate[0]));

        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainActivity.this,
                    (view1, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        selectedDate[0] = calendar.getTime();
                        tvSelectedDate.setText(dateFormat.format(selectedDate[0]));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Setup dialog buttons
        builder.setPositiveButton("Add", null); // We'll set this later to prevent auto-dismiss
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override the positive button to validate input
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etBillName.getText().toString().trim();
            String amountStr = etBillAmount.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            boolean isPaid = checkboxPaid.isChecked();

            // Validate input
            if (name.isEmpty()) {
                etBillName.setError("Please enter a name");
                return;
            }

            if (amountStr.isEmpty()) {
                etBillAmount.setError("Please enter an amount");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    etBillAmount.setError("Amount must be greater than 0");
                    return;
                }
            } catch (NumberFormatException e) {
                etBillAmount.setError("Invalid amount");
                return;
            }

            // Create new bill
            String billId = databaseRef.child("Groups").child(familyCode).child("bills").push().getKey();
            if (billId != null) {
                Bill newBill = new Bill(
                        billId,
                        name,
                        amount,
                        selectedDate[0],
                        isPaid,
                        category,
                        userId
                );

                // Save to Firebase
                databaseRef.child("Groups").child(familyCode).child("bills").child(billId)
                        .setValue(newBill)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Bill added successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, "Failed to add bill: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void showEditBillDialog(Bill bill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Bill");

        // Inflate the dialog layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_bill, null);
        builder.setView(view);

        // Get references to dialog views
        EditText etBillName = view.findViewById(R.id.et_bill_name);
        EditText etBillAmount = view.findViewById(R.id.et_bill_amount);
        Button btnPickDate = view.findViewById(R.id.btn_pick_date);
        TextView tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_bill_category);
        CheckBox checkboxPaid = view.findViewById(R.id.checkbox_bill_paid);

        // Create category adapter
        String[] categories = {"Rent/Mortgage", "Utilities", "Internet/Phone", "Insurance", "Subscription", "Credit Card", "Loan", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Fill with existing data
        etBillName.setText(bill.getName());
        etBillAmount.setText(String.format(Locale.getDefault(), "%.2f", bill.getAmount()));
        checkboxPaid.setChecked(bill.isPaid());

        // Set selected category
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(bill.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        // Date selection
        final Calendar calendar = Calendar.getInstance();
        if (bill.getDueDate() != null) {
            calendar.setTime(bill.getDueDate());
        }
        final Date[] selectedDate = {calendar.getTime()};

        // Format and show the date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        tvSelectedDate.setText(dateFormat.format(selectedDate[0]));

        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainActivity.this,
                    (view1, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        selectedDate[0] = calendar.getTime();
                        tvSelectedDate.setText(dateFormat.format(selectedDate[0]));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Add option to delete
        builder.setNeutralButton("Delete", (dialog, which) -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this bill?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        // Delete from Firebase
                        databaseRef.child("Groups").child(familyCode).child("bills").child(bill.getId())
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(MainActivity.this, "Bill deleted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Failed to delete bill",
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
            String name = etBillName.getText().toString().trim();
            String amountStr = etBillAmount.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            boolean isPaid = checkboxPaid.isChecked();

            // Validate input
            if (name.isEmpty()) {
                etBillName.setError("Please enter a name");
                return;
            }

            if (amountStr.isEmpty()) {
                etBillAmount.setError("Please enter an amount");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    etBillAmount.setError("Amount must be greater than 0");
                    return;
                }
            } catch (NumberFormatException e) {
                etBillAmount.setError("Invalid amount");
                return;
            }

            // Update bill
            Bill updatedBill = new Bill(
                    bill.getId(),
                    name,
                    amount,
                    selectedDate[0],
                    isPaid,
                    category,
                    bill.getCreatedBy() // preserve the original creator
            );

            // Save to Firebase
            databaseRef.child("Groups").child(familyCode).child("bills").child(bill.getId())
                    .setValue(updatedBill)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Bill updated successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Failed to update bill: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void loadFinancialGoals() {
        // Get goals from Firebase
        databaseRef.child("Groups").child(familyCode).child("goals")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear container
                        goalsContainer.removeAllViews();

                        // Check if goals exist
                        if (!snapshot.exists() || !snapshot.hasChildren()) {
                            // No goals found, show a message
                            TextView noGoalsText = new TextView(MainActivity.this);
                            noGoalsText.setText("No financial goals yet. Add your first goal!");
                            noGoalsText.setTextSize(16);
                            noGoalsText.setPadding(16, 16, 16, 16);
                            goalsContainer.addView(noGoalsText);
                            return;
                        }

                        // Add each goal to the container
                        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);

                        for (DataSnapshot goalSnapshot : snapshot.getChildren()) {
                            FinancialGoal goal = goalSnapshot.getValue(FinancialGoal.class);
                            if (goal != null) {
                                addGoalView(inflater, goal);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Failed to load goals: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addGoalView(LayoutInflater inflater, FinancialGoal goal) {
        View goalView = inflater.inflate(R.layout.item_financial_goal, goalsContainer, false);

        TextView goalNameTextView = goalView.findViewById(R.id.goal_name);
        TextView goalAmountTextView = goalView.findViewById(R.id.goal_amount);
        ProgressBar goalProgressBar = goalView.findViewById(R.id.goal_progress);

        goalNameTextView.setText(goal.getName());
        goalAmountTextView.setText(String.format("$%.0f / $%.0f",
                goal.getCurrentAmount(), goal.getTargetAmount()));

        goalProgressBar.setMax((int) goal.getTargetAmount());
        goalProgressBar.setProgress((int) goal.getCurrentAmount());

        // Add click listener to edit the goal
        goalView.setOnClickListener(v -> showEditGoalDialog(goal));

        goalsContainer.addView(goalView);
    }

    private void showAddGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Financial Goal");

        // Inflate the dialog layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null);
        builder.setView(view);

        // Get references to dialog views
        EditText etGoalName = view.findViewById(R.id.et_goal_name);
        EditText etGoalTarget = view.findViewById(R.id.et_goal_target);
        EditText etGoalCurrent = view.findViewById(R.id.et_goal_current);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_goal_category);

        // Create category adapter
        String[] categories = {"Savings", "Education", "Vacation", "Home", "Vehicle", "Emergency", "Retirement", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Setup dialog buttons
        builder.setPositiveButton("Add", null); // We'll set this later to prevent auto-dismiss
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override the positive button to validate input
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etGoalName.getText().toString().trim();
            String targetStr = etGoalTarget.getText().toString().trim();
            String currentStr = etGoalCurrent.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();

            // Validate input
            if (name.isEmpty()) {
                etGoalName.setError("Please enter a name");
                return;
            }

            if (targetStr.isEmpty()) {
                etGoalTarget.setError("Please enter a target amount");
                return;
            }

            double target;
            double current = 0;

            try {
                target = Double.parseDouble(targetStr);
                if (target <= 0) {
                    etGoalTarget.setError("Amount must be greater than 0");
                    return;
                }
            } catch (NumberFormatException e) {
                etGoalTarget.setError("Invalid amount");
                return;
            }

            if (!currentStr.isEmpty()) {
                try {
                    current = Double.parseDouble(currentStr);
                    if (current < 0) {
                        etGoalCurrent.setError("Amount cannot be negative");
                        return;
                    }
                    if (current > target) {
                        etGoalCurrent.setError("Current amount cannot exceed target");
                        return;
                    }
                } catch (NumberFormatException e) {
                    etGoalCurrent.setError("Invalid amount");
                    return;
                }
            }

            // Create new goal
            String goalId = databaseRef.child("Groups").child(familyCode).child("goals").push().getKey();
            if (goalId != null) {
                FinancialGoal newGoal = new FinancialGoal(
                        goalId,
                        name,
                        target,
                        current,
                        category,
                        userId
                );

                // Save to Firebase
                databaseRef.child("Groups").child(familyCode).child("goals").child(goalId)
                        .setValue(newGoal)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Goal added successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, "Failed to add goal: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void showEditGoalDialog(FinancialGoal goal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Financial Goal");

        // Inflate the dialog layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null);
        builder.setView(view);

        // Get references to dialog views
        EditText etGoalName = view.findViewById(R.id.et_goal_name);
        EditText etGoalTarget = view.findViewById(R.id.et_goal_target);
        EditText etGoalCurrent = view.findViewById(R.id.et_goal_current);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_goal_category);

        // Create category adapter
        String[] categories = {"Savings", "Education", "Vacation", "Home", "Vehicle", "Emergency", "Retirement", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Fill with existing data
        etGoalName.setText(goal.getName());
        etGoalTarget.setText(String.format(Locale.getDefault(), "%.0f", goal.getTargetAmount()));
        etGoalCurrent.setText(String.format(Locale.getDefault(), "%.0f", goal.getCurrentAmount()));

        // Set selected category
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(goal.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        // Add option to delete
        builder.setNeutralButton("Delete", (dialog, which) -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this goal?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        // Delete from Firebase
                        databaseRef.child("Groups").child(familyCode).child("goals").child(goal.getId())
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(MainActivity.this, "Goal deleted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Failed to delete goal",
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
            String name = etGoalName.getText().toString().trim();
            String targetStr = etGoalTarget.getText().toString().trim();
            String currentStr = etGoalCurrent.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();

            // Validate input
            if (name.isEmpty()) {
                etGoalName.setError("Please enter a name");
                return;
            }

            if (targetStr.isEmpty()) {
                etGoalTarget.setError("Please enter a target amount");
                return;
            }

            double target;
            double current = 0;

            try {
                target = Double.parseDouble(targetStr);
                if (target <= 0) {
                    etGoalTarget.setError("Amount must be greater than 0");
                    return;
                }
            } catch (NumberFormatException e) {
                etGoalTarget.setError("Invalid amount");
                return;
            }

            if (!currentStr.isEmpty()) {
                try {
                    current = Double.parseDouble(currentStr);
                    if (current < 0) {
                        etGoalCurrent.setError("Amount cannot be negative");
                        return;
                    }
                    if (current > target) {
                        etGoalCurrent.setError("Current amount cannot exceed target");
                        return;
                    }
                } catch (NumberFormatException e) {
                    etGoalCurrent.setError("Invalid amount");
                    return;
                }
            }

            // Update goal
            FinancialGoal updatedGoal = new FinancialGoal(
                    goal.getId(),
                    name,
                    target,
                    current,
                    category,
                    goal.getCreatedBy() // preserve the original creator
            );

            // Save to Firebase
            databaseRef.child("Groups").child(familyCode).child("goals").child(goal.getId())
                    .setValue(updatedGoal)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Goal updated successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Failed to update goal: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void loadProfilePicture(ImageView profileIcon) {
        if (userId != null) {
            StorageReference imageRef = storageRef.child(userId + ".jpg");

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Profile picture exists, load it using Glide
                Glide.with(this)
                        .load(uri)
                        .circleCrop() // Make the image circular
                        .into(profileIcon);
            }).addOnFailureListener(e -> {
                // Profile picture doesn't exist or error occurred, keep the default icon
                // No action needed as the default icon is already set in the layout
            });
        }
    }
}