package com.ckt.testauxiliarytool.webviewtest;

/**
 * Created by wgp on 2017/9/1.
 * 生成Excel的bean
 */

public class ExcelBean {
    private String id;//条目编号
    private String begin;//开始时间
    private String end;//结束时间
    private String isSucc;//是否成功
    private String time;//加载时间
    private String isdelay;//是否超时
    private String descrip;//失败原因

    public ExcelBean(String id, String begin, String end, String isSucc, String time, String isdelay, String descrip) {
        this.id = id;
        this.begin = begin;
        this.end = end;
        this.isSucc = isSucc;
        this.time = time;
        this.isdelay = isdelay;
        this.descrip = descrip;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getIsSucc() {
        return isSucc;
    }

    public void setIsSucc(String isSucc) {
        this.isSucc = isSucc;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getIsdelay() {
        return isdelay;
    }

    public void setIsdelay(String isdelay) {
        this.isdelay = isdelay;
    }

    public String getDescrip() {
        return descrip;
    }

    public void setDescrip(String descrip) {
        this.descrip = descrip;
    }
}
