package com.ckt.testauxiliarytool.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ckt.testauxiliarytool.cameratest.slrc.model.IConstValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Cc on 2017/9/13.
 * <p>
 * Catch application log.
 */

public class LogCatchUtil {

    private static final LogCatchUtil LOG_CATCH = new LogCatchUtil();

    private LogDumper mLogDumper;

    /**
     * If SDCard is not exist, mRootFile will be null.
     */
    private File mFolder;

    private int mPId;

    private LogCatchUtil() {
        mPId = android.os.Process.myPid();
    }

    public static LogCatchUtil getInstance() {
        return LOG_CATCH;
    }

    /**
     * Create a thread to catch log.
     */
    public void start(Context context) {
        if (mFolder == null) {
            mFolder = new File(MyConstants.getStorageRootDir(context), MyConstants.ROOT_DIR + "/"
                    + MyConstants.LOG_DIR);
        }

        if (mLogDumper == null) {
            mLogDumper = new LogDumper(context, String.valueOf(mPId));
            mLogDumper.start();
        }
    }

    /**
     * Stop catch the log.
     */
    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    private class LogDumper extends Thread {

        private Process logcatProcess;
        private BufferedReader mReader;
        private boolean mRunning;
        private String mCommand;
        private String mPID;
        private File mFile;
        private FileOutputStream out;
        private Context mContext;

        LogDumper(Context context, String pid) {
            mContext = context;

            mPID = pid;

            // 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
            // 显示当前mPID程序的 E和W等级的日志.

            /*mCommand = "logcat *:e *:w | grep \"(" + mPID + ")\"";*/
            /*mCommand = "logcat -s way";//打印标签过滤信息*/
            /* mCommand = "logcat *:e *:i | grep \"(" + mPID + ")\"";*/
            mCommand = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息

        }

        void stopLogs() {
            mRunning = false;
        }

        @Override
        public void run() {

            mRunning = true;

            try {
                // When mFolder is not exist, then create it cyclically in each 100 ms.
                while (mRunning && !mFolder.exists() && !mFolder.mkdirs()) Thread.sleep(100);

                mFile = new File(mFolder, "Log_" + DateTimeUtils.detailPictureFormat() + ".txt");

                // When the file is not exist and create it failed, then send a error log.
                if (!mFile.exists() && !mFile.createNewFile())
                    LogUtil.e(IConstValue.TAG, mFile.toString() + " file create fail!");

                out = new FileOutputStream(mFile, true);

                logcatProcess = Runtime.getRuntime().exec(mCommand);
                mReader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()
                ), 1024);

                String line;

                while (mRunning && (line = mReader.readLine()) != null) {
                    if (line.contains(mPID)) out.write((line + "\n").getBytes());
                }

                out.flush();

                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
                        .fromFile(mFile)));

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (logcatProcess != null) {
                    logcatProcess.destroy();
                    logcatProcess = null;
                }

                try {
                    if (mReader != null) {
                        mReader.close();
                        mReader = null;
                    }
                    if (out != null) {
                        out.close();
                        out = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mContext = null;

            }

        }

    }


}
