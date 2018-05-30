package com.ckt.testauxiliarytool.tp.model;

/**
 * <br/>Author: pecuyu
 * <br/>Email: yu.qin@ck-telecom.com
 * <br/>Date: 2017/10/16
 * <br/>TODO: Video条目
 */

public class VideoItem {
    private String videoName;
    private String time;
    private String size;
    private String videoPath;

    public VideoItem(String videoName, String videoPath, String time, String size) {
        this.videoName = videoName;
        this.videoPath = videoPath;
        this.time = time;
        this.size = size;
    }

    public String getVideoName() {
        return videoName;
    }

    public String getTime() {
        return time;
    }

    public String getSize() {
        return size;
    }

    public String getVideoPath() {
        return videoPath;
    }
}
