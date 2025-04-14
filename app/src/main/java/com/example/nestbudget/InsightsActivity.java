package com.example.nestbudget;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InsightsActivity extends AppCompatActivity {

    private static final String TAG = "InsightsActivity";

    private PieChart pieChart;
    private BottomNavigationView bottomNavigationView;
    private String familyCode;

    private DatabaseReference databaseRef;
    private ArrayList<Transaction> transactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insights);

        LoginManager loginManager = new LoginManager(this);
        familyCode = loginManager.getFamilyCode();

        if (familyCode == null || familyCode.isEmpty()) {
            Toast.makeText(this, "Family code is not set.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pieChart = findViewById(R.id.pieChart);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        databaseRef = FirebaseDatabase.getInstance().getReference();
        transactionList = new ArrayList<>();

//        setupBottomNavigation();
        fetchTransactions();
    }

    //
    private void fetchTransactions() {
        databaseRef.child("Groups").child(familyCode).child("transactions")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        transactionList.clear();
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            Transaction transaction = itemSnapshot.getValue(Transaction.class);
                            if (transaction != null) {
                                transactionList.add(transaction);
                            } else {
                                Log.e(TAG, "Transaction is null. Snapshot: " + itemSnapshot.toString());
                            }
                        }
                        updatePieChart();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(InsightsActivity.this, "Failed to load transactions.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Database error: " + error.getMessage(), error.toException());
                    }
                });
    }


    private void updatePieChart() {

        if (transactionList.isEmpty()) {
            Toast.makeText(this, "No transactions available for this user/family.", Toast.LENGTH_SHORT).show();
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        Map<String, Double> categoryTotals = new HashMap<>();


        for (Transaction transaction : transactionList) {
            String category = transaction.getTransactionCategory();
            if (category == null) {
                category = "Other";
            }

            try {
                double amount = Double.parseDouble(transaction.getTransactionAmount());
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            } catch (NumberFormatException | NullPointerException e) {
                Log.e(TAG, "Invalid transaction amount: " + transaction.getTransactionAmount(), e);
            }
        }

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "Transaction Categories");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS, this);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.invalidate();
    }



//
//    private void setupBottomNavigation() {
//        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
//            int itemId = item.getItemId();
//
//            if (itemId == R.id.menu_dashboard) {
//                return true;
//            } else if (itemId == R.id.menu_transactions) {
//                return true;
//            } else if (itemId == R.id.menu_insights) {
//                return true;
//            } else if (itemId == R.id.menu_journal) {
//                return true;
//            }
//            return false;
//        });
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        bottomNavigationView.setSelectedItemId(R.id.menu_insights);
//    }
}
