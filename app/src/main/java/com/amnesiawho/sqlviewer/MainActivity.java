package com.amnesiawho.sqlviewer;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseManager databaseManager;
    private TableAdapter tableAdapter;
    private MaterialButtonToggleGroup limitToggleGroup;
    private EditText tableNameInput;
    private Spinner engineSpinner;

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
        setupEngineSpinner();
        setupLimitSelector();

        // Загружаем данные сразу, чтобы пользователь видел демо-таблицу
        loadTable();
    }

    private void setupViews() {
        tableNameInput = findViewById(R.id.tableNameInput);
        engineSpinner = findViewById(R.id.engineSpinner);
        limitToggleGroup = findViewById(R.id.limitToggleGroup);
        RecyclerView tableRecycler = findViewById(R.id.tableRecycler);
        Button loadButton = findViewById(R.id.loadButton);

        tableAdapter = new TableAdapter(this);
        tableRecycler.setLayoutManager(new LinearLayoutManager(this));
        tableRecycler.setAdapter(tableAdapter);

        // Кнопка загрузки данных
        loadButton.setOnClickListener(v -> loadTable());
    }

    private void setupEngineSpinner() {
        // Собираем список заголовков и связываем с перечислением
        List<String> titles = new ArrayList<>();
        for (DbEngineType type : DbEngineType.values()) {
            titles.add(type.getTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, titles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        engineSpinner.setAdapter(adapter);
        engineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DbEngineType selected = DbEngineType.values()[position];
                databaseManager.switchEngine(selected);
                loadTable();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ничего не делаем
            }
        });
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
}
