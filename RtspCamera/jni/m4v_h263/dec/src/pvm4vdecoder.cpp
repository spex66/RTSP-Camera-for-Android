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
//////////////////////////////////////////////////////////////////////////////////
//                                                                              //
//  File: pvm4vdecoder.cpp                                                  //
//                                                                              //
//////////////////////////////////////////////////////////////////////////////////

#include "oscl_mem.h"
#include "mp4dec_api.h"
#include "pvm4vdecoder.h"


#define OSCL_DISABLE_WARNING_FORCING_INT_TO_BOOL
#include "osclconfig_compiler_warnings.h"


/////////////////////////////////////////////////////////////////////////////
PVM4VDecoder::PVM4VDecoder() : iVideoCtrls(NULL)
{
}


PVM4VDecoder* PVM4VDecoder::New(void)
{
    PVM4VDecoder* self = new PVM4VDecoder;

    if (self)
    {
        if (!self->Construct())
        {
            OSCL_DELETE(self);
            self = NULL;
        }
    }

    return self;
}

bool PVM4VDecoder::Construct()
{
    iVideoCtrls = (VideoDecControls *) new VideoDecControls;
    if (iVideoCtrls)
    {
        oscl_memset(iVideoCtrls, 0, sizeof(VideoDecControls));
        return true;
    }
    else
    {
        return false;
    }
}

/////////////////////////////////////////////////////////////////////////////
PVM4VDecoder::~PVM4VDecoder()
{
    if (iVideoCtrls)
    {
        OSCL_DELETE((VideoDecControls *)iVideoCtrls);
        iVideoCtrls = NULL;
    }
}

/////////////////////////////////////////////////////////////////////////////
bool PVM4VDecoder::InitVideoDecoder(uint8 *volbuf[],
                                    int32 *volbuf_size,
                                    int32 nLayers,
                                    int32* iWidth,
                                    int32* iHeight,
                                    int *mode)
{
    if (PVInitVideoDecoder((VideoDecControls *)iVideoCtrls, (uint8 **) volbuf, (int32*)volbuf_size, (int32)nLayers, *iWidth, *iHeight, (MP4DecodingMode) *mode))
    {
        GetVideoDimensions(iWidth, iHeight);
        *mode = (int)PVGetDecBitstreamMode((VideoDecControls *)iVideoCtrls);
        return true;
    }
    else
    {
        return false;
    }

}

/////////////////////////////////////////////////////////////////////////////
bool PVM4VDecoder::GetVolInfo(VolInfo* pVolInfo)
{
    if (!iVideoCtrls || !pVolInfo) return false;
    if (PVGetVolInfo((VideoDecControls *)iVideoCtrls, pVolInfo))
    {
        return true;
    }
    else
    {
        return false;
    }
}

/////////////////////////////////////////////////////////////////////////////
void PVM4VDecoder::CleanUpVideoDecoder(void)
{
    PVCleanUpVideoDecoder((VideoDecControls *)iVideoCtrls);
}

/////////////////////////////////////////////////////////////////////////////
bool PVM4VDecoder::DecodeVideoFrame(uint8 *bitstream[], uint32 *timestamp, int32 *buffer_size, uint *use_ext_timestamp, uint8 *currYUV)
{
    return PVDecodeVideoFrame((VideoDecControls *)iVideoCtrls, (uint8 **) bitstream, (uint32*)timestamp, (int32*)buffer_size, (uint *) use_ext_timestamp, (uint8 *) currYUV) ? true : false;
}

//////////////////////////////////////////////////////////////////////////////
void  PVM4VDecoder::SetReferenceYUV(uint8 *YUV)
{
    PVSetReferenceYUV((VideoDecControls *)iVideoCtrls, YUV);
}

/////////////////////////////////////////////////////////////////////////////
void PVM4VDecoder::GetVideoDimensions(int32 *display_width, int32 *display_height)
{
    PVGetVideoDimensions((VideoDecControls *)iVideoCtrls, display_width, display_height);
}

/////////////////////////////////////////////////////////////////////////////
void PVM4VDecoder::SetPostProcType(int32 mode)
{
    PVSetPostProcType((VideoDecControls *)iVideoCtrls, mode);
}

/////////////////////////////////////////////////////////////////////////////
uint32 PVM4VDecoder::GetVideoTimestamp(void)
{
    return PVGetVideoTimeStamp((VideoDecControls *)iVideoCtrls);
}

/////////////////////////////////////////////////////////////////////////////
bool PVM4VDecoder::IsIFrame(void)
{
    return IsIntraFrame((VideoDecControls *)iVideoCtrls) ? true : false;
}

/////////////////////////////////////////////////////////////////////////////
void PVM4VDecoder::DecPostProcess(uint8 *YUV)
{
    PVDecPostProcess((VideoDecControls *)iVideoCtrls, (uint8 *) YUV);
}

/////////////////////////////////////////////////////////////////////////////
uint8* PVM4VDecoder::GetDecOutputFrame(void)
{
    PVDecPostProcess((VideoDecControls *)iVideoCtrls, NULL);
    return (uint8 *) PVGetDecOutputFrame((VideoDecControls *)iVideoCtrls);
}

/////////////////////////////////////////////////////////////////////////////
bool PVM4VDecoder::ResetVideoDecoder(void)
{
    return PVResetVideoDecoder((VideoDecControls *)iVideoCtrls) ? true : false;
}

/////////////////////////////////////////////////////////////////////////////
void PVM4VDecoder::DecSetReference(uint8 *refYUV, uint32 timestamp)
{
    PVDecSetReference((VideoDecControls *)iVideoCtrls, (uint8*)refYUV, timestamp);
    return ;
}

/////////////////////////////////////////////////////////////////////////////
void PVM4VDecoder::DecSetEnhReference(uint8 *refYUV, uint32 timestamp)
{
    PVDecSetEnhReference((VideoDecControls *)iVideoCtrls, (uint8*)refYUV, timestamp);
    return ;
}
/////////////////////////////////////////////////////////////////////////////
uint32 PVM4VDecoder::GetDecBitrate(void)
{
    return ((uint32)PVGetDecBitrate((VideoDecControls *)iVideoCtrls));
}

/////////////////////////////////////////////////////////////////////////////
uint32 PVM4VDecoder::GetProfileAndLevel(void)
{
    VolInfo iVolInfo;
    if (GetVolInfo(&iVolInfo))
    {
        return iVolInfo.profile_level_id;
    }
    else
    {
        return 0;
    }
}
