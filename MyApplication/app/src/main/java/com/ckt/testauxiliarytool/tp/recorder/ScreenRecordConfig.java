package com.ckt.testauxiliarytool.tp.recorder;

import android.annotation.TargetApi;
import android.media.MediaRecorder;
import android.os.Build;

import com.ckt.testauxiliarytool.tp.model.Constant;

import java.io.File;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/8/21
 * TODO: 录屏配置文件
 */

public class ScreenRecordConfig {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static class Contants {  // 录屏配置信息，默认值
        public static final int AUDIO_SOURCE_DEFAULT = MediaRecorder.AudioSource.MIC;
        public static final int VIDEO_SOURCE_DEFAULT = MediaRecorder.VideoSource.SURFACE;
        public static final int OUTPUT_FORMATE_DEFAULT = MediaRecorder.OutputFormat.THREE_GPP;
        public static final String OUTPUT_FILE_DIR_DEFAULT = Constant.BASE_PATH + File.separator + "ScreenRecorder";
        public static final int VIDEO_SIZE_WIDTH_DEFAULT = 720;
        public static final int VIDEO_SIZE_HEIGHT_DEFAULT = 1080;
        public static final int AUDIO_ENCODER_DEFAULT = MediaRecorder.AudioEncoder.AMR_NB;
        public static final int VIDEO_ENCODER_DEFAULT = MediaRecorder.VideoEncoder.H264;
        public static final int VIDEO_ENCODING_BIT_RATE_DEFAULT = 5 * 1024 * 1024;
        public static final int VIDEO_FRAME_RATE_DEFAULT = 30;

        public static final String VIDEO_OUTPUT_FORMATE_TEMP = ".tmp"; // 临时文件的格式
        public static final String VIDEO_OUTPUT_FORMATE = ".mp4"; // 改变要同时改变OUTPUT_FORMATE_DEFAULT
    }

    public static String filePath;  // 文件路径

    public static int width;  // 屏幕宽度 px
    public static int height;  // 屏幕宽度 px
    public static int dpi;  // 屏幕dpi

    private ScreenRecordConfig() {}  // 无需创建实例

}

