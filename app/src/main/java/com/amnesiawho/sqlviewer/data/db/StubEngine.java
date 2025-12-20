package com.amnesiawho.sqlviewer.data.db;

import android.content.ContentValues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Заглушка для движков, требующих отдельного драйвера. Дает единое сообщение, чтобы UI не падал.
 */
public class StubEngine implements DatabaseEngine {

    private final DbEngineType type;
    private boolean connected = false;
    private final Map<String, List<String>> schemaTables = new HashMap<>();

    public StubEngine(DbEngineType type) {
        this.type = type;
        schemaTables.put("sandbox", new ArrayList<>(Collections.singletonList("draft_table")));
        schemaTables.put("public", new ArrayList<>(java.util.Arrays.asList("users", "events", "metrics")));
        schemaTables.put("analytics", new ArrayList<>(Collections.singletonList("reports")));
    }

    @Override
    public void connect() {
        // Здесь могла бы быть логика подключения через JDBC или REST.
        connected = true;
    }

    @Override
    public TableData readTable(String schema, String tableName, int limit) {
        // Возвращаем информативную таблицу, чтобы пользователь понимал, что нужно подключить драйвер.
        List<String> columns = Collections.singletonList("Движок " + type.getTitle());
        List<List<String>> rows = new ArrayList<>();
        rows.add(Collections.singletonList("Добавьте драйвер и реальное подключение, чтобы читать таблицу '" + tableName + "'."));
        return new TableData(columns, rows);
    }

    @Override
    public List<SchemaInfo> listSchemas() {
        List<SchemaInfo> schemas = new ArrayList<>();
        schemas.add(new SchemaInfo("public", false));
        schemas.add(new SchemaInfo("sandbox", true));
        schemas.add(new SchemaInfo("analytics", false));
        return schemas;
    }

    @Override
    public List<String> listTables(String schema) {
        String key = schema == null ? "" : schema.toLowerCase();
        List<String> tables = schemaTables.get(key);
        if (tables != null) {
            return new ArrayList<>(tables);
        }
        return new ArrayList<>(Collections.singletonList("example_table"));
    }

    @Override
    public void createTable(String schema, String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Название таблицы не может быть пустым");
        }
        String key = schema == null ? "" : schema.toLowerCase();
        List<String> tables = schemaTables.computeIfAbsent(key, s -> new ArrayList<>());
        if (!tables.contains(tableName)) {
            tables.add(tableName);
        }
    }

    @Override
    public long insert(String tableName, ContentValues values) {
        throw new UnsupportedOperationException("CRUD недоступен для движка " + type.getTitle());
    }

    @Override
    public int update(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        throw new UnsupportedOperationException("CRUD недоступен для движка " + type.getTitle());
    }

    @Override
    public int delete(String tableName, String whereClause, String[] whereArgs) {
        throw new UnsupportedOperationException("CRUD недоступен для движка " + type.getTitle());
    }

    @Override
    public void close() {
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }
}
