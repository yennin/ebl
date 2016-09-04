package info.patsch.ebl.books.exception;

import android.util.Log;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.e(thread.getName(), throwable.getMessage(), throwable);
    }
}
