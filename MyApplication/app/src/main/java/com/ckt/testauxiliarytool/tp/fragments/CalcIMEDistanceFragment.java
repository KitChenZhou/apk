package com.ckt.testauxiliarytool.tp.fragments;


import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.utils.KeyboardHelper;
import com.ckt.testauxiliarytool.tp.views.DistanceTouchView;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/9/18
 * TODO: IME 测距 Fragment
 */
public class CalcIMEDistanceFragment extends BaseFragment {
    // 尺寸输入
    private EditText mEtInput;
    // 测距View
    private DistanceTouchView mTouchView;
    // Keyboard帮助类
    private KeyboardHelper mKeyboardHelper;

    public CalcIMEDistanceFragment() {
        // empty public constructor
    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle(R.string.title_calc_ime_distance);
        View view = inflater.inflate(R.layout.tp_fragment_calc_imedistance, container, false);
        mTouchView = (DistanceTouchView) view.findViewById(R.id.id_dtv_ime);
        mTouchView.setWhetherDrawRefLine(false, true);
        mEtInput = (EditText) view.findViewById(R.id.editText);
        return view;
    }

    @Override
    public int getTitleId() {
        return R.string.title_calc_ime_distance;
    }

    @Override
    public void initData() {
        super.initData();
        if (mKeyboardHelper == null) {
            mKeyboardHelper = new KeyboardHelper(getContext(), getActivity(), mEtInput);
            mKeyboardHelper.setOnPointerStateChangeListener(new KeyboardHelper.OnPointerStateChangeListener() {
                @Override
                public void onChange(PointF first, PointF second, int pointerCount) {
                    mTouchView.updateDistanceInfo(first, second, pointerCount);
                }
            });
            mKeyboardHelper.registerEditText(mEtInput); // 注册，让EditText关联Keyboard
            mKeyboardHelper.showCustomKeyboard();
        }
    }

    @Override
    public void initEvent() {
        super.initEvent();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消监听、注册
        mKeyboardHelper.unregisterEditText(mEtInput);
        mKeyboardHelper.setOnPointerStateChangeListener(null);
    }

    @Override
    public void onPublishStateChange() {
        if (mTouchView != null) {
            mTouchView.updateSize();
        }
    }
}
