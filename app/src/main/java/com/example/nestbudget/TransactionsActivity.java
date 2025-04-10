package com.example.nestbudget;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class TransactionsActivity extends AppCompatActivity {

        private ArrayList<Transaction> transactionList = new ArrayList<>();
        private RecyclerView transactionRecyclerView;
        private TransactionAdapter transactionAdapter;
        private RecyclerView.LayoutManager layoutManager;
        private FloatingActionButton addTransactionFAB;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_transactions);

            transactionRecyclerView = findViewById(R.id.recyler_view);
            transactionRecyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(this);
            transactionRecyclerView.setLayoutManager(layoutManager);
            transactionAdapter = new TransactionAdapter(transactionList);
            transactionRecyclerView.setAdapter(transactionAdapter);

            addTransactionFAB = findViewById(R.id.floatingActionButton2);
            addTransactionFAB.setOnClickListener(v -> addTransactionPopUpDialog());
            itemTouchHelper();
        }

        private void addTransactionPopUpDialog() {
            AlertDialog.Builder dialogPopUp = new AlertDialog.Builder(this);
            dialogPopUp.setTitle("Add New Transaction:");

            final EditText nameInput = new EditText(this);
            nameInput.setHint("Transaction Name");
            final EditText amountInput = new EditText(this);
            amountInput.setHint("Transaction Amount");
            final EditText categoryInput = new EditText(this);
            categoryInput.setHint("Transaction Category");
            final EditText dateInput = new EditText(this);
            dateInput.setHint("Transaction Date");
            final EditText locationInput = new EditText(this);
            locationInput.setHint("Transaction Location");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 10, 40, 10);
            layout.addView(nameInput);
            layout.addView(amountInput);
            layout.addView(categoryInput);
            layout.addView(dateInput);
            layout.addView(locationInput);
            dialogPopUp.setView(layout);

            dialogPopUp.setPositiveButton("Add", (dialog, which) -> {
                try {
                    String name = nameInput.getText().toString().trim();
                    String amount = amountInput.getText().toString().trim();
                    String category = categoryInput.getText().toString().trim();
                    String date = dateInput.getText().toString().trim();
                    String location = locationInput.getText().toString().trim();

                    transactionList.add(0, new Transaction(name, amount, category, date, location));
                    transactionAdapter.notifyItemInserted(0);
                    Snackbar.make(transactionRecyclerView, "Transaction added!", Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> {
                                transactionList.remove(0);
                                transactionAdapter.notifyItemRemoved(0);
                            }).show();
                   transactionRecyclerView.scrollToPosition(0);
                } catch (Exception e) {
                Log.e("TransactionsActivity", "Error with adding transaction", e);
                Toast.makeText(this, "An error occurred while adding transaction.", Toast.LENGTH_SHORT).show();
            }
        });


            dialogPopUp.show();
        }

        private void itemTouchHelper() {
            ItemTouchHelper.SimpleCallback itemSlideLeftOrRight = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int pos = viewHolder.getAdapterPosition();
                    if (direction == ItemTouchHelper.RIGHT) {
                        Transaction deletedTransaction = transactionList.get(pos);
                        transactionList.remove(pos);
                        transactionAdapter.notifyItemRemoved(pos);

                        Snackbar.make(transactionRecyclerView, "Transaction deleted!", Snackbar.LENGTH_LONG)
                                .setAction("Undo", v -> {
                                    transactionList.add(pos, deletedTransaction);
                                    transactionAdapter.notifyItemInserted(pos);
                                }).show();
                    } else if (direction == ItemTouchHelper.LEFT) {
                        editTransactionPopUpDialog(pos);
                        transactionAdapter.notifyItemChanged(pos);
                    }
                }
            };

            new ItemTouchHelper(itemSlideLeftOrRight).attachToRecyclerView(transactionRecyclerView);
        }

        private void editTransactionPopUpDialog(int pos) {
            Transaction transactionToEdit = transactionList.get(pos);
            String oldName = transactionToEdit.getTransactionName();
            String oldAmount = String.valueOf(transactionToEdit.getTransactionAmt());
            String oldCategory = transactionToEdit.getTransactionCategory();
            String oldDate = transactionToEdit.getTransactionDate();
            String oldLocation = transactionToEdit.getTransactionLocation();

            AlertDialog.Builder dialogPopUp = new AlertDialog.Builder(this);
            dialogPopUp.setTitle("Edit Transaction:");

            final EditText nameInput = new EditText(this);
            nameInput.setText(transactionToEdit.getTransactionName());
            final EditText amountInput = new EditText(this);
            amountInput.setText(transactionToEdit.getTransactionAmt());
            final EditText categoryInput = new EditText(this);
            categoryInput.setText(transactionToEdit.getTransactionCategory());
            final EditText dateInput = new EditText(this);
            dateInput.setText(transactionToEdit.getTransactionDate());
            final EditText locationInput = new EditText(this);
            locationInput.setText(transactionToEdit.getTransactionLocation());

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 10, 40, 10);
            layout.addView(nameInput);
            layout.addView(amountInput);
            layout.addView(categoryInput);
            layout.addView(dateInput);
            layout.addView(locationInput);
            dialogPopUp.setView(layout);

            dialogPopUp.setPositiveButton("Save", (dialog, which) -> {
                transactionToEdit.setTransactionName(nameInput.getText().toString().trim());
                transactionToEdit.setTransactionAmt(amountInput.getText().toString().trim());
                transactionToEdit.setTransactionCategory(categoryInput.getText().toString().trim());
                transactionToEdit.setTransactionDate(dateInput.getText().toString().trim());
                transactionToEdit.setTransactionLocation(locationInput.getText().toString().trim());
                transactionAdapter.notifyItemChanged(pos);

                Snackbar.make(transactionRecyclerView, "Transaction updated!", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            transactionToEdit.setTransactionName(oldName);
                            transactionToEdit.setTransactionAmt(oldAmount);
                            transactionToEdit.setTransactionCategory(oldCategory);
                            transactionToEdit.setTransactionDate(oldDate);
                            transactionToEdit.setTransactionLocation(oldLocation);
                            transactionAdapter.notifyItemChanged(pos);
                        }).show();
            });

            dialogPopUp.show();
        }
    }
