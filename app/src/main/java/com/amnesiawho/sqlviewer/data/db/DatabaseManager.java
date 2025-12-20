package com.amnesiawho.sqlviewer.data.db;

import android.content.Context;

import com.amnesiawho.sqlviewer.core.exception.GlobalExceptionHandler;

/**
 * Центральная точка для выбора движка и работы с данными.
 */
public class DatabaseManager {

    private final Context appContext;
    private DatabaseEngine currentEngine;
    private DbEngineType currentType;

    public DatabaseManager(Context context) {
        this.appContext = context.getApplicationContext();
        switchEngine(DbEngineType.SQLITE);
    }

    /**
     * Переключение движка. Здесь можно добавить инициализацию JDBC драйверов при необходимости.
     */
    public void switchEngine(DbEngineType type) {
        currentType = type;
        if (currentEngine != null) {
            currentEngine.close();
        }
        if (type == DbEngineType.SQLITE) {
            currentEngine = new SQLiteEngine(appContext);
        } else {
            currentEngine = new StubEngine(type);
        }
        try {
            currentEngine.connect();
        } catch (Exception e) {
            GlobalExceptionHandler.reportHandled(appContext, e);
        }
    }

    public TableData readTable(String schema, String tableName, int limit) throws Exception {
        return currentEngine.readTable(schema, tableName, limit);
    }

    public java.util.List<SchemaInfo> listSchemas() throws Exception {
        return currentEngine.listSchemas();
    }

    public java.util.List<String> listTables(String schema) throws Exception {
        return currentEngine.listTables(schema);
    }

    public void createTable(String schema, String tableName) throws Exception {
        currentEngine.createTable(schema, tableName);
    }

    public DbEngineType getCurrentType() {
        return currentType;
    }
}
