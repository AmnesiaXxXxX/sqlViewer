package com.amnesiawho.sqlviewer.data.db;

/**
 * Модель описания схемы/пространства имён.
 */
public class SchemaInfo {
    private final String name;
    private final boolean editable;

    public SchemaInfo(String name, boolean editable) {
        this.name = name;
        this.editable = editable;
    }

    public String getName() {
        return name;
    }

    public boolean isEditable() {
        return editable;
    }
}
