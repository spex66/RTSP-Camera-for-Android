/* ------------------------------------------------------------------
 * Copyright (C) 1998-2010 PacketVideo
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
#include "pvavcencoder.h"
#include "oscl_mem.h"

// xxx pa
#define LOG_TAG "pvaencoder"
#include "android/log.h"


/* global static functions */

void CbAvcEncDebugLog(uint32 *userData, AVCLogType type, char *string1, int val1, int val2)
{
    OSCL_UNUSED_ARG(userData);
    OSCL_UNUSED_ARG(type);
    OSCL_UNUSED_ARG(string1);
    OSCL_UNUSED_ARG(val1);
    OSCL_UNUSED_ARG(val2);

    return ;
}

int CbAvcEncMalloc(void *userData, int32 size, int attribute)
{
    OSCL_UNUSED_ARG(userData);
    OSCL_UNUSED_ARG(attribute);

    uint8 *mem;

    mem = (uint8*) oscl_malloc(size);

    return (int)mem;
}

void CbAvcEncFree(void *userData, int mem)
{
    OSCL_UNUSED_ARG(userData);

    oscl_free((void*)mem);

    return ;
}

int CbAvcEncDPBAlloc(void *userData, uint frame_size_in_mbs, uint num_buffers)
{
    PVAVCEncoder *pAvcEnc = (PVAVCEncoder*) userData;

    return pAvcEnc->AVC_DPBAlloc(frame_size_in_mbs, num_buffers);
}

void CbAvcEncFrameUnbind(void *userData, int indx)
{
    PVAVCEncoder *pAvcEnc = (PVAVCEncoder*) userData;

    pAvcEnc->AVC_FrameUnbind(indx);

    return ;
}

int CbAvcEncFrameBind(void *userData, int indx, uint8 **yuv)
{
    PVAVCEncoder *pAvcEnc = (PVAVCEncoder*) userData;

    return pAvcEnc->AVC_FrameBind(indx, yuv);
}



