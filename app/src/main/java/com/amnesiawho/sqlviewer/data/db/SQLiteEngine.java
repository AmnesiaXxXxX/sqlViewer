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

    public SQLiteEngine(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
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
        // Инициализация базы. SQLiteOpenHelper сам управляет соединением.
        getWritableDatabase();
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
    public long insert(String tableName, ContentValues values) {
        return getWritableDatabase().insert(tableName, null, values);
    }

    @Override
    public int update(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        return getWritableDatabase().update(tableName, values, whereClause, whereArgs);
    }

    @Override
    public int delete(String tableName, String whereClause, String[] whereArgs) {
        return getWritableDatabase().delete(tableName, whereClause, whereArgs);
    }

    @Override
    public void close() {
        super.close();
    }
}
