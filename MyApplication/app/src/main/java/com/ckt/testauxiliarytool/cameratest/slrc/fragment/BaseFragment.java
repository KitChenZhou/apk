package com.ckt.testauxiliarytool.cameratest.slrc.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Cc on 2017/9/27.
 */

public abstract class BaseFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        return inflater.inflate(setLayoutResId(), container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setListener();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recycleObject();
    }

    /**
     * Set layout resource id. eg: R.layout.fragment
     *
     * @return layout resource id.
     */
    abstract int setLayoutResId();

    /**
     * Init view in {@link #onViewCreated(View, Bundle)} method.
     *
     * @param view the view return from {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    abstract void initView(View view);

    /**
     * Set view listener in {@link #onViewCreated(View, Bundle)} method.
     */
    abstract void setListener();

    /**
     * Init data in {@link #onActivityCreated(Bundle)} method.
     */
    abstract void initData();

    /**
     * Recycle in {@link #onDestroy()} method.
     */
    abstract void recycleObject();
}
