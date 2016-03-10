package com.dd.jni.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.dd.jni.fragment.BaseFragment;

import java.util.List;

/**
 * Created by dong on 16/3/7.
 */
public class FragmentAdapter extends FragmentPagerAdapter {
    private List<BaseFragment> baseFragments;

    public FragmentAdapter(FragmentManager fm, List<BaseFragment> list) {
        super(fm);
        baseFragments = list;
    }

    @Override
    public Fragment getItem(int position) {
        return baseFragments.get(position);
    }

    @Override
    public int getCount() {
        return baseFragments.size();
    }
}
