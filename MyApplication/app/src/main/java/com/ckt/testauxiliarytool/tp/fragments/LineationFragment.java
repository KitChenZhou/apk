package com.ckt.testauxiliarytool.tp.fragments;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.utils.UiUtil;
import com.ckt.testauxiliarytool.tp.views.LineationView;

import static com.ckt.testauxiliarytool.utils.UiUtil.updateFullscreenStatus;

/**
 * <br/>Author: pecuyu
 * <br/>Email: yu.qin@ck-telecom.com
 * <br/>Date: 2017/10/18
 * <br/>TODO: 划线测试页
 */

public class LineationFragment extends BaseFragment {
    private LineationView mLineationView;

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 设置全屏
        updateFullscreenStatus((AppCompatActivity) getActivity(), true);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.tp_fragment_lineation, container, false);
        mLineationView = (LineationView) view.findViewById(R.id.id_distance_touch_view_lineation);
        return view;
    }

    @Override
    public int getTitleId() {
        return R.string.lineation_page;
    }

    @Override
    public void initEvent() {
        super.initEvent();
        mLineationView.setOnLineationTouchListener(new LineationView.OnLineationTouchListener() {
            @Override
            public void onTouchDown() {
                UiUtil.hideNavigationBar(getActivity());
            }

            @Override
            public void onTouchUp() {
            //    UiUtil.showNavigationBar(getActivity());
            }
        });
    }

    @Override
    public void onFragmentHiddenChanged(boolean showing) {
        super.onFragmentHiddenChanged(showing);
        if (showing) { // 设置全屏
            updateFullscreenStatus((AppCompatActivity) getActivity(), true);
        } else {
            updateFullscreenStatus((AppCompatActivity) getActivity(), false);
        }
    }


}
