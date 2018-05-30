package com.ckt.testauxiliarytool.cameratest.capturetask;


import android.os.Environment;

import com.ckt.testauxiliarytool.MyApplication;
import com.ckt.testauxiliarytool.utils.MyConstants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yy on 2017/9/7.
 */

public class FileProducer {

    /**
     * the root directory of CameraTest.
     * it may be /storage/emulated/0/CameraTest
     */
    private static String cameraTestDir;

    /**
     * the counter of one test.
     */
    private static String REQ_NAME;

    /**
     * format the current time and return as string.
     */
    private static String getTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_mmssSSS");
        Date now = new Date();
        return sdf.format(now);
    }

    /**
     * initialization of vars needed when this class loaded on the first time.
     */
    static {
        cameraTestDir = MyConstants.getStorageRootDir(MyApplication.getContext());
        if (cameraTestDir != null) {
            cameraTestDir += "/" + MyConstants.ROOT_DIR +"/" + MyConstants.CAMERA_DIR + "/";
        }
    }

    /**
     * file type, picture file and log file.
     */
    private final static int PIC_FILE = 0;
    private static final int LOG_FILE = 1;

    /**
     * return the path of picture file or log file
     * @param reqName the name of request.
     * @param type the type of the file, picture or log.
     * @return string path of the file
     */
    private static String getFilePath(String reqName, int type){
        StringBuffer sb = new StringBuffer();
        sb.append(cameraTestDir);
        sb.append("/");
        sb.append(reqName.replace("\n", ""));
        File dir = new File(sb.toString());
        if (!dir.exists()){
            dir.mkdirs();
        }
        sb.append("/IMG_");
        sb.append(getTimeString());
        sb.append(".jpg");

        return sb.toString();
    }

    /**
     * get a picture file in the correct location to write data when needed.
     * @return picture File
     */
    public static File getPictureFile(String reqName){
        File file = null;
        if (cameraTestDir != null){
            file = new File(getFilePath(reqName,PIC_FILE));
            if (file.exists()){
                getPictureFile(reqName);
            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }
}


