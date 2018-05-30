LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := libmem_fill_tool
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_LDLIBS := \
	-llog \

LOCAL_SHARED_LIBRARIES := liblog libcutils  
 
LOCAL_SRC_FILES := main.c 

LOCAL_C_INCLUDES += jni

include $(BUILD_SHARED_LIBRARY)
