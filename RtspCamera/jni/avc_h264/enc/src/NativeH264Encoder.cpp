/* ------------------------------------------------------------------
 * Copyright (C) 2009 OrangeLabs
 *
 * Author: Alexis Gilabert Senar
 * Date: 2009-07-01
 * -------------------------------------------------------------------
 */
#define LOG_TAG "NativeEnc"
#include "NativeH264Encoder.h"
#include "pvavcencoder.h"
#include "android/log.h"

int     iSrcWidth;
int     iSrcHeight;
float   iSrcFrameRate;
int        FrameSize;
//int        NalComplete = 0;
// xxx pa
int        NalComplete = 1;

int SkipNextEncoding = 0;

/* variables needed in operation */
PVAVCEncoder                *encoder;
TAVCEIInputFormat        *iInputFormat;
TAVCEIEncodeParam        *iEncodeParam;
TAVCEIInputData                *iInData;
TAVCEIOutputData        *iOutData;
TAVCEI_RETVAL                status;


/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_encoder_NativeH264Encoder
 * Method:    InitEncoder
 * Signature: (IIF)I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_encoder_NativeH264Encoder_InitEncoder
  (JNIEnv *env, jclass iclass, jint width, jint height, jint framerate)
{
    /**
      * Init
      */
    iSrcWidth = width;
    iSrcHeight = height;
    iSrcFrameRate = framerate;
    FrameSize = (iSrcWidth*iSrcHeight*3)>>1;

    encoder = PVAVCEncoder::New();
    if (encoder==NULL) return 0;
    
    iInputFormat = (TAVCEIInputFormat*)malloc(sizeof(TAVCEIInputFormat));
    if (iInputFormat==NULL) {
      delete(encoder);
      return 0;
    }

    iEncodeParam = (TAVCEIEncodeParam*)malloc(sizeof(TAVCEIEncodeParam));
    if (iEncodeParam==NULL) {
      free(iInputFormat);
      delete(encoder);
      return 0;
    }

    iInData = (TAVCEIInputData*)malloc(sizeof(TAVCEIInputData));
    if(iInData==NULL){
      free(iEncodeParam);
      free(iInputFormat);
      delete(encoder);
      return 0;
    }

    iOutData = (TAVCEIOutputData*)malloc(sizeof(TAVCEIOutputData));
    if(iOutData==NULL){
      free(iInData);
      free(iEncodeParam);
      free(iInputFormat);
      delete(encoder);
      return 0;
    }
    iOutData->iBitstream = (uint8*)malloc(FrameSize);
    iOutData->iBitstreamSize = FrameSize;

    /**
      * Set Encoder params
      */
    iInputFormat->iFrameWidth = width;
    iInputFormat->iFrameHeight = height;
    iInputFormat->iFrameRate = (OsclFloat)(framerate);
    iInputFormat->iFrameOrientation = -1;
    iInputFormat->iVideoFormat = EAVCEI_VDOFMT_YUV420SEMIPLANAR;


    iEncodeParam->iEncodeID = 0;
    iEncodeParam->iProfile = EAVCEI_PROFILE_BASELINE;

    // xxx pa switch level due to changed screen size and bandwidth
    //    iEncodeParam->iLevel = EAVCEI_LEVEL_1B;
    iEncodeParam->iLevel = EAVCEI_LEVEL_12;
    
    iEncodeParam->iNumLayer = 1;
    iEncodeParam->iFrameWidth[0] = iInputFormat->iFrameWidth;
    iEncodeParam->iFrameHeight[0] = iInputFormat->iFrameHeight;
    iEncodeParam->iBitRate[0] = 64000;
    iEncodeParam->iFrameRate[0] = (OsclFloat)iInputFormat->iFrameRate;
    iEncodeParam->iEncMode = EAVCEI_ENCMODE_TWOWAY;
    // iEncodeParam->iOutOfBandParamSet = true;
    
    // xxx pa 120503 set to in-band parameter to trigger SPS and PPS with each IFrame
    iEncodeParam->iOutOfBandParamSet = false;
    
    iEncodeParam->iOutputFormat = EAVCEI_OUTPUT_RTP;
    iEncodeParam->iPacketSize = 8192;
    iEncodeParam->iRateControlType = EAVCEI_RC_CBR_1;
    iEncodeParam->iBufferDelay = (OsclFloat)2.0;
    iEncodeParam->iIquant[0]=15;
    iEncodeParam->iPquant[0]=12;
    iEncodeParam->iBquant[0]=0;
    iEncodeParam->iSceneDetection = false;
    // iEncodeParam->iIFrameInterval = 15;
    // xxx pa 120503 set shorten IFrame intervall for more often SPS/PPS NAL units
    iEncodeParam->iIFrameInterval = 2;
    iEncodeParam->iNumIntraMBRefresh = 50;
    iEncodeParam->iClipDuration = 0;
    iEncodeParam->iFSIBuff = NULL;
    iEncodeParam->iFSIBuffLength = 0;

    /**
      * Init encoder
      */
    return encoder->Initialize(iInputFormat,iEncodeParam);

}


