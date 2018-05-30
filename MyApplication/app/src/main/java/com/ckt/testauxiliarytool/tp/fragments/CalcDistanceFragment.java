package com.ckt.testauxiliarytool.tp.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.tp.model.Constant;
import com.ckt.testauxiliarytool.utils.AnimUtil;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;
import com.ckt.testauxiliarytool.utils.UiUtil;
import com.ckt.testauxiliarytool.tp.views.DistanceTouchView;

import static com.ckt.testauxiliarytool.utils.UiUtil.updateFullscreenStatus;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/9/18
 * TODO:空白测距页
 */
public class CalcDistanceFragment extends BaseFragment implements DistanceTouchView.OnTouchStateChangeListener {

    private FloatingActionButton mBtnInput;

    private DistanceTouchView mTouchView;
    private long mLastTime;

    public CalcDistanceFragment() {
        // empty public constructor
    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tp_fragment_calc_distance, container, false);
        mTouchView = (DistanceTouchView) view.findViewById(R.id.id_distance_touch_view);
        mBtnInput = (FloatingActionButton) view.findViewById(R.id.id_floating_btn);
        return view;
    }

    @Override
    public void initEvent() {
        super.initEvent();
        mTouchView.setOnTouchStateChangeListener(this);
        mBtnInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog(false);
            }
        });
    }

    @Override
    public int getTitleId() {
        return R.string.titile_calc_distance;
    }

    /**
     * 显示输入对话框
     *
     * @param auto true 则需要考虑是否有本地缓存，否则不考虑
     */
    private void showInputDialog(final boolean auto) {
        String screen_width = SharedPrefsUtil.name("screen_info").getString("screen_width", null);
        String screen_height = SharedPrefsUtil.name("screen_info").getString("screen_height", null);
        if (auto && screen_width != null && screen_height != null) {
            return;
        }
        // 自定义对话框视图
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.tp_layout_dialog_intput_wh, null);
        final EditText etWidth = (EditText) view.findViewById(R.id.id_et_input_width);
        final EditText etHeight = (EditText) view.findViewById(R.id.id_et_input_height);
        etWidth.setText(screen_width);
        etHeight.setText(screen_height);
        //创建对话框
        showDialog(view, etWidth, etHeight);
    }

    private void showDialog(View view, final EditText etWidth, final EditText etHeight) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.input_screen_size)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String width = etWidth.getText().toString().trim();
                        String height = etHeight.getText().toString().trim();
                        if (TextUtils.isEmpty(width) || TextUtils.isEmpty(height)) {
                            Toast.makeText(getActivity(), "input should not be null", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        SharedPrefsUtil.name("screen_info").putString("screen_width", width).putString("screen_height", height).recycle();

                        Snackbar.make(mBtnInput, R.string.input_success, Snackbar.LENGTH_SHORT).show();
                        mTouchView.updateSize();
                        notifyInputSizeChange();
                        alertDialog.dismiss();
                    }
                });
            }
        });
        alertDialog.show();
    }

    /**
     * 发送
     */
    private void notifyInputSizeChange() {
        Intent intent = new Intent(Constant.ACTION_UPDATE_SIZE);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onTouchDown() {
        AnimUtil.getInstance().animateOut(mBtnInput);
        if (getActivity() instanceof AppCompatActivity)
            if (System.currentTimeMillis() - mLastTime > 800) {
                mLastTime = System.currentTimeMillis();
                updateFullscreenStatus((AppCompatActivity) getActivity(), true);
            }
    }

    @Override
    public void onTouchUp() {
        AnimUtil.getInstance().animateIn(mBtnInput);
        if (getActivity() instanceof AppCompatActivity)
            UiUtil.updateFullscreenStatus((AppCompatActivity) getActivity(), false);
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden)
            getActivity().setTitle(getString(R.string.titile_calc_distance));
    }


    @Override
    public void onPublishStateChange() {
        super.onPublishStateChange();
        if (mTouchView != null) {
            mTouchView.updateSize();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBtnInput != null) {
            mBtnInput.setOnClickListener(null);
            mBtnInput = null;
        }

        if (mTouchView != null) {
            mTouchView.setOnTouchStateChangeListener(null);
            mTouchView = null;
        }
    }
}
