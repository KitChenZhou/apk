package com.ckt.testauxiliarytool.cameratest.common;

/**
 * Created by D22433 on 2017/9/1.
 */

public class ConstVar {
    /**
     * 线程休眠的默认时长、结束时提示所持续时长，单位：秒。
     */
    public static final int SLEEP_TIME_DEFAULT = 3;
    public static final int SLEEP_TIME_FINISH_NOTIFICATION = 5;
    /**
     * Required by 方兵。
     */
    public final static String BOOT_BROADCAST = "com.ckt.camera_test.boot_broadcast";
    public static final String BOOT_START = "com.ckt.camera_test.start_camera";
    public static final String START_TIME = "com.ckt.camera_test.start_time";
	public static final String USER_PRESENT_COUNT = "com.ckt.camera_test.user_present";
    public static final String START_COUNT = "com.ckt.camera_test.startCount";
    public static final String START_NUM = "com.ckt.camera_test.startNum";
    public static final String COUNT = "com.ckt.camera_test.count";
    public static final String UNUSUALLY = "com.ckt.camera_test.unusually";
    public static final String UNUSUALLY_START = "com.ckt.camera_test.unusuallyStart";
    public static final String TEST_DONE = "com.ckt.camera_test.test_is_done";
    public static final String START_CAMERA_ACTIVITY = "com.ckt.camera_test.StartCameraActivity";
    public static final String UNUSUALLY_EXIT_CAMERA = "com.ckt.camera_test.UnusuallyExitCamera";
    public static final String PICTURE_PATH = "com.ckt.camera_test.picture_path";
    public static final String CAMERA_OPENED = "com.ckt.camera_test.camera_opened";
    public static final String CAPTURE_COMPLETED = "com.ckt.camera_test.capture_completed";

    //Message of first time to take photo
    public static final int FIRST_TAKE_PHOTO_MESSAGE = 0x01;
    //Switch camera message
    public static final int SWITCH_CAMERA_MESSAGE = 0x02;
    //Message of first time to take photo
    public static final int SECOND_TAKE_PHOTO_MESSAGE = 0x03;
    //Message of take photo down
    public static final int TAKE_PHOTO_DONE = 0x04;
    /**
     * Required by 杨宇 in CameraApiImpl2.java
     */
    public static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    public static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;

    // New Key for new code architecture on 2017/11/23.
    /**
     * Indicate the current task id, the value can be
     * CAPTURE_TASK_NONE, CAPTURE_TASK_CFBC, CAPTURE_TASK_CIDB, CAPTURE_TASK_CRS.
     * For details of each task id , please refer to the variable definition.
     */
    public static final String CAPTURE_TASK_STATUS = "CameraTestCaptureTaskStatus";

    /**
     * The total count of all the task.
     */
    public static final String CAPTURE_TASK_TOTCNT = "CameraTestCaptureTaskTotalCount";

    /**
     * The current count of the specific task.
     */
    public static final String CAPTURE_TASK_CURCNT_CFBS = "CameraTestCaptureTaskCurrentCountCFBS";
    public static final String CAPTURE_TASK_CURCNT_CIDB = "CameraTestCaptureTaskCurrentCountCIDB";
    public static final String CAPTURE_TASK_CURCNT_CRS = "CameraTestCaptureTaskCurrentCountCRS";

    /**
     * Default value of current and total count.
     */
    public static final int CAPTURE_TASK_CURCNT_DEFAULT = 1;
    public static final int CAPTURE_TASK_TOTCNT_DEFAULT = 20;


    /**
     * Task id of CFBC, CIDB, CRS.
     * 0 for no task.
     */
    public static final int CAPTURE_TASK_NONE = 0;
    public static final int CAPTURE_TASK_CFBC = 1;
    public static final int CAPTURE_TASK_CIDB = 2;
    public static final int CAPTURE_TASK_CRS = 3;

    /**
     * Msg id of CaptureTaskHandler.
     */
    public static final int CT_MSG_TASK_BEGIN = 0x111;
    public static final int CT_MSG_TASK_COMPLETE = 0x110;
    public static final int CT_MSG_TASK_NOT_START = 0x10f;
    public static final int CT_MSG_TASK_CRS_INIT = 0x10e;
    public static final int CT_MSG_TASK_UPDATE_TIP = 0x10d;
    public static final int CT_MSG_TASK_IMG_INFO = 0x10c;
    public static final int CT_MSG_TASK_SWITCH_CAM = 0x10b;
    public static final int CT_MSG_TASK_REOPEN_FRAGMENT = 0x10a;

    // end of modification on 2017/11/24

}
