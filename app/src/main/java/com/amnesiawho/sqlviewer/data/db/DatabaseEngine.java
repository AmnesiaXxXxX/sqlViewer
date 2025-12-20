package com.amnesiawho.sqlviewer.data.db;

import android.content.ContentValues;

import java.util.List;

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
     * @param schema    имя схемы или пространства, где находится таблица
     * @param tableName имя таблицы
     * @param limit     лимит строк, -1 если лимита нет
     */
    TableData readTable(String schema, String tableName, int limit) throws Exception;

    /**
     * Список доступных схем/пространств имен.
     */
    List<SchemaInfo> listSchemas() throws Exception;

    /**
     * Возвращает список таблиц в выбранной схеме.
     */
    List<String> listTables(String schema) throws Exception;

    /**
     * Создает новую таблицу с базовыми колонками.
     *
     * @param schema    имя схемы/пространства, может быть null/пустым для движков без схем
     * @param tableName имя таблицы
     */
    void createTable(String schema, String tableName) throws Exception;

    long insert(String schema, String tableName, ContentValues values) throws Exception;

    int update(String schema, String tableName, ContentValues values, String whereClause, String[] whereArgs) throws Exception;

    int delete(String schema, String tableName, String whereClause, String[] whereArgs) throws Exception;

    void close();
}
