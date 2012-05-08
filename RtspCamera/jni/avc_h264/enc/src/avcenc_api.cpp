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
#include "oscl_types.h"
#include "oscl_mem.h"
#include "avcenc_api.h"
#include "avcenc_lib.h"

// xxx pa
#define LOG_TAG "avenc_api"
#include "android/log.h"

/* ======================================================================== */
/*  Function : PVAVCGetNALType()                                            */
/*  Date     : 11/4/2003                                                    */
/*  Purpose  : Sniff NAL type from the bitstream                            */
/*  In/out   :                                                              */
/*  Return   : AVCENC_SUCCESS if succeed, AVCENC_FAIL if fail.              */
/*  Modified :                                                              */
/* ======================================================================== */
OSCL_EXPORT_REF AVCEnc_Status PVAVCEncGetNALType(unsigned char *bitstream, int size,
        int *nal_type, int *nal_ref_idc)
{
    int forbidden_zero_bit;
    if (size > 0)
    {
        forbidden_zero_bit = bitstream[0] >> 7;
        if (forbidden_zero_bit != 0)
            return AVCENC_FAIL;
        *nal_ref_idc = (bitstream[0] & 0x60) >> 5;
        *nal_type = bitstream[0] & 0x1F;
        return AVCENC_SUCCESS;
    }

    return AVCENC_FAIL;
}

/* ======================================================================== */
/*  Function : PVAVCEncGetProfileLevel()                                            */
/*  Date     : 3/4/2010                                                    */
/*  Purpose  : Get profile and level type from the bitstream                            */
/*  In/out   :                                                              */
/*  Return   : AVCENC_SUCCESS if succeed, AVCENC_FAIL if fail.              */
/*  Modified :                                                              */
/* ======================================================================== */
OSCL_EXPORT_REF AVCEnc_Status PVAVCEncGetProfileLevel(AVCHandle* avcHandle, AVCProfile* profile, AVCLevel* level)
{
    AVCEncObject *encvid = (AVCEncObject*)avcHandle->AVCObject;
    AVCCommonObj *video = encvid->common;
    AVCSeqParamSet *seqParam = video->currSeqParams;

    if (encvid == NULL)
    {
        return AVCENC_UNINITIALIZED;
    }

    *profile = (AVCProfile)seqParam->profile_idc;
    *level = (AVCLevel)seqParam->level_idc;

    return AVCENC_SUCCESS;
}


