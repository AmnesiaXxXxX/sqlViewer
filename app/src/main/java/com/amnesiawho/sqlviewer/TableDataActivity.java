package com.amnesiawho.sqlviewer;

import android.os.Bundle;
import android.content.ContentValues;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amnesiawho.sqlviewer.core.exception.GlobalExceptionHandler;
import com.amnesiawho.sqlviewer.data.db.DatabaseManager;
import com.amnesiawho.sqlviewer.data.db.DbEngineType;
import com.amnesiawho.sqlviewer.data.db.TableData;
import com.amnesiawho.sqlviewer.ui.table.TableAdapter;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TableDataActivity extends AppCompatActivity {

    private DatabaseManager databaseManager;
    private TableAdapter tableAdapter;
    private MaterialButtonToggleGroup limitToggleGroup;
    private TextView tableTitle;
    private TextView schemaLabel;
    private TextView editabilityLabel;
    private TextView selectedRowLabel;
    private Button addRowButton;
    private Button editRowButton;
    private Button deleteRowButton;
    private View crudButtonsRow;

    private String schemaName;
    private String tableName;
    private boolean schemaEditable;
    private TableData currentData;
    private int selectedRowIndex = -1;
    private java.util.List<String> selectedRowData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_table_data);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tableDataRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        schemaName = getIntent().getStringExtra(TableSelectionActivity.EXTRA_SCHEMA_NAME);
        schemaEditable = getIntent().getBooleanExtra(TableSelectionActivity.EXTRA_SCHEMA_EDITABLE, false);
        tableName = getIntent().getStringExtra(TableSelectionActivity.EXTRA_SELECTED_TABLE);
        String engineTypeName = getIntent().getStringExtra(TableSelectionActivity.EXTRA_ENGINE_TYPE);
        String connectionUrl = getIntent().getStringExtra(TableSelectionActivity.EXTRA_CONNECTION_URL);

        if (schemaName == null || tableName == null || engineTypeName == null) {
            Toast.makeText(this, "Не удалось открыть данные таблицы", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.switchEngine(DbEngineType.valueOf(engineTypeName), connectionUrl);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupViews();
        bindHeader();
        loadTable();
    }

    private void setupViews() {
        tableTitle = findViewById(R.id.tableNameTitle);
        schemaLabel = findViewById(R.id.dataSchemaLabel);
        editabilityLabel = findViewById(R.id.dataSchemaAccessLabel);
        selectedRowLabel = findViewById(R.id.selectedRowLabel);
        crudButtonsRow = findViewById(R.id.crudButtonsRow);
        addRowButton = findViewById(R.id.addRowButton);
        editRowButton = findViewById(R.id.editRowButton);
        deleteRowButton = findViewById(R.id.deleteRowButton);
        limitToggleGroup = findViewById(R.id.dataLimitToggleGroup);
        Button reloadButton = findViewById(R.id.reloadTableButton);
        Button rlsButton = findViewById(R.id.rlsButton);

        RecyclerView recyclerView = findViewById(R.id.dataRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tableAdapter = new TableAdapter(this, this::onRowSelected);
        recyclerView.setAdapter(tableAdapter);

        limitToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                loadTable();
            }
        });

        reloadButton.setOnClickListener(v -> loadTable());
        rlsButton.setOnClickListener(v -> Toast.makeText(this, "Политики RLS скоро будут доступны", Toast.LENGTH_SHORT).show());

        addRowButton.setOnClickListener(v -> showRowDialog(false));
        editRowButton.setOnClickListener(v -> showRowDialog(true));
        deleteRowButton.setOnClickListener(v -> confirmDelete());
        applyCrudVisibility();
    }

    private void bindHeader() {
        tableTitle.setText("Таблица: " + tableName);
        schemaLabel.setText("Схема: " + schemaName);
        editabilityLabel.setText(schemaEditable ? "Редактирование доступно" : "Только чтение");
        editabilityLabel.setTextColor(getColor(schemaEditable ? R.color.supabase_on_surface : R.color.supabase_warning));
    }

    private int resolveLimit() {
        int checkedId = limitToggleGroup.getCheckedButtonId();
        if (checkedId == R.id.dataLimit1000) {
            return 1000;
        } else if (checkedId == R.id.dataLimit1500) {
            return 1500;
        } else if (checkedId == R.id.dataLimitUnlimited) {
            return -1;
        }
        return 100;
    }

    private void loadTable() {
        int limit = resolveLimit();
        try {
            currentData = databaseManager.readTable(schemaName, tableName, limit);
            tableAdapter.setData(currentData);
            clearSelection();
            if (currentData.isEmpty()) {
                Toast.makeText(this, "Данных нет или таблица пуста", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            GlobalExceptionHandler.reportHandled(this, e);
            Toast.makeText(this, "Не удалось загрузить данные таблицы", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyCrudVisibility() {
        crudButtonsRow.setVisibility(schemaEditable ? View.VISIBLE : View.GONE);
        addRowButton.setEnabled(schemaEditable);
        editRowButton.setEnabled(false);
        deleteRowButton.setEnabled(false);
    }

    private void onRowSelected(int rowIndex, java.util.List<String> rowData) {
        selectedRowIndex = rowIndex;
        selectedRowData = new java.util.ArrayList<>(rowData);
        selectedRowLabel.setText("Выбрана строка #" + (rowIndex + 1));
        tableAdapter.setSelectedRowIndex(rowIndex);
        editRowButton.setEnabled(schemaEditable);
        deleteRowButton.setEnabled(schemaEditable);
    }

    private void clearSelection() {
        selectedRowIndex = -1;
        selectedRowData = null;
        selectedRowLabel.setText("Строка не выбрана");
        tableAdapter.setSelectedRowIndex(-1);
        editRowButton.setEnabled(false);
        deleteRowButton.setEnabled(false);
    }

    private void showRowDialog(boolean isEdit) {
        if (!schemaEditable) {
            Toast.makeText(this, "Редактирование недоступно", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentData == null || currentData.getColumns().isEmpty()) {
            Toast.makeText(this, "Нет данных для редактирования", Toast.LENGTH_SHORT).show();
            return;
        }
        int idIndex = findIdColumnIndex();
        if (isEdit && (selectedRowIndex < 0 || selectedRowData == null)) {
            Toast.makeText(this, "Выберите строку для изменения", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isEdit && idIndex < 0) {
            Toast.makeText(this, "Редактирование невозможно: отсутствует столбец id", Toast.LENGTH_LONG).show();
            return;
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding / 2, padding, 0);

        java.util.List<TextInputEditText> inputs = new java.util.ArrayList<>();
        for (int i = 0; i < currentData.getColumns().size(); i++) {
            String column = currentData.getColumns().get(i);
            if ("id".equalsIgnoreCase(column)) {
                continue;
            }
            TextInputLayout layout = new TextInputLayout(this, null, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox);
            layout.setHint(column);
            TextInputEditText editText = new TextInputEditText(layout.getContext());
            layout.addView(editText);
            if (isEdit && selectedRowData != null && i < selectedRowData.size()) {
                editText.setText(selectedRowData.get(i));
            }
            inputs.add(editText);
            container.addView(layout);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(isEdit ? "Изменить строку" : "Добавить строку")
                .setView(container)
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .setPositiveButton(isEdit ? "Сохранить" : "Добавить", (dialog, which) -> {
                    ContentValues values = new ContentValues();
                    int inputIndex = 0;
                    for (String column : currentData.getColumns()) {
                        if ("id".equalsIgnoreCase(column)) {
                            continue;
                        }
                        String value = inputs.get(inputIndex).getText() != null ? inputs.get(inputIndex).getText().toString() : "";
                        values.put(column, value);
                        inputIndex++;
                    }
                    if (isEdit) {
                        updateRow(idIndex, values);
                    } else {
                        addRow(values);
                    }
                })
                .show();
    }

    private void addRow(ContentValues values) {
        try {
            databaseManager.insert(schemaName, tableName, values);
            Toast.makeText(this, "Строка добавлена", Toast.LENGTH_SHORT).show();
            loadTable();
        } catch (Exception e) {
            GlobalExceptionHandler.reportHandled(this, e);
            Toast.makeText(this, "Не удалось добавить строку", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRow(int idIndex, ContentValues values) {
        if (selectedRowData == null || idIndex >= selectedRowData.size()) {
            Toast.makeText(this, "Невозможно обновить строку", Toast.LENGTH_SHORT).show();
            return;
        }
        String idValue = selectedRowData.get(idIndex);
        try {
            databaseManager.update(schemaName, tableName, values, "id = ?", new String[]{idValue});
            Toast.makeText(this, "Строка обновлена", Toast.LENGTH_SHORT).show();
            loadTable();
        } catch (Exception e) {
            GlobalExceptionHandler.reportHandled(this, e);
            Toast.makeText(this, "Не удалось обновить строку", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        int idIndex = findIdColumnIndex();
        if (selectedRowData == null || selectedRowIndex < 0) {
            Toast.makeText(this, "Выберите строку для удаления", Toast.LENGTH_SHORT).show();
            return;
        }
        if (idIndex < 0) {
            Toast.makeText(this, "Удаление невозможно: отсутствует столбец id", Toast.LENGTH_LONG).show();
            return;
        }
        String idValue = selectedRowData.get(idIndex);
        new MaterialAlertDialogBuilder(this)
                .setTitle("Удалить строку?")
                .setMessage("Это действие необратимо.")
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Удалить", (dialog, which) -> deleteRow(idValue))
                .show();
    }

    private void deleteRow(String idValue) {
        try {
            databaseManager.delete(schemaName, tableName, "id = ?", new String[]{idValue});
            Toast.makeText(this, "Строка удалена", Toast.LENGTH_SHORT).show();
            loadTable();
        } catch (Exception e) {
            GlobalExceptionHandler.reportHandled(this, e);
            Toast.makeText(this, "Не удалось удалить строку", Toast.LENGTH_SHORT).show();
        }
    }

    private int findIdColumnIndex() {
        if (currentData == null || currentData.getColumns().isEmpty()) {
            return -1;
        }
        for (int i = 0; i < currentData.getColumns().size(); i++) {
            if ("id".equalsIgnoreCase(currentData.getColumns().get(i))) {
                return i;
            }
        }
        return -1;
    }
}
