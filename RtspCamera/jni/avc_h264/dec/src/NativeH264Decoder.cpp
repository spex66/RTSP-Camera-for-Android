/* ------------------------------------------------------------------
 * Copyright (C) 2009 OrangeLabs
 *
 * Author: Alexis Gilabert Senar
 * Date: 2009-07-01
 * -------------------------------------------------------------------
 */
#define LOG_TAG "NativeDec"
#include "android/log.h"
#include "NativeH264Decoder.h"
#include "pvavcdecoder.h"
#include "3GPVideoParser.h"
#include "yuv2rgb.h"

// xxx pa try to read nal unit type
#include "avcdec_api.h"


int     iSrcWidth = 352;
int     iSrcHeight = 288;


#define MB_BASED_DEBLOCK

typedef enum {
  SPS,
  PPS,
  SLICE
} DEC_STATE;

/*
 * Global variables
 *
*/

  PVAVCDecoder *decoder;
  int parserInitialized = 0;
  int decoderInitialized = 0;

  uint8*        aOutBuffer;
  uint8*        aInputBuf;

  DEC_STATE         state;
  AVCFrameIO outVid;

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder
 * Method:    InitDecoder
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_InitDecoder
  (JNIEnv * env, jclass clazz){

  state = SPS;
  aOutBuffer = (uint8*)malloc(iSrcWidth*iSrcHeight*3/2);
  decoder = PVAVCDecoder::New();
  return (decoder!=NULL)?1:0;
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder
 * Method:    DeinitDecoder
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_DeinitDecoder
  (JNIEnv * env, jclass clazz){
    state = SPS;
    free(aOutBuffer);
    delete(decoder);
    return 1;
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder
 * Method:    DecodeAndConvert
 * Signature: ([B[IJ)[I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_DecodeAndConvert
  (JNIEnv *env, jclass clazz, jbyteArray h264Frame, jintArray decoded)
{
  int32 size = 0;
  int32 status;
  int                 indexFrame;
  int                 releaseFrame;
  /* Set volbuf with h263Frame data*/
  jint len = env->GetArrayLength(h264Frame);
  jbyte data[len];
  env->GetByteArrayRegion(h264Frame, 0, len, data);

  aInputBuf = (uint8*)malloc(len);
  memcpy(aInputBuf,(uint8*)data,len);
  size = len;

  // xxx pa try to read nal unit type
  int nal_unit_type = (AVCNalUnitType)(aInputBuf[0] & 0x1F);
  __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "nal_unit_type : %d", nal_unit_type);

  // xxx pa new switch based on incoming NAL Units instead of fixed state machine
  // this is the only approach to react on SPS/PPS which are part of in-band parameter settings (sended in between)
  switch (nal_unit_type){
    case AVC_NALTYPE_SPS:

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "decode: AVC_NALTYPE_SPS");

    // xxx pa Reset decoder, prepare it for a new IDR frame.
    // decoder->ResetAVCDecoder();
    
      // ===========>
      if (decoder->DecodeSPS(aInputBuf,size)==AVCDEC_SUCCESS){
        
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "decode: state: SPS->PPS");
        state = PPS;
      } else {
        return 0;
      }
      break;
    case AVC_NALTYPE_PPS:
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "decode: AVC_NALTYPE_PPS");
        
        if (state != PPS) {
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "BREAK not in state: PPS");

            break;
        }
      // ===========>
      if (decoder->DecodePPS(aInputBuf,size)==AVCDEC_SUCCESS){
      
        state = SLICE;
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "decode: state: PPS->SLICE");
      } else {
      
        // xxx pa reset state to SPS lookup
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "decode: reset state: PPS->SPS");

        state = SPS;

        return 0;
      }
      break;
    case AVC_NALTYPE_IDR :
        // xxx pa Reset decoder, prepare it for a new IDR frame.
        decoder->ResetAVCDecoder();
        // don't break
    case AVC_NALTYPE_SLICE :

        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "decode: AVC_NALTYPE_SLICE or AVC_NALTYPE_IDR");

        // ===========>
        if ((status=decoder->DecodeAVCSlice(aInputBuf,&size))>AVCDEC_FAIL)
        {
        
          // ===========>
          // decoder->GetDecOutput(&indexFrame,&releaseFrame,&outVid);
          // xxx pa react on dbp:: DPBInitBuffer return AVC_NO_BUFFER failures (which should not happen)
          if ((decoder->GetDecOutput(&indexFrame,&releaseFrame,&outVid) == AVC_NO_BUFFER))
          {
            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,  "decode: GetDecOutput failed");
            return 0;
          }
        
          if (releaseFrame == 1){
            // ===========>
            decoder->AVC_FrameUnbind(indexFrame);
          }

          /* Copy result to YUV  array ! */
          memcpy(aOutBuffer,outVid.YCbCr[0],iSrcWidth*iSrcHeight);
          memcpy(aOutBuffer+(iSrcWidth*iSrcHeight),outVid.YCbCr[1],(iSrcWidth*iSrcHeight)/4);
          memcpy(aOutBuffer+(iSrcWidth*iSrcHeight)+((iSrcWidth*iSrcHeight)/4),outVid.YCbCr[2],(iSrcWidth*iSrcHeight)/4);
          
          /* Create the output buffer */
          uint32* resultBuffer= (uint32*) malloc(iSrcWidth*iSrcHeight*sizeof(uint32));
          if (resultBuffer == NULL) 
            return 0;
          
          /**********  Convert to rgb  ***********/
          convert(iSrcWidth,iSrcHeight,aOutBuffer,resultBuffer);
          
          /* Return Bitmap image */
          (env)->SetIntArrayRegion(decoded, 0, iSrcWidth*iSrcHeight, (const jint*)resultBuffer);
          free(resultBuffer);
          
        } else {
          __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "status: %ld",status);
        }
      break;
    default:
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "decode: UNKNOWN NAL unit type: %d", nal_unit_type);

  }
  return 1;
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder
 * Method:    InitParser
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_InitParser
  (JNIEnv *env, jclass clazz, jstring pathToFile){

  return 0;

}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    DeinitParser
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_DeinitParser
  (JNIEnv *env, jclass clazz){
        parserInitialized = 0;
        return release();
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoLength
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_getVideoLength
  (JNIEnv *env, jclass clazz)
{
  jint videoLength = getVideoDuration();
  return videoLength;
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoWidth
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_getVideoWidth
  (JNIEnv *env, jclass clazz)
{
    return getVideoWidth();
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoHeight
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_getVideoHeight
  (JNIEnv *env, jclass clazz)
{
    return getVideoHeight();
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoCoding
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_getVideoCoding
  (JNIEnv *env, jclass clazz)
{
  char* charVideoCoding = getVideoCodec();
  jstring stringVideoCoding = (env)->NewStringUTF(charVideoCoding);
  return stringVideoCoding;
}

/**
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoSample
 * Signature: ([I)Lcom/orangelabs/rcs/core/ims/protocol/rtp/codec/video/VideoSample
 */
JNIEXPORT jobject JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_decoder_NativeH264Decoder_getVideoSample
  (JNIEnv *env, jclass clazz, jintArray Decoded)
{
        jobject object = NULL;
        // Return created object
        return object;
}

/**
 * This is called by the VM when the shared library is first loaded.
 */
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint result = -1;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        goto bail;
    }
    /* success -- return valid version number */
    result = JNI_VERSION_1_4;
bail:
    return result;
}

