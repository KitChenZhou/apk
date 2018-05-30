package com.ckt.testauxiliarytool.tp.recorder;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Environment;

import com.ckt.testauxiliarytool.MyApplication;
import com.ckt.testauxiliarytool.utils.SharedPrefsUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/8/21
 * TODO: 录屏引擎
 * <p>
 * 使用步骤：
 * ①实例化引擎
 * ②初始化配置 setConfig ，setMediaProjection
 * ③启动录屏startRecord
 * ④停止录屏stopRecord
 * ⑤释放资源releaseRecorder
 */

public class ScreenRecordEngine {
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    private long mLastTime;
    // 录屏宽高与dpi
    private int mWidth = ScreenRecordConfig.Contants.VIDEO_SIZE_WIDTH_DEFAULT;
    private int mHeight = ScreenRecordConfig.Contants.VIDEO_SIZE_HEIGHT_DEFAULT;
    private int mDpi;

    // 录屏
    private VirtualDisplay mVirtualDisplay;
    private MediaProjection mMediaProjection;

    private String fileName;

    public ScreenRecordEngine() {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
    }

    /**
     * 初始化Recorder
     */
    private void initRecorder() {
        // 设置音频源
        mMediaRecorder.setAudioSource(ScreenRecordConfig.Contants.AUDIO_SOURCE_DEFAULT);
        // 设置视频源
        mMediaRecorder.setVideoSource(ScreenRecordConfig.Contants.VIDEO_SOURCE_DEFAULT);
        // 设置输出文件的格式
        mMediaRecorder.setOutputFormat(ScreenRecordConfig.Contants.OUTPUT_FORMATE_DEFAULT);
        // 设置输出文件,录屏中使用临时文件格式，结束后再恢复格式
        fileName = "CAPTURE_" + getSimpleTimeString(new Date(System.currentTimeMillis()));
        ScreenRecordConfig.filePath = getSavedFileDir() + File.separator + fileName + ScreenRecordConfig.Contants.VIDEO_OUTPUT_FORMATE_TEMP;
        SharedPrefsUtil.name("record_config").putString("lastRecordFile", ScreenRecordConfig.filePath)
                .putString("lastRecordFileName", fileName).recycle();

        mMediaRecorder.setOutputFile(ScreenRecordConfig.filePath);
        // 设置视频的宽高
        mMediaRecorder.setVideoSize(mWidth, mHeight);
        // 设置音频编码器
        mMediaRecorder.setAudioEncoder(ScreenRecordConfig.Contants.AUDIO_ENCODER_DEFAULT);
        // 设置视频编码器
        mMediaRecorder.setVideoEncoder(ScreenRecordConfig.Contants.VIDEO_ENCODER_DEFAULT);
        // 设置视频编码比特率
        mMediaRecorder.setVideoEncodingBitRate(ScreenRecordConfig.Contants.VIDEO_ENCODING_BIT_RATE_DEFAULT);// Call this method before prepare().
        // 设置要捕捉的视频帧率
        mMediaRecorder.setVideoFrameRate(ScreenRecordConfig.Contants.VIDEO_FRAME_RATE_DEFAULT); // Must be called after setVideoSource(). Call this after setOutFormat() but before prepare()

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始录制
     *
     * @return 成功返回true
     */
    public void startRecord() {
        if (mMediaRecorder == null || mMediaProjection == null || isRecording) {
            return;
        }
        initRecorder();
        createVirtualDisplay();
        mMediaRecorder.start();
        isRecording = true;
        mLastTime = System.currentTimeMillis();
    }

    /**
     * 结束录制,释放资源
     *
     * @return 成功返回 true
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean stopRecord() {
        if (!isRecording|| System.currentTimeMillis()-mLastTime<=1000) { // 停止与开始之前间隔要大于1秒
            return false;
        }
        isRecording = false;

        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        }
        if (mVirtualDisplay != null) mVirtualDisplay.release();
        if (mMediaProjection != null) mMediaProjection.stop();
        restoreFileFormat();
        return true;
    }

    /**
     * 将临时文件后缀改回来
     */
    private boolean restoreFileFormat() {
        String path = SharedPrefsUtil.name("record_config").getString("lastRecordFile", null);
        String fileName = SharedPrefsUtil.name("record_config").getString("lastRecordFileName", null);
        if (path != null && fileName != null) {
            File temp = new File(path);
            File file = new File(ScreenRecordConfig.Contants.OUTPUT_FILE_DIR_DEFAULT, fileName + ScreenRecordConfig.Contants.VIDEO_OUTPUT_FORMATE);
            return temp.renameTo(file); // 重命名
        }
        return false;
    }


    /**
     * 设置允许应用程序捕获屏幕内容和/或记录系统音频的一个标记
     */
    public void setMediaProjection(MediaProjection mediaProjection) {
        this.mMediaProjection = mediaProjection;
    }


    /**
     * 创建一个虚拟显示来捕捉屏幕的内容
     * VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR 参数是指创建屏幕镜像，所以我们实际录制内容的是屏幕镜像，
     * 但内容和实际屏幕是一样的，并且这里我们把 VirtualDisplay 的渲染目标 Surface 设置为
     * MediaRecorder.getSurface的surface，之后通过 MediaRecorder 将屏幕内容录制下来，并且存成 video 文件
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createVirtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenRecorder", mWidth,
                mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, // 镜像
                mMediaRecorder.getSurface(), null, null);
    }

    /**
     * 释放MediaRecorder资源
     * <br/>call this method after {@link #stopRecord()}
     *
     * @see #stopRecord()
     */
    public void releaseRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    /**
     * 是否正在录屏
     *
     * @return {@link Boolean}
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * 配置宽高以及dpi
     *
     * @param width  px
     * @param height px
     * @param dpi    dpi
     */
    public void setConfig(int width, int height, int dpi) {
        this.mWidth = width;
        this.mHeight = height;
        this.mDpi = dpi;
    }

    /**
     * 获取保存目录，不存在则创建
     *
     * @return 成功则返回存储目录，失败返回null
     */
    public String getSavedFileDir() {
        File dir = new File(ScreenRecordConfig.Contants.OUTPUT_FILE_DIR_DEFAULT);
        if (dir.exists()) {
            return ScreenRecordConfig.Contants.OUTPUT_FILE_DIR_DEFAULT;
        }
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (!dir.exists()) {  // 不存在
                if (!dir.mkdirs()) {  // 创建失败
                    return null;
                }
            }
            return dir.getPath();
        }
        return null;
    }

