/* ------------------------------------------------------------------
 * Copyright (C) 2009 OrangeLabs
 *
 * Author: Alexis Gilabert Senar
 * Date: 2009-07-01
 * -------------------------------------------------------------------
 */
#define LOG_TAG "NativeDec"
#include <stdio.h>
#include <stdlib.h>
#include "com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder.h"
#include "mp4dec_api.h"
#include "3GPVideoParser.h"
#include "yuv2rgb.h"

/*
 * Global variables
 *
*/
  VideoDecControls iDecoderControl;
  uint8* pFrame0,*pFrame1;
  int32 nLayers = 1;
  uint8** volbuf;
  int32 volbuf_size[]= {0};
  int32 iHeight = 0;
  int32 iWidth = 0;
  uint32 FrameSize = 0;
  uint32 VideoDecOutputSize = 0;
  MP4DecodingMode mode= H263_MODE;
  /* Parser */
  uint8* aOutBuffer;
  uint32 aOutBufferSize = 0;
  uint32 aOutTimestamp = 0;
  int parserInitialized = 0;
  int decoderInitialized = 0;
  bool Status = false;

/**
 * De-Init decoder
 */
int deinitDecoder(){
	if (PVCleanUpVideoDecoder(&iDecoderControl)== 1) {
		if (pFrame0) free(pFrame0);
		if (pFrame1) free(pFrame1);
		decoderInitialized = 0;
		return 1;
	} else {
		if (pFrame0) free(pFrame0);
		if (pFrame1) free(pFrame1);
		decoderInitialized = 0;
		return 0;
	}
}

/**
 * Init decoder
 * @param srcWidth video width
 * @param srcHeight video height
 */
int initDecoder(int srcWidth, int srcHeight){
	if (decoderInitialized == 1) deinitDecoder();
	iWidth = srcWidth;
	iHeight = srcHeight;
	FrameSize = (srcWidth * srcHeight);
	VideoDecOutputSize = (FrameSize * 3) >> 1;
	volbuf_size[0]= VideoDecOutputSize;
	volbuf = (uint8**)malloc(nLayers * sizeof(uint8*));
	if (volbuf == NULL) return 0;
	volbuf[0] = (uint8*)malloc(volbuf_size[0]);
	if (volbuf[0] == NULL) return 0;
	memset(*volbuf,0,volbuf_size[0]);
	if (!PVInitVideoDecoder(&iDecoderControl,volbuf, volbuf_size, nLayers, iWidth, iHeight, mode)) return 0;
	PVSetPostProcType(&iDecoderControl,2);
	pFrame0 = (uint8*) malloc(VideoDecOutputSize);
	if (pFrame0 == NULL) return 0;
	pFrame1 = (uint8*) malloc(VideoDecOutputSize);
	if (pFrame1 == NULL) return 0;
	memset(pFrame1,0,VideoDecOutputSize);
	PVSetReferenceYUV(&iDecoderControl, pFrame1);
	decoderInitialized = 1;
	return decoderInitialized;
 }

/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder
 * Method:    InitDecoder
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder_InitDecoder
  (JNIEnv * env, jclass clazz, jint srcWidth, jint srcHeight){
	return initDecoder(srcWidth,srcHeight);
}

/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder
 * Method:    DeinitDecoder
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder_DeinitDecoder
  (JNIEnv * env, jclass clazz){
	return deinitDecoder();
}