/* ======================================================================== */
/*  Function : PVAVCEncInitialize()                                         */
/*  Date     : 3/18/2004                                                    */
/*  Purpose  : Initialize the encoder library, allocate memory and verify   */
/*              the profile/level support/settings.                         */
/*  In/out   : Encoding parameters.                                         */
/*  Return   : AVCENC_SUCCESS for success.                                  */
/*  Modified :                                                              */
/* ======================================================================== */
OSCL_EXPORT_REF AVCEnc_Status PVAVCEncInitialize(AVCHandle *avcHandle, AVCEncParams *encParam,
        void* extSPS, void* extPPS)
{

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncInitialize");

    AVCEnc_Status status;
    AVCEncObject *encvid;
    AVCCommonObj *video;
    uint32 *userData = (uint32*) avcHandle->userData;
    int framesize;

    if (avcHandle->AVCObject != NULL)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncInitialize (AVCObject != NULL) -> return: AVCENC_ALREADY_INITIALIZED");

        return AVCENC_ALREADY_INITIALIZED; /* It's already initialized, need to cleanup first */
    }

    /* not initialized */

    /* allocate videoObject */
    avcHandle->AVCObject = (void*)avcHandle->CBAVC_Malloc(userData, sizeof(AVCEncObject), DEFAULT_ATTR);
    if (avcHandle->AVCObject == NULL)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncInitialize (AVCObject == NULL) -> return: AVCENC_MEMORY_FAIL");

        return AVCENC_MEMORY_FAIL;
    }

    encvid = (AVCEncObject*) avcHandle->AVCObject;
    oscl_memset(encvid, 0, sizeof(AVCEncObject)); /* reset everything */

    encvid->enc_state = AVCEnc_Initializing;

    encvid->avcHandle = avcHandle;

    encvid->common = (AVCCommonObj*) avcHandle->CBAVC_Malloc(userData, sizeof(AVCCommonObj), DEFAULT_ATTR);
    if (encvid->common == NULL)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncInitialize (encvid->common == NULL) -> return: AVCENC_MEMORY_FAIL");

        return AVCENC_MEMORY_FAIL;
    }

    video = encvid->common;
    oscl_memset(video, 0, sizeof(AVCCommonObj));

    /* allocate bitstream structure */
    encvid->bitstream = (AVCEncBitstream*) avcHandle->CBAVC_Malloc(userData, sizeof(AVCEncBitstream), DEFAULT_ATTR);
    if (encvid->bitstream == NULL)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncInitialize (encvid->bitstream == NULL) -> return: AVCENC_MEMORY_FAIL");
    
        return AVCENC_MEMORY_FAIL;
    }
    encvid->bitstream->encvid = encvid; /* to point back for reallocation */

    /* allocate sequence parameter set structure */
    video->currSeqParams = (AVCSeqParamSet*) avcHandle->CBAVC_Malloc(userData, sizeof(AVCSeqParamSet), DEFAULT_ATTR);
    if (video->currSeqParams == NULL)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncInitialize (video->currSeqParams == NULL) -> return: AVCENC_MEMORY_FAIL");

        return AVCENC_MEMORY_FAIL;
    }
    oscl_memset(video->currSeqParams, 0, sizeof(AVCSeqParamSet));

    /* allocate picture parameter set structure */
    video->currPicParams = (AVCPicParamSet*) avcHandle->CBAVC_Malloc(userData, sizeof(AVCPicParamSet), DEFAULT_ATTR);
    if (video->currPicParams == NULL)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncInitialize (video->currPicParams == NULL) -> return: AVCENC_MEMORY_FAIL");

        return AVCENC_MEMORY_FAIL;
    }
    oscl_memset(video->currPicParams, 0, sizeof(AVCPicParamSet));

    /* allocate slice header structure */
    video->sliceHdr = (AVCSliceHeader*) avcHandle->CBAVC_Malloc(userData, sizeof(AVCSliceHeader), DEFAULT_ATTR);
    if (video->sliceHdr == NULL)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncInitialize (video->sliceHdr == NULL) -> return: AVCENC_MEMORY_FAIL");
    
        return AVCENC_MEMORY_FAIL;
    }
    oscl_memset(video->sliceHdr, 0, sizeof(AVCSliceHeader));

    /* allocate encoded picture buffer structure*/
    video->decPicBuf = (AVCDecPicBuffer*) avcHandle->CBAVC_Malloc(userData, sizeof(AVCDecPicBuffer), DEFAULT_ATTR);
    if (video->decPicBuf == NULL)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncInitialize (video->decPicBuf == NULL) -> return: AVCENC_MEMORY_FAIL");

        return AVCENC_MEMORY_FAIL;
    }
    oscl_memset(video->decPicBuf, 0, sizeof(AVCDecPicBuffer));

    /* allocate rate control structure */
    encvid->rateCtrl = (AVCRateControl*) avcHandle->CBAVC_Malloc(userData, sizeof(AVCRateControl), DEFAULT_ATTR);
    if (encvid->rateCtrl == NULL)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncInitialize (encvid->rateCtrl == NULL) -> return: AVCENC_MEMORY_FAIL");

        return AVCENC_MEMORY_FAIL;
    }
    oscl_memset(encvid->rateCtrl, 0, sizeof(AVCRateControl));

    /* reset frame list, not really needed */
    video->currPic = NULL;
    video->currFS = NULL;
    encvid->currInput = NULL;
    video->prevRefPic = NULL;

    /* now read encParams, and allocate dimension-dependent variables */
    /* such as mblock */
    status = SetEncodeParam(avcHandle, encParam, extSPS, extPPS); /* initialized variables to be used in SPS*/
    if (status != AVCENC_SUCCESS)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncInitialize (SetEncodeParam() != AVCENC_SUCCESS) -> return: status: %d", status);

        return status;
    }

    if (encParam->use_overrun_buffer == AVC_ON)
    {
        /* allocate overrun buffer */
        encvid->oBSize = encvid->rateCtrl->cpbSize;
        if (encvid->oBSize > DEFAULT_OVERRUN_BUFFER_SIZE)
        {
            encvid->oBSize = DEFAULT_OVERRUN_BUFFER_SIZE;
        }
        encvid->overrunBuffer = (uint8*) avcHandle->CBAVC_Malloc(userData, encvid->oBSize, DEFAULT_ATTR);
        if (encvid->overrunBuffer == NULL)
        {
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncInitialize (encvid->overrunBuffer == NULL) -> return: AVCENC_MEMORY_FAIL");

            return AVCENC_MEMORY_FAIL;
        }
    }
    else
    {
        encvid->oBSize = 0;
        encvid->overrunBuffer = NULL;
    }

    /* allocate frame size dependent structures */
    framesize = video->FrameHeightInMbs * video->PicWidthInMbs;

    video->mblock = (AVCMacroblock*) avcHandle->CBAVC_Malloc(userData, sizeof(AVCMacroblock) * framesize, DEFAULT_ATTR);
    if (video->mblock == NULL)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncInitialize (video->mblock == NULL) -> return: AVCENC_MEMORY_FAIL");

        return AVCENC_MEMORY_FAIL;
    }

    video->MbToSliceGroupMap = (int*) avcHandle->CBAVC_Malloc(userData, sizeof(uint) * video->PicSizeInMapUnits * 2, DEFAULT_ATTR);
    if (video->MbToSliceGroupMap == NULL)
    {
        return AVCENC_MEMORY_FAIL;
    }

    encvid->mot16x16 = (AVCMV*) avcHandle->CBAVC_Malloc(userData, sizeof(AVCMV) * framesize, DEFAULT_ATTR);
    if (encvid->mot16x16 == NULL)
    {
        return AVCENC_MEMORY_FAIL;
    }
    oscl_memset(encvid->mot16x16, 0, sizeof(AVCMV)*framesize);

    encvid->intraSearch = (uint8*) avcHandle->CBAVC_Malloc(userData, sizeof(uint8) * framesize, DEFAULT_ATTR);
    if (encvid->intraSearch == NULL)
    {
        return AVCENC_MEMORY_FAIL;
    }

    encvid->min_cost = (int*) avcHandle->CBAVC_Malloc(userData, sizeof(int) * framesize, DEFAULT_ATTR);
    if (encvid->min_cost == NULL)
    {
        return AVCENC_MEMORY_FAIL;
    }

    /* initialize motion search related memory */
    if (AVCENC_SUCCESS != InitMotionSearchModule(avcHandle))
    {
        return AVCENC_MEMORY_FAIL;
    }

    if (AVCENC_SUCCESS != InitRateControlModule(avcHandle))
    {
        return AVCENC_MEMORY_FAIL;
    }

    /* intialize function pointers */
    encvid->functionPointer = (AVCEncFuncPtr*) avcHandle->CBAVC_Malloc(userData, sizeof(AVCEncFuncPtr), DEFAULT_ATTR);
    if (encvid->functionPointer == NULL)
    {
        return AVCENC_MEMORY_FAIL;
    }
    encvid->functionPointer->SAD_Macroblock = &AVCSAD_Macroblock_C;
    encvid->functionPointer->SAD_MB_HalfPel[0] = NULL;
    encvid->functionPointer->SAD_MB_HalfPel[1] = &AVCSAD_MB_HalfPel_Cxh;
    encvid->functionPointer->SAD_MB_HalfPel[2] = &AVCSAD_MB_HalfPel_Cyh;
    encvid->functionPointer->SAD_MB_HalfPel[3] = &AVCSAD_MB_HalfPel_Cxhyh;

    /* initialize timing control */
    encvid->modTimeRef = 0;     /* ALWAYS ASSUME THAT TIMESTAMP START FROM 0 !!!*/
    video->prevFrameNum = 0;
    encvid->prevCodedFrameNum = 0;
    encvid->dispOrdPOCRef = 0;

    if (encvid->outOfBandParamSet == TRUE)
    {
        encvid->enc_state = AVCEnc_Encoding_SPS;
    }
    else
    {
        // xxx pa
        encvid->enc_state = AVCEnc_Analyzing_Frame;
        //encvid->enc_state = AVCEnc_Encoding_Frame;
    }

    return AVCENC_SUCCESS;
}