    /**
     * 获取录屏文件的绝对路径
     *
     * @return {@link String}
     */
    public String getAbsoluteFilePath() {
        if (ScreenRecordConfig.filePath == null) {
            String path = SharedPrefsUtil.name("record_config").getString("lastRecordFile", null);
            SharedPrefsUtil.recycle("record_config");
            return path;
        }
        return ScreenRecordConfig.filePath;
    }

    /**
     * 通过date得到简单的日期,由时间+日期组成
     *
     * @param date {@link Date}
     * @return {@link String}
     */
    public String getSimpleTimeString(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat formatter;
        if (is_TIME_24(MyApplication.getContext())) {
            formatter = new SimpleDateFormat("HH:mm:ss_MM月dd日", Locale.CHINA);
            return formatter.format(date);
        } else {
            formatter = new SimpleDateFormat("hh:mm:ss_MM月dd日", Locale.CHINA);
            return (is_AM() ? "上午_" : "下午_") + formatter.format(date);
        }
    }

    /**
     * 判断时间是否为24小时制
     *
     * @param ctx Context
     * @return boolean
     */
    public static boolean is_TIME_24(Context ctx) {
        ContentResolver cv = ctx.getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(cv,
                android.provider.Settings.System.TIME_12_24);
        return (strTimeFormat != null && strTimeFormat.equals("24")); //strTimeFormat某些rom12小时制时会返回null
    }

    /**
     * 判断当前为上午还是下午
     *
     * @return boolean
     */
    public boolean is_AM() {
        int AM_PM = Calendar.getInstance().get(Calendar.AM_PM);
        return Calendar.AM == AM_PM;
    }

    public String getLastFileName() {
        return fileName;
    }
}
