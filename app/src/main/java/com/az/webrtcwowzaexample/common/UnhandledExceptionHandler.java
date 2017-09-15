package com.az.webrtcwowzaexample.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;

/**
 * Created by zorin.a on 12.09.2017.
 */

public class UnhandledExceptionHandler implements UncaughtExceptionHandler {
    private static final String TAG = UnhandledExceptionHandler.class.getSimpleName();
    private final WeakReference<Activity> activity;

    public UnhandledExceptionHandler(final Activity activity) {
        this.activity = new WeakReference<Activity>(activity);
    }

    @Override
    public void uncaughtException(Thread unusedThread, final Throwable e) {
        activity.get().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String title = "Fatal error";
                String msg = e.getMessage();
                TextView errorView = new TextView(activity.get());
                errorView.setText(msg);
                errorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                ScrollView scrollingContainer = new ScrollView(activity.get());
                scrollingContainer.addView(errorView);
                Log.e(TAG, title + "\n\n" + msg);
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        System.exit(1);
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(activity.get());
                builder.setTitle(title)
                        .setView(scrollingContainer)
                        .setPositiveButton("Exit", listener)
                        .show();
            }
        });
    }
}