/* ======================================================================== */
/*  Function : PVAVCEncGetMaxOutputSize()                                   */
/*  Date     : 11/29/2008                                                   */
/*  Purpose  : Return max output buffer size that apps should allocate for  */
/*              output buffer.                                              */
/*  In/out   :                                                              */
/*  Return   : AVCENC_SUCCESS for success.                                  */
/*  Modified :   size                                                       */
/* ======================================================================== */

OSCL_EXPORT_REF AVCEnc_Status PVAVCEncGetMaxOutputBufferSize(AVCHandle *avcHandle, int* size)
{
    AVCEncObject *encvid = (AVCEncObject*)avcHandle->AVCObject;

    if (encvid == NULL)
    {
        return AVCENC_UNINITIALIZED;
    }

    *size = encvid->rateCtrl->cpbSize;

    return AVCENC_SUCCESS;
}

/* ======================================================================== */
/*  Function : PVAVCEncSetInput()                                           */
/*  Date     : 4/18/2004                                                    */
/*  Purpose  : To feed an unencoded original frame to the encoder library.  */
/*  In/out   :                                                              */
/*  Return   : AVCENC_SUCCESS for success.                                  */
/*  Modified :                                                              */
/* ======================================================================== */
OSCL_EXPORT_REF AVCEnc_Status PVAVCEncSetInput(AVCHandle *avcHandle, AVCFrameIO *input)
{
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncSetInput");

    AVCEncObject *encvid = (AVCEncObject*)avcHandle->AVCObject;
    AVCCommonObj *video = encvid->common;
    AVCRateControl *rateCtrl = encvid->rateCtrl;

    AVCEnc_Status status;
    uint frameNum;

    if (encvid == NULL)
    {
    
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncSetInput return: AVCENC_UNINITIALIZED");

        return AVCENC_UNINITIALIZED;
    }

    if (encvid->enc_state == AVCEnc_WaitingForBuffer)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncSetInput goto: RECALL_INITFRAME");
        goto RECALL_INITFRAME;
    }
    else if (encvid->enc_state != AVCEnc_Analyzing_Frame)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncSetInput return: AVCENC_FAIL");

        return AVCENC_FAIL;
    }

    if (input->pitch > 0xFFFF)
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncSetInput return: AVCENC_NOT_SUPPORTED");
        return AVCENC_NOT_SUPPORTED; // we use 2-bytes for pitch
    }

    /***********************************/

    /* Let's rate control decide whether to encode this frame or not */
    /* Also set video->nal_unit_type, sliceHdr->slice_type, video->slice_type */
    if (AVCENC_SUCCESS != RCDetermineFrameNum(encvid, rateCtrl, input->coding_timestamp, &frameNum))
    {
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncSetInput return: AVCENC_SKIPPED_PICTURE");

        return AVCENC_SKIPPED_PICTURE; /* not time to encode, thus skipping */
    }

    /* we may not need this line */
    //nextFrmModTime = (uint32)((((frameNum+1)*1000)/rateCtrl->frame_rate) + modTimeRef); /* rec. time */
    //encvid->nextModTime = nextFrmModTime - (encvid->frameInterval>>1) - 1; /* between current and next frame */

    encvid->currInput = input;
    encvid->currInput->coding_order = frameNum;

