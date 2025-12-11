package com.amnesiawho.sqlviewer.data.db;

import android.content.ContentValues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Заглушка для движков, требующих отдельного драйвера. Дает единое сообщение, чтобы UI не падал.
 */
public class StubEngine implements DatabaseEngine {

    private final DbEngineType type;
    private boolean connected = false;

    public StubEngine(DbEngineType type) {
        this.type = type;
    }

    @Override
    public void connect() {
        // Здесь могла бы быть логика подключения через JDBC или REST.
        connected = true;
    }

    @Override
    public TableData readTable(String tableName, int limit) {
        // Возвращаем информативную таблицу, чтобы пользователь понимал, что нужно подключить драйвер.
        List<String> columns = Collections.singletonList("Движок " + type.getTitle());
        List<List<String>> rows = new ArrayList<>();
        rows.add(Collections.singletonList("Добавьте драйвер и реальное подключение, чтобы читать таблицу '" + tableName + "'."));
        return new TableData(columns, rows);
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
