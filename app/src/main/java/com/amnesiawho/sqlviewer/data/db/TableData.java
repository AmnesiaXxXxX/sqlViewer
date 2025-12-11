package com.amnesiawho.sqlviewer.data.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Простая модель для хранения табличных данных.
 */
public class TableData {
    private final List<String> columns;
    private final List<List<String>> rows;

    public TableData(List<String> columns, List<List<String>> rows) {
        // Создаем новые коллекции, чтобы защититься от внешних изменений
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>(rows);
    }

    public List<String> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public List<List<String>> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }
}
