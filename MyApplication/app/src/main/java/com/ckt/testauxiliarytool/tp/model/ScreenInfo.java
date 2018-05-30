package com.ckt.testauxiliarytool.tp.model;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/8/24
 * TODO:
 */

public class ScreenInfo {
    /**
     * 屏幕宽度，px
     */
    public int widthPixels;
    /**
     * 屏幕高度，px
     */
    public int heightPixels;
    /**
     * 屏幕dpi，dots/inch
     */
    public int densityDpi;
    /**
     * 屏幕密度
     */
    public float density;
    /**
     * 屏幕xdpi，px/inch
     */
    public float xdpi;
    /**
     * 屏幕ydpi，px/inch
     */
    public float ydpi;
    /**
     * 屏幕尺寸，inch
     */
    public double screenInche;
    /**
     * 屏幕实际宽度，mm
     */
    public float realWidth;
    /**
     * 屏幕实际高度，mm
     */
    public float realHeight;

    public ScreenInfo() {
    }

    public ScreenInfo(int widthPixels, int heightPixels, int densityDpi, float density, float xdpi, float ydpi, double screenInche, float realWidth, float realHeight) {
        this.widthPixels = widthPixels;
        this.heightPixels = heightPixels;
        this.densityDpi = densityDpi;
        this.density = density;
        this.xdpi = xdpi;
        this.ydpi = ydpi;
        this.screenInche = screenInche;
        this.realWidth = realWidth;
        this.realHeight = realHeight;
    }

    public ScreenInfo(String[] args) {
        if (args.length < 9) {
            throw new IllegalArgumentException(" args's length is too short ");
        }
        widthPixels = Integer.parseInt(args[0]);
        heightPixels= Integer.parseInt(args[1]);
        densityDpi= Integer.parseInt(args[2]);
        density= Float.parseFloat(args[3]);
        xdpi= Float.parseFloat(args[4]);
        ydpi= Float.parseFloat(args[5]);
        screenInche= Double.parseDouble(args[6]);
        realWidth= Float.parseFloat(args[7]);
        realHeight= Float.parseFloat(args[8]);
    }

    @Override
    public String toString() {
        return "ScreenInfo{" +
                "widthPixels=" + widthPixels +
                ", heightPixels=" + heightPixels +
                ", densityDpi=" + densityDpi +
                ", density=" + density +
                ", xdpi=" + xdpi +
                ", ydpi=" + ydpi +
                ", screenInche=" + screenInche +
                ", realWidth=" + realWidth +
                ", realHeight=" + realHeight +
                '}';
    }

    public String toList() {
        return widthPixels +
                "," + heightPixels +
                "," + densityDpi +
                "," + density +
                "," + xdpi +
                "," + ydpi +
                "," + screenInche +
                "," + realWidth +
                "," + realHeight;
    }
}
