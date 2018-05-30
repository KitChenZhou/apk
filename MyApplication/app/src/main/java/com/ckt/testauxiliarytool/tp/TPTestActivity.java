package com.ckt.testauxiliarytool.tp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ckt.testauxiliarytool.BaseActivity;
import com.ckt.testauxiliarytool.MyApplication;
import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.tp.fragments.BaseFragment;
import com.ckt.testauxiliarytool.tp.fragments.CalcDistanceFragment;
import com.ckt.testauxiliarytool.tp.fragments.CalcIMEDistanceFragment;
import com.ckt.testauxiliarytool.tp.fragments.LineationFragment;
import com.ckt.testauxiliarytool.tp.fragments.MainFragment;
import com.ckt.testauxiliarytool.tp.fragments.SizeFragment;
import com.ckt.testauxiliarytool.tp.fragments.VideoManageFragment;
import com.ckt.testauxiliarytool.tp.model.Constant;
import com.ckt.testauxiliarytool.tp.model.ScreenInfo;
import com.ckt.testauxiliarytool.tp.recorder.IOnRecordStateControlListener;
import com.ckt.testauxiliarytool.tp.recorder.IOnRecorderStateChangeCallback;
import com.ckt.testauxiliarytool.tp.recorder.IRecorderController;
import com.ckt.testauxiliarytool.tp.recorder.ScreenRecordConfig;
import com.ckt.testauxiliarytool.tp.recorder.ScreenRecordService;
import com.ckt.testauxiliarytool.utils.ActivityCollector;
import com.ckt.testauxiliarytool.utils.LogUtil;
import com.ckt.testauxiliarytool.utils.PermissionUtils;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;
import com.ckt.testauxiliarytool.utils.SizeUpdateReceiver;
import com.ckt.testauxiliarytool.utils.SizeUtil;
import com.ckt.testauxiliarytool.utils.StateChangeManager;

import java.util.List;


/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/8/16
 * TODO:主活动
 */
public class TPTestActivity extends BaseActivity implements IOnRecorderStateChangeCallback {
    public static final String TAG = TPTestActivity.class.getSimpleName();

    /* screen record */
    private MediaProjectionManager mProjectionManager;
    private RecordServiceConnection mServiceConnection;
    private IRecorderController mRecorderController;

    /* fragment controller */
    private IOnRecordStateControlListener mListener;

