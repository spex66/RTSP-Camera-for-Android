/* ------------------------------------------------------------------
 * Copyright (C) 2009 OrangeLabs
 *
 * Author: Alexis Gilabert Senar
 * Date: 2009-07-01
 * -------------------------------------------------------------------
 */
#define LOG_TAG "NativeEnc"
#include "com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_encoder_NativeH263Encoder.h"
#include <stdio.h>
#include "mp4enc_api.h"


    VideoEncControls iEncoderControl;
    VideoEncOptions aEncOption;
    int iSrcHeight;
    int iSrcWidth;
    unsigned long long NextTimestamp;
    uint8* aOutBuffer;
    uint8* YUV;
    uint8* yuvPtr;
    ULong modTime;

/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_encoder_NativeH263Encoder
 * Method:    InitEncoder
 * Signature: (Lcom/orangelabs/rcs/core/ims/protocol/rtp/codec/h263/encoder/EncOptions;)I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_encoder_NativeH263Encoder_InitEncoder
  (JNIEnv *env, jclass iclass, jobject params)
{
    /**
      * Clean encoder
      */
    memset((void*) &iEncoderControl, 0, sizeof(VideoEncControls));
    PVCleanUpVideoEncoder(&iEncoderControl);

    /**
      * Setting encoder options
      */
    PVGetDefaultEncOption(&aEncOption, 0);

    jclass objClass = (env)->GetObjectClass(params);
    if (objClass == NULL) return -2;

    jfieldID encMode = (env)->GetFieldID(objClass,"encMode","I");
    if (encMode == NULL) return -3;
    aEncOption.encMode= (MP4EncodingMode)(env)->GetIntField(params,encMode);
    //LOGI("[H263Encoder parameters] encMode = %d",aEncOption.encMode);

    jfieldID packetSize = (env)->GetFieldID(objClass,"packetSize","I");
    if (packetSize == NULL) return -4;
    aEncOption.packetSize= (env)->GetIntField(params,packetSize);
    //LOGI("[H263Encoder parameters] packetSize = %d",aEncOption.packetSize);

    jfieldID profile_level = (env)->GetFieldID(objClass,"profile_level","I");
    if (profile_level == NULL) return -5;
    aEncOption.profile_level= (ProfileLevelType)(env)->GetIntField(params,profile_level);
    //LOGI("[H263Encoder parameters] profile_level = %d",aEncOption.profile_level);

    jfieldID rvlcEnable = (env)->GetFieldID(objClass,"rvlcEnable","Z");
    if (rvlcEnable == NULL) return -6;
    aEncOption.rvlcEnable = ((env)->GetBooleanField(params,rvlcEnable) == true)? (ParamEncMode)1:(ParamEncMode)0;
    //LOGI("[H263Encoder parameters] rvlcEnable = %d",aEncOption.rvlcEnable);

    jfieldID gobHeaderInterval = (env)->GetFieldID(objClass,"gobHeaderInterval","I");
    if (gobHeaderInterval == NULL) return -7;
    aEncOption.gobHeaderInterval = (env)->GetIntField(params,gobHeaderInterval);
    //LOGI("[H263Encoder parameters] gobHeaderInterval = %d",aEncOption.gobHeaderInterval);

    jfieldID numLayers = (env)->GetFieldID(objClass,"numLayers","I");
    if (numLayers == NULL) return -8;
    aEncOption.numLayers= (env)->GetIntField(params,numLayers);
    //LOGI("[H263Encoder parameters] numLayers = %d",aEncOption.numLayers);

    jfieldID timeIncRes = (env)->GetFieldID(objClass,"timeIncRes","I");
    if (timeIncRes == NULL) return -9;
    aEncOption.timeIncRes = (env)->GetIntField(params,timeIncRes);
    //LOGI("[H263Encoder parameters] timeIncRes = %d",aEncOption.timeIncRes);

    jfieldID tickPerSrc = (env)->GetFieldID(objClass,"tickPerSrc","I");
    if (tickPerSrc == NULL) return -10;
    aEncOption.tickPerSrc= (env)->GetIntField(params,tickPerSrc);
    //LOGI("[H263Encoder parameters] tickPerSrc = %d",aEncOption.tickPerSrc);

    jfieldID encHeight = (env)->GetFieldID(objClass,"encHeight","I");
    if (encHeight == NULL) return -11;
    aEncOption.encHeight[0] = aEncOption.encHeight[1] = (env)->GetIntField(params,encHeight);
    //LOGI("[H263Encoder parameters] encHeight = %d",aEncOption.encHeight[0]);

    jfieldID encWidth = (env)->GetFieldID(objClass,"encWidth","I");
    if (encWidth == NULL) return -12;
    aEncOption.encWidth[0] = aEncOption.encWidth[1] = (env)->GetIntField(params,encWidth);
    //LOGI("[H263Encoder parameters] encWidth = %d",aEncOption.encWidth[0]);

    jfieldID encFrameRate = (env)->GetFieldID(objClass,"encFrameRate","F");
    if (encFrameRate == NULL) return -13;
    aEncOption.encFrameRate[0] = aEncOption.encFrameRate[1] = (env)->GetFloatField(params,encFrameRate);
    //LOGI("[H263Encoder parameters] encFrameRate = %f",aEncOption.encFrameRate[0]);

    jfieldID bitRate = (env)->GetFieldID(objClass,"bitRate","I");
    if (bitRate == NULL) return -14;
    aEncOption.bitRate[0] = aEncOption.bitRate[1] = (env)->GetIntField(params,bitRate);
    //LOGI("[H263Encoder parameters] bitRate = %d",aEncOption.bitRate[0]);

    jfieldID iQuant = (env)->GetFieldID(objClass,"iQuant","I");
    if (iQuant == NULL) return -15;
    aEncOption.iQuant[0] = aEncOption.iQuant[1] = (env)->GetIntField(params,iQuant);
    //LOGI("[H263Encoder parameters] iQuant = %d",aEncOption.iQuant[0]);

    jfieldID pQuant = (env)->GetFieldID(objClass,"pQuant","I");
    if (pQuant == NULL) return -16;
    aEncOption.pQuant[0] = aEncOption.pQuant[1] = (env)->GetIntField(params,pQuant);
    //LOGI("[H263Encoder parameters] pQuant = %d",aEncOption.pQuant[0]);

    jfieldID quantType = (env)->GetFieldID(objClass,"quantType","I");
    if (quantType == NULL) return -17;
    aEncOption.quantType[0] = aEncOption.quantType[1] = (env)->GetIntField(params,quantType);
    //LOGI("[H263Encoder parameters] quantType = %d",aEncOption.quantType[0]);

    jfieldID rcType = (env)->GetFieldID(objClass,"rcType","I");
    if (rcType == NULL) return -18;
    aEncOption.rcType = (MP4RateControlType)(env)->GetIntField(params,rcType);
    //LOGI("[H263Encoder parameters] rcType = %d",aEncOption.rcType);

    jfieldID vbvDelay = (env)->GetFieldID(objClass,"vbvDelay","F");
    if (vbvDelay == NULL) return -19;
    aEncOption.vbvDelay = (env)->GetFloatField(params,vbvDelay);
    //LOGI("[H263Encoder parameters] vbvDelay = %f",aEncOption.vbvDelay);

    jfieldID noFrameSkipped = (env)->GetFieldID(objClass,"noFrameSkipped","Z");
    if (noFrameSkipped == NULL) return -20;
    aEncOption.noFrameSkipped = ((env)->GetBooleanField(params,noFrameSkipped) == true)? (ParamEncMode)1:(ParamEncMode)0;
    //LOGI("[H263Encoder parameters] noFrameSkipped = %d",aEncOption.noFrameSkipped);

    jfieldID intraPeriod = (env)->GetFieldID(objClass,"intraPeriod","I");
    if (intraPeriod == NULL) return -21;
    aEncOption.intraPeriod = (env)->GetIntField(params,intraPeriod);
    //LOGI("[H263Encoder parameters] intraPeriod = %d",aEncOption.intraPeriod);

    jfieldID numIntraMB = (env)->GetFieldID(objClass,"numIntraMB","I");
    if (numIntraMB == NULL) return -22;
    aEncOption.numIntraMB = (env)->GetIntField(params,numIntraMB);
    //LOGI("[H263Encoder parameters] numIntraMB = %d",aEncOption.numIntraMB);

    jfieldID sceneDetect = (env)->GetFieldID(objClass,"sceneDetect","Z");
    if (sceneDetect == NULL) return -23;
    aEncOption.sceneDetect = ((env)->GetBooleanField(params,sceneDetect) == true)?(ParamEncMode)1:(ParamEncMode)0;
    //LOGI("[H263Encoder parameters] sceneDetect = %d",aEncOption.sceneDetect);

    jfieldID searchRange = (env)->GetFieldID(objClass,"searchRange","I");
    if (searchRange == NULL) return -24;
    aEncOption.searchRange = (env)->GetIntField(params,searchRange);
    //LOGI("[H263Encoder parameters] searchRange = %d",aEncOption.searchRange);

    jfieldID mv8x8Enable = (env)->GetFieldID(objClass,"mv8x8Enable","Z");
    if (mv8x8Enable == NULL) return -25;
    aEncOption.mv8x8Enable = ((env)->GetBooleanField(params,mv8x8Enable)==true)?(ParamEncMode)1:(ParamEncMode)0;
    //LOGI("[H263Encoder parameters] mv8x8Enable = %d",aEncOption.mv8x8Enable);

    jfieldID intraDCVlcTh = (env)->GetFieldID(objClass,"intraDCVlcTh","I");
    if (intraDCVlcTh == NULL) return -26;
    aEncOption.intraDCVlcTh = (env)->GetIntField(params,intraDCVlcTh);
    //LOGI("[H263Encoder parameters] intraDCVlcTh= %d",aEncOption.intraDCVlcTh);

    jfieldID useACPred = (env)->GetFieldID(objClass,"useACPred","Z");
    if (useACPred == NULL) return -27;
    aEncOption.useACPred = ((env)->GetBooleanField(params,useACPred)==true)?1:0;
    //LOGI("[H263Encoder parameters] useACPred = %d",aEncOption.useACPred);

    /**
      * Init
      */
    iSrcWidth = aEncOption.encWidth[0];
    //LOGI("[H263Encoder parameters] iSrcWidth = %d",iSrcWidth);
    iSrcHeight = aEncOption.encHeight[0];
    //LOGI("[H263Encoder parameters] iSrcHeight = %d",iSrcHeight);
    modTime = 0;

    /**
      * Init ptr for encode method
      */
    YUV =  (uint8*)malloc((iSrcWidth*iSrcHeight*3/2));
    if (YUV == NULL){
      return -1;
    }
    yuvPtr = YUV;

    aOutBuffer = (uint8*)malloc((iSrcWidth*iSrcHeight*3/2));
    if (aOutBuffer == NULL){
      return -1;
    }

    /**
      * Init encoder
      */
    return PVInitVideoEncoder(&iEncoderControl, &aEncOption);

}


