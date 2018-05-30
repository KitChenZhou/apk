package com.ckt.testauxiliarytool.utils;
/**
 * Created by wgp on 2017/8/22.
 * 判断用户输入是否合法的工具类
 */

public class InputUtils {

    /**
     * 判断用户的输入不能为空
     *
     * @param interal 测试的时间间隔
     * @param count   测试的总次数
     * @return
     */
    public static boolean handleParams(String interal, String count) {
        if (interal.equals("") || count.equals("")) {
            return false;
        }
        return true;
    }

    /**
     * 判断用户网页测试时输入参数
     *
     * @param interal 时间间隔
     * @param delay   延迟时间
     * @param con     持续时间
     * @param count   测试的次数
     * @return
     */
    public static boolean handleParams(String interal, String delay, String con, String count, String on) {
        if (interal.equals("") || delay.equals("") || con.equals("") || count.equals("") ||
                on.equals("")) {
            return false;
        }
        return true;
    }
}
