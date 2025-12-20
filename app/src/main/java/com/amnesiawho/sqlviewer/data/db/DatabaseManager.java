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
        switchEngine(DbEngineType.SQLITE, null);
    }

    /**
     * Переключение движка. Здесь можно добавить инициализацию JDBC драйверов при необходимости.
     */
    public void switchEngine(DbEngineType type, String connectionUrl) {
        currentType = type;
        if (currentEngine != null) {
            currentEngine.close();
        }
        if (type == DbEngineType.SQLITE) {
            currentEngine = new SQLiteEngine(appContext, connectionUrl);
        } else {
            throw new UnsupportedOperationException("Подключение к " + type.getTitle() + " пока не поддержано — требуется реальный драйвер");
        }
        try {
            currentEngine.connect();
        } catch (Exception e) {
            GlobalExceptionHandler.reportHandled(appContext, e);
            throw new IllegalStateException("Не удалось подключиться к базе: " + e.getMessage(), e);
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

    public long insert(String schema, String tableName, android.content.ContentValues values) throws Exception {
        return currentEngine.insert(schema, tableName, values);
    }

    public int update(String schema, String tableName, android.content.ContentValues values, String whereClause, String[] whereArgs) throws Exception {
        return currentEngine.update(schema, tableName, values, whereClause, whereArgs);
    }

    public int delete(String schema, String tableName, String whereClause, String[] whereArgs) throws Exception {
        return currentEngine.delete(schema, tableName, whereClause, whereArgs);
    }

    public DbEngineType getCurrentType() {
        return currentType;
    }
}
