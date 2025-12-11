package com.amnesiawho.sqlviewer.core.exception;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * Общий перехватчик необработанных исключений.
 * Позволяет логировать и уведомлять пользователя, не обрушивая UI.
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Application application;
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private GlobalExceptionHandler(Application application) {
        this.application = application;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    /**
     * Устанавливаем обработчики: для главного потока и фоновых.
     */
    public static void install(Application application) {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(application);
        Thread.setDefaultUncaughtExceptionHandler(handler);
        // Дополнительно страхуем главный поток, чтобы сообщение ушло в лог и в Toast
        Looper.getMainLooper().getThread().setUncaughtExceptionHandler(handler);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logAndToast("Необработанная ошибка: " + e.getMessage());
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(t, e);
        }
    }

    /**
     * Обработка ожидаемых ошибок, которые мы поймали вручную.
     */
    public static void reportHandled(Context context, Throwable throwable) {
        Log.e("SqlViewer", "Перехвачено", throwable);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void logAndToast(String message) {
        Log.e("SqlViewer", message);
        mainHandler.post(() -> Toast.makeText(application, message, Toast.LENGTH_LONG).show());
    }
}
