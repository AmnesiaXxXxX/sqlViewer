package com.amnesiawho.sqlviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.amnesiawho.sqlviewer.ui.table.TableListAdapter;

import java.util.List;

public class TableSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_SCHEMA_NAME = "schema_name";
    public static final String EXTRA_SCHEMA_EDITABLE = "schema_editable";
    public static final String EXTRA_ENGINE_TYPE = "engine_type";

    public static final String EXTRA_SELECTED_TABLE = "selected_table";

    private DatabaseManager databaseManager;
    private TableListAdapter tableListAdapter;
    private TextView selectedSchemaLabel;
    private TextView schemaAccessLabel;
    private TextView selectedTableLabel;
    private Button addTableButton;
    private Button openTableButton;

    private String schemaName;
    private boolean schemaEditable;
    private String selectedTable;
    private DbEngineType engineType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_table_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tableSelectionRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        schemaName = getIntent().getStringExtra(EXTRA_SCHEMA_NAME);
        schemaEditable = getIntent().getBooleanExtra(EXTRA_SCHEMA_EDITABLE, false);
        String engineTypeName = getIntent().getStringExtra(EXTRA_ENGINE_TYPE);

        if (schemaName == null || engineTypeName == null) {
            Toast.makeText(this, "Не удалось открыть список таблиц", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        engineType = DbEngineType.valueOf(engineTypeName);
        databaseManager = new DatabaseManager(this);
        databaseManager.switchEngine(engineType);

        setupViews();
        bindSchemaInfo();
        loadTables();
    }

    private void setupViews() {
        selectedSchemaLabel = findViewById(R.id.selectedSchemaLabel);
        schemaAccessLabel = findViewById(R.id.schemaAccessLabel);
        selectedTableLabel = findViewById(R.id.selectedTableLabel);
        addTableButton = findViewById(R.id.addTableButton);
        openTableButton = findViewById(R.id.openTableButton);

        RecyclerView tableListRecyclerView = findViewById(R.id.tableListRecycler);

        tableListAdapter = new TableListAdapter(this::onTableSelected);
        tableListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tableListRecyclerView.setAdapter(tableListAdapter);

        openTableButton.setOnClickListener(v -> openTableScreen());
        addTableButton.setOnClickListener(v -> handleAddTable());
    }

    private void bindSchemaInfo() {
        selectedSchemaLabel.setText("Схема: " + schemaName);
        schemaAccessLabel.setText(schemaEditable ? "Редактирование доступно" : "Только чтение");
        schemaAccessLabel.setTextColor(getColor(schemaEditable ? R.color.supabase_on_surface : R.color.supabase_warning));
        addTableButton.setVisibility(schemaEditable ? View.VISIBLE : View.GONE);
        openTableButton.setEnabled(false);
    }

    private void loadTables() {
        try {
            List<String> tables = databaseManager.listTables(schemaName);
            tableListAdapter.setItems(tables);
            selectedTableLabel.setText("Таблица не выбрана");
            openTableButton.setEnabled(false);
        } catch (Exception e) {
            GlobalExceptionHandler.reportHandled(this, e);
            Toast.makeText(this, "Не удалось загрузить таблицы", Toast.LENGTH_SHORT).show();
        }
    }

    private void onTableSelected(String tableName) {
        selectedTable = tableName;
        selectedTableLabel.setText("Выбрана таблица: " + tableName);
        openTableButton.setEnabled(true);
    }

    private void handleAddTable() {
        if (!schemaEditable) {
            Toast.makeText(this, "Редактирование недоступно для выбранной схемы", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Функционал добавления таблиц будет доступен позже", Toast.LENGTH_SHORT).show();
    }

    private void openTableScreen() {
        if (selectedTable == null) {
            Toast.makeText(this, "Выберите таблицу", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, TableDataActivity.class);
        intent.putExtra(EXTRA_SCHEMA_NAME, schemaName);
        intent.putExtra(EXTRA_SCHEMA_EDITABLE, schemaEditable);
        intent.putExtra(EXTRA_ENGINE_TYPE, engineType.name());
        intent.putExtra(EXTRA_SELECTED_TABLE, selectedTable);
        startActivity(intent);
    }
}
