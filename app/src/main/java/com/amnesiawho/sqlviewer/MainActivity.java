package com.amnesiawho.sqlviewer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
import com.amnesiawho.sqlviewer.data.db.UrlEngineResolver;
import com.amnesiawho.sqlviewer.ui.table.TableAdapter;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class MainActivity extends AppCompatActivity {

    private DatabaseManager databaseManager;
    private TableAdapter tableAdapter;
    private MaterialButtonToggleGroup limitToggleGroup;
    private EditText tableNameInput;
    private EditText urlInput;
    private TextView engineLabel;
    private Button connectButton;
    private boolean isConnected = false;

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
        tableNameInput = findViewById(R.id.tableNameInput);
        urlInput = findViewById(R.id.urlInput);
        engineLabel = findViewById(R.id.engineLabel);
        connectButton = findViewById(R.id.connectButton);
        limitToggleGroup = findViewById(R.id.limitToggleGroup);
        RecyclerView tableRecycler = findViewById(R.id.tableRecycler);
        Button loadButton = findViewById(R.id.loadButton);

        tableAdapter = new TableAdapter(this);
        tableRecycler.setLayoutManager(new LinearLayoutManager(this));
        tableRecycler.setAdapter(tableAdapter);

        // Кнопка загрузки данных
        loadButton.setOnClickListener(v -> loadTable());
    }

    private void setupConnectionBlock() {
        connectButton.setOnClickListener(v -> attemptConnection());
        urlInput.setText("sqlite://demo");
        updateEngineLabel(null);
    }

    private void setupLimitSelector() {
        // Устанавливаем обработчик смены лимита
        limitToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                loadTable();
            }
        });
    }

    private int getSelectedLimit() {
        int checkedId = limitToggleGroup.getCheckedButtonId();
        if (checkedId == R.id.limit100) {
            return 100;
        } else if (checkedId == R.id.limit1000) {
            return 1000;
        } else if (checkedId == R.id.limit1500) {
            return 1500;
        } else if (checkedId == R.id.limitNoLimit) {
            return -1;
        }
        // Значение по умолчанию
        return 100;
    }

    private void loadTable() {
        if (!isConnected) {
            Toast.makeText(this, "Сначала подключитесь к базе данных", Toast.LENGTH_SHORT).show();
            return;
        }
        String tableName = tableNameInput.getText().toString();
        int limit = getSelectedLimit();
        try {
            TableData data = databaseManager.readTable(tableName, limit);
            tableAdapter.setData(data);
            if (data.isEmpty()) {
                Toast.makeText(this, "Данных нет или таблица пуста", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // Пробрасываем ошибку в общий обработчик
            GlobalExceptionHandler.reportHandled(this, e);
        }
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
            Toast.makeText(this, "Подключение успешно: " + engineType.getTitle(), Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException ex) {
            isConnected = false;
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
}