RECALL_INITFRAME:
    /* initialize and analyze the frame */
    status = InitFrame(encvid);

    if (status == AVCENC_SUCCESS)
    {
    
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncSetInput AVCENC_SUCCESS -> enc_state = AVCEnc_Encoding_Frame");

        encvid->enc_state = AVCEnc_Encoding_Frame;
    }
    else if (status == AVCENC_NEW_IDR)
    {
        if (encvid->outOfBandParamSet == TRUE)
        {
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncSetInput AVCENC_NEW_IDR -> enc_state = AVCEnc_Encoding_Frame");

            encvid->enc_state = AVCEnc_Encoding_Frame;
        }
        else // assuming that in-band paramset keeps sending new SPS and PPS.
        {
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncSetInput AVCENC_NEW_IDR -> enc_state = AVCEnc_Encoding_SPS");

            encvid->enc_state = AVCEnc_Encoding_SPS;
            //video->currSeqParams->seq_parameter_set_id++;
            //if(video->currSeqParams->seq_parameter_set_id > 31) // range check
            {
                video->currSeqParams->seq_parameter_set_id = 0;  // reset
            }
        }

        video->sliceHdr->idr_pic_id++;
        if (video->sliceHdr->idr_pic_id > 65535) // range check
        {
            video->sliceHdr->idr_pic_id = 0;  // reset
        }
    }
    /* the following logics need to be revisited */
    else if (status == AVCENC_PICTURE_READY) // no buffers returned back to the encoder
    {
    
        __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncSetInput AVCENC_PICTURE_READY -> enc_state = AVCEnc_WaitingForBuffer");

        encvid->enc_state = AVCEnc_WaitingForBuffer; // Input accepted but can't continue
        // need to free up some memory before proceeding with Encode
    }
    
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncSetInput return: final status");

    return status; // return status, including the AVCENC_FAIL case and all 3 above.
}

