package com.amnesiawho.sqlviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.amnesiawho.sqlviewer.data.db.SchemaInfo;
import com.amnesiawho.sqlviewer.ui.schema.SchemaAdapter;

import java.util.List;

public class SchemaSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_ENGINE_TYPE = "engine_type";
    public static final String EXTRA_CONNECTION_URL = "connection_url";

    private DatabaseManager databaseManager;
    private SchemaAdapter schemaAdapter;
    private TextView engineTitleLabel;
    private DbEngineType engineType;
    private String connectionUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schema_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.schemaSelectionRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String engineTypeName = getIntent().getStringExtra(EXTRA_ENGINE_TYPE);
        connectionUrl = getIntent().getStringExtra(EXTRA_CONNECTION_URL);
        if (engineTypeName == null) {
            Toast.makeText(this, "Не удалось определить подключение", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        engineType = DbEngineType.valueOf(engineTypeName);
        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.switchEngine(engineType, connectionUrl);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupViews();
        loadSchemas();
    }

    private void setupViews() {
        engineTitleLabel = findViewById(R.id.schemaEngineLabel);
        engineTitleLabel.setText("Движок: " + engineType.getTitle());

        RecyclerView schemaRecycler = findViewById(R.id.schemaSelectionRecycler);
        schemaAdapter = new SchemaAdapter(this::onSchemaSelected);
        schemaRecycler.setLayoutManager(new LinearLayoutManager(this));
        schemaRecycler.setAdapter(schemaAdapter);
    }

    private void loadSchemas() {
        try {
            List<SchemaInfo> schemas = databaseManager.listSchemas();
            schemaAdapter.setItems(schemas);
            schemaAdapter.setSelectedSchema(null);
            findViewById(R.id.schemaSelectionCard).setVisibility(View.VISIBLE);
        } catch (Exception e) {
            findViewById(R.id.schemaSelectionCard).setVisibility(View.GONE);
            GlobalExceptionHandler.reportHandled(this, e);
            Toast.makeText(this, "Не удалось загрузить схемы", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSchemaSelected(SchemaInfo schemaInfo) {
        schemaAdapter.setSelectedSchema(schemaInfo.getName());
        Intent intent = new Intent(this, TableSelectionActivity.class);
        intent.putExtra(TableSelectionActivity.EXTRA_SCHEMA_NAME, schemaInfo.getName());
        intent.putExtra(TableSelectionActivity.EXTRA_SCHEMA_EDITABLE, schemaInfo.isEditable());
        intent.putExtra(TableSelectionActivity.EXTRA_ENGINE_TYPE, engineType.name());
        intent.putExtra(TableSelectionActivity.EXTRA_CONNECTION_URL, connectionUrl);
        startActivity(intent);
    }
}
