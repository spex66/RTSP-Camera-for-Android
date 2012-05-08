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
#ifndef PVVIDEODECODERINTERFACE_H_INCLUDED
#define PVVIDEODECODERINTERFACE_H_INCLUDED

// includes
#ifndef OSCL_BASE_H_INCLUDED
#include "oscl_base.h"
#endif

#ifndef VISUAL_HEADER_H_INCLUDED
#include "visual_header.h"
#endif

#include "oscl_aostatus.h"

// PVVideoDecoderInterface pure virtual interface class
class PVVideoDecoderInterface
{
    public:
        virtual ~PVVideoDecoderInterface() {};
        virtual bool    InitVideoDecoder(uint8 *volbuf[], int32 *volbuf_size, int32 nLayers, int32* iWidth, int32* iHeight, int *mode) = 0;
        virtual void    CleanUpVideoDecoder(void) = 0;
        virtual bool    DecodeVideoFrame(uint8 *bitstream[], uint32 *timestamp, int32 *buffer_size, uint *use_ext_ts, uint8 *yuv) = 0;

        // decode for dual core asynchronous operation
        virtual bool    DecodeVideoFrame(uint8 *bitstream[], uint32 *timestamp, int32 *buffer_size, uint *use_ext_ts, uint8 *yuv, OsclAOStatus *asynch)
        {
            OSCL_UNUSED_ARG(bitstream);
            OSCL_UNUSED_ARG(timestamp);
            OSCL_UNUSED_ARG(buffer_size);
            OSCL_UNUSED_ARG(use_ext_ts);
            OSCL_UNUSED_ARG(yuv);
            OSCL_UNUSED_ARG(asynch);
            return true;
        };
        virtual bool    DecodeVideoFrameAsyncResp(uint32 timestamp[], int32 buffer_size[])
        {
            OSCL_UNUSED_ARG(timestamp);
            OSCL_UNUSED_ARG(buffer_size);
            return true;
        };
//  virtual uint8*  GetDecOutputFrame(void) {};
        virtual void    GetDecOutputFrame(uint8*) {};
        virtual bool    getSynchResponse(uint32 timestamp[], int32 buffer_size[])
        {
            OSCL_UNUSED_ARG(timestamp);
            OSCL_UNUSED_ARG(buffer_size);
            return true;
        };
        virtual bool    DSPDecoderBusy()
        {
            return true;
        };

        virtual void    SetReferenceYUV(uint8 *YUV) = 0;
        virtual void    GetVideoDimensions(int32 *display_width, int32 *display_height) = 0;
        virtual void    SetPostProcType(int32 mode) = 0;
        virtual uint32  GetVideoTimestamp(void) = 0;
        virtual bool    GetVolInfo(VolInfo* pVolInfo) = 0;
        virtual bool    IsIFrame(void) = 0;
        virtual void    DecPostProcess(uint8 *YUV) = 0;
        virtual uint8*  GetDecOutputFrame(void) = 0;
        virtual bool    ResetVideoDecoder(void) = 0;
        virtual void    DecSetReference(uint8 *refYUV, uint32 timestamp) = 0;
        virtual void    DecSetEnhReference(uint8 *refYUV, uint32 timestamp) = 0;
        virtual uint32 GetProfileAndLevel(void) = 0;
        virtual uint32 GetDecBitrate(void) = 0; // This function returns the average bits per second.
};

#endif // PVVIDEODECODERINTERFACE_H_INCLUDED