/* ======================================================================== */
/*  Function : PVAVCEncodeNAL()                                             */
/*  Date     : 4/29/2004                                                    */
/*  Purpose  : To encode one NAL/slice.                                     */
/*  In/out   :                                                              */
/*  Return   : AVCENC_SUCCESS for success.                                  */
/*  Modified :                                                              */
/* ======================================================================== */
OSCL_EXPORT_REF AVCEnc_Status PVAVCEncodeNAL(AVCHandle *avcHandle, unsigned char *buffer, unsigned int *buf_nal_size, int *nal_type)
{

    AVCEncObject *encvid = (AVCEncObject*)avcHandle->AVCObject;
    AVCCommonObj *video = encvid->common;
    AVCEncBitstream *bitstream = encvid->bitstream;
    AVCEnc_Status status;

    if (encvid == NULL)
    {
        return AVCENC_UNINITIALIZED;
    }

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncodeNAL: %d",encvid->enc_state);
    
    switch (encvid->enc_state)
    {
        case AVCEnc_Initializing:
            return AVCENC_UNINITIALIZED;
        case AVCEnc_Encoding_SPS:
            
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncodeNAL: AVCEnc_Encoding_SPS");
            
            /* initialized the structure */
            BitstreamEncInit(bitstream, buffer, *buf_nal_size, NULL, 0);
            BitstreamWriteBits(bitstream, 8, (1 << 5) | AVC_NALTYPE_SPS);

            /* encode SPS */
            status = EncodeSPS(encvid, bitstream);
            if (status != AVCENC_SUCCESS)
            {
                return status;
            }

            /* closing the NAL with trailing bits */
            status = BitstreamTrailingBits(bitstream, buf_nal_size);
            if (status == AVCENC_SUCCESS)
            {
                __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncodeNAL AVCEnc_Encoding_SPS -> enc_state = AVCEnc_Encoding_PPS");

                encvid->enc_state = AVCEnc_Encoding_PPS;
                video->currPicParams->seq_parameter_set_id = video->currSeqParams->seq_parameter_set_id;
                video->currPicParams->pic_parameter_set_id++;
                *nal_type = AVC_NALTYPE_SPS;
                *buf_nal_size = bitstream->write_pos;
            }
            break;
        case AVCEnc_Encoding_PPS:

            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncodeNAL: AVCEnc_Encoding_PPS");

            /* initialized the structure */
            BitstreamEncInit(bitstream, buffer, *buf_nal_size, NULL, 0);
            BitstreamWriteBits(bitstream, 8, (1 << 5) | AVC_NALTYPE_PPS);

            /* encode PPS */
            status = EncodePPS(encvid, bitstream);
            if (status != AVCENC_SUCCESS)
            {
                return status;
            }

            /* closing the NAL with trailing bits */
            status = BitstreamTrailingBits(bitstream, buf_nal_size);
            if (status == AVCENC_SUCCESS)
            {
                if (encvid->outOfBandParamSet == TRUE) // already extract PPS, SPS
                {
                
                    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncodeNAL AVCEnc_Encoding_PPS -> enc_state = AVCEnc_Analyzing_Frame");

                    encvid->enc_state = AVCEnc_Analyzing_Frame;
                }
                else    // SetInput has been called before SPS and PPS.
                {
                    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncodeNAL AVCEnc_Encoding_PPS -> enc_state = AVCEnc_Encoding_Frame");

                    encvid->enc_state = AVCEnc_Encoding_Frame;
                }

                *nal_type = AVC_NALTYPE_PPS;
                *buf_nal_size = bitstream->write_pos;
            }
            break;

        case AVCEnc_Encoding_Frame:

            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncodeNAL: AVCEnc_Encoding_Frame");

            /* initialized the structure */
            BitstreamEncInit(bitstream, buffer, *buf_nal_size, encvid->overrunBuffer, encvid->oBSize);
            BitstreamWriteBits(bitstream, 8, (video->nal_ref_idc << 5) | (video->nal_unit_type));

            /* Re-order the reference list according to the ref_pic_list_reordering() */
            /* We don't have to reorder the list for the encoder here. This can only be done
            after we encode this slice. We can run thru a second-pass to see if new ordering
            would save more bits. Too much delay !! */
            /* status = ReOrderList(video);*/
            status = InitSlice(encvid);
            if (status != AVCENC_SUCCESS)
            {
                return status;
            }

            /* when we have everything, we encode the slice header */
            status = EncodeSliceHeader(encvid, bitstream);
            if (status != AVCENC_SUCCESS)
            {
                return status;
            }

            status = AVCEncodeSlice(encvid);

            video->slice_id++;

            /* closing the NAL with trailing bits */
            BitstreamTrailingBits(bitstream, buf_nal_size);

            *buf_nal_size = bitstream->write_pos;

            encvid->rateCtrl->numFrameBits += ((*buf_nal_size) << 3);

            *nal_type = video->nal_unit_type;

            if (status == AVCENC_PICTURE_READY)
            {
                status = RCUpdateFrame(encvid);
                if (status == AVCENC_SKIPPED_PICTURE) /* skip current frame */
                {
                    DPBReleaseCurrentFrame(avcHandle, video);
                    
                    __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncodeNAL AVCEnc_Encoding_Frame+AVCENC_SKIPPED_PICTURE -> enc_state = AVCEnc_Analyzing_Frame");

                    encvid->enc_state = AVCEnc_Analyzing_Frame;

                    return status;
                }

                /* perform loop-filtering on the entire frame */
                DeblockPicture(video);

                /* update the original frame array */
                encvid->prevCodedFrameNum = encvid->currInput->coding_order;

                /* store the encoded picture in the DPB buffer */
                StorePictureInDPB(avcHandle, video);

                if (video->currPic->isReference)
                {
                    video->PrevRefFrameNum = video->sliceHdr->frame_num;
                }

                /* update POC related variables */
                PostPOC(video);
                
                __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncodeNAL AVCEnc_Encoding_Frame -> enc_state = AVCEnc_Analyzing_Frame");

                encvid->enc_state = AVCEnc_Analyzing_Frame;
                status = AVCENC_PICTURE_READY;

            }
            break;
        default:
            
            __android_log_print(ANDROID_LOG_INFO, LOG_TAG,  "PVAVCEncodeNAL: status = WRONG STATE");
            
            status = AVCENC_WRONG_STATE;
    }

    return status;
}

