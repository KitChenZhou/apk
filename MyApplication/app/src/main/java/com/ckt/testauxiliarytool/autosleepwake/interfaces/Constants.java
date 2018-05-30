package com.ckt.testauxiliarytool.autosleepwake.interfaces;

import com.ckt.testauxiliarytool.MyApplication;
import com.ckt.testauxiliarytool.utils.MyConstants;

import java.io.File;

/**
 * 保存程序中用到的常量
 */
public interface Constants {
    //测试时间间隔和测试次数
    String TEST_TIME = "test_time";
    String TEST_COUNT = "test_count";

    // 文件路径key值
    String FILE_PATH = "file_path";

    // 日志保存的目录
    String LOG_DIR = MyConstants.getStorageRootDir(MyApplication.getContext()) + File.separator + MyConstants.ROOT_DIR + File.separator + MyConstants.AUTO_SLEEP_WEAK_DIR;

}