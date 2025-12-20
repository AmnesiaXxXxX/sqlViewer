package com.amnesiawho.sqlviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amnesiawho.sqlviewer.data.db.DatabaseManager;
import com.amnesiawho.sqlviewer.data.db.DbEngineType;
import com.amnesiawho.sqlviewer.data.db.SchemaInfo;
import com.amnesiawho.sqlviewer.data.db.TableData;
import com.amnesiawho.sqlviewer.data.db.UrlEngineResolver;
import com.amnesiawho.sqlviewer.ui.schema.SchemaAdapter;
import com.amnesiawho.sqlviewer.ui.table.TableAdapter;
import com.amnesiawho.sqlviewer.ui.table.TableListAdapter;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseManager databaseManager;
    private TableAdapter tableAdapter;
    private TableListAdapter tableListAdapter;
    private SchemaAdapter schemaAdapter;
    private MaterialButtonToggleGroup limitToggleGroup;
    private EditText urlInput;
    private TextView engineLabel;
    private TextView selectedSchemaLabel;
    private TextView schemaAccessLabel;
    private TextView selectedTableLabel;
    private Button connectButton;
    private Button addTableButton;
    private View schemaCard;
    private View tableSelectorCard;

    private boolean isConnected = false;
    private String selectedSchema = null;
    private String selectedTable = null;
    private boolean selectedSchemaEditable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализация инфраструктуры
        databaseManager = new DatabaseManager(this);
        setupViews();
        setupConnectionBlock();
        setupLimitSelector();
    }

    private void setupViews() {
        urlInput = findViewById(R.id.urlInput);
        engineLabel = findViewById(R.id.engineLabel);
        connectButton = findViewById(R.id.connectButton);
        limitToggleGroup = findViewById(R.id.limitToggleGroup);
        selectedSchemaLabel = findViewById(R.id.selectedSchemaLabel);
        schemaAccessLabel = findViewById(R.id.schemaAccessLabel);
        selectedTableLabel = findViewById(R.id.selectedTableLabel);
        addTableButton = findViewById(R.id.addTableButton);
        schemaCard = findViewById(R.id.schemaCard);
        tableSelectorCard = findViewById(R.id.tableSelectorCard);
        RecyclerView tableRecycler = findViewById(R.id.tableRecycler);
        RecyclerView schemaRecyclerView = findViewById(R.id.schemaRecycler);
        RecyclerView tableListRecyclerView = findViewById(R.id.tableListRecycler);
        Button loadButton = findViewById(R.id.loadButton);

        tableAdapter = new TableAdapter(this);
        tableRecycler.setLayoutManager(new LinearLayoutManager(this));
        tableRecycler.setAdapter(tableAdapter);

        schemaAdapter = new SchemaAdapter(this::onSchemaSelected);
        schemaRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        schemaRecyclerView.setAdapter(schemaAdapter);

        tableListAdapter = new TableListAdapter(this::onTableSelected);
        tableListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tableListRecyclerView.setAdapter(tableListAdapter);

        // Кнопка загрузки данных
        loadButton.setOnClickListener(v -> loadTable());
        addTableButton.setOnClickListener(v -> handleAddTable());
    }

    private void setupConnectionBlock() {
        connectButton.setOnClickListener(v -> attemptConnection());
        urlInput.setText("sqlite://demo");
        updateEngineLabel(null);
        schemaCard.setVisibility(View.GONE);
        tableSelectorCard.setVisibility(View.GONE);
    }

    private void attemptConnection() {
        String url = urlInput.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Введите URL подключения", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            DbEngineType engineType = UrlEngineResolver.resolve(url);
            databaseManager.switchEngine(engineType);
            updateEngineLabel(engineType);
            Toast.makeText(this, "Подключение успешно: " + engineType.getTitle(), Toast.LENGTH_SHORT).show();
            openSchemaSelection(engineType);
        } catch (IllegalArgumentException ex) {
            updateEngineLabel(null);
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateEngineLabel(DbEngineType engineType) {
        if (engineType == null) {
            engineLabel.setText("Движок будет определён автоматически");
        } else {
            engineLabel.setText("Определён движок: " + engineType.getTitle());
        }
    }

    private void loadTable() {
        if (!isConnected) {
            Toast.makeText(this, "Сначала подключитесь к базе данных", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSchema == null) {
            Toast.makeText(this, "Выберите схему", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTable == null || selectedTable.isEmpty()) {
            Toast.makeText(this, "Выберите таблицу", Toast.LENGTH_SHORT).show();
            return;
        }
        int limit = getSelectedLimit();
        try {
            TableData data = databaseManager.readTable(selectedSchema, selectedTable, limit);
            tableAdapter.setData(data);
            if (data.isEmpty()) {
                Toast.makeText(this, "Данных нет или таблица пуста", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // Пробрасываем ошибку в общий обработчик
            GlobalExceptionHandler.reportHandled(this, e);
        }
    }

    private void loadSchemas() {
        try {
            List<SchemaInfo> schemas = databaseManager.listSchemas();
            schemaAdapter.setItems(schemas);
            schemaAdapter.setSelectedSchema(null);
            schemaCard.setVisibility(View.VISIBLE);
            tableSelectorCard.setVisibility(View.GONE);
            selectedSchema = null;
            selectedTable = null;
            selectedSchemaEditable = false;
            selectedSchemaLabel.setText("Выберите схему");
            schemaAccessLabel.setText("Статус схемы не выбран");
            selectedTableLabel.setText("Таблица не выбрана");
            updateEditingUi();
        } catch (Exception e) {
            schemaCard.setVisibility(View.GONE);
            tableSelectorCard.setVisibility(View.GONE);
            GlobalExceptionHandler.reportHandled(this, e);
        }
    }

    private void onSchemaSelected(SchemaInfo schemaInfo) {
        selectedSchema = schemaInfo.getName();
        selectedSchemaEditable = schemaInfo.isEditable();
        selectedTable = null;
        schemaAdapter.setSelectedSchema(selectedSchema);
        selectedSchemaLabel.setText("Схема: " + selectedSchema);
        selectedTableLabel.setText("Таблица не выбрана");
        updateEditingUi();
        loadTablesForSchema(selectedSchema);
    }

    private void loadTablesForSchema(String schema) {
        try {
            List<String> tables = databaseManager.listTables(schema);
            tableListAdapter.setItems(tables);
            tableSelectorCard.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            tableSelectorCard.setVisibility(View.GONE);
            GlobalExceptionHandler.reportHandled(this, e);
        }
    }

    private void onTableSelected(String tableName) {
        selectedTable = tableName;
        selectedTableLabel.setText("Выбрана таблица: " + tableName);
    }

    private void updateEditingUi() {
        if (selectedSchema == null) {
            schemaAccessLabel.setText("Статус схемы не выбран");
            schemaAccessLabel.setTextColor(getColor(R.color.supabase_on_surface));
            addTableButton.setVisibility(View.GONE);
            return;
        }

        if (selectedSchemaEditable) {
            schemaAccessLabel.setText("Редактирование доступно");
            addTableButton.setVisibility(View.VISIBLE);
            schemaAccessLabel.setTextColor(getColor(R.color.supabase_on_surface));
        } else {
            schemaAccessLabel.setText("Только чтение");
            addTableButton.setVisibility(View.GONE);
            schemaAccessLabel.setTextColor(getColor(R.color.supabase_warning));
        }
    }

    private void handleAddTable() {
        if (!selectedSchemaEditable) {
            Toast.makeText(this, "Редактирование недоступно для выбранной схемы", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Функционал добавления таблиц будет доступен позже", Toast.LENGTH_SHORT).show();
    }

    private void attemptConnection() {
        String url = urlInput.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Введите URL подключения", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            DbEngineType engineType = UrlEngineResolver.resolve(url);
            databaseManager.switchEngine(engineType);
            isConnected = true;
            updateEngineLabel(engineType);
            loadSchemas();
            Toast.makeText(this, "Подключение успешно: " + engineType.getTitle(), Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException ex) {
            isConnected = false;
            updateEngineLabel(null);
            schemaCard.setVisibility(View.GONE);
            tableSelectorCard.setVisibility(View.GONE);
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateEngineLabel(DbEngineType engineType) {
        if (engineType == null) {
            engineLabel.setText("Движок будет определён автоматически");
        } else {
            engineLabel.setText("Определён движок: " + engineType.getTitle());
        }
    }
}
