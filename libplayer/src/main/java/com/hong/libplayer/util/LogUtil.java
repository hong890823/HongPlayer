package com.hong.libplayer.util;

import android.util.Log;

public class LogUtil {
    private static final String TAG = "HongPlayer";

    public static void logD(String msg){
        Log.d(TAG, msg);
    }

    public static void logE(String msg){
        Log.e(TAG, msg);
    }
}
