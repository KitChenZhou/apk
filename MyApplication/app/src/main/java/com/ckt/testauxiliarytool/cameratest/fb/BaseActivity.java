package com.ckt.testauxiliarytool.cameratest.fb;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.ckt.testauxiliarytool.utils.LogCatchUtil;


public abstract class BaseActivity extends AppCompatActivity {
    protected HandlerThread mHandlerThread;
    protected Handler mChildHandler;
    private boolean mIfSaveLog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //keep screen on
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        String boot_start = intent.getStringExtra(BootBroadcastReceiver.BOOT_START);
        if (boot_start != null
                && boot_start.equals(BootBroadcastReceiver.BOOT_START_VALUE)) {
            //save log
            LogCatchUtil.getInstance().start(this);
            mIfSaveLog = true;
        }
    }

    /**
     * get String value
     *
     * @param id
     * @return
     */
    protected String getStringRes(int id) {
        return getResources().getString(id);
    }

    /**
     * update TextView
     *
     * @param textView
     * @param text
     */
    protected void updateTextView(final TextView textView, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (text != null) {
                    textView.setText(text);
                }
            }
        });
    }

    /**
     * start thread
     */
    protected void startThread() {
        mHandlerThread = new HandlerThread("CameraHelper");
        mHandlerThread.start();
    }

    /**
     * stop thread
     */
    protected void stopThread() {
        if (mHandlerThread != null) {
            try {
                mHandlerThread.quitSafely();
                mHandlerThread.quit();
                mHandlerThread.join();
                mHandlerThread = null;
                mChildHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopThread();
        if (mIfSaveLog) {
            LogCatchUtil.getInstance().stop();
        }
    }

    /**
     * init view when first start
     */
    protected abstract void initView();

    /**
     * define handler
     */
    protected abstract void initHandler();

    /**
     * register BroadcastReceiver
     */
    protected abstract void registerReceiver();

    /**
     * Start CameraActivity when task is done
     */
    protected abstract void jumpToMainActivity();

}
