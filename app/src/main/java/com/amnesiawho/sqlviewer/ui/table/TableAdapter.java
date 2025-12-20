package com.amnesiawho.sqlviewer.ui.table;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.amnesiawho.sqlviewer.R;
import com.amnesiawho.sqlviewer.data.db.TableData;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер, который строит строки таблицы на лету.
 */
public class TableAdapter extends RecyclerView.Adapter<TableAdapter.RowViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ROW = 1;

    private final LayoutInflater inflater;
    private final List<List<String>> rows = new ArrayList<>();
    private List<String> columns = new ArrayList<>();
    private final OnRowSelectListener listener;
    private int selectedRowIndex = -1;

    public TableAdapter(Context context, OnRowSelectListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public void setData(TableData data) {
        columns = new ArrayList<>(data.getColumns());
        rows.clear();
        rows.addAll(data.getRows());
        selectedRowIndex = -1;
        notifyDataSetChanged();
    }

    public void setSelectedRowIndex(int index) {
        selectedRowIndex = index;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_ROW;
    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_table_row, parent, false);
        return new RowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        boolean isHeader = position == 0;
        boolean isEvenRow = position % 2 == 0;
        boolean isSelected = !isHeader && (position - 1) == selectedRowIndex;
        holder.bind(position == 0 ? columns : rows.get(position - 1),
                isHeader,
                isEvenRow,
                isSelected,
                listener,
                isHeader ? -1 : position - 1);
    }

    @Override
    public int getItemCount() {
        return rows.size() + 1; // +1 для заголовка
    }

    static class RowViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout rowContainer;

        RowViewHolder(@NonNull View itemView) {
            super(itemView);
            rowContainer = itemView.findViewById(R.id.rowContainer);
        }

        void bind(List<String> data, boolean isHeader, boolean isEvenRow, boolean isSelected, OnRowSelectListener listener, int rowIndex) {
            rowContainer.removeAllViews();
            for (String cell : data) {
                TextView textView = createCell(rowContainer.getContext(), cell, isHeader, isEvenRow, isSelected);
                rowContainer.addView(textView);
            }
            if (!isHeader && listener != null) {
                itemView.setOnLongClickListener(v -> {
                    listener.onRowSelect(rowIndex, data);
                    return true;
                });
                itemView.setOnClickListener(null);
            } else {
                itemView.setOnLongClickListener(null);
                itemView.setOnClickListener(null);
            }
        }

        private TextView createCell(Context context, String text, boolean isHeader, boolean isEvenRow, boolean isSelected) {
            TextView textView = new TextView(context);
            textView.setText(text);
            textView.setPadding(24, 16, 24, 16);
            textView.setTypeface(null, isHeader ? Typeface.BOLD : Typeface.NORMAL);
            textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

            // Подбираем фон и цвета текста под тёмную тему Supabase
            int backgroundColor = ContextCompat.getColor(context, isHeader
                    ? R.color.supabase_surface_high
                    : isSelected ? R.color.supabase_primary_tint : isEvenRow ? R.color.supabase_surface : R.color.supabase_surface_alt);
            int textColor = ContextCompat.getColor(context, R.color.supabase_on_surface);
            textView.setBackgroundColor(backgroundColor);
            textView.setTextColor(textColor);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 12, 12);
            textView.setLayoutParams(params);
            return textView;
        }
    }

    public interface OnRowSelectListener {
        void onRowSelect(int rowIndex, List<String> rowData);
    }
}
