package com.ckt.testauxiliarytool.tp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ckt.testauxiliarytool.utils.StateChangeManager;

/**
 * <br/>Author: pecuyu
 * <br/>Email: yu.qin@ck-telecom.com
 * <br/>Date: 2017/10/18
 * <br/>TODO: Fragment 抽象基类
 */

public abstract class BaseFragment extends Fragment implements StateChangeManager.OnPublishStateChangeListener {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateChangeManager.getInstance().setPublishStateChangeListener(getTag(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 设置标题
        getActivity().setTitle(getTitleId());
        return initViews(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initEvent();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StateChangeManager.getInstance().removeListener(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            getActivity().setTitle(getTitleId());
        }
        onFragmentHiddenChanged(!hidden);
    }

    /**
     * 当Fragment显示状态改变
     * <p>
     * <br/> called on {@link Fragment#onHiddenChanged(boolean)}
     *
     * @param showing 是否在显示
     */
    public void onFragmentHiddenChanged(boolean showing) {
        // TODO:do some work when fragment show and hide
    }

    /**
     * 初始化事件
     * <br/> called on {@link Fragment#onActivityCreated}
     */
    public void initEvent() {
        // TODO:init the event for your fragment
    }

    /**
     * 初始化数据
     * <br/> called on {@link Fragment#onActivityCreated(Bundle)}
     */
    public void initData() {
        // TODO:init the date for your fragment
    }

    /**
     * 初始化视图
     *
     * @param inflater           LayoutInflater
     * @param container          ViewGroup
     * @param savedInstanceState Bundle
     *                           <br/> called on {@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * @return View
     */
    protected abstract View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * 获取标题id
     * <p>
     * <br/> called on {@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     *
     * @return int
     */
    public abstract int getTitleId();

    @Override
    public void onPublishStateChange() {

    }
}
