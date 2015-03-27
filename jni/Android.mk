LOCAL_PATH := $(call my-dir)


include $(CLEAR_VARS)

LOCAL_SRC_FILES+= com_android_ffmpeglib_H264Decoder.c
LOCAL_C_INCLUDES+=$(LOCAL_PATH)/libffmpeg
LOCAL_STATIC_LIBRARIES:= libavcodec libavformat libavutil
LOCAL_LDLIBS:=-llog
LOCAL_MODULE:=ffmpeglib
include $(BUILD_SHARED_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))
