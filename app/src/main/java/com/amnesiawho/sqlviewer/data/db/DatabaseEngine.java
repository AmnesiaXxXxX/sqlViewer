package com.amnesiawho.sqlviewer.data.db;

import android.content.ContentValues;

/**
 * Базовый контракт CRUD-операций для разных движков БД.
 */
public interface DatabaseEngine {
    /**
     * Подготовка подключения. Для SQLite это инициализация файла, для удаленных БД — подключение по сети.
     */
    void connect() throws Exception;

    /**
     * Чтение таблицы с ограничением по строкам.
     *
     * @param tableName имя таблицы
     * @param limit     лимит строк, -1 если лимита нет
     */
    TableData readTable(String tableName, int limit) throws Exception;

    long insert(String tableName, ContentValues values) throws Exception;

    int update(String tableName, ContentValues values, String whereClause, String[] whereArgs) throws Exception;

    int delete(String tableName, String whereClause, String[] whereArgs) throws Exception;

    void close();
}
