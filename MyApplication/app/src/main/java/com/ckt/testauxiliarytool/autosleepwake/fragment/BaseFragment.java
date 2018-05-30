package com.ckt.testauxiliarytool.autosleepwake.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutResId(), container, false);
        initViews(rootView);
        initListener();
        return rootView;
    }

    /**
     * 子类应当在该方法中初始化数据
     */
    protected abstract void initData(@Nullable Bundle savedInstanceState);

    /**
     * 初始化监听器，子类应在该方法中为控件设置监听器
     */
    protected abstract void initListener();

    /**
     * 初始化控件，子类应在该方法中初始化控件引用
     *
     * @param rootView 加载的布局文件对应的根View
     */
    protected abstract void initViews(View rootView);

    /**
     * 获取布局资源ID
     *
     * @return 布局资源ID
     */
    protected abstract int getLayoutResId();

}
