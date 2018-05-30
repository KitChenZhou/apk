package com.ckt.testauxiliarytool.autosleepwake.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ckt.testauxiliarytool.autosleepwake.fragment.factory.FragmentFactory;


public class FragmentAdapter extends FragmentPagerAdapter {
    private String[] mTabTitles;

    public FragmentAdapter(FragmentManager fm, String[] tabTitles) {
        super(fm);
        mTabTitles = tabTitles;
    }

    @Override
    public Fragment getItem(int position) {
        return FragmentFactory.newFragment(position);
    }

    @Override
    public int getCount() {
        return mTabTitles == null ? 0 : mTabTitles.length;
    }

    // 重写该方法用于TabLayout显示标题
    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitles[position];
    }
}