    private SizeUpdateReceiver mUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        initEvent();
    }


    protected int getLayoutResId() {
        return R.layout.tp_activity_tptest;
    }

    /*初始化视图*/
    protected void initView() {
        setContentView(getLayoutResId());
        lunchFragment(Constant.TAG_FRAGMENT_MAIN, false);
    }

    /*初始化数据*/
    protected void initData() {
        initScreenInfo();
        initConfig();
    }

    /*处理相关事项*/
    public void initEvent() {
        checkBeforeRequestPermissions();
        bindRecordService();
        registerUpdateReceiver();
    }

    /**
     * 处理按钮状态
     */
    private void dealWithEnableState(boolean enabled) {
        if (!enabled) {
            Toast.makeText(TPTestActivity.this, R.string.unsupport_screen_record, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 开始录屏
     */
    private void startScreenRecord() {
        // 检查权限，并准备请求录屏
        final String[] perms = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        PermissionUtils.requestPermissions(this, Constant.REQUEST_CODE_STORAGE_AND_AUDIO_PERMISSION, perms, new PermissionUtils.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {
                requestRecordScreen();
            }

            @Override
            public void onPermissionDenied(String[] deniedPermissions) {
                StateChangeManager.getInstance().publishStateChange(Constant.TAG_FRAGMENT_MAIN);// 拒绝则重置状态
            }
        });
    }


    /**
     * 停止录屏
     */
    private void stopScreenRecord(View view) {
        if (mRecorderController.isRecording() && Constant.IS_CAN_RECORD) {
            boolean result = mRecorderController.stopRecord();
            if (result) {
                Fragment fragment = getShowingFragment();
                if (fragment != null) {
                    String tag = fragment.getTag();
                    StateChangeManager.getInstance().publishStateChange(tag);
                }
            }
        }
    }

    /**
     * 查询或请求添加悬浮窗的权限
     * 没有授权则跳转至授权页面，请求授权
     */
    private void checkAlertWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, Constant.REQUEST_CODE_OVERLAY_WINDOW);
        }
    }

    /**
     * 绑定录屏服务
     */
    void bindRecordService() {
        if (Constant.IS_CAN_RECORD) {
            // 启动录屏服务
            ScreenRecordService.start(this);
            // 绑定服务
            Intent intent = new Intent(this, ScreenRecordService.class);
            mServiceConnection = new RecordServiceConnection();
            bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        }
    }

    /**
     * 请求录屏
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestRecordScreen() {
        if (Constant.IS_CAN_RECORD) {
            // 通过系统服务获取MediaProjectionManager
            mProjectionManager = (MediaProjectionManager) MyApplication.getContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            // 发送录屏请求，在onActivityResult中处理请求结果
            Intent intent = mProjectionManager.createScreenCaptureIntent();
            startActivityForResult(intent, Constant.REQUEST_CODE_SCREEN_CAPTURE);
        } else {
            Toast.makeText(TPTestActivity.this, R.string.unsupport_screen_record, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 初始化屏幕信息
     */
    private void initScreenInfo() {
        if (SharedPrefsUtil.name("screen_info").getString("screenInfo", null) == null) { // 没有信息则进行初始化
            ScreenInfo screenInfo = new ScreenInfo();
            SizeUtil.getScreenInfo(screenInfo);
            SharedPrefsUtil.name("screen_info").putString("screenInfo", screenInfo.toList());
        }
        SharedPrefsUtil.recycle("screen_info");
    }

    /**
     * 初始化尺寸信息
     */
    private void initConfig() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        // 通过DisplayMetrics得到屏幕宽高及dpi
        ScreenRecordConfig.width = metrics.widthPixels;
        ScreenRecordConfig.height = metrics.heightPixels;
        ScreenRecordConfig.dpi = metrics.densityDpi;
    }


    /* ServiceConnection */
    private class RecordServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i(TPTestActivity.class.getName(), "TPTestActivity#onServiceConnected");
            mRecorderController = (IRecorderController) service;
            mRecorderController.setConfig(ScreenRecordConfig.width, ScreenRecordConfig.height, ScreenRecordConfig.dpi);
            // 设置回调，当服务端状态变化时，会回调相关方法
            mRecorderController.addRecordingCallback(TPTestActivity.this);
            StateChangeManager.getInstance().publishStateChange(Constant.TAG_FRAGMENT_MAIN);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }


    @Override
    public void onRecordStart() {
        notifyStateChange(null);
    }

    @Override
    public void onRecordUpdate(String time) {
        notifyStateChange(time);
    }

    @Override
    public void onRecordStop(boolean error) {
        notifyStateChange(null);
    }

    /**
     * 通知录屏状态变化
     */
    private void notifyStateChange(final String time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Fragment fragment = getShowingFragment();
                if (fragment != null) {
                    String tag = fragment.getTag();
                    StateChangeManager.getInstance().publishStateChange(tag);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constant.REQUEST_CODE_SCREEN_CAPTURE: // 处理启动录屏
                if (resultCode == RESULT_OK) {
                      /* 获取屏幕采集的接口 */
                    MediaProjection mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
                    mRecorderController.setMediaProject(mMediaProjection);
                    mRecorderController.startRecord();
                } else {  // 取消则恢复状态
                    StateChangeManager.getInstance().publishStateChange(Constant.TAG_FRAGMENT_MAIN);
                }
                break;
            case Constant.REQUEST_CODE_OVERLAY_WINDOW:  // 处理浮窗权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) { // 许可了浮窗权限
                        Toast.makeText(getApplicationContext(), R.string.granted_alert_window_perm, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.denied_alert_window_perm, Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    /**
     * 运行时权限检查，检查读写、录音权限
     */
    private boolean checkBeforeRequestPermissions() {
        // 如果请求的权限没有授权，则弹窗提示，反则反之
        boolean check = checkGrantedPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO});
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            check = check && Settings.canDrawOverlays(this);
        }
        if (check) {
            //Toast.makeText(TPTestActivity.this, R.string.all_perms_granted, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.titil_allow_perm)
                    .setMessage(R.string.text_allow_perm)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(TPTestActivity.this, R.string.deny_perms_request_warn, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
            builder.show();
        }
        return false;
    }

    private boolean checkGrantedPermissions(Activity activity, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 请求权限
     */
    private void requestPermissions() {
        final String[] permissions = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        PermissionUtils.requestPermissions(this, Constant.REQUEST_CODE_STORAGE_AND_AUDIO_PERMISSION, permissions, new PermissionUtils.OnPermissionListener() {

            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied(String[] deniedPermissions) {
                Toast.makeText(getApplicationContext(), R.string.notice_perms_denied, Toast.LENGTH_SHORT).show();
            }
        });


        // 浮窗权限
        checkAlertWindowPermission();
    }


    /**
     * 是否正在录屏
     */
    public boolean isRecording() {
        return mRecorderController != null && mRecorderController.isRecording();
    }

    /**
     * 是否允许录屏
     */
    public boolean isCanRecord() {
        return Constant.IS_CAN_RECORD;
    }

    public IRecorderController getRecorderController() {
        return mRecorderController;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tp_menu_tptest_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_open_record_dir_menu:  //  打开录屏目录
                lunchFragment(Constant.TAG_FRAGMENT_VIDEO_LIST);
                break;
            case R.id.id_start_distance_detail:  // 空白测距页
                lunchFragment(Constant.TAG_FRAGMENT_CALC_DISTANCE);
                break;
            case R.id.id_start_size_detail:  // 尺寸页
                lunchFragment(Constant.TAG_FRAGMENT_SIZE);
                break;
            case R.id.id_calc_ime_distance:   // 输入法测距
                lunchFragment(Constant.TAG_FRAGMENT_CALC_IME_DISTANCE);
                break;
            case R.id.id_lineation_page:   // 划线测试页
                lunchFragment(Constant.TAG_FRAGMENT_LINEATION);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 见{@link #lunchFragment(String, boolean)}
     *
     * @param tag {@link String}
     */
    public void lunchFragment(String tag) {
        lunchFragment(tag, true);
    }


    /**
     * 通过tag来启动对应的fragment
     *
     * @param tag  TAG
     * @param anim 是否需要动画
     */
    public void lunchFragment(String tag, boolean anim) {
        Fragment fragment = getFragmentByTag(tag);
        if (fragment != null && (fragment != getShowingFragment())) { // 启动非当前的非空Fragment
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            // replace 时用 ，replace每次都会导致fragment重建，onCreateView被调用
            //if (fm.findFragmentById(R.id.id_fragment_container) instanceof MainFragment) {
            //  ft.addToBackStack(null);  // 当前fragment为MainFragment才添加到回退栈
            //}

            if (anim)   // 设置切换动画
                ft.setCustomAnimations(R.anim.tp_fragment_right_in, R.anim.tp_fragment_left_out, R.anim.tp_fragment_pop_in, R.anim.tp_fragment_pop_out);
            if (!fragment.isAdded())  // 是否已添加
                ft.add(R.id.id_fragment_container, fragment, tag);
            if (getShowingFragment() != null) // 当前显示不为null则隐藏
                ft.hide(getShowingFragment());

            //LogUtil.e(TAG, "lunchFragment");
            ft.show(fragment).commit();
        }
    }


    /**
     * 通过tag来找到对应的fragment
     *
     * @param tag TAG
     * @return Fragment
     */
    public Fragment getFragmentByTag(String tag) {
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (mListener == null) mListener = new MyRecordControlStateChangeListner();

        if (fragment == null) {
            switch (tag) {
                case Constant.TAG_FRAGMENT_MAIN:
                    fragment = MainFragment.newInstance(mListener);
                    break;
                case Constant.TAG_FRAGMENT_CALC_DISTANCE:
                    fragment = new CalcDistanceFragment();
                    break;
                case Constant.TAG_FRAGMENT_SIZE:
                    fragment = new SizeFragment();
                    break;
                case Constant.TAG_FRAGMENT_CALC_IME_DISTANCE:
                    fragment = new CalcIMEDistanceFragment();
                    break;
                case Constant.TAG_FRAGMENT_VIDEO_LIST:
                    fragment = new VideoManageFragment();
                    break;
                case Constant.TAG_FRAGMENT_LINEATION:
                    fragment = new LineationFragment();
                    break;
            }
        }
        return fragment;
    }

    /**
     * 隐藏所有除了指定的
     *
     * @param target Fragment
     */
    private void hideAllFragmentsButThis(Fragment target) {
        FragmentManager ft = getSupportFragmentManager();
        List<Fragment> fragmentList = ft.getFragments();
        if (fragmentList == null) return;

        for (Fragment fragment : fragmentList) {
            if (fragment != null && !fragment.isHidden() && fragment != target)
                ft.beginTransaction().hide(fragment).commit();
        }
    }

    /**
     * 隐藏所有Fragments
     *
     * @param ignoreMain whether or not ignore the main fragment,set true then ignore
     */
    public void hideAllFragments(boolean ignoreMain) {
        FragmentManager ft = getSupportFragmentManager();
        List<Fragment> fragmentList = ft.getFragments();
        if (fragmentList == null) return;

        for (Fragment fragment : fragmentList) {
            boolean ignore = (fragment instanceof MainFragment) && ignoreMain;  // 是否隐藏main
            if (fragment != null && !fragment.isHidden() && !ignore)
                ft.beginTransaction().hide(fragment).commit();
        }
    }

    /**
     * 获取当前显示的Fragment
     *
     * @return Fragment
     */
    public Fragment getShowingFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments == null) return null;

        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible() && !fragment.isHidden()) {
                return fragment;
            }
        }

        return null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Fragment fragment = getShowingFragment();
        if (fragment != null) {
            String tag = fragment.getTag();
            outState.putString(Constant.TAG_SAVE_FRAGMENT_STATE, tag);  // 保存当前页，用于恢复
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // 处理旋转恢复
        MainFragment fragment = (MainFragment) getFragmentByTag(Constant.TAG_FRAGMENT_MAIN);
        if (fragment != null) {
            fragment.setOnRecordControlStateChangeListener(mListener);
            fragment.onPublishStateChange();
        }

        String tag = savedInstanceState.getString(Constant.TAG_SAVE_FRAGMENT_STATE);
        if (tag != null) {   // 恢复之前页
            hideAllFragments(false);
            lunchFragment(tag);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getShowingFragment() instanceof MainFragment) { // 退出
                finish();
            } else {  // 回退到main
                showMain();
            }
            return true;
        }
        return false;
    }

    private void showMain() {
        Fragment showing = getShowingFragment();
        MainFragment main = (MainFragment) getFragmentByTag(Constant.TAG_FRAGMENT_MAIN);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (showing != null) ft.hide(showing);
        if (main != null)
            ft.show(main).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
    }

    /* 录屏控制*/
    private class MyRecordControlStateChangeListner implements IOnRecordStateControlListener {
        @Override
        public void startRecordScreen() {
            TPTestActivity.this.startScreenRecord();
        }

        @Override
        public void stopRecordScreen(View view) {
            TPTestActivity.this.stopScreenRecord(view);
        }

        @Override
        public void notifyEnableState(boolean enabled) {
            dealWithEnableState(enabled);
        }

        @Override
        public boolean isRecording() {
            return TPTestActivity.this.isRecording();
        }

        @Override
        public boolean isCanRecord() {
            return TPTestActivity.this.isCanRecord();
        }

        @Override
        public String getStoreFileAbsolutePath() {
            return TPTestActivity.this.getRecorderController().getStoreFileAbsolutePath();
        }
    }

    private class MyUpdateReceiver extends SizeUpdateReceiver {
        @Override
        public String getAction() {
            return Constant.ACTION_UPDATE_SIZE;
        }

        @Override
        public void doUpdate() {
            // 更新mainFragment
            StateChangeManager.getInstance().publishStateChange(Constant.TAG_FRAGMENT_MAIN);
            // 更新distanceFragment
            StateChangeManager.getInstance().publishStateChange(Constant.TAG_FRAGMENT_CALC_DISTANCE);
            // 更新imeDistanceFragment
            StateChangeManager.getInstance().publishStateChange(Constant.TAG_FRAGMENT_CALC_IME_DISTANCE);
        }
    }


    /**
     * 用于监听用户输入尺
     */
    private void registerUpdateReceiver() {
        if (mUpdateReceiver == null) {
            mUpdateReceiver = new MyUpdateReceiver();
        }
        MyUpdateReceiver.register(mUpdateReceiver, mUpdateReceiver.getAction());
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this); // 移除Activity
        StateChangeManager.getInstance().clearAllStateListeners();
        // 取消注册
        SizeUpdateReceiver.unregister(mUpdateReceiver);
        //  移除监听
        MainFragment fragment = (MainFragment) getFragmentByTag(Constant.TAG_FRAGMENT_MAIN);
        if (fragment != null) {
            fragment.setOnRecordControlStateChangeListener(null);
            mListener = null;
            fragment = null;
        }
        // 取消回调，
        if (mRecorderController != null) {
            // 在没有录屏，且无活动时结束服务
            if (!mRecorderController.isRecording()) {  // 在没有录屏的时候一并停止后台服务
                ScreenRecordService.stop(this);
            }
            mRecorderController.removeRecordingCallback(this);
            mRecorderController = null;
        }

        // 解绑服务
        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }
        LogUtil.e(TAG, TAG + "onDestroy");
    }
}
