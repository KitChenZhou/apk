package com.ckt.testauxiliarytool.tp.recorder;

import android.media.projection.MediaProjection;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/8/16
 * TODO:屏幕录制控制接口
 */

public interface IRecorderController {
    /**
     * 开始录制
     */
    void startRecord();

    /**
     * 结束录制
     *
     * @return boolean
     */
    boolean stopRecord();

    /**
     * 设置配置信息
     *
     * @param width int
     * @param height int
     * @param dpi int
     */
    void setConfig(int width, int height, int dpi);

    /**
     * 是否正在录制
     *
     * @return boolean
     */
    boolean isRecording();

    /**
     * 设置MediaProjection
     *
     * @param projection {@link MediaProjection}
     */
    void setMediaProject(MediaProjection projection);

    /**
     * 设置录制状态改变回调
     * <br/> 在适当的时候调用{@link #removeRecordingCallback(IOnRecorderStateChangeCallback)}，防止内存泄漏
     *
     * @param callback {@link IOnRecorderStateChangeCallback}
     */
    void addRecordingCallback(IOnRecorderStateChangeCallback callback);

    /**
     * 移除录制状态改变回调
     * <br/> 防止内存泄漏
     * @param callback {@link IOnRecorderStateChangeCallback}
     */
    void removeRecordingCallback(IOnRecorderStateChangeCallback callback);

    /**
     * 获取文件存储的绝对路径
     *
     * @return String
     */
    String getStoreFileAbsolutePath();

    /**
     * 获取存储录屏文件的目录
     *
     * @return  String
     */
    String getStoreFileDir();
}

