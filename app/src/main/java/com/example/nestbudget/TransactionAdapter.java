package com.example.nestbudget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction> transactions;
    private Context context;
    private String familyCode;
    private DatabaseReference databaseRef;

    public TransactionAdapter(Context context, List<Transaction> transactions, String familyCode) {
        this.context = context;
        this.transactions = transactions;
        this.familyCode = familyCode;
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView transactionName;
        TextView transactionAmount;
        TextView transactionDate;
        ImageButton deleteButton;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            transactionName = itemView.findViewById(R.id.transaction_name);
            transactionAmount = itemView.findViewById(R.id.transaction_amount);
            transactionDate = itemView.findViewById(R.id.transaction_date);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, int position) {
        if (position < 0 || position >= transactions.size()) {
            return;
        }

        Transaction transaction = transactions.get(position);
        holder.transactionName.setText(transaction.getTransactionName());
        holder.transactionAmount.setText(transaction.getTransactionAmount());
        holder.transactionDate.setText(transaction.getTransactionDate());


        holder.deleteButton.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && currentPosition < transactions.size()) {
                Transaction transactionToDelete = transactions.get(currentPosition);

                databaseRef.child("Groups").child(familyCode).child("transactions")
                        .child(transactionToDelete.getTransactionId()).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && currentPosition < transactions.size()) {
                showEditDialog(currentPosition);
            }
        });
    }

    private void showEditDialog(int position) {
        if (position < 0 || position >= transactions.size()) {
            return;
        }

        Transaction currentTransaction = transactions.get(position);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_transaction, null);
        final EditText editTextName = dialogView.findViewById(R.id.etTransactionName);
        final EditText editTextCategory = dialogView.findViewById(R.id.etTransactionCategory);
        final EditText editTextAmount = dialogView.findViewById(R.id.etTransactionAmount);
        final EditText editTextLocation = dialogView.findViewById(R.id.etTransactionLocation);
        final EditText editTextDate = dialogView.findViewById(R.id.etTransactionDate);


        editTextName.setText(currentTransaction.getTransactionName());
        editTextCategory.setText(currentTransaction.getTransactionCategory());
        editTextAmount.setText(currentTransaction.getTransactionAmount());
        editTextLocation.setText(currentTransaction.getTransactionLocation());
        editTextDate.setText(currentTransaction.getTransactionDate());

        new AlertDialog.Builder(context)
                .setTitle("Edit Transaction")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = editTextName.getText().toString().trim();
                    String newCategory = editTextCategory.getText().toString().trim();
                    String newAmount = editTextAmount.getText().toString().trim();
                    String newLocation = editTextLocation.getText().toString().trim();
                    String newDate = editTextDate.getText().toString().trim();

                    if (newName.isEmpty() || newCategory.isEmpty() || newAmount.isEmpty() ||
                            newLocation.isEmpty() || newDate.isEmpty()) {
                        Toast.makeText(context, "All fields are required!", Toast.LENGTH_SHORT).show();
                    } else {

                        Transaction updatedTransaction = new Transaction(
                                currentTransaction.getTransactionId(),
                                newName,
                                newCategory,
                                newAmount,
                                newLocation,
                                newDate
                        );

                        databaseRef.child("Groups").child(familyCode).child("transactions")
                                .child(currentTransaction.getTransactionId()).setValue(updatedTransaction)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Transaction updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to update transaction. Please try again", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    @Override
    public int getItemCount() {
        return transactions.size();
    }
}
