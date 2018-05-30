LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)


LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := android-support-design \
    jxl-2.6.12 \
    MPChartLib \
    reactive-streams-1.0.1 \
    rxandroid-2.0.1 \
    rxjava-2.1.7 \
    android-support-v4 \
    android-support-v7-appcompat \
	android-support-v7-recyclerview

#LOCAL_JAVA_LIBRARIES += appcompat-v7 
LOCAL_JAVA_LIBRARIES := telephony-common

LOCAL_JNI_SHARED_LIBRARIES += libmem_fill_tool

#LOCAL_ASSET_DIR += 

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages android.support.design
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.appcompat
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.recyclerview

LOCAL_SRC_FILES := \
    $(call all-java-files-under, src) \
    $(call all-renderscript-files-under, src)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_RESOURCE_DIR += frameworks/support/v7/appcompat/res
LOCAL_RESOURCE_DIR += prebuilts/sdk/current/support/design/res
LOCAL_RESOURCE_DIR += frameworks/support/v7/recyclerview/res

LOCAL_PACKAGE_NAME := TestAuxiliaryTool


include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    jxl-2.6.12:libs/jxl-2.6.12.jar \
    MPChartLib:libs/MPChartLib.jar \
    reactive-streams-1.0.1:libs/reactive-streams-1.0.1.jar\
    rxandroid-2.0.1:libs/rxandroid-2.0.1.aar \
    rxjava-2.1.7:libs/rxjava-2.1.7.jar
	
include $(BUILD_MULTI_PREBUILT)

#include $(BUILD_PREBUILT)



include $(call all-makefiles-under, jni)

include $(call all-makefiles-under, $(LOCAL_PATH))
