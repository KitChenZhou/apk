package com.ckt.testauxiliarytool.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.PowerManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by D22431 on 2017/9/14.
 * <p>
 * Define method that check if device is rooted
 */

public class RebootTool {

    public static class Root {

        /**
         * check if device is rooted
         *
         * @return
         */
        public static boolean isDeviceRooted() {
            if (checkRootMethod1()) {
                return true;
            }
            if (checkRootMethod2()) {
                return true;
            }
            if (checkRootMethod3()) {
                return true;
            }
            return false;
        }

        public static boolean checkRootMethod1() {
            String buildTags = android.os.Build.TAGS;

            if (buildTags != null && buildTags.contains("test-keys")) {
                return true;
            }
            return false;
        }

        public static boolean checkRootMethod2() {
            try {
                File file = new File("/system/app/Superuser.apk");
                if (file.exists()) {
                    return true;
                }
            } catch (Exception e) {
            }
            return false;
        }

        public static boolean checkRootMethod3() {
            if (new ExecShell().executeCommand(ExecShell.SHELL_CMD.check_su_binary) != null) {
                return true;
            } else {
                return false;
            }
        }
    }


    public static class ExecShell {

        private static String LOG_TAG = ExecShell.class.getName();

        public enum SHELL_CMD {
            check_su_binary(new String[]{"/system/xbin/which", "su"});
            String[] command;

            SHELL_CMD(String[] command) {
                this.command = command;
            }
        }

        public ArrayList<String> executeCommand(SHELL_CMD shellCmd) {
            String line;
            ArrayList<String> fullResponse = new ArrayList<>();
            Process localProcess;

            try {
                localProcess = Runtime.getRuntime().exec(shellCmd.command);
            } catch (Exception e) {
                return null;
                //e.printStackTrace();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));

            try {
                while ((line = in.readLine()) != null) {
                    Log.d(LOG_TAG, "--> Line received: " + line);
                    fullResponse.add(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(LOG_TAG, "--> Full response was: " + fullResponse);
            return fullResponse;
        }
    }

    /**
     * Reboot(root) for user app
     */
    public static void rebootForUserApp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    String cmd = "su -c reboot -p";
                    Process proc;
                    proc = Runtime.getRuntime().exec(cmd);
                    proc.waitFor();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * check is system app
     */
    public static boolean isSystemApp(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
            return true;
        }
        return false;
    }

    public static void rebootForSystemApp(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        powerManager.reboot(null);
    }
}
