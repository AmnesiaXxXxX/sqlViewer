package com.amnesiawho.sqlviewer.ui.table;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.amnesiawho.sqlviewer.R;

import java.util.ArrayList;
import java.util.List;

public class TableListAdapter extends RecyclerView.Adapter<TableListAdapter.TableViewHolder> {

    public interface OnTableClickListener {
        void onTableClick(String tableName);
    }

    private final List<String> items = new ArrayList<>();
    private final OnTableClickListener listener;
    private String selectedTable;

    public TableListAdapter(OnTableClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<String> tables) {
        items.clear();
        items.addAll(tables);
        selectedTable = null;
        notifyDataSetChanged();
    }

    public void setSelectedTable(String tableName) {
        selectedTable = tableName;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table_entry, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        String tableName = items.get(position);
        holder.bind(tableName, tableName.equals(selectedTable), listener, () -> {
            selectedTable = tableName;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TableViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameView;
        private final View container;

        TableViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.tableItemContainer);
            nameView = itemView.findViewById(R.id.tableItemName);
        }

        void bind(String tableName, boolean isSelected, OnTableClickListener listener, Runnable onSelected) {
            nameView.setText(tableName);
            int background = ContextCompat.getColor(itemView.getContext(),
                    isSelected ? R.color.supabase_surface_high : R.color.supabase_surface);
            container.setBackgroundColor(background);
            itemView.setOnClickListener(v -> {
                onSelected.run();
                listener.onTableClick(tableName);
            });
        }
    }
}
