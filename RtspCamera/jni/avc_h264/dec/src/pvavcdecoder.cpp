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
#include "pvavcdecoder.h"
#include "oscl_mem.h"

// xxx pa
#define LOG_TAG "pvavcdecoder"
#include "android/log.h"


/* global static functions */

void CbAvcDecDebugLog(uint32 *userData, AVCLogType type, char *string1, int val1, int val2)
{
    OSCL_UNUSED_ARG(userData);
    OSCL_UNUSED_ARG(type);
    OSCL_UNUSED_ARG(string1);
    OSCL_UNUSED_ARG(val1);
    OSCL_UNUSED_ARG(val2);

    return ;
}

int CbAvcDecMalloc(void *userData, int32 size, int attribute)
{
    OSCL_UNUSED_ARG(userData);
    OSCL_UNUSED_ARG(attribute);

    uint8 *mem;

    mem = (uint8*) oscl_malloc(size);

    return (int)mem;
}

void CbAvcDecFree(void *userData, int mem)
{
    OSCL_UNUSED_ARG(userData);

    oscl_free((void*)mem);

    return ;
}

int CbAvcDecDPBAlloc(void *userData, uint frame_size_in_mbs, uint num_buffers)
{
    PVAVCDecoder *pAvcDec = (PVAVCDecoder*) userData;

    return pAvcDec->AVC_DPBAlloc(frame_size_in_mbs, num_buffers);
}

void CbAvcDecFrameUnbind(void *userData, int indx)
{
    PVAVCDecoder *pAvcDec = (PVAVCDecoder*) userData;

    pAvcDec->AVC_FrameUnbind(indx);

    return ;
}

int CbAvcDecFrameBind(void *userData, int indx, uint8 **yuv)
{
    PVAVCDecoder *pAvcDec = (PVAVCDecoder*) userData;

    return pAvcDec->AVC_FrameBind(indx, yuv);
}



/* ///////////////////////////////////////////////////////////////////////// */
PVAVCDecoder::PVAVCDecoder()
{

//iDecoderControl
}

/* ///////////////////////////////////////////////////////////////////////// */
PVAVCDecoder::~PVAVCDecoder()
{
    CleanUpAVCDecoder();
}

/* ///////////////////////////////////////////////////////////////////////// */
PVAVCDecoder* PVAVCDecoder::New(void)
{
    PVAVCDecoder* self = new PVAVCDecoder;
    if (self && self->Construct())
        return self;
    if (self)
        delete self;
    return NULL;
}

/* ///////////////////////////////////////////////////////////////////////// */
bool PVAVCDecoder::Construct()
{
    oscl_memset((void*)&iAvcHandle, 0, sizeof(AVCHandle));

    // xxx pa callback setter
    iAvcHandle.CBAVC_DPBAlloc = &CbAvcDecDPBAlloc;
    iAvcHandle.CBAVC_FrameBind = &CbAvcDecFrameBind;
    iAvcHandle.CBAVC_FrameUnbind = &CbAvcDecFrameUnbind;
    iAvcHandle.CBAVC_Free = &CbAvcDecFree;
    iAvcHandle.CBAVC_Malloc = &CbAvcDecMalloc;
    iAvcHandle.CBAVC_DebugLog = &CbAvcDecDebugLog;
    iAvcHandle.userData = this;

    iFramePtr = NULL;
    iDPB = NULL;
    iFrameUsed = NULL;
    iNumFrames = NULL;

    return true;
}

/////////////////////////////////////////////////////////////////////////////
void PVAVCDecoder::CleanUpAVCDecoder(void)
{
    PVAVCCleanUpDecoder((AVCHandle *)&iAvcHandle);
}


void PVAVCDecoder::ResetAVCDecoder(void)
{
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "ResetAVCDecoder START");

    PVAVCDecReset((AVCHandle *)&iAvcHandle);
    
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "ResetAVCDecoder END");

}

/////////////////////////////////////////////////////////////////////////////

int32 PVAVCDecoder::DecodeSPS(uint8 *bitstream, int32 buffer_size)
{
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "DecodeSPS");

    return PVAVCDecSeqParamSet((AVCHandle *)&iAvcHandle, bitstream, buffer_size);
}

int32 PVAVCDecoder::DecodePPS(uint8 *bitstream, int32 buffer_size)
{
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "DecodePPS");

    return PVAVCDecPicParamSet((AVCHandle *)&iAvcHandle, bitstream, buffer_size);
}

int32 PVAVCDecoder::DecodeAVCSlice(uint8 *bitstream, int32 *buffer_size)
{
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "DecodeAVCSlice");

    return (PVAVCDecodeSlice((AVCHandle *)&iAvcHandle, bitstream, *buffer_size));
}

bool PVAVCDecoder::GetDecOutput(int *indx, int *release, AVCFrameIO* output)
{
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "GetDecOutput");

    return (PVAVCDecGetOutput((AVCHandle *)&iAvcHandle, indx, release, output) != AVCDEC_SUCCESS) ? false : true;
}


void PVAVCDecoder::GetVideoDimensions(int32 *width, int32 *height, int32 *top, int32 *left, int32 *bottom, int32 *right)
{
    AVCDecSPSInfo seqInfo;
    PVAVCDecGetSeqInfo((AVCHandle *)&iAvcHandle, &seqInfo);
    *width = seqInfo.FrameWidth;
    *height = seqInfo.FrameHeight;

    /* assuming top left corner aligned */
    *top = seqInfo.frame_crop_top;
    *left = seqInfo.frame_crop_left;
    *bottom = seqInfo.frame_crop_bottom;
    *right = seqInfo.frame_crop_right;
}

/* ///////////////////////////////////////////////////////////////////////// */

int PVAVCDecoder::AVC_DPBAlloc(uint frame_size_in_mbs, uint num_buffers)
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
void PVAVCDecoder::AVC_FrameUnbind(int indx)
{
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "AVC_FrameUnbind(%d)", indx);

    if (indx < iNumFrames)
    {
        iFrameUsed[indx] = false;
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "AVC_FrameUnbind iFrameUsed[indx(%d)] = false;", indx);
    }

    return ;
}

/* ///////////////////////////////////////////////////////////////////////// */
int PVAVCDecoder::AVC_FrameBind(int indx, uint8** yuv)
{

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "AVC_FrameBind(%d)", indx);

    if ((iFrameUsed[indx] == true) || (indx >= iNumFrames))
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "AVC_FrameBind return 0: (iFrameUsed[indx] == true) --> %d // (indx (%d) >= iNumFrames (%d)) --> %d", (iFrameUsed[indx] == true), indx, iNumFrames, (indx >= iNumFrames));

        return 0; // already in used
    }

    iFrameUsed[indx] = true;
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "AVC_FrameBind iFrameUsed[indx(%d)] = true;", indx);

    *yuv = iFramePtr[indx];
    
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "AVC_FrameBind final return 1");

    return 1;
}
