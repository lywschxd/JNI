package com.dd.jni.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.dd.jni.R;
import com.dd.jni.fragment.BaseFragment;
import com.dd.jni.fragment.FirstFragment;
import com.dd.jni.fragment.SecondFragment;
import com.dd.jni.tranformer.ZoomOutPageTransformer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {
    private ViewPager viewPager;
    private FragmentAdapter fragmentAdapter;
    private List<BaseFragment> baseFragments;
    private FirstFragment firstFragment;
    private SecondFragment secondFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), baseFragments);
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
    }

    private void initData() {
        baseFragments = new ArrayList<BaseFragment>();
        firstFragment = new FirstFragment();
        secondFragment = new SecondFragment();
        baseFragments.add(firstFragment);
        baseFragments.add(secondFragment);
    }

}
