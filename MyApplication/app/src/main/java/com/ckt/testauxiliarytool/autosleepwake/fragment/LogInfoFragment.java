package com.ckt.testauxiliarytool.autosleepwake.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.autosleepwake.interfaces.Constants;
import com.ckt.testauxiliarytool.utils.FileUtils;

import java.lang.ref.WeakReference;

/**
 * 显示Log详情信息的Fragment，点击log列表后进入
 */
public class LogInfoFragment extends BaseFragment {
    private String mFilePath;
    private TextView mLogDetailTv;
    private UIUpdateHandler mUIUpdateHandler;

    @Override
    protected void initData(@Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mFilePath = arguments.getString(Constants.FILE_PATH);
        }
    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void initViews(View rootView) {
        mLogDetailTv = (TextView) rootView.findViewById(R.id.tv_log_detail);
        mUIUpdateHandler = new UIUpdateHandler(this);
        new ReadLogThread(this).start();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.asw_fragment_log_info;
    }

    private static class UIUpdateHandler extends Handler {
        private WeakReference<LogInfoFragment> mLogInfoFragmentRef;

        UIUpdateHandler(LogInfoFragment logInfoFragment) {
            mLogInfoFragmentRef = new WeakReference<>(logInfoFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            LogInfoFragment logInfoFragment = mLogInfoFragmentRef.get();
            logInfoFragment.mLogDetailTv.setText((String) msg.obj);
        }
    }

    private static class ReadLogThread extends Thread {
        private WeakReference<LogInfoFragment> mLogInfoFragmentRef;

        ReadLogThread(LogInfoFragment logInfoFragment) {
            mLogInfoFragmentRef = new WeakReference<>(logInfoFragment);
        }

        @Override
        public void run() {
            LogInfoFragment logInfoFragment = mLogInfoFragmentRef.get();
            if (logInfoFragment != null) {
                String logInfo = FileUtils.readFile2String(logInfoFragment.mFilePath);
                Message message = new Message();
                message.obj = logInfo;
                logInfoFragment.mUIUpdateHandler.sendMessage(message);
            }
        }
    }
}
