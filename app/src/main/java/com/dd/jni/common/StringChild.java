package com.dd.jni.common;

import android.util.Log;

/**
 * Created by dong on 16/3/7.
 */
public class StringChild extends StringFather {
    public static native void initNative();
    static {
        System.loadLibrary("string-handler");
        initNative();
    }

    private static String TAG = "StringChild";
    private int count = 15;
    private String str = "Hello world";
    private float num = 4.5f;

    public String getStr() {
        return str;
    }

    public float getNum() {
        return num;
    }

    public void setArgs(int c, String s, float b) {
        count = c;
        str = s;
        num = b;
    }

    @Override
    public String toString() {
        return "count:"+count+", str:"+str+", num:"+num;
    }

    public void throwd() {
        throw new NullPointerException();
    }

    @Override
    public void echo() {
        Log.e("string-handle", "this is StringChild class");
    }

    public native int print(String str);
    public native int getIntField(String attr);
    public native void setStringFiled(String attr);
    public native void callJavaMethod(String method);
    public native void NewJavaObject(String obj);
    public native byte[] CallBasisArray(String type);
    public native void CallObjectArray(String type, String[] array) throws NullPointerException;

    public native void nativeAddOne();
    public native void nativeMinusOne();
    public native int getNativeValue();

    public native void createNativeThread();
}
