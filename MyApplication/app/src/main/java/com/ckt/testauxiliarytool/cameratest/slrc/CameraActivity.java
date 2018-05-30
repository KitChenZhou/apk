package com.ckt.testauxiliarytool.cameratest.slrc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.cameratest.capturetask.CaptureTaskFragment;
import com.ckt.testauxiliarytool.cameratest.capturetask.Params;
import com.ckt.testauxiliarytool.cameratest.common.ConstVar;
import com.ckt.testauxiliarytool.cameratest.slrc.fragment.CloseMessageDialog;
import com.ckt.testauxiliarytool.cameratest.slrc.fragment.SLRCFragment;
import com.ckt.testauxiliarytool.cameratest.slrc.fragment.StartFragment;
import com.ckt.testauxiliarytool.cameratest.slrc.model.IConstValue;
import com.ckt.testauxiliarytool.utils.LogUtil;

import java.lang.reflect.Field;

public class CameraActivity extends CameraBaseActivity {

    private static final String CLOSE_DIALOG_FRAGMENT = "CloseDialogFragment";

    private DrawerLayout mDrawerLayout;

    private SLRCFragment mSLRCFragment;

    private CaptureTaskFragment mCaptureTaskFragment;

    /**
     * Show dialog when press BACK button.
     */
    private CloseMessageDialog mCloseMessageDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().replace(R.id.start_fragment_container,
                    StartFragment.newInstance()).commit();
        }

        if (null == savedInstanceState && !checkPermissions()) {
            mSLRCFragment = SLRCFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,
                    mSLRCFragment).commit();
        }

        Params.setGlobalContext(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCloseMessageDialog != null && mCloseMessageDialog.isVisible()) {
            mCloseMessageDialog.dismiss();
            mCloseMessageDialog = null;
        }

    }

    @Override
    int getLayoutResId() {
        return R.layout.ct_activity_main;
    }

    @Override
    void initData() {

        /*
         * When task is done, start CameraActivity and toast the message
         */
        Intent intent = getIntent();
        String done = intent.getStringExtra(ConstVar.TEST_DONE);
        if (done != null) {
            switch (done) {
                case ConstVar.START_CAMERA_ACTIVITY:
                    Toast.makeText(this, getResources().getString(R.string
                            .ct_start_camera_test_done), Toast.LENGTH_SHORT).show();
                    break;
                case ConstVar.UNUSUALLY_EXIT_CAMERA:
                    Toast.makeText(this, getResources().getString(R.string
                            .ct_unusually_exit_camera_test_done), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    void initView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    }

    @Override
    void setListener() {
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

                if (mSLRCFragment != null && mSLRCFragment.isResumed()) {
                    mSLRCFragment.onSwitchRadioButtonChecked("start");
                }

                if (mCaptureTaskFragment != null && mCaptureTaskFragment.isResumed()) {
                    mCaptureTaskFragment.onSwitchRadioButtonChecked("start");
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {

                closeKeyboard();

                if (mSLRCFragment != null && mSLRCFragment.isResumed()) {
                    mSLRCFragment.onSwitchRadioButtonChecked("main");
                }

                if (mCaptureTaskFragment != null && mCaptureTaskFragment.isResumed()) {
                    mCaptureTaskFragment.onSwitchRadioButtonChecked("main");
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        setDrawerLeftEdgeSize(this, mDrawerLayout, 0.8f);
    }

    /**
     * 设置侧滑起始的距离
     */
    private void setDrawerLeftEdgeSize(Activity activity, DrawerLayout drawerLayout, float
            displayWidthPercentage) {
        if (activity == null || drawerLayout == null) return;
        try {
            // 找到 ViewDragHelper 并设置 Accessible 为true
            Field leftDraggerField = drawerLayout.getClass().getDeclaredField("mLeftDragger");
            leftDraggerField.setAccessible(true);
            ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);

            // 找到 edgeSizeField 并设置 Accessible 为true
            Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(leftDragger);

            // 设置新的边缘大小
            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            edgeSizeField.setInt(leftDragger, Math.max(edgeSize, (int) (displaySize.x *
                    displayWidthPercentage)));
        } catch (NoSuchFieldException e) {
            LogUtil.e(IConstValue.TAG, "Could not get such field!");
        } catch (IllegalArgumentException e) {
            LogUtil.e(IConstValue.TAG, e.toString());
        } catch (IllegalAccessException e) {
            LogUtil.e(IConstValue.TAG, e.toString());
        }
    }

    @Override
    void doAfterGrantPermission() {
        replaceSLRCFragment();
    }

    /**
     * Replace a new SLRCFragment.
     */
    private void replaceSLRCFragment() {
        mSLRCFragment = SLRCFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,
                mSLRCFragment).commit();
    }

    /**
     * It first replace fragment if SLRCFragment is not exits. Then start SLRCFragment.SLRCThread
     *
     * @param resId to do which function.
     * @param times will do times.
     */
    public void startSLRCThread(int resId, int times) {

        if (mSLRCFragment == null || !mSLRCFragment.isResumed()) replaceSLRCFragment();

        if (mDrawerLayout.isDrawerOpen(Gravity.START)) mDrawerLayout.closeDrawers();

        mSLRCFragment.startSLRCThread(resId, times);
    }

    public void replaceFragment(CaptureTaskFragment fragment) {
        mCaptureTaskFragment = fragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,
                fragment, CaptureTaskFragment.TAG).commitAllowingStateLoss();
    }

    private void closeKeyboard() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            if (inputMethodManager != null && inputMethodManager.isActive())
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean whetherCanBeClosed() {
        if (mSLRCFragment != null && mSLRCFragment.isResumed() && !mSLRCFragment.getManageCamera
                ().isPreview()) {
            return false;
        } else if (mCaptureTaskFragment != null) {
            if (Params.get(ConstVar.CAPTURE_TASK_STATUS, ConstVar.CAPTURE_TASK_NONE) != ConstVar
                    .CAPTURE_TASK_NONE) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mCloseMessageDialog == null) {
                mCloseMessageDialog = CloseMessageDialog.newInstance();
            }

            if (!mCloseMessageDialog.isVisible()) {

                mCloseMessageDialog.show(getSupportFragmentManager(), CLOSE_DIALOG_FRAGMENT);
            }

            return false;
        }

        return super.onKeyDown(keyCode, event);
    }

}
