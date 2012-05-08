#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# This makefile supplies the rules for building a library of JNI code for
# use by our example platform shared library.

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

# This is the target being built.
LOCAL_MODULE:= libH263Decoder

# All of the source files that we will compile.
LOCAL_SRC_FILES:= \
src/com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder.cpp \
src/adaptive_smooth_no_mmx.cpp \
src/bitstream.cpp \
src/block_idct.cpp \
src/cal_dc_scaler.cpp \
src/chv_filter.cpp \
src/chvr_filter.cpp \
src/combined_decode.cpp \
src/conceal.cpp \
src/datapart_decode.cpp \
src/dcac_prediction.cpp \
src/dec_pred_intra_dc.cpp \
src/deringing_chroma.cpp \
src/deringing_luma.cpp \
src/find_min_max.cpp \
src/get_pred_adv_b_add.cpp \
src/get_pred_outside.cpp \
src/idct.cpp \
src/idct_vca.cpp  \
src/mb_motion_comp.cpp \
src/mb_utils.cpp \
src/pvdec_api.cpp \
src/packet_util.cpp \
src/post_filter.cpp \
src/post_proc_semaphore.cpp \
src/pp_semaphore_chroma_inter.cpp \
src/pp_semaphore_luma.cpp \
src/scaling_tab.cpp \
src/vlc_decode.cpp \
src/vlc_dequant.cpp \
src/vlc_tab.cpp \
src/vop.cpp \
src/zigzag_tab.cpp \
src/yuv2rgb.cpp \
src/3GPVideoParser.cpp

# All of the shared libraries we link against.
LOCAL_SHARED_LIBRARIES := 

# No static libraries.
LOCAL_STATIC_LIBRARIES :=

# Also need the JNI headers.
LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE) \
	$(LOCAL_PATH)/src \
 	$(LOCAL_PATH)/include \
	$(LOCAL_PATH)/oscl

# No specia compiler flags.
LOCAL_CFLAGS +=

# Don't prelink this library.  For more efficient code, you may want
# to add this library to the prelink map and set this to true.
LOCAL_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)

