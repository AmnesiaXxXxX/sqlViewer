package com.amnesiawho.sqlviewer.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Простая реализация движка для локальной SQLite.
 */
public class SQLiteEngine extends SQLiteOpenHelper implements DatabaseEngine {

    private static final String DB_NAME = "sqlviewer_local.db";
    private static final int DB_VERSION = 1;
    private static final String DEMO_TABLE = "demo_users";
    private final String customPath;
    private SQLiteDatabase externalDb;

    public SQLiteEngine(Context context, String connectionUrl) {
        super(context, resolveDbName(connectionUrl), null, DB_VERSION);
        this.customPath = resolveCustomPath(connectionUrl);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создаем демо-таблицу, чтобы пользователь сразу видел данные
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DEMO_TABLE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "created_at TEXT NOT NULL" +
                ");");

        // Заполняем демонстрационными данными
        ContentValues values = new ContentValues();
        values.put("name", "Администратор");
        values.put("role", "admin");
        values.put("created_at", "2024-01-01");
        db.insert(DEMO_TABLE, null, values);

        values.clear();
        values.put("name", "Гость");
        values.put("role", "viewer");
        values.put("created_at", "2024-02-10");
        db.insert(DEMO_TABLE, null, values);

        values.clear();
        values.put("name", "Разработчик");
        values.put("role", "developer");
        values.put("created_at", "2024-03-20");
        db.insert(DEMO_TABLE, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Для демонстрации просто пересоздаем таблицу
        db.execSQL("DROP TABLE IF EXISTS " + DEMO_TABLE);
        onCreate(db);
    }

    @Override
    public void connect() {
        if (customPath != null) {
            java.io.File dbFile = new java.io.File(customPath);
            if (!dbFile.exists()) {
                throw new IllegalArgumentException("База данных не найдена по пути: " + customPath);
            }
            externalDb = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        } else {
            // Инициализация базы. SQLiteOpenHelper сам управляет соединением.
            getWritableDatabase();
        }
    }

    @Override
    public TableData readTable(String schema, String tableName, int limit) {
        List<String> columns = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(tableName, null, null, null, null, null, null, limit > 0 ? String.valueOf(limit) : null);
        try {
            if (cursor == null) {
                return new TableData(columns, rows);
            }
            // Собираем названия столбцов
            String[] columnNames = cursor.getColumnNames();
            for (String columnName : columnNames) {
                columns.add(columnName);
            }

            // Считываем построчно, приводя значения к строкам
            while (cursor.moveToNext()) {
                List<String> row = new ArrayList<>();
                for (int i = 0; i < columnNames.length; i++) {
                    row.add(cursor.getString(i));
                }
                rows.add(row);
            }
        } catch (Exception e) {
            Log.e("SQLiteEngine", "Ошибка чтения таблицы", e);
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return new TableData(columns, rows);
    }

    @Override
    public List<SchemaInfo> listSchemas() {
        List<SchemaInfo> schemas = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("PRAGMA database_list;", null);
        try {
            int nameIndex = cursor.getColumnIndexOrThrow("name");
            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIndex);
                boolean editable = "main".equalsIgnoreCase(name);
                schemas.add(new SchemaInfo(name, editable));
            }
        } finally {
            cursor.close();
        }

        if (schemas.isEmpty()) {
            schemas.add(new SchemaInfo("main", true));
            schemas.add(new SchemaInfo("temp", false));
        }
        return schemas;
    }

    @Override
    public List<String> listTables(String schema) {
        List<String> tables = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String masterTable;
        if (schema == null || schema.isEmpty()) {
            masterTable = "sqlite_master";
        } else if ("temp".equalsIgnoreCase(schema)) {
            masterTable = "sqlite_temp_master";
        } else {
            masterTable = schema + ".sqlite_master";
        }
        Cursor cursor = db.rawQuery(
                "SELECT name FROM " + masterTable + " WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name;",
                null
        );
        try {
            int nameIndex = cursor.getColumnIndexOrThrow("name");
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(nameIndex));
            }
        } finally {
            cursor.close();
        }

        if (tables.isEmpty()) {
            tables.add(DEMO_TABLE);
        }
        return tables;
    }

    @Override
    public void createTable(String schema, String tableName) {
        validateTableName(tableName);
        String qualifiedName = buildQualifiedName(schema, tableName);
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS " + qualifiedName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                ");");
    }

    @Override
    public long insert(String schema, String tableName, ContentValues values) {
        String qualifiedName = buildQualifiedName(schema, tableName);
        return getWritableDatabase().insert(qualifiedName, null, values);
    }

    @Override
    public int update(String schema, String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        String qualifiedName = buildQualifiedName(schema, tableName);
        return getWritableDatabase().update(qualifiedName, values, whereClause, whereArgs);
    }

    @Override
    public int delete(String schema, String tableName, String whereClause, String[] whereArgs) {
        String qualifiedName = buildQualifiedName(schema, tableName);
        return getWritableDatabase().delete(qualifiedName, whereClause, whereArgs);
    }

    @Override
    public void close() {
        if (externalDb != null && externalDb.isOpen()) {
            externalDb.close();
        } else {
            super.close();
        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return externalDb != null ? externalDb : super.getReadableDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return externalDb != null ? externalDb : super.getWritableDatabase();
    }

    private void validateTableName(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Название таблицы не может быть пустым");
        }
        if (!tableName.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Используйте латинские буквы, цифры и подчёркивания, начиная с буквы");
        }
    }

    private String buildQualifiedName(String schema, String tableName) {
        if (schema == null || schema.isEmpty() || "main".equalsIgnoreCase(schema)) {
            return tableName;
        }
        if ("temp".equalsIgnoreCase(schema)) {
            return "temp." + tableName;
        }
        return schema + "." + tableName;
    }

    private static String resolveDbName(String connectionUrl) {
        if (connectionUrl == null || connectionUrl.trim().isEmpty()) {
            return DB_NAME;
        }
        java.net.URI uri = java.net.URI.create(connectionUrl.trim());
        String path = uri.getPath();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return DB_NAME;
        }
        String fileName = new java.io.File(path).getName();
        return fileName.isEmpty() ? DB_NAME : fileName;
    }

    private static String resolveCustomPath(String connectionUrl) {
        if (connectionUrl == null || connectionUrl.trim().isEmpty()) {
            return null;
        }
        java.net.URI uri = java.net.URI.create(connectionUrl.trim());
        String path = uri.getPath();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return null;
        }
        return path;
    }
}
