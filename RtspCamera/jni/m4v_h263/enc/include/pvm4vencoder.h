/* ------------------------------------------------------------------
 * Copyright (C) 1998-2009 PacketVideo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 * -------------------------------------------------------------------
 */
/*  File: pvm4vencoder.h                                                        */
/** This file contains MP4 encoder related classes, structures and enumerations. */

#ifndef __PVM4VENCODER_H
#define __PVM4VENCODER_H

#include "cvei.h"
#include "mp4enc_api.h"
#include "ccrgb24toyuv420.h"
#include "ccrgb12toyuv420.h"
#include "ccyuv420semitoyuv420.h"

#define KCVEIMaxOutputBuffer    10
#define VISUAL_OBJECT_SEQUENCE_START_CODE   0x01B0
#define VISUAL_OBJECT_SEQUENCE_END_CODE     0x01B1
#define VISUAL_OBJECT_START_CODE   0x01B5
#define VO_START_CODE           0x8
#define VO_HEADER_LENGTH        32
#define VOL_START_CODE 0x12
#define VOL_START_CODE_LENGTH 28

#define GROUP_START_CODE    0x01B3
#define GROUP_START_CODE_LENGTH  32

#define VOP_ID_CODE_LENGTH      5
#define VOP_TEMP_REF_CODE_LENGTH    16

#define USER_DATA_START_CODE        0x01B2
#define USER_DATA_START_CODE_LENGTH 32

#define SHORT_VIDEO_START_MARKER        0x20
#define SHORT_VIDEO_START_MARKER_LENGTH  22


/** Encoding mode specific to MPEG4. */
enum TMP4EncodingMode
{
    /** H263 mode. */
    EH263_MODE,

    /** Data partitioning mode, packet size must be specified. */
    EDATA_PARTITIONG_MODE,

    /** Combined mode without resync markers. */
    ECOMBINING_MODE_NO_ERR_RES,

    /** COmbined mode with resync markers, packet size must be specified. */
    ECOMBINING_MODE_WITH_ERR_RES
};

/** Generic ON/OFF. */
enum TParamEncMode
{
    EPV_OFF,
    EPV_ON
};

typedef struct
{
    uint8 *data;
    uint32 numBytes;
    uint32 bytePos;
    uint32 bitBuf;
    uint32 dataBitPos;
    uint32  bitPos;
} mp4StreamType;

static const uint32 MASK[33] =
{
    0x00000000, 0x00000001, 0x00000003, 0x00000007,
    0x0000000f, 0x0000001f, 0x0000003f, 0x0000007f,
    0x000000ff, 0x000001ff, 0x000003ff, 0x000007ff,
    0x00000fff, 0x00001fff, 0x00003fff, 0x00007fff,
    0x0000ffff, 0x0001ffff, 0x0003ffff, 0x0007ffff,
    0x000fffff, 0x001fffff, 0x003fffff, 0x007fffff,
    0x00ffffff, 0x01ffffff, 0x03ffffff, 0x07ffffff,
    0x0fffffff, 0x1fffffff, 0x3fffffff, 0x7fffffff,
    0xffffffff
};

/** MPEG4 encoder class interface. See CommonVideoEncoder APIs for
virtual functions definitions. */
class CPVM4VEncoder : public CommonVideoEncoder
{

    public:
        OSCL_IMPORT_REF static CPVM4VEncoder* New(int32 aThreadId);
        OSCL_IMPORT_REF ~CPVM4VEncoder();

        OSCL_IMPORT_REF virtual TCVEI_RETVAL SetObserver(MPVCVEIObserver *aObserver);
        OSCL_IMPORT_REF virtual TCVEI_RETVAL AddBuffer(TPVVideoOutputData *aVidOut);
        OSCL_IMPORT_REF virtual TCVEI_RETVAL Encode(TPVVideoInputData *aVidIn);

        OSCL_IMPORT_REF virtual TCVEI_RETVAL Initialize(TPVVideoInputFormat *aVidInFormat, TPVVideoEncodeParam *aEncParam);
        OSCL_IMPORT_REF virtual int32 GetBufferSize();
        OSCL_IMPORT_REF virtual TCVEI_RETVAL GetVolHeader(uint8 *volHeader, int32 *size, int32 layer);