/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_encoder_NativeH264Encoder
 * Method:    EncodeFrame
 * Signature: ([BJ)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_encoder_NativeH264Encoder_EncodeFrame
  (JNIEnv *env, jclass iclass, jbyteArray frame, jlong timestamp)
{
    jbyteArray result ;

    /**
      * Check NAL
      */
    if (NalComplete == 0){
      __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Checking NAL");
      int32 NalSize = 30;
      int NalType = 0;
      uint8* NalBuff = (uint8*)malloc(NalSize*sizeof(uint8));
      if(encoder->GetParameterSet(NalBuff,&NalSize,&NalType)== EAVCEI_SUCCESS){
        result=(env)->NewByteArray(NalSize);
        (env)->SetByteArrayRegion(result, 0, NalSize, (jbyte*)NalBuff);
        free(NalBuff);

        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Checking NAL GetParameterSet->EAVCEI_SUCCESS status? %d", status);

      return result;
      } else {
        NalComplete = 1; // Now encode video
      }
    }

   
    // only encode if not in MORE_NAL state
    jint len = env->GetArrayLength(frame);
    uint8* data = (uint8*)malloc(len);
    
    if (SkipNextEncoding == 0){
    
        /**
          * EncodeFrame
          */
        env->GetByteArrayRegion (frame, (jint)0, (jint)len, (jbyte*)data);

        iInData->iSource=(uint8*)data;
        iInData->iTimeStamp = timestamp;
        
        // ==============>
        status = encoder->Encode(iInData);
        
        if(status != EAVCEI_SUCCESS){
          __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Encode fail with code: %d",status);
          result=(env)->NewByteArray(0);
          free(data);
          return result;
        }
    } else {
        /**
        * xxx pa skipped encoding due to MORE NAL output signal
        */
      __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Skiped encoding");
    }
    
    int remainingByte = 0;
    iOutData->iBitstreamSize = FrameSize;
    
    // ==============>
    status = encoder->GetOutput(iOutData,&remainingByte);

    
    if(!(status == EAVCEI_SUCCESS || status == EAVCEI_MORE_NAL)){
//    if(status != EAVCEI_SUCCESS){
      __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Get output fail with code: %d",status);
      result=(env)->NewByteArray(0);
      free(data);
      return result;
    }

    // try to get more NAL
    if(status == EAVCEI_MORE_NAL){
        SkipNextEncoding = 1;
    } else {
        // reset flag
        SkipNextEncoding = 0;
    }
    
    // Copy aOutBuffer into result
    result=(env)->NewByteArray(iOutData->iBitstreamSize);
    (env)->SetByteArrayRegion(result, 0, iOutData->iBitstreamSize, (jbyte*)iOutData->iBitstream);
    free(data);
    return result;

}

/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_encoder_NativeH264Encoder
 * Method:    getLastEncodeStatus
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_encoder_NativeH264Encoder_getLastEncodeStatus
  (JNIEnv *env, jclass clazz){
    return status;
}

/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_encoder_NativeH264Encoder
 * Method:    DeinitEncoder
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h264_encoder_NativeH264Encoder_DeinitEncoder
  (JNIEnv *env, jclass clazz){
    delete(encoder);
    free(iInputFormat);
    free(iEncodeParam);
    free(iInData);
    free(iOutData);
    NalComplete = 0;
    return 1;
}

/*
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
