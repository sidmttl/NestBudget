package com.example.nestbudget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ToBuyAdapter extends RecyclerView.Adapter<ToBuyAdapter.ItemViewHolder> {
    private List<ToBuyList> items;

    public ToBuyAdapter(List<ToBuyList> items) {
        this.items = items;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        ImageButton deleteButton;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.textItemName);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.to_buy_item_row, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        ToBuyList item = items.get(position);
        holder.itemName.setText(item.getName());

        holder.deleteButton.setOnClickListener(v -> {
            items.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
