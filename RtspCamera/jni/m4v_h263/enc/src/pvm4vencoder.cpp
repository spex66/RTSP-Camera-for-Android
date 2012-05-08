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
#include "pvm4vencoder.h"
#include "oscl_mem.h"

#include "oscl_dll.h"
OSCL_DLL_ENTRY_POINT_DEFAULT()

/* ///////////////////////////////////////////////////////////////////////// */
CPVM4VEncoder::CPVM4VEncoder()
{
#if defined(RGB24_INPUT) || defined (RGB12_INPUT) || defined(YUV420SEMIPLANAR_INPUT)
    ccRGBtoYUV = NULL;
#endif
    //iEncoderControl
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF CPVM4VEncoder::~CPVM4VEncoder()
{
#if defined(RGB24_INPUT) || defined (RGB12_INPUT) || defined(YUV420SEMIPLANAR_INPUT)
    OSCL_DELETE(ccRGBtoYUV);
#endif

    Cancel(); /* CTimer function */
    Terminate();
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF CPVM4VEncoder* CPVM4VEncoder::New(int32 aThreadId)
{
    CPVM4VEncoder* self = new CPVM4VEncoder;
    if (self && self->Construct(aThreadId))
        return self;
    if (self)
        OSCL_DELETE(self);
    return NULL;
}

/* ///////////////////////////////////////////////////////////////////////// */
bool CPVM4VEncoder::Construct(int32 aThreadId)
{
    oscl_memset((void *)&iEncoderControl, 0, sizeof(VideoEncControls));
    iInitialized = false;
    iObserver = NULL;
    iNumOutputData = 0;
    iYUVIn = NULL;
    for (int i = 0; i < KCVEIMaxOutputBuffer; i++)
    {
        iOutputData[i] = NULL;
    }
    iState = EIdle;

    if (aThreadId >= 0)
        AddToScheduler();

    return true;
}

/* ///////////////////////////////////////////////////////////////////////// */
void CPVM4VEncoder::DoCancel()
{
    /* called when Cancel() is called.*/
    // They use Stop for PVEngine.cpp in PVPlayer.
    return ;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TCVEI_RETVAL CPVM4VEncoder::SetObserver(MPVCVEIObserver *aObserver)
{
    iObserver = aObserver;
    return ECVEI_SUCCESS;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TCVEI_RETVAL CPVM4VEncoder::AddBuffer(TPVVideoOutputData *aVidOut)
{
    if (iNumOutputData >= KCVEIMaxOutputBuffer)
    {
        return ECVEI_FAIL;
    }

    iOutputData[iNumOutputData++] = aVidOut;

    return ECVEI_SUCCESS;
}


/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TCVEI_RETVAL CPVM4VEncoder::Encode(TPVVideoInputData *aVidIn)
{
    ULong modTime;
    VideoEncFrameIO vid_in;

    if (iState != EIdle || iObserver == NULL)
    {
        return ECVEI_FAIL;
    }

    if (aVidIn->iTimeStamp >= iNextModTime)
    {
        if (iVideoFormat == ECVEI_YUV420)
#ifdef YUV_INPUT
        {
            if (iYUVIn) /* iSrcWidth is not multiple of 4 or iSrcHeight is odd number */
            {
                CopyToYUVIn(aVidIn->iSource, iSrcWidth, iSrcHeight,
                ((iSrcWidth + 15) >> 4) << 4, ((iSrcHeight + 15) >> 4) << 4);
                iVideoIn = iYUVIn;
            }
            else /* otherwise, we can just use aVidIn->iSource */
            {
                iVideoIn = aVidIn->iSource;
            }
        }
#else
            return ECVEI_FAIL;
#endif

        if ((iVideoFormat == ECVEI_RGB12) || (iVideoFormat == ECVEI_RGB24) || (iVideoFormat == ECVEI_YUV420SEMIPLANAR))
#if defined(RGB24_INPUT) || defined (RGB12_INPUT) || defined(YUV420SEMIPLANAR_INPUT)
        {
            ccRGBtoYUV->Convert(aVidIn->iSource, iYUVIn);
            iVideoIn = iYUVIn;
        }
#else
            return ECVEI_FAIL;
#endif

        /* assign with backward-P or B-Vop this timestamp must be re-ordered */
        iTimeStamp = aVidIn->iTimeStamp;

        modTime = iTimeStamp;

#ifdef NO_SLICE_ENCODE
        return ECVEI_FAIL;
#else
        vid_in.height = ((iSrcHeight + 15) >> 4) << 4;
        vid_in.pitch = ((iSrcWidth + 15) >> 4) << 4;
        vid_in.timestamp = modTime;
        vid_in.yChan = (UChar*)iVideoIn;
        vid_in.uChan = (UChar*)(iVideoIn + vid_in.height * vid_in.pitch);
        vid_in.vChan = vid_in.uChan + ((vid_in.height * vid_in.pitch) >> 2);

        /*status = */
        (int) PVEncodeFrameSet(&iEncoderControl, &vid_in, &modTime, &iNumLayer);
#endif

        iState = EEncode;
        RunIfNotReady();

        return ECVEI_SUCCESS;
    }
    else /* if(aVidIn->iTimeStamp >= iNextModTime) */
    {
        iTimeStamp = aVidIn->iTimeStamp;
        iNumLayer = -1;
        iState = EEncode;
        RunIfNotReady();
        return ECVEI_SUCCESS;
    }
}

/* ///////////////////////////////////////////////////////////////////////// */
void CPVM4VEncoder::Run()
{
#ifndef NO_SLICE_ENCODE

    //Bool status;
    Int Size, endOfFrame = 0;
    ULong modTime;
    int32 oindx;
    VideoEncFrameIO vid_out;

    switch (iState)
    {
        case EEncode:
            /* find available bitstream */
            if (iNumOutputData <= 0)
            {
                iObserver->HandlePVCVEIEvent(iId, ECVEI_NO_BUFFERS);
                RunIfNotReady(50);
                break;
            }
            oindx   =   --iNumOutputData;                       /* last-in first-out */
            Size    =   iOutputData[oindx]->iBitStreamSize;
            iOutputData[oindx]->iExternalTimeStamp  = iTimeStamp;
            iOutputData[oindx]->iVideoTimeStamp     = iTimeStamp;
            iOutputData[oindx]->iFrame              = iVideoOut;

            if (iNumLayer == -1)
            {
                iOutputData[oindx]->iBitStreamSize = 0;
                iOutputData[oindx]->iLayerNumber = iNumLayer;
                iState = EIdle;
                iObserver->HandlePVCVEIEvent(iId, ECVEI_FRAME_DONE, (uint32)iOutputData[oindx]);
                break;
            }

            /*status = */
            (int) PVEncodeSlice(&iEncoderControl, (UChar*)iOutputData[oindx]->iBitStream, &Size,
                                &endOfFrame, &vid_out, &modTime);

            iOutputData[oindx]->iBitStreamSize = Size;
            iOutputData[oindx]->iLayerNumber = iNumLayer;
            if (endOfFrame != 0) /* done with this frame */
            {
                iNextModTime = modTime;
                iOutputData[oindx]->iFrame = iVideoOut = (uint8*)vid_out.yChan;
                iOutputData[oindx]->iVideoTimeStamp = vid_out.timestamp;

                if (endOfFrame == -1) /* pre-skip */
                {
                    iOutputData[oindx]->iLayerNumber = -1;
                }
                else
                {
                    PVGetHintTrack(&iEncoderControl, &(iOutputData[oindx]->iHintTrack));
                }
                iState = EIdle;
                iObserver->HandlePVCVEIEvent(iId, ECVEI_FRAME_DONE, (uint32)iOutputData[oindx]);
            }
            else
            {
                RunIfNotReady();
                iObserver->HandlePVCVEIEvent(iId, ECVEI_BUFFER_READY, (uint32)iOutputData[oindx]);
            }

            break;
        default:
            break;
    }
#endif /* NO_SLICE_ENCODE */

    return ;
}

TCVEI_RETVAL CPVM4VEncoder::ParseFSI(uint8* aFSIBuff, int FSILength, VideoEncOptions *aEncOption)
{
    uint32 codeword;
    mp4StreamType *psBits;
    psBits = (mp4StreamType *) oscl_malloc(sizeof(mp4StreamType));
    if (psBits == NULL)
        return ECVEI_FAIL;
    psBits->data = aFSIBuff;
    psBits->numBytes = FSILength;
    psBits->bitBuf = 0;
    psBits->bitPos = 32;
    psBits->bytePos = 0;
    psBits->dataBitPos = 0;

    //visual_object_sequence_start_code
    ShowBits(psBits, 32, &codeword);

    if (codeword == VISUAL_OBJECT_SEQUENCE_START_CODE)
    {
        psBits->dataBitPos += 32;
        psBits->bitPos += 32;

        ReadBits(psBits, 8, &codeword); /* profile_and_level_indication */

        switch (codeword)
        {
            case 0x08: /* SP_LVL0 */
            {
                aEncOption->profile_level =  SIMPLE_PROFILE_LEVEL0;
                break;
            }
            case 0x01: /* SP_LVL1 */
            {
                aEncOption->profile_level =  SIMPLE_PROFILE_LEVEL1;
                break;
            }
            case 0x02: /* SP_LVL2 */
            {
                aEncOption->profile_level =  SIMPLE_PROFILE_LEVEL2;
                break;
            }
            case 0x03: /* SP_LVL3 */
            {
                aEncOption->profile_level =  SIMPLE_PROFILE_LEVEL3;
                break;
            }
            case 0x21: /* CP_LVL1 */
            {
                aEncOption->profile_level =  CORE_PROFILE_LEVEL1;
                break;
            }
            case 0x22: /* CP_LVL2 */
            {
                aEncOption->profile_level =  CORE_PROFILE_LEVEL2;
                break;
            }
            default:
            {
                goto FREE_PS_BITS_AND_FAIL;
            }
        }

        ShowBits(psBits, 32, &codeword);
        if (codeword == USER_DATA_START_CODE)
        {
            goto FREE_PS_BITS_AND_FAIL;
        }

        //visual_object_start_code
        ReadBits(psBits, 32, &codeword);
        if (codeword != VISUAL_OBJECT_START_CODE) goto FREE_PS_BITS_AND_FAIL;

        /*  is_visual_object_identifier            */
        ReadBits(psBits, 1, &codeword);
        if (codeword) goto FREE_PS_BITS_AND_FAIL;

        /* visual_object_type                                 */
        ReadBits(psBits, 4, &codeword);
        if (codeword != 1) goto FREE_PS_BITS_AND_FAIL;

        /* video_signal_type */
        ReadBits(psBits, 1, &codeword);
        if (codeword) goto FREE_PS_BITS_AND_FAIL;

        /* next_start_code() */
        ByteAlign(psBits);

        ShowBits(psBits, 32, &codeword);
        if (codeword == USER_DATA_START_CODE)
        {
            goto FREE_PS_BITS_AND_FAIL;
        }
    }

    ShowBits(psBits, 27, &codeword);

    if (codeword == VO_START_CODE)
    {

        ReadBits(psBits, 32, &codeword);

        /* video_object_layer_start_code                   */
        ShowBits(psBits, 28, &codeword);
        if (codeword != VOL_START_CODE)
        {
            ShowBits(psBits, 22, &codeword);
            if (codeword == SHORT_VIDEO_START_MARKER)
            {
                iDecodeShortHeader(psBits, aEncOption);
                return ECVEI_SUCCESS;
            }
            else
            {
                goto FREE_PS_BITS_AND_FAIL;

            }
        }
        /* video_object_layer_start_code                   */
        ReadBits(psBits, 28, &codeword);

        /* vol_id (4 bits) */
        ReadBits(psBits, 4, & codeword);

        // RandomAccessibleVOLFlag
        ReadBits(psBits, 1, &codeword);

        //Video Object Type Indication
        ReadBits(psBits, 8, &codeword);
        if (codeword > 2)
        {
            goto FREE_PS_BITS_AND_FAIL;
        }

        // is_object_layer_identifier
        ReadBits(psBits, 1, &codeword);
        if (codeword) goto FREE_PS_BITS_AND_FAIL;

        // aspect ratio
        ReadBits(psBits, 4, &codeword);
        if (codeword != 1) goto FREE_PS_BITS_AND_FAIL;

        //vol_control_parameters
        ReadBits(psBits, 1, &codeword);
        if (codeword != 0) goto FREE_PS_BITS_AND_FAIL;

        //      video_object_layer_shape
        ReadBits(psBits, 2, &codeword);
        if (codeword != 0) goto FREE_PS_BITS_AND_FAIL;

        //Marker bit
        ReadBits(psBits, 1, &codeword);
        if (codeword != 1) goto FREE_PS_BITS_AND_FAIL;

        //  vop_time_increment_resolution
        ReadBits(psBits, 16, &codeword);
        aEncOption->timeIncRes = codeword;

        //Marker bit
        ReadBits(psBits, 1, &codeword);
        if (codeword != 1)
            goto FREE_PS_BITS_AND_FAIL;

        //      fixed_vop_rate
        ReadBits(psBits, 1, &codeword);
        if (codeword != 0) goto FREE_PS_BITS_AND_FAIL;

        /* video_object_layer_shape is RECTANGULAR */
        //Marker bit
        ReadBits(psBits, 1, &codeword);
        if (codeword != 1) goto FREE_PS_BITS_AND_FAIL;

        /* this should be 176 for QCIF */
        ReadBits(psBits, 13, &codeword);
        aEncOption->encWidth[0] = codeword;

        //Marker bit
        ReadBits(psBits, 1, &codeword);
        if (codeword != 1) goto FREE_PS_BITS_AND_FAIL;

        /* this should be 144 for QCIF */
        ReadBits(psBits, 13, &codeword);
        aEncOption->encHeight[0] = codeword;

        //Marker bit
        ReadBits(psBits, 1, &codeword);
        if (codeword != 1) goto FREE_PS_BITS_AND_FAIL;

        //Interlaced
        ReadBits(psBits, 1, &codeword);
        if (codeword != 0) goto FREE_PS_BITS_AND_FAIL;

        //obmc_disable
        ReadBits(psBits, 1, &codeword);
        if (codeword != 1) goto FREE_PS_BITS_AND_FAIL;

        //sprite_enable
        ReadBits(psBits, 1, &codeword);
        if (codeword != 0) goto FREE_PS_BITS_AND_FAIL;

        //not_8_bit
        ReadBits(psBits, 1, &codeword);
        if (codeword != 0) goto FREE_PS_BITS_AND_FAIL;

        /* video_object_layer_shape is not GRAY_SCALE  */
        //quant_type
        ReadBits(psBits, 1, &codeword);
        aEncOption->quantType[0] = codeword;
        if (codeword != 0) //quant_type = 1
        {
            ReadBits(psBits, 1, &codeword); //load_intra_quant_mat
            if (codeword) goto FREE_PS_BITS_AND_FAIL; // No support for user defined matrix.

            ReadBits(psBits, 1, &codeword); //load_nonintra_quant_mat
            if (codeword) goto FREE_PS_BITS_AND_FAIL; // No support for user defined matrix.

        }

        //complexity_estimation_disable
        ReadBits(psBits, 1, &codeword);
        if (!codeword)
        {
            goto FREE_PS_BITS_AND_FAIL;
        }

        //resync_marker_disable
        ReadBits(psBits, 1, &codeword);
        if (codeword)
        {
            aEncOption->packetSize = 0;
        }

        //data_partitioned
        ReadBits(psBits, 1, &codeword);
        if (codeword)
        {
            aEncOption->encMode = DATA_PARTITIONING_MODE;
            //reversible_vlc
            ReadBits(psBits, 1, &codeword);
            aEncOption->rvlcEnable = (ParamEncMode) codeword;

        }
        else
        {
            // No data_partitioned
            if (aEncOption->packetSize > 0)
            {
                aEncOption->encMode = COMBINE_MODE_WITH_ERR_RES;
            }
            else
            {
                aEncOption->encMode = COMBINE_MODE_NO_ERR_RES;
            }
        }

        //scalability
        ReadBits(psBits, 1, &codeword);
        if (codeword) goto FREE_PS_BITS_AND_FAIL;

    }
    else
    {
        /* SHORT_HEADER */
        ShowBits(psBits, SHORT_VIDEO_START_MARKER_LENGTH, &codeword);
        if (codeword == SHORT_VIDEO_START_MARKER)
        {
            iDecodeShortHeader(psBits, aEncOption);
        }
        else
        {
            goto FREE_PS_BITS_AND_FAIL;

        }
    }
    return ECVEI_SUCCESS;

FREE_PS_BITS_AND_FAIL:

    oscl_free(psBits);

    return ECVEI_FAIL;
}

int16 CPVM4VEncoder::iDecodeShortHeader(mp4StreamType *psBits, VideoEncOptions *aEncOption)
{
    uint32 codeword;
    int *width, *height;

    //Default values
    aEncOption->quantType[0] =  0;
    aEncOption->rvlcEnable = PV_OFF;
    aEncOption->packetSize = 0;     // Since, by default resync_marker_disable = 1;
    aEncOption->encMode = SHORT_HEADER;     // NO error resilience
    width = &(aEncOption->encWidth[0]);
    height = &(aEncOption->encHeight[0]);

    //short_video_start_marker
    ShowBits(psBits, 22, &codeword);
    if (codeword !=  0x20)
    {
        return ECVEI_FAIL;
    }
    FlushBits(psBits, 22);

    //temporal_reference
    ReadBits(psBits, 8, &codeword);

    //marker_bit
    ReadBits(psBits, 1, &codeword);
    if (codeword == 0) return ECVEI_FAIL;

    //zero_bit
    ReadBits(psBits, 1, &codeword);
    if (codeword == 1) return ECVEI_FAIL;

    //split_screen_indicator
    ReadBits(psBits, 1, &codeword);
    if (codeword == 1) return ECVEI_FAIL;

    //document_camera_indicator
    ReadBits(psBits, 1, &codeword);
    if (codeword == 1) return ECVEI_FAIL;

    //full_picture_freeze_release
    ReadBits(psBits, 1, &codeword);
    if (codeword == 1) return ECVEI_FAIL;

    /* source format */
    ReadBits(psBits, 3, &codeword);
    switch (codeword)
    {
        case 1:
            *width = 128;
            *height = 96;
            break;

        case 2:
            *width = 176;
            *height = 144;
            break;

        case 3:
            *width = 352;
            *height = 288;
            break;

        case 4:
            *width = 704;
            *height = 576;
            break;

        case 5:
            *width = 1408;
            *height = 1152;
            break;

        default:
            return ECVEI_FAIL;
    }

    return 0;
}

int16 CPVM4VEncoder::ShowBits(
    mp4StreamType *pStream,           /* Input Stream */
    uint8 ucNBits,          /* nr of bits to read */
    uint32 *pulOutData      /* output target */
)
{
    uint8 *bits;
    uint32 dataBitPos = pStream->dataBitPos;
    uint32 bitPos = pStream->bitPos;
    uint32 dataBytePos;

    uint i;

    if (ucNBits > (32 - bitPos))    /* not enough bits */
    {
        dataBytePos = dataBitPos >> 3; /* Byte Aligned Position */
        bitPos = dataBitPos & 7; /* update bit position */
        if (dataBytePos > pStream->numBytes - 4)
        {
            pStream->bitBuf = 0;
            for (i = 0; i < pStream->numBytes - dataBytePos; i++)
            {
                pStream->bitBuf |= pStream->data[dataBytePos+i];
                pStream->bitBuf <<= 8;
            }
            pStream->bitBuf <<= 8 * (3 - i);
        }
        else
        {
            bits = &pStream->data[dataBytePos];
            pStream->bitBuf = (bits[0] << 24) | (bits[1] << 16) | (bits[2] << 8) | bits[3];
        }
        pStream->bitPos = bitPos;
    }

    bitPos += ucNBits;

    *pulOutData = (pStream->bitBuf >> (32 - bitPos)) & MASK[(uint16)ucNBits];


    return 0;
}

int16 CPVM4VEncoder::FlushBits(
    mp4StreamType *pStream,           /* Input Stream */
    uint8 ucNBits                      /* number of bits to flush */
)
{
    uint8 *bits;
    uint32 dataBitPos = pStream->dataBitPos;
    uint32 bitPos = pStream->bitPos;
    uint32 dataBytePos;


    if ((dataBitPos + ucNBits) > (uint32)(pStream->numBytes << 3))
        return (-2); // Buffer over run

    dataBitPos += ucNBits;
    bitPos     += ucNBits;

    if (bitPos > 32)
    {
        dataBytePos = dataBitPos >> 3;    /* Byte Aligned Position */
        bitPos = dataBitPos & 7; /* update bit position */
        bits = &pStream->data[dataBytePos];
        pStream->bitBuf = (bits[0] << 24) | (bits[1] << 16) | (bits[2] << 8) | bits[3];
    }

    pStream->dataBitPos = dataBitPos;
    pStream->bitPos     = bitPos;

    return 0;
}

int16 CPVM4VEncoder::ReadBits(
    mp4StreamType *pStream,           /* Input Stream */
    uint8 ucNBits,                     /* nr of bits to read */
    uint32 *pulOutData                 /* output target */
)
{
    uint8 *bits;
    uint32 dataBitPos = pStream->dataBitPos;
    uint32 bitPos = pStream->bitPos;
    uint32 dataBytePos;


    if ((dataBitPos + ucNBits) > (pStream->numBytes << 3))
    {
        *pulOutData = 0;
        return (-2); // Buffer over run
    }

    //  dataBitPos += ucNBits;

    if (ucNBits > (32 - bitPos))    /* not enough bits */
    {
        dataBytePos = dataBitPos >> 3;    /* Byte Aligned Position */
        bitPos = dataBitPos & 7; /* update bit position */
        bits = &pStream->data[dataBytePos];
        pStream->bitBuf = (bits[0] << 24) | (bits[1] << 16) | (bits[2] << 8) | bits[3];
    }

    pStream->dataBitPos += ucNBits;
    pStream->bitPos      = (unsigned char)(bitPos + ucNBits);

    *pulOutData = (pStream->bitBuf >> (32 - pStream->bitPos)) & MASK[(uint16)ucNBits];

    return 0;
}



int16 CPVM4VEncoder::ByteAlign(
    mp4StreamType *pStream           /* Input Stream */
)
{
    uint8 *bits;
    uint32 dataBitPos = pStream->dataBitPos;
    uint32 bitPos = pStream->bitPos;
    uint32 dataBytePos;
    uint32 leftBits;

    leftBits =  8 - (dataBitPos & 0x7);
    if (leftBits == 8)
    {
        if ((dataBitPos + 8) > (uint32)(pStream->numBytes << 3))
            return (-2); // Buffer over run
        dataBitPos += 8;
        bitPos += 8;
    }
    else
    {
        dataBytePos = dataBitPos >> 3;
        dataBitPos += leftBits;
        bitPos += leftBits;
    }


    if (bitPos > 32)
    {
        dataBytePos = dataBitPos >> 3;    /* Byte Aligned Position */
        bits = &pStream->data[dataBytePos];
        pStream->bitBuf = (bits[0] << 24) | (bits[1] << 16) | (bits[2] << 8) | bits[3];
    }

    pStream->dataBitPos = dataBitPos;
    pStream->bitPos     = bitPos;

    return 0;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TCVEI_RETVAL CPVM4VEncoder::Initialize(TPVVideoInputFormat *aVidInFormat, TPVVideoEncodeParam *aEncParam)
{

    int i;
    TCVEI_RETVAL status;
    MP4EncodingMode ENC_Mode ;
    ParamEncMode RvlcMode = PV_OFF; /* default no RVLC */
    Int quantType[2] = {0, 0};      /* default H.263 quant*/
    VideoEncOptions aEncOption; /* encoding options */

    iState = EIdle ; // stop encoding
    iId = aEncParam->iEncodeID;

    iOverrunBuffer = NULL;
    iOBSize = 0;

    if (aEncParam->iContentType ==  ECVEI_STREAMING)
    {
        ENC_Mode = DATA_PARTITIONING_MODE;
    }
    else if (aEncParam->iContentType == ECVEI_DOWNLOAD)
    {
        if (aEncParam->iPacketSize > 0)
        {
            ENC_Mode = COMBINE_MODE_WITH_ERR_RES;
        }
        else
        {
            ENC_Mode = COMBINE_MODE_NO_ERR_RES;
        }
    }
    else if (aEncParam->iContentType == ECVEI_H263)
    {
        if (aEncParam->iPacketSize > 0)
        {
            ENC_Mode = H263_MODE_WITH_ERR_RES;
        }
        else
        {
            ENC_Mode = H263_MODE;
        }
    }
    else
    {
        return ECVEI_FAIL;
    }

    iSrcWidth = aVidInFormat->iFrameWidth;
    iSrcHeight = aVidInFormat->iFrameHeight;
    iSrcFrameRate = (int) aVidInFormat->iFrameRate;
    iVideoFormat = (TPVVideoFormat) aVidInFormat->iVideoFormat;
    iFrameOrientation = aVidInFormat->iFrameOrientation;

    if (iInitialized == true)  /* clean up before re-initialized */
    {
        /*status = */
        (int) PVCleanUpVideoEncoder(&iEncoderControl);
        if (iYUVIn)
        {
            oscl_free(iYUVIn);
            iYUVIn = NULL;
        }
    }

    // allocate iYUVIn
    if (((iSrcWidth&0xF) || (iSrcHeight&0xF)) || iVideoFormat != ECVEI_YUV420) /* Not multiple of 16 */
    {
        iYUVIn = (uint8*) oscl_malloc(((((iSrcWidth + 15) >> 4) * ((iSrcHeight + 15) >> 4)) * 3) << 7);
        if (iYUVIn == NULL)
        {
            return ECVEI_FAIL;
        }
    }

    // check the buffer delay according to the clip duration
    if (aEncParam->iClipDuration > 0 && aEncParam->iRateControlType == EVBR_1)
    {
        if (aEncParam->iBufferDelay > aEncParam->iClipDuration / 10000.0)   //enforce 10% variation of the clip duration as the bound of buffer delay
        {
            aEncParam->iBufferDelay = aEncParam->iClipDuration / (float)10000.0;
        }
    }

    if (iVideoFormat == ECVEI_RGB24)
    {
#ifdef RGB24_INPUT
        ccRGBtoYUV = CCRGB24toYUV420::New();
#else
        return ECVEI_FAIL;
#endif
    }
    if (iVideoFormat == ECVEI_RGB12)
    {
#ifdef RGB12_INPUT
        ccRGBtoYUV = CCRGB12toYUV420::New();
#else
        return ECVEI_FAIL;
#endif
    }
    if (iVideoFormat == ECVEI_YUV420SEMIPLANAR)
    {
#ifdef YUV420SEMIPLANAR_INPUT
        ccRGBtoYUV = CCYUV420SEMItoYUV420::New();
#else
        return ECVEI_FAIL;
#endif
    }

    if ((iVideoFormat == ECVEI_RGB12) || (iVideoFormat == ECVEI_RGB24) || (iVideoFormat == ECVEI_YUV420SEMIPLANAR))
    {
#if defined(RGB24_INPUT) || defined (RGB12_INPUT) || defined (YUV420SEMIPLANAR_INPUT)
        ccRGBtoYUV->Init(iSrcWidth, iSrcHeight, iSrcWidth, iSrcWidth, iSrcHeight, ((iSrcWidth + 15) >> 4) << 4, (iFrameOrientation == 1 ? CCBOTTOM_UP : 0));
#endif
    }

    PVGetDefaultEncOption(&aEncOption, 0);


    /* iContentType is M4v && FSI Buffer is input parameter */
    if ((aEncParam->iContentType != ECVEI_H263) && (aEncParam->iFSIBuffLength))
    {
        aEncOption.encMode = ENC_Mode;
        aEncOption.packetSize = aEncParam->iPacketSize;
        aEncOption.numLayers = aEncParam->iNumLayer;

        status = ParseFSI(aEncParam->iFSIBuff, aEncParam->iFSIBuffLength, &aEncOption);
        if (ECVEI_FAIL == status)
        {
            return ECVEI_FAIL;
        }

        aEncOption.tickPerSrc = (int)(aEncOption.timeIncRes / aVidInFormat->iFrameRate + 0.5);

        for (i = 0; i < aEncParam->iNumLayer; i++)
        {
            aEncOption.encFrameRate[i] = iEncFrameRate[i] = aEncParam->iFrameRate[i];
            aEncOption.bitRate[i] = aEncParam->iBitRate[i];
            aEncOption.iQuant[i] = aEncParam->iIquant[i];
            aEncOption.pQuant[i] = aEncParam->iPquant[i];
        }

        if (aEncParam->iRateControlType == ECONSTANT_Q)
            aEncOption.rcType = CONSTANT_Q;
        else if (aEncParam->iRateControlType == ECBR_1)
            aEncOption.rcType = CBR_1;
        else if (aEncParam->iRateControlType == EVBR_1)
            aEncOption.rcType = VBR_1;
        else
            return ECVEI_FAIL;

    }
    else    // All default Settings
    {
        aEncOption.encMode = ENC_Mode;
        aEncOption.packetSize = aEncParam->iPacketSize;

        Int profile_level = (Int)ECVEI_CORE_LEVEL2;
        if (aEncParam->iNumLayer > 1) profile_level = (Int)ECVEI_CORE_SCALABLE_LEVEL3;
        aEncOption.profile_level = (ProfileLevelType)profile_level;

        aEncOption.rvlcEnable = RvlcMode;
        aEncOption.numLayers = aEncParam->iNumLayer;
        aEncOption.timeIncRes = 1000;
        aEncOption.tickPerSrc = (int)(1000 / aVidInFormat->iFrameRate + 0.5);

        for (i = 0; i < aEncParam->iNumLayer; i++)
        {
            aEncOption.encWidth[i] = iEncWidth[i] = aEncParam->iFrameWidth[i];
            aEncOption.encHeight[i] = iEncHeight[i] = aEncParam->iFrameHeight[i];
            aEncOption.encFrameRate[i] = iEncFrameRate[i] = aEncParam->iFrameRate[i];
            aEncOption.bitRate[i] = aEncParam->iBitRate[i];
            aEncOption.iQuant[i] = aEncParam->iIquant[i];
            aEncOption.pQuant[i] = aEncParam->iPquant[i];
            aEncOption.quantType[i] = quantType[i]; /* default to H.263 */
        }

        if (aEncParam->iRateControlType == ECONSTANT_Q)
            aEncOption.rcType = CONSTANT_Q;
        else if (aEncParam->iRateControlType == ECBR_1)
            aEncOption.rcType = CBR_1;
        else if (aEncParam->iRateControlType == EVBR_1)
            aEncOption.rcType = VBR_1;
        else
            return ECVEI_FAIL;

        // Check the bitrate, framerate, image size and buffer delay for 3GGP compliance
#ifdef FOR_3GPP_COMPLIANCE
        Check3GPPCompliance(aEncParam, iEncWidth, iEncHeight);
#endif
    }


    aEncOption.vbvDelay = (float)aEncParam->iBufferDelay;
    switch (aEncParam->iIFrameInterval)
    {
        case -1:
            aEncOption.intraPeriod = -1;
            break;
        case 0:
            aEncOption.intraPeriod = 0;
            break;
        default:
            aEncOption.intraPeriod = (int)(aEncParam->iIFrameInterval *  aVidInFormat->iFrameRate);
            break;
    }
    aEncOption.numIntraMB = aEncParam->iNumIntraMBRefresh;
    aEncOption.sceneDetect = (aEncParam->iSceneDetection == true) ? PV_ON : PV_OFF;
    aEncOption.noFrameSkipped = (aEncParam->iNoFrameSkip == true) ? PV_ON : PV_OFF;
    aEncOption.searchRange = aEncParam->iSearchRange;
    aEncOption.mv8x8Enable = (aEncParam->iMV8x8 == true) ? PV_ON : PV_OFF;

    if (PV_FALSE == PVInitVideoEncoder(&iEncoderControl, &aEncOption))
    {
        goto FAIL;
    }

    iNextModTime = 0;
    iInitialized = true;
    return ECVEI_SUCCESS;

FAIL:
    iInitialized = false;
    return ECVEI_FAIL;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF int32 CPVM4VEncoder::GetBufferSize()
{
    Int bufSize = 0;

    //PVGetVBVSize(&iEncoderControl,&bufSize);
    PVGetMaxVideoFrameSize(&iEncoderControl, &bufSize);

    return (int32) bufSize;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF int32 CPVM4VEncoder::GetEncodeWidth(int32 aLayer)
{
    return (int32)iEncWidth[aLayer];
}

OSCL_EXPORT_REF int32 CPVM4VEncoder::GetEncodeHeight(int32 aLayer)
{
    return (int32)iEncHeight[aLayer];
}

OSCL_EXPORT_REF float CPVM4VEncoder::GetEncodeFrameRate(int32 aLayer)
{
    return iEncFrameRate[aLayer];
}
OSCL_EXPORT_REF TCVEI_RETVAL CPVM4VEncoder::GetVolHeader(uint8 *volHeader, int32 *size, int32 layer)
{
    Int aSize, aLayer = layer;

    if (iInitialized == false) /* has to be initialized first */
        return ECVEI_FAIL;

    aSize = *size;
    if (PVGetVolHeader(&iEncoderControl, (UChar*)volHeader, &aSize, aLayer) == PV_FALSE)
        return ECVEI_FAIL;

    *size = aSize;
    return ECVEI_SUCCESS;
}

#ifdef PVAUTHOR_PROFILING
#include "pvauthorprofile.h"
#endif

/* ///////////////////////////////////////////////////////////////////////// */
// Value of aRemainingBytes is relevant when overrun buffer is used and return value is ECVEI_MORE_OUTPUT
OSCL_EXPORT_REF TCVEI_RETVAL CPVM4VEncoder::EncodeFrame(TPVVideoInputData  *aVidIn, TPVVideoOutputData *aVidOut, int *aRemainingBytes
#ifdef PVAUTHOR_PROFILING
        , void *aParam1
#endif
                                                       )
{

    Bool status;
    Int Size;
    Int nLayer = 0;
    ULong modTime;
    VideoEncFrameIO vid_in, vid_out;
    *aRemainingBytes = 0;

    if (iState == EEncode && iOverrunBuffer) // more output buffer to be copied out.
    {
        if (iOBSize > aVidOut->iBitStreamSize)
        {
            oscl_memcpy(aVidOut->iBitStream, iOverrunBuffer, aVidOut->iBitStreamSize);
            iOBSize -= aVidOut->iBitStreamSize;
            iOverrunBuffer += aVidOut->iBitStreamSize;
            *aRemainingBytes = iOBSize;
            return ECVEI_MORE_OUTPUT;
        }
        else
        {
            oscl_memcpy(aVidOut->iBitStream, iOverrunBuffer, iOBSize);
            aVidOut->iBitStreamSize = iOBSize;
            iOverrunBuffer = NULL;
            iOBSize = 0;
            iState = EIdle;
            *aRemainingBytes = 0;
            return ECVEI_SUCCESS;
        }
    }

    if (aVidIn->iSource == NULL)
    {
        return ECVEI_FAIL;
    }

    if (aVidIn->iTimeStamp >= iNextModTime) /* time to encode */
    {
        iState = EIdle; /* stop current encoding */

        Size = aVidOut->iBitStreamSize;

#ifdef PVAUTHOR_PROFILING
        if (aParam1)((CPVAuthorProfile*)aParam1)->Start();
#endif

        if (iVideoFormat == ECVEI_YUV420)
#ifdef YUV_INPUT
        {
            if (iYUVIn) /* iSrcWidth or iSrcHeight is not multiple of 16 */
            {
                CopyToYUVIn(aVidIn->iSource, iSrcWidth, iSrcHeight,
                ((iSrcWidth + 15) >> 4) << 4, ((iSrcHeight + 15) >> 4) << 4);
                iVideoIn = iYUVIn;
            }
            else /* otherwise, we can just use aVidIn->iSource */
            {
                iVideoIn = aVidIn->iSource;
            }
        }
#else
            return ECVEI_FAIL;
#endif
        else if ((iVideoFormat == ECVEI_RGB12) || (iVideoFormat == ECVEI_RGB24) || (iVideoFormat == ECVEI_YUV420SEMIPLANAR))
#if defined(RGB24_INPUT) || defined (RGB12_INPUT) || defined(YUV420SEMIPLANAR_INPUT)
        {
            ccRGBtoYUV->Convert((uint8*)aVidIn->iSource, iYUVIn);
            iVideoIn = iYUVIn;
        }
#else
            return ECVEI_FAIL;
#endif

#ifdef PVAUTHOR_PROFILING
        if (aParam1)((CPVAuthorProfile*)aParam1)->Stop(CPVAuthorProfile::EColorInput);
#endif

#ifdef PVAUTHOR_PROFILING
        if (aParam1)((CPVAuthorProfile*)aParam1)->Start();
#endif

        /* with backward-P or B-Vop this timestamp must be re-ordered */
        aVidOut->iExternalTimeStamp = aVidIn->iTimeStamp;
        aVidOut->iVideoTimeStamp = aVidOut->iExternalTimeStamp;

        vid_in.height = ((iSrcHeight + 15) >> 4) << 4;
        vid_in.pitch = ((iSrcWidth + 15) >> 4) << 4;
        vid_in.timestamp = aVidIn->iTimeStamp;
        vid_in.yChan = (UChar*)iVideoIn;
        vid_in.uChan = (UChar*)(iVideoIn + vid_in.height * vid_in.pitch);
        vid_in.vChan = vid_in.uChan + ((vid_in.height * vid_in.pitch) >> 2);

        status = PVEncodeVideoFrame(&iEncoderControl,
                                    &vid_in, &vid_out, &modTime, (UChar*)aVidOut->iBitStream, &Size, &nLayer);

        if (status == PV_TRUE)
        {
            iNextModTime = modTime;
            aVidOut->iLayerNumber = nLayer;
            aVidOut->iFrame = iVideoOut = (uint8*)vid_out.yChan;
            aVidOut->iVideoTimeStamp = vid_out.timestamp;

            PVGetHintTrack(&iEncoderControl, &aVidOut->iHintTrack);

#ifdef PVAUTHOR_PROFILING
            if (aParam1)((CPVAuthorProfile*)aParam1)->Stop(CPVAuthorProfile::EVideoEncode);
#endif

            iOverrunBuffer = PVGetOverrunBuffer(&iEncoderControl);
            if (iOverrunBuffer != NULL && nLayer != -1)
            {
                oscl_memcpy(aVidOut->iBitStream, iOverrunBuffer, aVidOut->iBitStreamSize);
                iOBSize = Size - aVidOut->iBitStreamSize;
                iOverrunBuffer += aVidOut->iBitStreamSize;
                iState = EEncode;
                return ECVEI_MORE_OUTPUT;
            }
            else
            {
                aVidOut->iBitStreamSize = Size;
                return ECVEI_SUCCESS;
            }
        }
        else
            return ECVEI_FAIL;
    }
    else /* if(aVidIn->iTimeStamp >= iNextModTime) */
    {
        aVidOut->iLayerNumber = -1;
        aVidOut->iBitStreamSize = 0;
#ifdef PVAUTHOR_PROFILING
        if (aParam1)((CPVAuthorProfile*)aParam1)->AddVal
            (CPVAuthorProfile::EVidSkip, iNextModTime - aVidIn->iTimeStamp);
#endif
        return ECVEI_SUCCESS;
    }
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TCVEI_RETVAL CPVM4VEncoder::FlushOutput(TPVVideoOutputData *aVidOut)
{
    OSCL_UNUSED_ARG(aVidOut);
    return ECVEI_SUCCESS;
}

/* ///////////////////////////////////////////////////////////////////////// */
TCVEI_RETVAL CPVM4VEncoder::Terminate()
{
    iState = EIdle; /* stop current encoding */
    if (iInitialized == true)
    {
        PVCleanUpVideoEncoder(&iEncoderControl);
        iInitialized = false;

    }

    if (iYUVIn)
    {
        oscl_free(iYUVIn);
        iYUVIn = NULL;
    }
    return ECVEI_SUCCESS;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TCVEI_RETVAL CPVM4VEncoder::UpdateBitRate(int32 aNumLayer, int32 *aBitRate)
{
#ifndef LIMITED_API
    Int i, bitRate[2] = {0, 0};

    for (i = 0; i < aNumLayer; i++)
    {
        bitRate[i] = aBitRate[i];
    }

    if (PVUpdateBitRate(&iEncoderControl, &bitRate[0]) == PV_TRUE)
        return ECVEI_SUCCESS;
    else
#endif
        return ECVEI_FAIL;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TCVEI_RETVAL CPVM4VEncoder::UpdateFrameRate(int32 aNumLayer, float *aFrameRate)
{
    OSCL_UNUSED_ARG(aNumLayer);
#ifndef LIMITED_API
    if (PVUpdateEncFrameRate(&iEncoderControl, aFrameRate) == PV_TRUE)
        return ECVEI_SUCCESS;
    else
#else
    OSCL_UNUSED_ARG(aFrameRate);
#endif
        return ECVEI_FAIL;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TCVEI_RETVAL CPVM4VEncoder::UpdateIFrameInterval(int32 aIFrameInterval)
{
#ifndef LIMITED_API
    if (PVUpdateIFrameInterval(&iEncoderControl, (Int)aIFrameInterval) == PV_TRUE)
        return ECVEI_SUCCESS;
    else
#endif
        return ECVEI_FAIL;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TCVEI_RETVAL CPVM4VEncoder::IFrameRequest()
{
#ifndef LIMITED_API
    if (PVIFrameRequest(&iEncoderControl) == PV_TRUE)
        return ECVEI_SUCCESS;
    else
#endif
        return ECVEI_FAIL;
}

/* ///////////////////////////////////////////////////////////////////////// */
OSCL_EXPORT_REF TCVEI_RETVAL CPVM4VEncoder::SetIntraMBRefresh(int32 aNumMBRefresh)
{
#ifndef LIMITED_API
    if (PVUpdateNumIntraMBRefresh(&iEncoderControl, aNumMBRefresh) == PV_TRUE)
        return ECVEI_SUCCESS;
    else
#endif
        return ECVEI_FAIL;
}

#ifdef YUV_INPUT
/* ///////////////////////////////////////////////////////////////////////// */
/* Copy from YUV input to YUV frame inside M4VEnc lib                       */
/* When input is not YUV, the color conv will write it directly to iVideoInOut. */
/* ///////////////////////////////////////////////////////////////////////// */

void CPVM4VEncoder::CopyToYUVIn(uint8 *YUV, Int width, Int height, Int width_16, Int height_16)
{
    UChar *y, *u, *v, *yChan, *uChan, *vChan;
    Int y_ind, ilimit, jlimit, i, j, ioffset;
    Int size = width * height;
    Int size16 = width_16 * height_16;

    /* do padding at the bottom first */
    /* do padding if input RGB size(height) is different from the output YUV size(height_16) */
    if (height < height_16 || width < width_16) /* if padding */
    {
        Int offset = (height < height_16) ? height : height_16;

        offset = (offset * width_16);

        if (width < width_16)
        {
            offset -= (width_16 - width);
        }

        yChan = (UChar*)(iYUVIn + offset);
        oscl_memset(yChan, 16, size16 - offset); /* pad with zeros */

        uChan = (UChar*)(iYUVIn + size16 + (offset >> 2));
        oscl_memset(uChan, 128, (size16 - offset) >> 2);

        vChan = (UChar*)(iYUVIn + size16 + (size16 >> 2) + (offset >> 2));
        oscl_memset(vChan, 128, (size16 - offset) >> 2);
    }

    /* then do padding on the top */
    yChan = (UChar*)iYUVIn; /* Normal order */
    uChan = (UChar*)(iYUVIn + size16);
    vChan = (UChar*)(uChan + (size16 >> 2));

    u = (UChar*)(&(YUV[size]));
    v = (UChar*)(&(YUV[size*5/4]));

    /* To center the output */
    if (height_16 > height)   /* output taller than input */
    {
        if (width_16 >= width)  /* output wider than or equal input */
        {
            i = ((height_16 - height) >> 1) * width_16 + (((width_16 - width) >> 3) << 2);
            /* make sure that (width_16-width)>>1 is divisible by 4 */
            j = ((height_16 - height) >> 2) * (width_16 >> 1) + (((width_16 - width) >> 4) << 2);
            /* make sure that (width_16-width)>>2 is divisible by 4 */
        }
        else  /* output narrower than input */
        {
            i = ((height_16 - height) >> 1) * width_16;
            j = ((height_16 - height) >> 2) * (width_16 >> 1);
            YUV += ((width - width_16) >> 1);
            u += ((width - width_16) >> 2);
            v += ((width - width_16) >> 2);
        }
        oscl_memset((uint8 *)yChan, 16, i);
        yChan += i;
        oscl_memset((uint8 *)uChan, 128, j);
        uChan += j;
        oscl_memset((uint8 *)vChan, 128, j);
        vChan += j;
    }
    else   /* output shorter or equal input */
    {
        if (width_16 >= width)   /* output wider or equal input */
        {
            i = (((width_16 - width) >> 3) << 2);
            /* make sure that (width_16-width)>>1 is divisible by 4 */
            j = (((width_16 - width) >> 4) << 2);
            /* make sure that (width_16-width)>>2 is divisible by 4 */
            YUV += (((height - height_16) >> 1) * width);
            u += (((height - height_16) >> 1) * width) >> 2;
            v += (((height - height_16) >> 1) * width) >> 2;
        }
        else  /* output narrower than input */
        {
            i = 0;
            j = 0;
            YUV += (((height - height_16) >> 1) * width + ((width - width_16) >> 1));
            u += (((height - height_16) >> 1) * width + ((width - width_16) >> 1)) >> 2;
            v += (((height - height_16) >> 1) * width + ((width - width_16) >> 1)) >> 2;
        }
        oscl_memset((uint8 *)yChan, 16, i);
        yChan += i;
        oscl_memset((uint8 *)uChan, 128, j);
        uChan += j;
        oscl_memset((uint8 *)vChan, 128, j);
        vChan += j;
    }

    /* Copy with cropping or zero-padding */
    if (height < height_16)
        jlimit = height;
    else
        jlimit = height_16;

    if (width < width_16)
    {
        ilimit = width;
        ioffset = width_16 - width;
    }
    else
    {
        ilimit = width_16;
        ioffset = 0;
    }

    /* Copy Y */
    /* Set up pointer for fast looping */
    y = (UChar*)YUV;

    if (width == width_16 && height == height_16) /* no need to pad */
    {
        oscl_memcpy(yChan, y, size);
    }
    else
    {
        for (y_ind = 0; y_ind < (jlimit - 1) ; y_ind++)
        {
            oscl_memcpy(yChan, y, ilimit);
            oscl_memset(yChan + ilimit, 16, ioffset); /* pad with zero */
            yChan += width_16;
            y += width;
        }
        oscl_memcpy(yChan, y, ilimit); /* last line no padding */
    }
    /* Copy U and V */
    /* Set up pointers for fast looping */
    if (width == width_16 && height == height_16) /* no need to pad */
    {
        oscl_memcpy(uChan, u, size >> 2);
        oscl_memcpy(vChan, v, size >> 2);
    }
    else
    {
        for (y_ind = 0; y_ind < (jlimit >> 1) - 1; y_ind++)
        {
            oscl_memcpy(uChan, u, ilimit >> 1);
            oscl_memcpy(vChan, v, ilimit >> 1);
            oscl_memset(uChan + (ilimit >> 1), 128, ioffset >> 1);
            oscl_memset(vChan + (ilimit >> 1), 128, ioffset >> 1);
            uChan += (width_16 >> 1);
            u += (width >> 1);
            vChan += (width_16 >> 1);
            v += (width >> 1);
        }
        oscl_memcpy(uChan, u, ilimit >> 1); /* last line no padding */
        oscl_memcpy(vChan, v, ilimit >> 1);
    }

    return ;
}
#endif

#ifdef FOR_3GPP_COMPLIANCE
void CPVM4VEncoder::Check3GPPCompliance(TPVVideoEncodeParam *aEncParam, Int *aEncWidth, Int *aEncHeight)
{

//MPEG-4 Simple profile and level 0
#define MAX_BITRATE 64000
#define MAX_FRAMERATE 15
#define MAX_WIDTH 176
#define MAX_HEIGHT 144
#define MAX_BUFFERSIZE 163840

    // check bitrate, framerate, video size and vbv buffer
    if (aEncParam->iBitRate[0] > MAX_BITRATE) aEncParam->iBitRate[0] = MAX_BITRATE;
    if (aEncParam->iFrameRate[0] > MAX_FRAMERATE) aEncParam->iFrameRate[0] = MAX_FRAMERATE;
    if (aEncWidth[0] > MAX_WIDTH) aEncWidth[0] = MAX_WIDTH;
    if (aEncHeight[0] > MAX_HEIGHT) aEncHeight[0] = MAX_HEIGHT;
    if (aEncParam->iBitRate[0]*aEncParam->iBufferDelay > MAX_BUFFERSIZE)
        aEncParam->iBufferDelay = (float)MAX_BUFFERSIZE / aEncParam->iBitRate[0];
}
#endif