/* ///////////////////////////////////////////////////////////////////////// */
PVAVCEncoder::PVAVCEncoder()
{

//iEncoderControl
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF PVAVCEncoder::~PVAVCEncoder()
{
    CleanupEncoder();
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF PVAVCEncoder* PVAVCEncoder::New()
{
    PVAVCEncoder* self = new PVAVCEncoder;
    if (self && self->Construct())
        return self;
    if (self)
        delete self;
    return NULL;
}

/* ///////////////////////////////////////////////////////////////////////// */
bool PVAVCEncoder::Construct()
{
    oscl_memset((void *)&iAvcHandle, 0, sizeof(AVCHandle));

    iAvcHandle.CBAVC_DPBAlloc = &CbAvcEncDPBAlloc;
    iAvcHandle.CBAVC_FrameBind = &CbAvcEncFrameBind;
    iAvcHandle.CBAVC_FrameUnbind = &CbAvcEncFrameUnbind;
    iAvcHandle.CBAVC_Free = &CbAvcEncFree;
    iAvcHandle.CBAVC_Malloc = &CbAvcEncMalloc;
    iAvcHandle.CBAVC_DebugLog = &CbAvcEncDebugLog;
    iAvcHandle.userData = this;

    iYUVIn = NULL;
    iState = ECreated;
    iFramePtr = NULL;
    iDPB = NULL;
    iFrameUsed = NULL;

    return true;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TAVCEI_RETVAL PVAVCEncoder::Initialize(TAVCEIInputFormat *aVidInFormat, TAVCEIEncodeParam *aEncParam)
{

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Initialize");

    AVCEncParams aEncOption; /* encoding options */

    iOverrunBuffer = NULL;
    iOBSize = 0;

    if (EAVCEI_SUCCESS != Init(aVidInFormat, aEncParam, aEncOption))
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Initialize (EAVCEI_SUCCESS != Init(aVidInFormat, aEncParam, aEncOption)) -> return EAVCEI_FAIL");
        return EAVCEI_FAIL;
    }


    if (AVCENC_SUCCESS != PVAVCEncInitialize(&iAvcHandle, &aEncOption, NULL, NULL))
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Initialize (AVCENC_SUCCESS != PVAVCEncInitialize(&iAvcHandle, &aEncOption, NULL, NULL)) -> return EAVCEI_FAIL");
        return EAVCEI_FAIL;
    }

    iIDR = true;
    iDispOrd = 0;
    iState = EInitialized; // change state to initialized

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Initialize final return EAVCEI_SUCCESS");
    return EAVCEI_SUCCESS;
}

/* ///////////////////////////////////////////////////////////////////////// */
int32 PVAVCEncoder::GetMaxOutputBufferSize()
{
    int size = 0;

    PVAVCEncGetMaxOutputBufferSize(&iAvcHandle, &size);

    return size;
}

/* ///////////////////////////////////////////////////////////////////////// */
TAVCEI_RETVAL PVAVCEncoder::Init(TAVCEIInputFormat* aVidInFormat, TAVCEIEncodeParam* aEncParam, AVCEncParams& aEncOption)
{

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Init");

    if (iState == EInitialized || iState == EEncoding)  /* clean up before re-initialized */
    {

        PVAVCCleanUpEncoder(&iAvcHandle);
        if (iYUVIn)
        {
            oscl_free(iYUVIn);
            iYUVIn = NULL;
        }

    }

    iState = ECreated; // change state back to created

    iId = aEncParam->iEncodeID;

    iSrcWidth = aVidInFormat->iFrameWidth;
    iSrcHeight = aVidInFormat->iFrameHeight;
    iSrcFrameRate = aVidInFormat->iFrameRate;
    iVideoFormat =  aVidInFormat->iVideoFormat;
    iFrameOrientation = aVidInFormat->iFrameOrientation;

    // allocate iYUVIn
    if (iVideoFormat == EAVCEI_VDOFMT_YUV420SEMIPLANAR) /* Not multiple of 16 */
    {
        iYUVIn = (uint8*) oscl_malloc((iSrcWidth*iSrcHeight* 3)>>1);
        if (iYUVIn == NULL)
        {
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Init (iYUVIn == NULL) -> return: EAVCEI_FAIL");

            return EAVCEI_FAIL;
        }
    }

    // check the buffer delay according to the clip duration
    if (aEncParam->iClipDuration > 0 && aEncParam->iRateControlType == EAVCEI_RC_VBR_1)
    {
        if (aEncParam->iBufferDelay > (float)(aEncParam->iClipDuration / 10000.0))   //enforce 10% variation of the clip duration as the bound of buffer delay
        {
            aEncParam->iBufferDelay = (float)(aEncParam->iClipDuration / 10000.0);
        }
    }

    /* Check color format */
    if ( (iVideoFormat != EAVCEI_VDOFMT_YUV420SEMIPLANAR))
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Init ( (iVideoFormat != EAVCEI_VDOFMT_YUV420SEMIPLANAR)) -> return: EAVCEI_FAIL");

        return EAVCEI_FAIL;
    }

    if (aEncParam->iNumLayer > 1)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Init (aEncParam->iNumLayer > 1) -> return: EAVCEI_FAIL");

        return EAVCEI_FAIL;
    }

    aEncOption.width = iEncWidth = aEncParam->iFrameWidth[0];
    aEncOption.height = iEncHeight = aEncParam->iFrameHeight[0];

    iEncFrameRate = aEncParam->iFrameRate[0];
    aEncOption.frame_rate = (uint32)(1000 * iEncFrameRate);

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Init aEncParam->iRateControlType = %d", aEncParam->iRateControlType);
    
    if (aEncParam->iRateControlType == EAVCEI_RC_CONSTANT_Q)
    {
        aEncOption.rate_control = AVC_OFF;
        aEncOption.bitrate = 64000; // default
    }
    else if (aEncParam->iRateControlType == EAVCEI_RC_CBR_1)
    {
        aEncOption.rate_control = AVC_ON;
    }
    else if (aEncParam->iRateControlType == EAVCEI_RC_VBR_1)
    {
        aEncOption.rate_control = AVC_ON;
    }
    else
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Init (Unknown aEncParam->iRateControlType = %d) -> return: EAVCEI_FAIL", aEncParam->iRateControlType);
        return EAVCEI_FAIL;
    }

    // future :: map aEncParam->iEncMode to EncMode inside AVCEncoder

    iPacketSize = aEncParam->iPacketSize;
    aEncOption.profile = mapProfile(aEncParam->iProfile);
    aEncOption.level = mapLevel(aEncParam->iLevel);

    //aEncOption.src_interval = (int)(1000/aVidInFormat->iFrameRate + 0.5);

    aEncOption.bitrate = aEncParam->iBitRate[0];
    aEncOption.initQP = aEncParam->iIquant[0];

    aEncOption.init_CBP_removal_delay = (uint32)(aEncParam->iBufferDelay * 1000); // make it millisecond
    aEncOption.CPB_size = ((uint32)((uint32)aEncParam->iBufferDelay * (aEncOption.bitrate)));

    switch (aEncParam->iIFrameInterval)
    {
        case -1:
            aEncOption.idr_period = 0;  /* all P-frames */
            break;
        case 0:
            aEncOption.idr_period = 1;  /* all IDR-frames */
            break;
        default:
            aEncOption.idr_period = (int)(aEncParam->iIFrameInterval *  aVidInFormat->iFrameRate);
            break;
    }

    aEncOption.intramb_refresh = aEncParam->iNumIntraMBRefresh;
    aEncOption.auto_scd = (aEncParam->iSceneDetection == true) ? AVC_ON : AVC_OFF;
    aEncOption.out_of_band_param_set = (aEncParam->iOutOfBandParamSet == true) ? AVC_ON : AVC_OFF;
    aEncOption.use_overrun_buffer = AVC_OFF; // hardcode it to off

    /* default values */
    aEncOption.poc_type = 0;
    aEncOption.num_ref_frame = 1;

    aEncOption.log2_max_poc_lsb_minus_4 = 12;
    aEncOption.num_slice_group = 1;
    aEncOption.fmo_type = 0; /// FMO is disabled for now.
    aEncOption.db_filter = AVC_ON;
    aEncOption.disable_db_idc = 0;
    aEncOption.alpha_offset = 0;
    aEncOption.beta_offset = 0;
    aEncOption.constrained_intra_pred = AVC_OFF;

    aEncOption.data_par = AVC_OFF;
    aEncOption.fullsearch = AVC_OFF;
    aEncOption.search_range = 16;
    aEncOption.sub_pel = AVC_ON;
    aEncOption.submb_pred = AVC_OFF;
    aEncOption.rdopt_mode = AVC_OFF;
    aEncOption.bidir_pred = AVC_OFF;

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Init final return: EAVCEI_SUCCESS");

    return EAVCEI_SUCCESS;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TAVCEI_RETVAL PVAVCEncoder::GetParameterSet(uint8 *paramSet, int32 *size, int *aNALType)
{

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetParameterSet");

    uint aSize;
    AVCEnc_Status avcStatus ;

    if (iState != EInitialized) {/* has to be initialized first */
        
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetParameterSet return: EAVCEI_FAIL");
        
        return EAVCEI_FAIL;
    }
    aSize = *size;

    if (paramSet == NULL || size == NULL)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetParameterSet return: EAVCEI_INPUT_ERROR");

        return EAVCEI_INPUT_ERROR;
    }

    //=================>
    avcStatus = PVAVCEncodeNAL(&iAvcHandle, paramSet, &aSize, aNALType);

    if (avcStatus == AVCENC_WRONG_STATE)
    {
        *size = 0;
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetParameterSet return: AVCENC_WRONG_STATE-> EAVCEI_FAIL");
        return EAVCEI_FAIL;
    }

    switch (*aNALType)
    {
        case AVC_NALTYPE_SPS:
        case AVC_NALTYPE_PPS:
            *size = aSize;
            
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetParameterSet return: SPS/PPS-> EAVCEI_SUCCESS");

            return EAVCEI_SUCCESS;
        default:
            *size = 0;
            
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetParameterSet return: default-> EAVCEI_FAIL");

            return EAVCEI_FAIL;
    }

}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TAVCEI_RETVAL PVAVCEncoder::Encode(TAVCEIInputData *aVidIn)
{

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Encode");

    AVCEnc_Status status;

    if ((aVidIn == NULL) || (aVidIn->iSource == NULL))
    {
        return EAVCEI_INPUT_ERROR;
    }
    // we need to check the timestamp here. If it's before the proper time,
    // we need to return EAVCEI_FRAME_DROP here.
    // also check whether encoder is ready to take a new frame.
    if (iState == EEncoding)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Encode (iState == EEncoding) -> return: EAVCEI_NOT_READY");

        return EAVCEI_NOT_READY;
    }
    else if (iState == ECreated)
    {
    
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Encode (iState == ECreated) -> return: EAVCEI_FAIL");

        return EAVCEI_FAIL;
    }

    if (iVideoFormat == EAVCEI_VDOFMT_YUV420SEMIPLANAR)

    {
        if (iYUVIn) /* iSrcWidth is not multiple of 4 or iSrcHeight is odd number */
        {
            CopyToYUVIn(aVidIn->iSource,iSrcWidth,iSrcHeight);
            iVideoIn = iYUVIn;
        }
        else /* otherwise, we can just use aVidIn->iSource */
        {
            iVideoIn = aVidIn->iSource;  //   Sept 14, 2005 */
        }
    } else {
    
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Encode (iVideoFormat != EAVCEI_VDOFMT_YUV420SEMIPLANAR) -> return: EAVCEI_INPUT_ERROR");

    	return EAVCEI_INPUT_ERROR;
    }

    /* assign with backward-P or B-Vop this timestamp must be re-ordered */
    iTimeStamp = aVidIn->iTimeStamp;

    iVidIn.height = ((iSrcHeight + 15) >> 4) << 4;
    iVidIn.pitch = ((iSrcWidth + 15) >> 4) << 4;
    iVidIn.coding_timestamp = iTimeStamp;
    iVidIn.YCbCr[0] = (uint8*)iVideoIn;
    iVidIn.YCbCr[1] = (uint8*)(iVideoIn + iVidIn.height * iVidIn.pitch);
    iVidIn.YCbCr[2] = iVidIn.YCbCr[1] + ((iVidIn.height * iVidIn.pitch) >> 2);
    iVidIn.disp_order = iDispOrd;

    //================>
    status = PVAVCEncSetInput(&iAvcHandle, &iVidIn);

    switch (status)
    {
        case AVCENC_SKIPPED_PICTURE:
        
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Encode AVCENC_SKIPPED_PICTURE-> return: EAVCEI_FRAME_DROP");

            return EAVCEI_FRAME_DROP;
        case AVCENC_FAIL: // not in the right state
        
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Encode AVCENC_FAIL-> return: EAVCEI_NOT_READY");

            return EAVCEI_NOT_READY;
        case AVCENC_SUCCESS:
            iState = EEncoding;
            iDispOrd++;
            
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Encode AVCENC_SUCCESS-> return: EAVCEI_SUCCESS");

            return EAVCEI_SUCCESS;
        case AVCENC_NEW_IDR:
            iState = EEncoding;
            iDispOrd++;
            iIDR = true;
            
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Encode AVCENC_NEW_IDR-> return: EAVCEI_SUCCESS");

            return EAVCEI_SUCCESS;
        default:
            
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "Encode return: default->EAVCEI_FAIL");

            return EAVCEI_FAIL;
    }
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TAVCEI_RETVAL PVAVCEncoder::GetOutput(TAVCEIOutputData *aVidOut, int *aRemainingBytes)
{

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetOuput");

    AVCEnc_Status status;
    TAVCEI_RETVAL ret;
    uint Size;
    int nalType;
    AVCFrameIO recon;
    *aRemainingBytes = 0;

    if (iState != EEncoding)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetOuput return: EAVCEI_NOT_READY");

        return EAVCEI_NOT_READY;
    }

    if (aVidOut == NULL)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetOuput return: EAVCEI_INPUT_ERROR");

        return EAVCEI_INPUT_ERROR;
    }


    if (iOverrunBuffer) // more output buffer to be copied out.
    {
        aVidOut->iFragment = true;
        aVidOut->iTimeStamp = iTimeStamp;
        aVidOut->iKeyFrame = iIDR;
        aVidOut->iLastNAL = (iEncStatus == AVCENC_PICTURE_READY) ? true : false;

        if (iOBSize > aVidOut->iBitstreamSize)
        {
            oscl_memcpy(aVidOut->iBitstream, iOverrunBuffer, aVidOut->iBitstreamSize);
            iOBSize -= aVidOut->iBitstreamSize;
            iOverrunBuffer += aVidOut->iBitstreamSize;
            aVidOut->iLastFragment = false;
            *aRemainingBytes = iOBSize;

            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetOuput return: (iLastFragment = false) EAVCEI_MORE_DATA");

            return EAVCEI_MORE_DATA;
        }
        else
        {
            oscl_memcpy(aVidOut->iBitstream, iOverrunBuffer, iOBSize);
            aVidOut->iBitstreamSize = iOBSize;
            iOverrunBuffer = NULL;
            iOBSize = 0;
            aVidOut->iLastFragment = true;
            *aRemainingBytes = 0;

            if (iEncStatus == AVCENC_PICTURE_READY)
            {
                iState = EInitialized;
                if (iIDR == true)
                {
                    iIDR = false;
                }
                __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetOuput return: (iLastFragment = true) EAVCEI_SUCCESS");

                return EAVCEI_SUCCESS;
            }
            else
            {
                __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetOuput return: (iLastFragment = true) EAVCEI_MORE_DATA");

                return EAVCEI_MORE_NAL;
            }
        }
    }

    // Otherwise, call library to encode another NAL

    Size = aVidOut->iBitstreamSize;

    // ==============>
    iEncStatus = PVAVCEncodeNAL(&iAvcHandle, (uint8*)aVidOut->iBitstream, &Size, &nalType);

    if (iEncStatus == AVCENC_SUCCESS)
    {
    
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetOuput (iEncStatus == AVCENC_SUCCESS) -> return EAVCEI_MORE_NAL");

        aVidOut->iLastNAL = false;
        aVidOut->iKeyFrame = iIDR;
        ret = EAVCEI_MORE_NAL;
    }
    else if (iEncStatus == AVCENC_PICTURE_READY)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetOuput (iEncStatus == AVCENC_PICTURE_READY) -> return EAVCEI_SUCCESS");

        aVidOut->iLastNAL = true;
        aVidOut->iKeyFrame = iIDR;
        ret = EAVCEI_SUCCESS;
        iState = EInitialized;

        status = PVAVCEncGetRecon(&iAvcHandle, &recon);
        if (status == AVCENC_SUCCESS)
        {
            aVidOut->iFrame = recon.YCbCr[0];

            PVAVCEncReleaseRecon(&iAvcHandle, &recon);
        }
    }
    else if (iEncStatus == AVCENC_SKIPPED_PICTURE)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetOuput iEncStatus == AVCENC_SKIPPED_PICTURE");

        aVidOut->iLastFragment = true;
        aVidOut->iFragment = false;
        aVidOut->iBitstreamSize = 0;
        aVidOut->iTimeStamp = iTimeStamp;
        iState = EInitialized;
        return EAVCEI_FRAME_DROP;
    }
    else
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetOuput iEncStatus else");

        return EAVCEI_FAIL;
    }

    iOverrunBuffer = PVAVCEncGetOverrunBuffer(&iAvcHandle);

    if (iOverrunBuffer) // OB is used
    {
        if (Size < (uint)aVidOut->iBitstreamSize) // encoder decides to use OB even though the buffer is big enough
        {
            oscl_memcpy(aVidOut->iBitstream, iOverrunBuffer, Size);
            iOverrunBuffer = NULL; // reset it
            iOBSize = 0;
        }
        else
        {
            oscl_memcpy(aVidOut->iBitstream, iOverrunBuffer, aVidOut->iBitstreamSize);
            iOBSize = Size - aVidOut->iBitstreamSize;
            iOverrunBuffer += aVidOut->iBitstreamSize;
            if (iOBSize > 0) // there are more data
            {
                iState = EEncoding; // still encoding..
                aVidOut->iLastFragment = false;
                aVidOut->iFragment = true;
                aVidOut->iTimeStamp = iTimeStamp;
                return EAVCEI_MORE_DATA; // only copy out from iOverrunBuffer next time.
            }
        }

    }

    aVidOut->iLastFragment = true; /* for now */
    aVidOut->iFragment = false;  /* for now */
    aVidOut->iBitstreamSize = Size;
    aVidOut->iTimeStamp = iTimeStamp;

    if (iEncStatus == AVCENC_PICTURE_READY && iIDR == true)
    {
        iIDR = false;
    }

    return ret;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TAVCEI_RETVAL PVAVCEncoder::FlushInput()
{
    // do nothing for now.
    return EAVCEI_SUCCESS;
}

