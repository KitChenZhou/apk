package com.ckt.testauxiliarytool.cameratest.common;

import android.media.MediaActionSound;

/**
 * Created by yy on 9/27/17.
 */

public class SoundPlayer {

    private static MediaActionSound mMediaActionSound;

    /**
     * 初始化参数
     */
    private static void init(){
        mMediaActionSound = new MediaActionSound();
        mMediaActionSound.load(MediaActionSound.SHUTTER_CLICK);
    }

    /**
     * 播放声音
     */
    public static void shoot() {
        if (mMediaActionSound == null)
            init();
        mMediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
    }

    public static void release() {
        if (mMediaActionSound != null) {
            mMediaActionSound.release();
            mMediaActionSound = null;
        }
    }

}
