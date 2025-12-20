package com.amnesiawho.sqlviewer.data.db;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Утилита для определения движка базы данных по URL.
 */
public final class UrlEngineResolver {

    private UrlEngineResolver() {
    }

    /**
     * Определяет движок по схеме URL. Поддерживает JDBC префикс.
     *
     * @param url строка подключения
     * @return тип движка
     * @throws IllegalArgumentException если схема не распознана или URL некорректен
     */
    public static DbEngineType resolve(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL подключения не должен быть пустым");
        }
        String normalized = url.trim();
        if (normalized.toLowerCase(Locale.ROOT).startsWith("jdbc:")) {
            normalized = normalized.substring(5);
        }
        URI uri;
        try {
            uri = new URI(normalized);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Некорректный формат URL: " + e.getMessage());
        }

        String scheme = uri.getScheme();
        if (scheme == null || scheme.isEmpty()) {
            throw new IllegalArgumentException("URL должен содержать схему (например, sqlite, postgres)");
        }

        switch (scheme.toLowerCase(Locale.ROOT)) {
            case "sqlite":
                return DbEngineType.SQLITE;
            case "mysql":
            case "mariadb":
                return DbEngineType.MYSQL;
            case "postgres":
            case "postgresql":
                return DbEngineType.POSTGRESQL;
            case "sqlserver":
            case "mssql":
                return DbEngineType.SQLSERVER;
            case "oracle":
                return DbEngineType.ORACLE;
            default:
                throw new IllegalArgumentException("Неизвестная схема URL: " + scheme);
        }
    }
}
