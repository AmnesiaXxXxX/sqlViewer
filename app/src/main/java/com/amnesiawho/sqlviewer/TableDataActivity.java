package com.amnesiawho.sqlviewer;

import android.os.Bundle;
import android.widget.Button;
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

public class TableDataActivity extends AppCompatActivity {

    private DatabaseManager databaseManager;
    private TableAdapter tableAdapter;
    private MaterialButtonToggleGroup limitToggleGroup;
    private TextView tableTitle;
    private TextView schemaLabel;
    private TextView editabilityLabel;

    private String schemaName;
    private String tableName;
    private boolean schemaEditable;

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
        limitToggleGroup = findViewById(R.id.dataLimitToggleGroup);
        Button reloadButton = findViewById(R.id.reloadTableButton);
        Button rlsButton = findViewById(R.id.rlsButton);

        RecyclerView recyclerView = findViewById(R.id.dataRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tableAdapter = new TableAdapter(this);
        recyclerView.setAdapter(tableAdapter);

        limitToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                loadTable();
            }
        });

        reloadButton.setOnClickListener(v -> loadTable());
        rlsButton.setOnClickListener(v -> Toast.makeText(this, "Политики RLS скоро будут доступны", Toast.LENGTH_SHORT).show());
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
            TableData data = databaseManager.readTable(schemaName, tableName, limit);
            tableAdapter.setData(data);
            if (data.isEmpty()) {
                Toast.makeText(this, "Данных нет или таблица пуста", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            GlobalExceptionHandler.reportHandled(this, e);
            Toast.makeText(this, "Не удалось загрузить данные таблицы", Toast.LENGTH_SHORT).show();
        }
    }
}