/* ======================================================================== */
/*  Function : PVAVCEncGetOverrunBuffer()                                   */
/*  Purpose  : To retrieve the overrun buffer. Check whether overrun buffer */
/*              is used or not before returning                             */
/*  In/out   :                                                              */
/*  Return   : Pointer to the internal overrun buffer.                      */
/*  Modified :                                                              */
/* ======================================================================== */
OSCL_EXPORT_REF uint8* PVAVCEncGetOverrunBuffer(AVCHandle* avcHandle)
{
    AVCEncObject *encvid = (AVCEncObject*)avcHandle->AVCObject;
    AVCEncBitstream *bitstream = encvid->bitstream;

    if (bitstream->overrunBuffer == bitstream->bitstreamBuffer) /* OB is used */
    {
        return encvid->overrunBuffer;
    }
    else
    {
        return NULL;
    }
}


/* ======================================================================== */
/*  Function : PVAVCEncGetRecon()                                           */
/*  Date     : 4/29/2004                                                    */
/*  Purpose  : To retrieve the most recently encoded frame.                 */
/*              assume that user will make a copy if they want to hold on   */
/*              to it. Otherwise, it is not guaranteed to be reserved.      */
/*              Most applications prefer to see original frame rather than  */
/*              reconstructed frame. So, we stay away from complex          */
/*              buffering mechanism. If needed, can be added later.         */
/*  In/out   :                                                              */
/*  Return   : AVCENC_SUCCESS for success.                                  */
/*  Modified :                                                              */
/* ======================================================================== */
OSCL_EXPORT_REF AVCEnc_Status PVAVCEncGetRecon(AVCHandle *avcHandle, AVCFrameIO *recon)
{
    AVCEncObject *encvid = (AVCEncObject*)avcHandle->AVCObject;
    AVCCommonObj *video = encvid->common;
    AVCFrameStore *currFS = video->currFS;

    if (encvid == NULL)
    {
        return AVCENC_UNINITIALIZED;
    }

    recon->YCbCr[0] = currFS->frame.Sl;
    recon->YCbCr[1] = currFS->frame.Scb;
    recon->YCbCr[2] = currFS->frame.Scr;
    recon->height = currFS->frame.height;
    recon->pitch = currFS->frame.pitch;
    recon->disp_order = currFS->PicOrderCnt;
    recon->coding_order = currFS->FrameNum;
    recon->id = (uint32) currFS->base_dpb; /* use the pointer as the id */

    currFS->IsOutputted |= 1;

    return AVCENC_SUCCESS;
}

OSCL_EXPORT_REF AVCEnc_Status PVAVCEncReleaseRecon(AVCHandle *avcHandle, AVCFrameIO *recon)
{
    OSCL_UNUSED_ARG(avcHandle);
    OSCL_UNUSED_ARG(recon);

    return AVCENC_SUCCESS; //for now
}

