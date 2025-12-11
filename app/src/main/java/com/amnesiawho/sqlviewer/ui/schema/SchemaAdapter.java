package com.amnesiawho.sqlviewer.ui.schema;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.amnesiawho.sqlviewer.R;
import com.amnesiawho.sqlviewer.data.db.SchemaInfo;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class SchemaAdapter extends RecyclerView.Adapter<SchemaAdapter.SchemaViewHolder> {

    public interface OnSchemaClickListener {
        void onSchemaClick(SchemaInfo schemaInfo);
    }

    private final List<SchemaInfo> items = new ArrayList<>();
    private final OnSchemaClickListener listener;
    private String selectedSchema;

    public SchemaAdapter(OnSchemaClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<SchemaInfo> schemas) {
        items.clear();
        items.addAll(schemas);
        notifyDataSetChanged();
    }

    public void setSelectedSchema(String schema) {
        this.selectedSchema = schema;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SchemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schema, parent, false);
        return new SchemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SchemaViewHolder holder, int position) {
        SchemaInfo info = items.get(position);
        holder.bind(info, info.getName().equals(selectedSchema), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SchemaViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameView;
        private final TextView modeView;
        private final MaterialCardView cardView;

        SchemaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            nameView = itemView.findViewById(R.id.schemaName);
            modeView = itemView.findViewById(R.id.schemaMode);
        }

        void bind(SchemaInfo info, boolean isSelected, OnSchemaClickListener listener) {
            nameView.setText(info.getName());
            modeView.setText(info.isEditable() ? "Редактируемая" : "Только чтение");

            int strokeColor = ContextCompat.getColor(itemView.getContext(),
                    isSelected ? R.color.supabase_primary : R.color.supabase_border);
            int background = ContextCompat.getColor(itemView.getContext(),
                    info.isEditable() ? R.color.supabase_surface : R.color.supabase_surface_high);

            cardView.setStrokeColor(strokeColor);
            cardView.setCardBackgroundColor(background);
            cardView.setCardElevation(isSelected ? 6f : 2f);

            int modeColor = ContextCompat.getColor(itemView.getContext(),
                    info.isEditable() ? R.color.supabase_on_surface : R.color.supabase_warning);
            modeView.setTextColor(modeColor);

            itemView.setOnClickListener(v -> listener.onSchemaClick(info));
        }
    }
}