/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_encoder_NativeH263Encoder
 * Method:    EncodeFrame
 * Signature: ([BIJ)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_encoder_NativeH263Encoder_EncodeFrame
  (JNIEnv *env, jclass iclass, jbyteArray frame, jlong timestamp)
{
    VideoEncFrameIO vid_in, vid_out;
    int Size = 0;
    uint status;
    int nLayer = 0;
    jbyteArray result ;

    /**
      * Init for EncodeFrame
      */
    jint len = env->GetArrayLength(frame);
    // Read the byte array
    uint8* data = (uint8*)malloc(len);
    uint8* whereToFree = data;
    env->GetByteArrayRegion (frame, (jint)0, (jint)len, (jbyte*)data);

    // Convert YUV input to have distinct Y U and V channels
    // Copy Y data
    yuvPtr = YUV;
    for (int i=0;i<iSrcHeight;i++){
      for (int j=0;j<iSrcWidth;j++){
    	  *yuvPtr= *data;
    	  yuvPtr++;
    	  data++;
      }
    }

    // Copy UV data
    uint8 *uPos = YUV + (iSrcWidth*iSrcHeight);
    uint8 *vPos = YUV + (iSrcWidth*iSrcHeight) + ((iSrcWidth*iSrcHeight)>>2);
    uint16 temp = 0;
    uint16* iVideoPtr = (uint16*)data;
    for (int i=0;i<(iSrcHeight>>1);i++){
	for (int j=0;j<(iSrcWidth>>1);j++){
		temp = *iVideoPtr++; // U1V1
		*vPos++= (uint8)(temp & 0xFF);
		*uPos++= (uint8)((temp >> 8) & 0xFF);
	}
    }

    vid_in.height = iSrcHeight;
    vid_in.pitch = iSrcWidth;
    vid_in.timestamp = (ULong)(timestamp & 0xFFFFFFFF);
    vid_in.yChan = YUV;
    vid_in.uChan = (YUV + vid_in.height * vid_in.pitch);
    vid_in.vChan = vid_in.uChan + ((vid_in.height * vid_in.pitch) >> 2);
    Size = len;

    // encode the frame
    status = PVEncodeVideoFrame(&iEncoderControl, &vid_in, &vid_out, &modTime, (UChar*)aOutBuffer, &Size, &nLayer);
    if (status != 1) return (env)->NewByteArray(1);

    // Copy aOutBuffer into result
    result=(env)->NewByteArray(Size);
    (env)->SetByteArrayRegion(result, 0, Size, (jbyte*)aOutBuffer);
    free(whereToFree);
    // Return
    return result;

}

/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_encoder_NativeH263Encoder
 * Method:    DeinitEncoder
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_encoder_NativeH263Encoder_DeinitEncoder
  (JNIEnv *env, jclass clazz){
    return PVCleanUpVideoEncoder(&iEncoderControl);
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
