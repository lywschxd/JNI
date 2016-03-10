package com.dd.jni.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.dd.jni.R;
import com.dd.jni.common.StringChild;

/**
 * Created by dong on 16/3/7.
 */
public class FirstFragment extends BaseFragment  implements View.OnClickListener{
    private static final String TAG = "FirstFragment";
    private StringChild stringChild;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_page0, null);
        ((Button) view.findViewById(R.id.btn0)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.btn1)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.btn2)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.btn3)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.btn4)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.btn5)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.btn6)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.btn7)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.btn8)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.btn9)).setOnClickListener(this);

        stringChild = new StringChild();
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn0:
                Log.e(TAG, ""+stringChild.print("Hello world"));
                break;
            case R.id.btn1:
                ToastUtil.showToast(getActivity(), "从native取得的属性值为:"+ stringChild.getIntField("count"));
                break;
            case R.id.btn2:
                ToastUtil.showToast(getActivity(), "native 调用前属性值:"+stringChild.getStr());
                Log.d(TAG, "=======>"+stringChild.getStr());
                stringChild.setStringFiled("str");
                ToastUtil.showToast(getActivity(), "native 调用后属性值:"+stringChild.getStr());
                Log.d(TAG, "======>"+stringChild.getStr());
                break;
            case R.id.btn3:
                stringChild.callJavaMethod("echo");
                break;
            case R.id.btn4:
                ToastUtil.showToast(getActivity(), "native 调用前:"+stringChild.toString());
                Log.d(TAG, "========>"+stringChild.toString());
                stringChild.callJavaMethod("setArgs");
                ToastUtil.showToast(getActivity(), "native 调用后:"+stringChild.toString());
                Log.d(TAG, "========>"+stringChild.toString());
                break;
            case R.id.btn5:
                stringChild.callJavaMethod("Fecho");
                break;
            case R.id.btn6:
                stringChild.NewJavaObject("StringChild");
                break;
            case R.id.btn7:
                byte[] nati = stringChild.CallBasisArray("new Basic");
                for (int i = 0; i < nati.length; i++) {
                    Log.d(TAG, "i:"+i+", value:"+nati[i]);
                }
                break;
            case R.id.btn8:
                String[] strs = new String[] {
                        "Hello ",
                        "Jni ",
                        "from ",
                        "Java!"
                };
                stringChild.CallObjectArray("Object", strs);
                for (int i = 0; i < strs.length; i++) {
                    Log.d(TAG, "i:"+i+", str:"+strs[i]);
                }
                break;
            case R.id.btn9:
                try {
                    stringChild.callJavaMethod("thowd");
                }catch (NullPointerException e) {
                    Log.d(TAG, e.getMessage());
                }
                break;
        }
    }

    public static class ToastUtil {
        private static String oldMsg;
        protected static Toast toast   = null;
        private static long oneTime=0;
        private static long twoTime=0;

        private static void showToast(Context context, String s){
            if(toast==null){
                toast =Toast.makeText(context, s, Toast.LENGTH_SHORT);
                toast.show();
                oneTime=System.currentTimeMillis();
            }else{
                twoTime=System.currentTimeMillis();
                if(s.equals(oldMsg)){
                    if(twoTime-oneTime>Toast.LENGTH_SHORT){
                        toast.show();
                    }
                }else{
                    oldMsg = s;
                    toast.setText(s);
                    toast.show();
                }
            }
            oneTime=twoTime;
        }

        public static void showToast(Context context, int resId){
            showToast(context, context.getString(resId));
        }
    }
}