/* ///////////////////////////////////////////////////////////////////////// */
TAVCEI_RETVAL PVAVCEncoder::CleanupEncoder()
{
    if (iState == EInitialized || iState == EEncoding)
    {
        PVAVCCleanUpEncoder(&iAvcHandle);
        iState = ECreated;

        if (iYUVIn)
        {
            oscl_free(iYUVIn);
            iYUVIn = NULL;
        }
    }
    if (iFrameUsed)
    {
        oscl_free(iFrameUsed);
        iFrameUsed = NULL;
    }
    if (iDPB)
    {
        oscl_free(iDPB);
        iDPB = NULL;
    }
    if (iFramePtr)
    {
        oscl_free(iFramePtr);
        iFramePtr = NULL;
    }
    return EAVCEI_SUCCESS;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TAVCEI_RETVAL PVAVCEncoder::UpdateBitRate(int32 *aBitRate)
{
    if (PVAVCEncUpdateBitRate(&iAvcHandle, aBitRate[0]) == AVCENC_SUCCESS)
        return EAVCEI_SUCCESS;
    else
        return EAVCEI_FAIL;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TAVCEI_RETVAL PVAVCEncoder::UpdateFrameRate(OsclFloat *aFrameRate)
{
    if (PVAVCEncUpdateFrameRate(&iAvcHandle, (uint32)(1000*aFrameRate[0]), 1000) == AVCENC_SUCCESS)
        return EAVCEI_SUCCESS;
    else
        return EAVCEI_FAIL;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TAVCEI_RETVAL PVAVCEncoder::UpdateIDRFrameInterval(int32 aIDRFrameInterval)
{
    if (PVAVCEncUpdateIDRInterval(&iAvcHandle, aIDRFrameInterval) == AVCENC_SUCCESS)
        return EAVCEI_SUCCESS;
    else
        return EAVCEI_FAIL;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TAVCEI_RETVAL PVAVCEncoder::IDRRequest()
{
    if (PVAVCEncIDRRequest(&iAvcHandle) == AVCENC_SUCCESS)
        return EAVCEI_SUCCESS;
    else
        return EAVCEI_FAIL;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF int32 PVAVCEncoder::GetEncodeWidth(int32 aLayer)
{
    OSCL_UNUSED_ARG(aLayer);
    return iEncWidth;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF int32 PVAVCEncoder::GetEncodeHeight(int32 aLayer)
{
    OSCL_UNUSED_ARG(aLayer);
    return iEncHeight;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF OsclFloat PVAVCEncoder::GetEncodeFrameRate(int32 aLayer)
{
    OSCL_UNUSED_ARG(aLayer);
    return iEncFrameRate;
}



/* ///////////////////////////////////////////////////////////////////////// */
/* Copy from YUV input to YUV frame inside M4VEnc lib                       */
/* When input is not YUV, the color conv will write it directly to iVideoInOut. */
/* ///////////////////////////////////////////////////////////////////////// */

void PVAVCEncoder::CopyToYUVIn(uint8 *YUV, int width, int height)
{
	// Save YUV pointer
	uint8* saveiYUVIn = iYUVIn;

	// Convert YUV input to have distinct Y U and V channels
	// Copy Y data
	for (int i=0;i<height;i++){
	  for (int j=0;j<width;j++){
		  *iYUVIn= *YUV;
		  iYUVIn++;
		  YUV++;
	  }
	}

	// Copy UV data
	uint8 *uPos = iYUVIn;
	uint8 *vPos = uPos + ((width*height)>>2);
	uint16 temp = 0;
	uint16* iVideoPtr = (uint16*)YUV;
	for (int i=0;i<(height>>1);i++){
		for (int j=0;j<(width>>1);j++){
			temp = *iVideoPtr++; // U1V1
			*vPos++= (uint8)(temp & 0xFF);
			*uPos++= (uint8)((temp >> 8) & 0xFF);
		}
	}

	// Restore pointer
	iYUVIn = saveiYUVIn;

    return ;
}

AVCProfile PVAVCEncoder::mapProfile(TAVCEIProfile in)
{
    AVCProfile out;

    switch (in)
    {
        case EAVCEI_PROFILE_DEFAULT:
        case EAVCEI_PROFILE_BASELINE:
            out = AVC_BASELINE;
            break;
        case EAVCEI_PROFILE_MAIN:
            out = AVC_MAIN;
            break;
        case EAVCEI_PROFILE_EXTENDED:
            out = AVC_EXTENDED;
            break;
        case EAVCEI_PROFILE_HIGH:
            out = AVC_HIGH;
            break;
        case EAVCEI_PROFILE_HIGH10:
            out = AVC_HIGH10;
            break;
        case EAVCEI_PROFILE_HIGH422:
            out = AVC_HIGH422;
            break;
        case EAVCEI_PROFILE_HIGH444:
            out = AVC_HIGH444;
            break;
        default:
            out = AVC_BASELINE;
            break;
    }

    return out;
}

AVCLevel PVAVCEncoder::mapLevel(TAVCEILevel in)
{
    AVCLevel out;

    switch (in)
    {
        case EAVCEI_LEVEL_AUTODETECT:
            out = AVC_LEVEL_AUTO;
            break;
        case EAVCEI_LEVEL_1:
            out = AVC_LEVEL1;
            break;
        case EAVCEI_LEVEL_1B:
            out = AVC_LEVEL1_B;
            break;
        case EAVCEI_LEVEL_11:
            out = AVC_LEVEL1_1;
            break;
        case EAVCEI_LEVEL_12:
            out = AVC_LEVEL1_2;
            break;
        case EAVCEI_LEVEL_13:
            out = AVC_LEVEL1_3;
            break;
        case EAVCEI_LEVEL_2:
            out = AVC_LEVEL2;
            break;
        case EAVCEI_LEVEL_21:
            out = AVC_LEVEL2_1;
            break;
        case EAVCEI_LEVEL_22:
            out = AVC_LEVEL2_2;
            break;
        case EAVCEI_LEVEL_3:
            out = AVC_LEVEL3;
            break;
        case EAVCEI_LEVEL_31:
            out = AVC_LEVEL3_1;
            break;
        case EAVCEI_LEVEL_32:
            out = AVC_LEVEL3_2;
            break;
        case EAVCEI_LEVEL_4:
            out = AVC_LEVEL4;
            break;
        case EAVCEI_LEVEL_41:
            out = AVC_LEVEL4_1;
            break;
        case EAVCEI_LEVEL_42:
            out = AVC_LEVEL4_2;
            break;
        case EAVCEI_LEVEL_5:
            out = AVC_LEVEL5;
            break;
        case EAVCEI_LEVEL_51:
            out = AVC_LEVEL5_1;
            break;
        default:
            out = AVC_LEVEL5_1;
            break;
    }

    return out;
}


/* ///////////////////////////////////////////////////////////////////////// */

int PVAVCEncoder::AVC_DPBAlloc(uint frame_size_in_mbs, uint num_buffers)
{
    int ii;
    uint frame_size = (frame_size_in_mbs << 8) + (frame_size_in_mbs << 7);

    if (iDPB) oscl_free(iDPB); // free previous one first

    iDPB = (uint8*) oscl_malloc(sizeof(uint8) * frame_size * num_buffers);
    if (iDPB == NULL)
    {
        return 0;
    }

    iNumFrames = num_buffers;

    if (iFrameUsed) oscl_free(iFrameUsed); // free previous one

    iFrameUsed = (bool*) oscl_malloc(sizeof(bool) * num_buffers);
    if (iFrameUsed == NULL)
    {
        return 0;
    }

    if (iFramePtr) oscl_free(iFramePtr); // free previous one
    iFramePtr = (uint8**) oscl_malloc(sizeof(uint8*) * num_buffers);
    if (iFramePtr == NULL)
    {
        return 0;
    }

    iFramePtr[0] = iDPB;
    iFrameUsed[0] = false;

    for (ii = 1; ii < (int)num_buffers; ii++)
    {
        iFrameUsed[ii] = false;
        iFramePtr[ii] = iFramePtr[ii-1] + frame_size;
    }

    return 1;
}

/* ///////////////////////////////////////////////////////////////////////// */
void PVAVCEncoder::AVC_FrameUnbind(int indx)
{
    if (indx < iNumFrames)
    {
        iFrameUsed[indx] = false;
    }

    return ;
}

/* ///////////////////////////////////////////////////////////////////////// */
int PVAVCEncoder::AVC_FrameBind(int indx, uint8** yuv)
{
    if ((iFrameUsed[indx] == true) || (indx >= iNumFrames))
    {
        return 0; // already in used
    }

    iFrameUsed[indx] = true;
    *yuv = iFramePtr[indx];

    return 1;
}

