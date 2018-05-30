package com.ckt.testauxiliarytool.tp.fragments;


import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.tp.model.ScreenInfo;
import com.ckt.testauxiliarytool.utils.LogUtil;
import com.ckt.testauxiliarytool.utils.SizeUtil;

import java.util.Locale;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/9/18
 * TODO:Size Fragment
 */

public class SizeFragment extends BaseFragment {

    private TextView mTvWidth;  // width px
    private TextView mTvHeight; // height px
    private TextView mTvDpi;  // dpi
    private TextView mTvDensity;  // density
    private TextView mTvRealWidth; // width ,mm
    private TextView mTvRealHeight; // height , mm
    private TextView mTvXdpi;       // x_ppi
    private TextView mTvYdpi;       // y_ppi

    private Button mBtnRefetch;

    public SizeFragment() {
        // Required empty public constructor
    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tp_fragment_size, container, false);
        mTvWidth = (TextView) view.findViewById(R.id.id_tv_width_size);
        mTvHeight = (TextView) view.findViewById(R.id.id_tv_height_size);
        mTvDpi = (TextView) view.findViewById(R.id.id_tv_dpi_size);
        mTvDensity = (TextView) view.findViewById(R.id.id_tv_density_size);
        mTvRealWidth = (TextView) view.findViewById(R.id.id_tv_real_width_size);
        mTvRealHeight = (TextView) view.findViewById(R.id.id_tv_real_height_size);
        mTvXdpi = (TextView) view.findViewById(R.id.id_tv_xdpi_size);
        mTvYdpi = (TextView) view.findViewById(R.id.id_tv_ydpi_size);
        mBtnRefetch = (Button) view.findViewById(R.id.id_btn_refetch);

        return view;
    }

    @Override
    public int getTitleId() {
        return R.string.title_size_fragment;
    }


    @Override
    public void initData() {
        super.initData();
        getScreenSizeAndShow();
    }

    @Override
    public void initEvent() {
        super.initEvent();
        mBtnRefetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreenSizeAndShow();
            }
        });
    }

    /**
     * 获取并显示尺寸
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    void getScreenSizeAndShow() {
        ScreenInfo screenInfo = new ScreenInfo();
        SizeUtil.getScreenInfo(screenInfo);

        int widthPixels = screenInfo.widthPixels;
        int heightPixels = screenInfo.heightPixels;
        int densityDpi = screenInfo.densityDpi;   // dots-per-inch
        float density = screenInfo.density;
        float realW = screenInfo.realWidth;
        float realH = screenInfo.realHeight;
        float xdpi = screenInfo.xdpi;
        float ydpi = screenInfo.ydpi;
        double screenInche = screenInfo.screenInche;

        mTvWidth.setText(String.format("widthPixels=%s px", widthPixels));
        mTvHeight.setText(String.format("heightPixels=%s px", heightPixels));
        mTvDpi.setText(String.format(Locale.CHINA, "densityDpi=%d dots/inch", densityDpi));
        mTvDensity.setText(String.format(Locale.CHINA, "density=%f", density));

        mTvRealWidth.setText("realWidth=" + realW + " mm");
        mTvRealHeight.setText("realHeight=" + realH + " mm");
//        mTvXdpi.setText(String.format("xdpi=%f px/inch", xdpi));
//        mTvYdpi.setText(String.format("ydpi=%f px/inch", ydpi));

        LogUtil.i("TAG", screenInfo.toString());
    }



}
