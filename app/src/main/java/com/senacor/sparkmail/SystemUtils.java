package com.senacor.sparkmail;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by acollinson on 26.07.18.
 */

public final class SystemUtils {

    static boolean isAndroidApiVersion22OrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    static void showShortToast(final String message, Context context) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    static void showLongToast(final String message, Context context) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }
}
