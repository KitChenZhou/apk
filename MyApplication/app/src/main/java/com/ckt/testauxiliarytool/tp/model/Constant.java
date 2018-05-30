package com.ckt.testauxiliarytool.tp.model;

import android.os.Build;

import com.ckt.testauxiliarytool.MyApplication;
import com.ckt.testauxiliarytool.utils.MyConstants;

import java.io.File;

/**
 * <br/>Author: pecuyu
 * <br/>Email: yu.qin@ck-telecom.com
 * <br/>Date: 2017/10/27
 * <br/>TODO: 一些常量
 */

public final class Constant {
    //--------------------------------- request codes ---------------------------------
    /**
     * 请求码：请求录屏
     */
    public static final int REQUEST_CODE_SCREEN_CAPTURE = 0x99;
    /**
     * 请求码：存储和录音
     */
    public static final int REQUEST_CODE_STORAGE_AND_AUDIO_PERMISSION = 0x11;
    /**
     * 请求码：打开录屏目录开始播放视频
     */
    @Deprecated
    public static final int REQUEST_CODE_GET_VIDEO_FROM_DIR = 0x66;
    /**
     * 请求码：悬浮窗的请求码
     */
    public static final int REQUEST_CODE_OVERLAY_WINDOW = 0x10;
    //--------------------------------- request codes ---------------------------------


    //--------------------------------- actions---------------------------------
    /**
     * 应用处于前台
     */
    public static final String ACTION_APP_BACKGROUND = "com.yu.screenrecorder.ScreenRecordService.action_app_background";
    /**
     * 应用处于后台
     */
    public static final String ACTION_APP_FOREGROUND = "com.yu.screenrecorder.ScreenRecordService.action_app_foreground";
    /**
     * 应用停止录屏
     */
    public static final String ACTION_APP_STOP_RECORD = "com.yu.screenrecorder.ScreenRecordService.action_stop_record";
    /**
     * 更新屏幕尺寸
     * <br/> 通常是用在手动输入屏幕尺寸来通知更新
     */
    public static final String ACTION_UPDATE_SIZE = "com.ckt.test.tp.ACTION_UPDATE_SIZE";
    //--------------------------------- actions---------------------------------


    //---------------------------------  fragments tags ---------------------------------
    /**
     * record_fragment
     */
    @Deprecated
    public static final String TAG_FRAGMENT_RECORD = "record_fragment";
    /**
     * calc_distance_fragment
     */
    public static final String TAG_FRAGMENT_CALC_DISTANCE = "calc_distance_fragment";
    /**
     * calc_ime_distance_fragment
     */
    public static final String TAG_FRAGMENT_CALC_IME_DISTANCE = "calc_ime_distance_fragment";
    /**
     * size_fragment
     */
    public static final String TAG_FRAGMENT_SIZE = "size_fragment";
    /**
     * main_fragment
     */
    public static final String TAG_FRAGMENT_MAIN = "main_fragment";
    /**
     * video_list_fragment
     */
    public static final String TAG_FRAGMENT_VIDEO_LIST = "video_list_fragment";
    /**
     * lineation_fragment
     */
    public static final String TAG_FRAGMENT_LINEATION = "lineation_fragment";

    /**
     * tag_save_fragment_state , used to save Fragment state
     */
    public static final String TAG_SAVE_FRAGMENT_STATE = "tag_save_fragment_state";
    //---------------------------------  fragments tags ---------------------------------


    //---------------------------------  flags ---------------------------------
    /**
     * 是否可以录屏
     * <br/>  判断系统版本,大于等于5.0才允许录屏
     */
    public static final boolean IS_CAN_RECORD = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP;
    //---------------------------------  flags ---------------------------------

    public static final String BASE_PATH = MyConstants.getStorageRootDir(MyApplication.getContext()) + File.separator + MyConstants.ROOT_DIR + File.separator + MyConstants.TP_DIR;
    public static final String CRASH_OUTPUT_FILE_DIR = BASE_PATH + File.separator + "CrashLog";

    public static final String FILE_PROVIDER_AUTHORITIES = "com.ckt.testauxiliarytool.tp.fileprovider";
}
