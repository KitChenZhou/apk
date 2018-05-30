package com.ckt.testauxiliarytool.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: pecuyu
 * Email: yu.qin@ck-telecom.com
 * Date: 2017/9/11
 * TODO: Activity管理器
 */

public class ActivityCollector {
    private static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        if (activity == null) return;
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        if (activity == null) return;
        activities.remove(activity);
    }

    public static void finishAll() {
        for (Activity activity : activities) {
            activities.remove(activity);
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    public static List<Activity> getActivities() {
        return activities;
    }
}
