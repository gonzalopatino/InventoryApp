/**
 * Inventory Management App
 * Developed by Gonzalo Patino
 * Software Engineer
 * Southern New Hampshire University
 * CS360 - Software Architecture
 *
 * Description: This app provides an inventory management system with user authentication,
 * SMS notifications for low inventory and upcoming events, and a structured UI
 * following the MVC architecture. It integrates an SQLite database for user authentication
 * and ensures incremental testing with JUnit at each development stage.
 *
 * Date: February 22, 2025
 */


package com.zybooks.inventorymapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private final List<String[]> dataList;

    /**
     * Constructor for InventoryAdapter.
     *
     * @param dataList List of inventory items (each item is a String array: [name, quantity, date]).
     */
    public InventoryAdapter(List<String[]> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] item = dataList.get(position);
        holder.tvItemName.setText(item[0]);
        holder.tvQuantity.setText(item[1]);
        holder.tvDate.setText(item[2]);

        // Handle delete button click event
        holder.btnDeleteRow.setOnClickListener(v -> removeItem(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    /**
     * Removes an item from the list and updates the RecyclerView properly.
     *
     * @param position Index of the item to remove.
     */
    private void removeItem(int position) {
        if (position >= 0 && position < dataList.size()) {
            dataList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, dataList.size()); // Prevent index issues
        }
    }

    /**
     * ViewHolder class to hold UI components for each RecyclerView item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvQuantity, tvDate;
        Button btnDeleteRow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDeleteRow = itemView.findViewById(R.id.btnDeleteRow);
        }
    }
}
