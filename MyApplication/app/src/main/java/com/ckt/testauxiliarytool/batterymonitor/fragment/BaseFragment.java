package com.ckt.testauxiliarytool.batterymonitor.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 基类Fragment
 */
public abstract class BaseFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutResourceId(), container, false);
        initData();
        findViews(rootView);
        setListeners();
        return rootView;
    }

    /**
     * 获取布局资源ID
     *
     * @return 布局资源ID
     */
    protected abstract int getLayoutResourceId();

    /**
     * 子类需在该方法中初始化数据
     */
    protected abstract void initData();

    /**
     * 子类在该方法中初始化布局文件中的子View
     *
     * @param rootView 根View
     */
    protected abstract void findViews(View rootView);

    /**
     * 子类在该方法中设置监听器
     */
    protected abstract void setListeners();

}