        OSCL_IMPORT_REF virtual TCVEI_RETVAL EncodeFrame(TPVVideoInputData  *aVidIn, TPVVideoOutputData *aVidOut, int *aRemainingBytes
#ifdef PVAUTHOR_PROFILING
                , void *aParam1 = 0
#endif
                                                        );

        OSCL_IMPORT_REF virtual TCVEI_RETVAL FlushOutput(TPVVideoOutputData *aVidOut);
        OSCL_IMPORT_REF virtual TCVEI_RETVAL Terminate();
        OSCL_IMPORT_REF virtual TCVEI_RETVAL UpdateBitRate(int32 aNumLayer, int32 *aBitRate);
        OSCL_IMPORT_REF virtual TCVEI_RETVAL UpdateFrameRate(int32 aNumLayer, float *aFrameRate);
        OSCL_IMPORT_REF virtual TCVEI_RETVAL UpdateIFrameInterval(int32 aIFrameInterval);
        OSCL_IMPORT_REF virtual TCVEI_RETVAL IFrameRequest();

        /** Set the forced number of intra macroblock per frame for error resiliency. */
        OSCL_IMPORT_REF TCVEI_RETVAL SetIntraMBRefresh(int32 aNumMBRefresh);

        OSCL_IMPORT_REF virtual int32 GetEncodeWidth(int32 aLayer);
        OSCL_IMPORT_REF virtual int32 GetEncodeHeight(int32 aLayer);
        OSCL_IMPORT_REF virtual float GetEncodeFrameRate(int32 aLayer);
    private:

        CPVM4VEncoder();
        bool Construct(int32 aThreadId);
#ifdef  YUV_INPUT
        void CopyToYUVIn(uint8 *YUV, int width, int height, int width_16, int height_16);
#endif

        /** Color conversion instance RGB24/RGB12/YUV420SEMI to YUV 420 */
#if defined(RGB24_INPUT) || defined (RGB12_INPUT) || defined(YUV420SEMIPLANAR_INPUT)
        ColorConvertBase *ccRGBtoYUV;
#endif

#ifdef FOR_3GPP_COMPLIANCE
        void Check3GPPCompliance(TPVVideoEncodeParam *aEncParam, int *aEncWidth, int *aEncHeight);
#endif

        /* Parsing FSI */
        TCVEI_RETVAL ParseFSI(uint8* aFSIBuff, int FSILength, VideoEncOptions *aEncOption);
        int16 ShowBits(mp4StreamType *pStream, uint8 ucNBits, uint32 *pulOutData);
        int16 FlushBits(mp4StreamType *pStream, uint8 ucNBits);
        int16 ReadBits(mp4StreamType *pStream, uint8 ucNBits, uint32 *pulOutData);
        int16 ByteAlign(mp4StreamType *pStream);
        int16 iDecodeShortHeader(mp4StreamType *psBits, VideoEncOptions *aEncOption);


        /* Pure virtuals from OsclActiveObject implemented in this derived class */
        virtual void Run(void);
        virtual void DoCancel(void);
        MPVCVEIObserver *iObserver;

        int     iSrcWidth;
        int     iSrcHeight;
        int     iSrcFrameRate;
        int     iFrameOrientation;
        int     iEncWidth[4];
        int     iEncHeight[4];
        float   iEncFrameRate[4];
        TPVVideoFormat  iVideoFormat;

        /* variables needed in operation */
        VideoEncControls iEncoderControl;
        bool    iInitialized;
        uint8   *iYUVIn;
        uint8   *iVideoIn;
        uint8   *iVideoOut;
        TPVVideoOutputData *iOutputData[KCVEIMaxOutputBuffer];
        int32       iNumOutputData;
        uint32      iTimeStamp;
        uint32      iNextModTime;
        uint8   *iOverrunBuffer;
        int     iOBSize;

        /* Tables in color coversion */
        uint8  *iY_Table;
        uint16 *iCb_Table, *iCr_Table, *ipCb_Table, *ipCr_Table;


        int     iNumLayer;
};

#endif
