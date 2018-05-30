package com.ckt.testauxiliarytool.utils;

import java.util.regex.Pattern;

/**
 * Created by wgp on 2017/8/21.
 * 一个校验URL的工具类
 */

public class URLUtils {
    private static final String pattern = "^(http://|https://)?((?:[A-Za-z0-9]+-[A-Za-z0-9]+|[A-Za-z0-9]+)\\.)+([A-Za-z]+)[/\\?\\:]?.*$";

    /**
     * 验证一个url是否为合法的url
     * @param url 输入的URL
     * @return
     */
    public static boolean isUrl(String url) {
        Pattern pt = Pattern.compile(pattern);
        return pt.matcher(url).matches();
    }
}
