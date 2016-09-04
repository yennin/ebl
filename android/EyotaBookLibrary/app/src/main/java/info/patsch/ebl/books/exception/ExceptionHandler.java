package info.patsch.ebl.books.exception;

import android.util.Log;

/**
 * Created by patsch on 04.09.16.
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.e(thread.getName(), throwable.getMessage(), throwable);
    }
}
