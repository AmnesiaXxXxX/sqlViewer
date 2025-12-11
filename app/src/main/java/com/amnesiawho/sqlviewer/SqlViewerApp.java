package com.amnesiawho.sqlviewer;

import android.app.Application;

import com.amnesiawho.sqlviewer.core.exception.GlobalExceptionHandler;

/**
 * Точка входа приложения: подключаем глобальные обработчики.
 */
public class SqlViewerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GlobalExceptionHandler.install(this);
    }
}
