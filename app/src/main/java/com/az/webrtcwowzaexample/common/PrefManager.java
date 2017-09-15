package com.az.webrtcwowzaexample.common;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by zorin.a on 12.09.2017.
 */

public class PrefManager {

    public static void saveRemoteStreamName(Context context, String talkName) {
        SharedPreferences preferences = context.getSharedPreferences("pref", MODE_PRIVATE);
        preferences.edit().putString("remote_stream", talkName).apply();
    }

    public static String getRemoteStreamName(Context context) {
        return context.getSharedPreferences("pref", MODE_PRIVATE).getString("remote_stream", "");
    }

    public static void saveLocalStreamName(Context context, String talkName) {
        SharedPreferences preferences = context.getSharedPreferences("pref", MODE_PRIVATE);
        preferences.edit().putString("local_stream", talkName).apply();
    }

    public static String getLocalStreamName(Context context) {
        return context.getSharedPreferences("pref", MODE_PRIVATE).getString("local_stream", "");
    }
}
