package com.amnesiawho.sqlviewer.ui.table;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    public TableAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public void setData(TableData data) {
        columns = new ArrayList<>(data.getColumns());
        rows.clear();
        rows.addAll(data.getRows());
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
        holder.bind(position == 0 ? columns : rows.get(position - 1), position == 0);
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

        void bind(List<String> data, boolean isHeader) {
            rowContainer.removeAllViews();
            for (String cell : data) {
                TextView textView = createCell(rowContainer.getContext(), cell, isHeader);
                rowContainer.addView(textView);
            }
        }

        private TextView createCell(Context context, String text, boolean isHeader) {
            TextView textView = new TextView(context);
            textView.setText(text);
            textView.setPadding(24, 16, 24, 16);
            textView.setTypeface(null, isHeader ? Typeface.BOLD : Typeface.NORMAL);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return textView;
        }
    }
}