/* ======================================================================== */
/*  Function : PVAVCCleanUpEncoder()                                        */
/*  Date     : 4/18/2004                                                    */
/*  Purpose  : To clean up memories allocated by PVAVCEncInitialize()       */
/*  In/out   :                                                              */
/*  Return   : AVCENC_SUCCESS for success.                                  */
/*  Modified :                                                              */
/* ======================================================================== */
OSCL_EXPORT_REF void    PVAVCCleanUpEncoder(AVCHandle *avcHandle)
{
    AVCEncObject *encvid = (AVCEncObject*) avcHandle->AVCObject;
    AVCCommonObj *video;
    uint32 *userData = (uint32*) avcHandle->userData;

    if (encvid != NULL)
    {
        CleanMotionSearchModule(avcHandle);

        CleanupRateControlModule(avcHandle);

        if (encvid->functionPointer != NULL)
        {
            avcHandle->CBAVC_Free(userData, (int)encvid->functionPointer);
        }

        if (encvid->min_cost)
        {
            avcHandle->CBAVC_Free(userData, (int)encvid->min_cost);
        }

        if (encvid->intraSearch)
        {
            avcHandle->CBAVC_Free(userData, (int)encvid->intraSearch);
        }

        if (encvid->mot16x16)
        {
            avcHandle->CBAVC_Free(userData, (int)encvid->mot16x16);
        }

        if (encvid->rateCtrl)
        {
            avcHandle->CBAVC_Free(userData, (int)encvid->rateCtrl);
        }

        if (encvid->overrunBuffer)
        {
            avcHandle->CBAVC_Free(userData, (int)encvid->overrunBuffer);
        }

        video = encvid->common;
        if (video != NULL)
        {
            if (video->MbToSliceGroupMap)
            {
                avcHandle->CBAVC_Free(userData, (int)video->MbToSliceGroupMap);
            }
            if (video->mblock != NULL)
            {
                avcHandle->CBAVC_Free(userData, (int)video->mblock);
            }
            if (video->decPicBuf != NULL)
            {
                CleanUpDPB(avcHandle, video);
                avcHandle->CBAVC_Free(userData, (int)video->decPicBuf);
            }
            if (video->sliceHdr != NULL)
            {
                avcHandle->CBAVC_Free(userData, (int)video->sliceHdr);
            }
            if (video->currPicParams != NULL)
            {
                if (video->currPicParams->slice_group_id)
                {
                    avcHandle->CBAVC_Free(userData, (int)video->currPicParams->slice_group_id);
                }

                avcHandle->CBAVC_Free(userData, (int)video->currPicParams);
            }
            if (video->currSeqParams != NULL)
            {
                avcHandle->CBAVC_Free(userData, (int)video->currSeqParams);
            }
            if (encvid->bitstream != NULL)
            {
                avcHandle->CBAVC_Free(userData, (int)encvid->bitstream);
            }
            if (video != NULL)
            {
                avcHandle->CBAVC_Free(userData, (int)video);
            }
        }

        avcHandle->CBAVC_Free(userData, (int)encvid);

        avcHandle->AVCObject = NULL;
    }

    return ;
}

/* ======================================================================== */
/*  Function : PVAVCEncUpdateBitRate()                                      */
/*  Date     : 2/20/2010                                                    */
/*  Purpose  : Update bitrate while encoding.                               */
/*  In/out   :                                                              */
/*  Return   : AVCENC_SUCCESS for success, else fail.                       */
/*  Modified :                                                              */
/* ======================================================================== */

OSCL_EXPORT_REF AVCEnc_Status PVAVCEncUpdateBitRate(AVCHandle *avcHandle, uint32 bitrate)
{
    AVCEncObject *encvid = (AVCEncObject*)avcHandle->AVCObject;
    AVCCommonObj *video = encvid->common;
    AVCRateControl *rateCtrl = encvid->rateCtrl;
    AVCSeqParamSet *seqParam = video->currSeqParams;

    int lev_idx;

    if (encvid == NULL)
    {
        return AVCENC_UNINITIALIZED;
    }

    // only allow changing bit rate right after encoding a frame and before a new frame is analyzed.
    if (encvid->enc_state != AVCEnc_Analyzing_Frame)
    {
        return AVCENC_WRONG_STATE;
    }

    if (bitrate && rateCtrl->cpbSize && (rateCtrl->rcEnable == TRUE))
    {
        // verify level constraint
        // Note we keep the same cbpsize, hence the vbv delay will be affected.
        lev_idx = mapLev2Idx[seqParam->level_idc];

        if (bitrate > (uint32)(MaxBR[lev_idx]*1000))
        {
            return AVCENC_FAIL;
        }

        rateCtrl->bitRate = bitrate;

        // update other rate control parameters
        RCUpdateParams(rateCtrl, encvid);

        return AVCENC_SUCCESS;
    }
    else
    {
        return AVCENC_FAIL;
    }
}

/* ======================================================================== */
/*  Function : PVAVCEncUpdateFrameRate()                                    */
/*  Date     : 2/20/2010                                                    */
/*  Purpose  : Update frame rate while encoding.                            */
/*  In/out   :                                                              */
/*  Return   : AVCENC_SUCCESS for success, else fail.                       */
/*  Limitation: Changing frame rate will affect the first IDR frame coming  */
/*             after this call. It may come earlier or later than expected  */
/*             but after this first IDR frame, the IDR period will be back  */
/*             to normal.                                                   */
/*  Modified :                                                              */
/* ======================================================================== */

