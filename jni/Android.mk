LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := H264Android
LOCAL_SRC_FILES := H264Android.c cabac.c common.c dsputil.c golomb.c h264.c h264utils.c mpegvideo.c

include $(BUILD_SHARED_LIBRARY)