/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder
 * Method:    DecodeAndConvert
 * Signature: ([B[IJ)[I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder_DecodeAndConvert
  (JNIEnv *env, jclass clazz, jbyteArray h263Frame, jintArray decoded, jlong jtimestamp){

	/* Return if decoder is not initialized */
	if (!decoderInitialized){
		return 0;
	}

	/* Set volbuf with h263Frame data*/
	jint len = env->GetArrayLength(h263Frame);
	jbyte data[len];
	env->GetByteArrayRegion(h263Frame, 0, len, data);

	/* Decode */
	uint32 timestamp[]={(uint32)(jtimestamp & 0xFFFFFFFF)};
	uint usetimestamp[]={0};
	volbuf[0] = (uint8*)data;
	volbuf_size[0]=len;

	if (PVDecodeVideoFrame(&iDecoderControl, volbuf,timestamp,volbuf_size,usetimestamp,pFrame0) == 0){
		return 0;
	}

	/* Copy result to YUV  array ! */
	uint8* decodedFrame = iDecoderControl.outputFrame;
	uint8* pTempFrame;
	pTempFrame = (uint8*) pFrame0;
	pFrame0 = (uint8*) pFrame1;
	pFrame1 = (uint8*) pTempFrame;

	/* Create the output buffer */
	uint32* resultBuffer= (uint32*) malloc(iWidth*iHeight*sizeof(uint32));
	if (resultBuffer == NULL) return 0;

	/***********  Convert to rgb  ***************/
	if (convert(iWidth,iHeight,decodedFrame,resultBuffer) == 0){
	  return 0;
	}

	/* Return Bitmap image */
	(env)->SetIntArrayRegion(decoded, 0, iWidth*iHeight, (const jint*)resultBuffer);
	free(resultBuffer);
	return 1;

}

/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder
 * Method:    InitParser
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder_InitParser
  (JNIEnv *env, jclass clazz, jstring pathToFile){

  char *str;
  str = (char*)env->GetStringUTFChars(pathToFile, NULL);
  if (str == NULL) return 0;

  /* Init parser */
  if (parserInitialized == 1){
	  parserInitialized = 0;
	  release();
  }
  if(Init3GPVideoParser(str) == 1){
	env->ReleaseStringUTFChars(pathToFile, str);
	iWidth = getVideoWidth();
	iHeight = getVideoHeight();
	FrameSize = iWidth * iHeight;
	VideoDecOutputSize = (FrameSize * 3)>>1;
	aOutBuffer = (uint8*)malloc(VideoDecOutputSize);
	parserInitialized = 1;
  } else {
	env->ReleaseStringUTFChars(pathToFile, str);
	return 0;
  }

  /* Init decoder */
  if (decoderInitialized == 1){
	  deinitDecoder();
  }
  if(initDecoder(iWidth,iHeight)== 0) return 0;

  return 1;

}

/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    DeinitParser
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder_DeinitParser
  (JNIEnv *env, jclass clazz){
	if (decoderInitialized == 1) deinitDecoder();
	parserInitialized = 0;
	return release();
}

/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoLength
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder_getVideoLength
  (JNIEnv *env, jclass clazz){
	jint videoLength = getVideoDuration();
  return videoLength;
}

/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoWidth
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder_getVideoWidth
  (JNIEnv *env, jclass clazz)
{
    return getVideoWidth();
}

/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoHeight
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder_getVideoHeight
  (JNIEnv *env, jclass clazz)
{
    return getVideoHeight();
}

/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoCoding
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder_getVideoCoding
  (JNIEnv *env, jclass clazz)
{
  jstring stringVideoCoding;
  char* charVideoCoding = getVideoCodec();
  stringVideoCoding = (env)->NewStringUTF(charVideoCoding);
  return stringVideoCoding;
}

/*
 * Class:     com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_Native3GPPFileParser
 * Method:    getVideoSample
 * Signature: ([I)Lcom/orangelabs/rcs/core/ims/protocol/rtp/codec/video/VideoSample
 */
JNIEXPORT jobject JNICALL Java_com_orangelabs_rcs_core_ims_protocol_rtp_codec_video_h263_decoder_NativeH263Decoder_getVideoSample
  (JNIEnv *env, jclass clazz, jintArray Decoded)
{
	jobject object = NULL;

	/* Error return */
	if (parserInitialized == 0){
		return object;
	}
	// Get the new frame
	if (getFrame(aOutBuffer,&aOutBufferSize,&aOutTimestamp)!= VPAtomSucces){
		return object;
	}

	/* Set frame with aOutBuffer data and timestamp*/
	jbyteArray H263Frame = (env)->NewByteArray(aOutBufferSize);
	(env)->SetByteArrayRegion(H263Frame, 0, aOutBufferSize, (const jbyte*)aOutBuffer);

	/* Decode */
	uint32 timestamp[]={aOutTimestamp};
	uint usetimestamp[]={0};
	volbuf[0] = aOutBuffer;
	volbuf_size[0]=aOutBufferSize;

	if (!PVDecodeVideoFrame(&iDecoderControl, volbuf,timestamp,volbuf_size,usetimestamp,pFrame0)){
		return object;
	}
	/* Copy result to YUV  array ! */
	uint8* pTempFrame;
	uint8* decodedFrame = iDecoderControl.outputFrame;
	pTempFrame = (uint8*) pFrame0;
	pFrame0 = (uint8*) pFrame1;
	pFrame1 = (uint8*) pTempFrame;

  	/* Create the output buffer */
	uint32* resultBuffer = (uint32*)malloc(FrameSize*sizeof(uint32));
  	/* Convert YUV to RGB */
	convert(iWidth,iHeight,decodedFrame,resultBuffer);
  	/* Set Decoded */
	(env)->SetIntArrayRegion(Decoded, 0, FrameSize, (const jint*)resultBuffer);
  	free(resultBuffer);
	
	// Create new object
	/* Find class and method to return VideoSample*/
	jclass classe = (env)->FindClass("com/orangelabs/rcs/core/ims/protocol/rtp/codec/video/h263/decoder/VideoSample");
	if (classe == 0) {
	  return object;
	}
	jmethodID mid = (env)->GetMethodID(classe,"<init>","([BI)V");
	if (mid == 0) {
	  return object;
	}
	object = (env)->NewObject(classe,mid,H263Frame,aOutTimestamp);
	if (object == 0) {
	  return object;
	}
	// Return created object
	return object;
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

