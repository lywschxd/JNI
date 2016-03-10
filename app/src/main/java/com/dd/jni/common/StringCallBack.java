package com.dd.jni.common;

import android.util.Log;

/**
 * Created by dong on 16/3/10.
 */
public class StringCallBack {
    private static final String TAG = "StringCallBack";

    public static void nativeCallBack(int time) {
        Log.d(TAG, "native time :"+time);
    }
}
