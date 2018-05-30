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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.tp.model.Constant;
import com.ckt.testauxiliarytool.tp.recorder.IOnRecordStateControlListener;
import com.ckt.testauxiliarytool.utils.AnimUtil;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;
import com.ckt.testauxiliarytool.tp.views.DistanceTouchView;
import com.ckt.testauxiliarytool.tp.views.RecordButton;

import static com.ckt.testauxiliarytool.utils.UiUtil.updateFullscreenStatus;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/9/18
 * TODO:主Fragment
 */
public class MainFragment extends BaseFragment {
    private RecordButton mBtnRecord;
    private FloatingActionButton mBtnInput;
    private DistanceTouchView mTouchView;
    private LinearLayout mBtnContaniner;
    private long mLastTime;
    private IOnRecordStateControlListener mListener;

    public MainFragment() {
        // empty public constructor
    }

    public static MainFragment newInstance(IOnRecordStateControlListener listener) {
        MainFragment fragment = new MainFragment();
        fragment.setOnRecordControlStateChangeListener(listener);
        return fragment;
    }


    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tp_fragment_main, container, false);
        mBtnRecord = (RecordButton) view.findViewById(R.id.id_cbv_record_fragment_main);
        mBtnInput = (FloatingActionButton) view.findViewById(R.id.id_floating_btn_fragment_main);
        mTouchView = (DistanceTouchView) view.findViewById(R.id.id_dtv_fragment_main);
        mBtnContaniner = (LinearLayout) view.findViewById(R.id.id_btn_container_fragment_main);

        return view;
    }

    @Override
    public int getTitleId() {
        return R.string.tp_module_name;
    }


    @Override
    public void initEvent() {
        super.initEvent();
        mTouchView.setOnTouchStateChangeListener(new DistanceTouchView.OnTouchStateChangeListener() {
            @Override
            public void onTouchDown() {
                // 使功能按钮飞出，并隐藏标题栏、导航栏，达到全屏状态
                AnimUtil.getInstance().animateOut(mBtnContaniner);
                if (getActivity() instanceof AppCompatActivity)
                    if (System.currentTimeMillis() - mLastTime > 800) {
                        mLastTime = System.currentTimeMillis();
                        updateFullscreenStatus((AppCompatActivity) getActivity(), true);
                    }
            }

            @Override
            public void onTouchUp() {
                // 恢复
                AnimUtil.getInstance().animateIn(mBtnContaniner);
                if (getActivity() instanceof AppCompatActivity)
                    updateFullscreenStatus((AppCompatActivity) getActivity(), false);
            }
        });


        mBtnRecord.setOnClickStateChangeListener(new RecordButton.OnStateChangeListener() {
            @Override
            public void onStart() {
                // 检查权限，并准备请求录屏
                if (mListener != null) mListener.startRecordScreen();
            }

            @Override
            public void onStop() {
                if (mListener != null) mListener.stopRecordScreen(mBtnRecord);
            }

            @Override
            public void onEnabled(boolean enabled) {
                if (mListener != null) mListener.notifyEnableState(enabled);
            }
        });

        mBtnInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog(false);
            }
        });
    }


    /**
     * 状态改变时调用
     */
    private void notifyStateChange() {
        if (mListener != null && mBtnRecord != null) {
            mBtnRecord.setEnabled(mListener.isCanRecord());       // 重置录屏状态
            mBtnRecord.setToState(mListener.isRecording());
        }
    }

    /**
     * 更新尺寸
     */
    private void doUpdateSize() {
        if (mTouchView != null) {
            mTouchView.updateSize();
        }
    }

    /**
     * 显示输入对话框
     *
     * @param hasCache true则需要考虑是否有本地缓存，否则不考虑
     */
    private void showInputDialog(final boolean hasCache) {
        String screen_width = SharedPrefsUtil.name("screen_info").getString("screen_width", null);
        String screen_height = SharedPrefsUtil.name("screen_info").getString("screen_height", null);
        if (hasCache && screen_width != null && screen_height != null) {
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
                .setPositiveButton(R.string.comfirm, null)
                .setNegativeButton(R.string.cancel, null)
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


    /* 通知输入发生改变 */
    private void notifyInputSizeChange() {
        Intent intent = new Intent(Constant.ACTION_UPDATE_SIZE);
        getActivity().sendBroadcast(intent);
    }

    /* 设置录屏状态控制监听 */
    public void setOnRecordControlStateChangeListener(IOnRecordStateControlListener listener) {
        this.mListener = listener;
    }


    @Override
    public void onResume() {
        super.onResume();

        // 防止在DistanceActivity输入了尺寸而在MainActivity中未更新
        doUpdateSize();
    }

    @Override
    public void onFragmentHiddenChanged(boolean showing) {
        super.onFragmentHiddenChanged(showing);
        if (showing) {
            if (mListener != null) {
                //  恢复录屏按钮的状态
                mBtnRecord.setToState(mListener.isRecording());
            }
        }
    }

    @Override
    public void onPublishStateChange() {
        super.onPublishStateChange();
        doUpdateSize();
        notifyStateChange();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBtnRecord != null) {
            mBtnRecord.setOnClickStateChangeListener(null);
            mBtnRecord = null;
        }

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
