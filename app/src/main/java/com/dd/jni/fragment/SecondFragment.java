package com.dd.jni.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dd.jni.R;
import com.dd.jni.common.StringChild;

/**
 * Created by dong on 16/3/7.
 */
public class SecondFragment extends BaseFragment implements View.OnClickListener{
    private static final String TAG = "SecondFragment";
    StringChild stringChild = null;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_page1, null);

        ((Button)view.findViewById(R.id.btn0)).setOnClickListener(this);
        ((Button)view.findViewById(R.id.btn1)).setOnClickListener(this);

        stringChild = new StringChild();
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn0:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 100;
                        while (i-- > 0) {
                            synchronized (stringChild) { //防止线程入侵
                                Log.e("TAG", "begin[=====" + "name:thread 2" + ", value:" + stringChild.getNativeValue());
                                stringChild.nativeMinusOne();
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                stringChild.nativeMinusOne();
                                Log.e("TAG", "end[" + "name:thread 2" + ", value:" + stringChild.getNativeValue());
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 100;
                        while (i-- > 0) {
                            synchronized (stringChild) {    //防止线程入侵
                                Log.e("TAG", "begin[" + "name:thread 1" + ", value:" + stringChild.getNativeValue());
                                stringChild.nativeAddOne();
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                stringChild.nativeAddOne();
                                Log.e("TAG", "end[" + "name:thread 1" + ", value:" + stringChild.getNativeValue());
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                break;
            case R.id.btn1:
                stringChild.createNativeThread();
                break;
        }

    }
}
