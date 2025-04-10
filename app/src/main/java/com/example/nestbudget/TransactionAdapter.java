package com.example.nestbudget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private final ArrayList<Transaction> transactionList;
    
    public TransactionAdapter(ArrayList<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction currentTransaction = transactionList.get(position);

        holder.nameTV.setText(currentTransaction.getTransactionName());
        holder.categoryTV.setText(currentTransaction.getTransactionCategory());
        holder.amountTV.setText(currentTransaction.getTransactionAmt());
        holder.locationTV.setText(currentTransaction.getTransactionLocation());
        holder.dateTV.setText(currentTransaction.getTransactionDate());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTV;
        private final TextView categoryTV;
        private final TextView amountTV;
        private final TextView locationTV;
        private final TextView dateTV;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTV = itemView.findViewById(R.id.transaction_name);
            categoryTV = itemView.findViewById(R.id.transaction_category);
            amountTV = itemView.findViewById(R.id.transaction_amount);
            locationTV = itemView.findViewById(R.id.transaction_location);
            dateTV = itemView.findViewById(R.id.transaction_date);
        }
    }
}
