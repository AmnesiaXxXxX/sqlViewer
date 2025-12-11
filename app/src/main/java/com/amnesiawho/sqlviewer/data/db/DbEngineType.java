package com.amnesiawho.sqlviewer.data.db;

/**
 * Список поддерживаемых движков. Реализация общая, детали подключения инкапсулируются в конкретных классах.
 */
public enum DbEngineType {
    SQLITE("SQLite"),
    MYSQL("MySQL"),
    POSTGRESQL("PostgreSQL"),
    SQLSERVER("SQL Server"),
    ORACLE("Oracle");

    private final String title;

    DbEngineType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
