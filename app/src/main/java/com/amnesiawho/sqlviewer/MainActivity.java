package com.amnesiawho.sqlviewer;

import android.content.Intent;
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

import com.amnesiawho.sqlviewer.data.db.DatabaseManager;
import com.amnesiawho.sqlviewer.data.db.DbEngineType;
import com.amnesiawho.sqlviewer.data.db.UrlEngineResolver;

public class MainActivity extends AppCompatActivity {

    private DatabaseManager databaseManager;
    private EditText urlInput;
    private TextView engineLabel;
    private Button connectButton;
    private String lastConnectionUrl;

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
    }

    private void setupViews() {
        urlInput = findViewById(R.id.urlInput);
        engineLabel = findViewById(R.id.engineLabel);
        connectButton = findViewById(R.id.connectButton);
    }

    private void setupConnectionBlock() {
        connectButton.setOnClickListener(v -> attemptConnection());
        urlInput.setText("sqlite://demo");
        updateEngineLabel(null);
    }

    private void attemptConnection() {
        String url = urlInput.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Введите URL подключения", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            DbEngineType engineType = UrlEngineResolver.resolve(url);
            databaseManager.switchEngine(engineType, url);
            updateEngineLabel(engineType);
            Toast.makeText(this, "Подключение успешно: " + engineType.getTitle(), Toast.LENGTH_SHORT).show();
            lastConnectionUrl = url;
            openSchemaSelection(engineType, url);
        } catch (IllegalArgumentException ex) {
            updateEngineLabel(null);
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        } catch (UnsupportedOperationException ex) {
            updateEngineLabel(null);
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IllegalStateException ex) {
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

    private void openSchemaSelection(DbEngineType engineType, String connectionUrl) {
        Intent intent = new Intent(this, SchemaSelectionActivity.class);
        intent.putExtra(SchemaSelectionActivity.EXTRA_ENGINE_TYPE, engineType.name());
        intent.putExtra(SchemaSelectionActivity.EXTRA_CONNECTION_URL, connectionUrl);
        startActivity(intent);
    }
}
