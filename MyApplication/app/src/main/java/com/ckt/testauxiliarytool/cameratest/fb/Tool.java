package com.ckt.testauxiliarytool.cameratest.fb;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.util.Log;
import android.widget.Toast;

import com.ckt.testauxiliarytool.cameratest.common.ConstVar;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by D22431 on 2017/11/24.
 */

public class Tool {
    // Custom delay pops up messages
    public static void showToast(Context context, String words) {
        Toast.makeText(context, words, Toast.LENGTH_SHORT).show();
    }

    //Clear repeat times
    public static void clearFBRepeatTimes(Context context) {
        SharedPreferences startBootPreferences = context.getApplicationContext()
                .getSharedPreferences(ConstVar.BOOT_START, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = startBootPreferences.edit();
        editor.putInt(ConstVar.START_COUNT, -1);
        editor.putInt(ConstVar.START_NUM, -1);
        editor.apply();
    }

    //Create a directory and return the path
    static String createFileDirectory(String parentPath, String childPath) {
        if (childPath != null) {
            String[] dir = childPath.split("/");
            File file;
            for (String p : dir) {
                file = new File(parentPath, p);
                if (!file.exists() || !file.isDirectory()) {
                    //Create a directory
                    if (!file.mkdir()) {
                        Log.e("Tool", "create file directory failed");
                        return null;
                    }
                }
                parentPath = file.getPath();
            }
        }
        return parentPath;
    }

    // get the largest supported size
    public static Camera.Size getMaxSize(List<Camera.Size> sizeList) {
        Collections.sort(sizeList, new SizeComparator());
        Camera.Size result = null;
        if (sizeList != null && !sizeList.isEmpty()) {
            result = sizeList.get(sizeList.size() - 1);
        }
        return result;
    }

    // Sort from small to large
    public static class SizeComparator implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            return Long.signum((long) lhs.width * lhs.height -
                    (long) rhs.width * rhs.height);
        }
    }

}