OSCL_EXPORT_REF AVCEnc_Status PVAVCEncUpdateFrameRate(AVCHandle *avcHandle, uint32 num, uint32 denom)
{
    AVCEncObject *encvid = (AVCEncObject*)avcHandle->AVCObject;
    AVCCommonObj *video = encvid->common;
    AVCRateControl *rateCtrl = encvid->rateCtrl;
    AVCSeqParamSet *seqParam = video->currSeqParams;

    int mb_per_sec;
    int lev_idx;

    if (encvid == NULL)
    {
        return AVCENC_UNINITIALIZED;
    }

    // only allow changing frame rate right after encoding a frame and before a new frame is analyzed.
    if (encvid->enc_state != AVCEnc_Analyzing_Frame)
    {
        return AVCENC_WRONG_STATE;
    }

    if (num && denom && (rateCtrl->rcEnable == TRUE))
    {
        mb_per_sec = ((video->PicSizeInMbs * num) + denom - 1) / denom;

        // copy some code from VerifyLevel here
        lev_idx = mapLev2Idx[seqParam->level_idc];

        if (mb_per_sec > MaxMBPS[lev_idx])
        {
            return AVCENC_FAIL;
        }

        rateCtrl->frame_rate = (OsclFloat)num / denom;

        // update other rate control parameters
        RCUpdateParams(rateCtrl, encvid);

        return AVCENC_SUCCESS;
    }
    else
    {
        return AVCENC_FAIL;
    }

    return AVCENC_FAIL;
}


/* ======================================================================== */
/*  Function : PVAVCEncUpdateIDRInterval()                                  */
/*  Date     : 2/20/2010                                                    */
/*  Purpose  : Update IDR interval while encoding.                          */
/*  In/out   :                                                              */
/*  Return   : AVCENC_SUCCESS for success, else fail.                       */
/*  Limitation: See PVAVCEncUpdateFrameRate.                                */
/*  Modified :                                                              */
/* ======================================================================== */
OSCL_EXPORT_REF AVCEnc_Status PVAVCEncUpdateIDRInterval(AVCHandle *avcHandle, int IDRInterval)
{
    AVCEncObject *encvid = (AVCEncObject*)avcHandle->AVCObject;
    AVCCommonObj *video = encvid->common;
    AVCRateControl *rateCtrl = encvid->rateCtrl;

    if (encvid == NULL)
    {
        return AVCENC_UNINITIALIZED;
    }

    if (IDRInterval > (int)video->MaxFrameNum)
    {
        return AVCENC_FAIL;
    }

    /* Note : IDRInterval defines periodicity of IDR frames after every nPFrames.*/
    rateCtrl->idrPeriod = IDRInterval;

    /* Note, when set to 1 (all I-frame), rate control is turned off */

    return AVCENC_SUCCESS;
}

/* ======================================================================== */
/*  Function : PVAVCEncIDRRequest()                                         */
/*  Date     : 2/20/2010                                                    */
/*  Purpose  : Request next frame to be IDR.                                */
/*  In/out   :                                                              */
/*  Return   : AVCENC_SUCCESS for success, else fail.                       */
/*  Modified :                                                              */
/* ======================================================================== */
OSCL_EXPORT_REF AVCEnc_Status PVAVCEncIDRRequest(AVCHandle *avcHandle)
{
    AVCEncObject *encvid = (AVCEncObject*)avcHandle->AVCObject;
    AVCRateControl *rateCtrl = encvid->rateCtrl;

    if (encvid == NULL)
    {
        return AVCENC_UNINITIALIZED;
    }

    // only allow changing frame rate right after encoding a frame and before a new frame is analyzed.
    if (encvid->enc_state != AVCEnc_Analyzing_Frame)
    {
        return AVCENC_WRONG_STATE;
    }

    rateCtrl->first_frame = 1;

    return AVCENC_SUCCESS;
}


/* ======================================================================== */
/*  Function : PVAVCEncUpdateIMBRefresh()                                   */
/*  Date     : 2/20/2010                                                    */
/*  Purpose  : Update number of minimal I MBs per frame.                    */
/*  In/out   :                                                              */
/*  Return   : AVCENC_SUCCESS for success, else fail.                       */
/*  Modified :                                                              */
/* ======================================================================== */
OSCL_EXPORT_REF AVCEnc_Status PVAVCEncUpdateIMBRefresh(AVCHandle *avcHandle, int numMB)
{
    AVCEncObject *encvid = (AVCEncObject*)avcHandle->AVCObject;
    AVCRateControl *rateCtrl = encvid->rateCtrl;
    AVCCommonObj *video = encvid->common;

    if (encvid == NULL)
    {
        return AVCENC_UNINITIALIZED;
    }

    if (numMB <= (int)video->PicSizeInMbs)
    {
        rateCtrl->intraMBRate = numMB;
        return AVCENC_SUCCESS;
    }

    return AVCENC_FAIL;
}

void PVAVCEncGetFrameStats(AVCHandle *avcHandle, AVCEncFrameStats *avcStats)
{
    AVCEncObject *encvid = (AVCEncObject*) avcHandle->AVCObject;
    AVCRateControl *rateCtrl = encvid->rateCtrl;

    avcStats->avgFrameQP = GetAvgFrameQP(rateCtrl);
    avcStats->numIntraMBs = encvid->numIntraMB;

    return ;
}



