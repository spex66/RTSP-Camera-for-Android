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
#ifndef PVAVCDECODER_H_INCLUDED
#define PVAVCDECODER_H_INCLUDED

#ifndef PVAVCDECODERINTERFACE_H_INCLUDED
#include "pvavcdecoderinterface.h"
#endif

#ifndef AVCDEC_API_H_INCLUDED
#include "avcdec_api.h"
#endif

// AVC video decoder
class PVAVCDecoder : public PVAVCDecoderInterface
{

    public:
        static PVAVCDecoder* New(void);
        virtual ~PVAVCDecoder();
        virtual int AVC_DPBAlloc(uint frame_size_in_mbs, uint num_buffers);
        virtual void AVC_FrameUnbind(int indx);
        virtual int AVC_FrameBind(int indx, uint8** yuv);
        virtual void    CleanUpAVCDecoder(void);
        virtual void    ResetAVCDecoder(void);
        virtual int32   DecodeSPS(uint8 *bitstream, int32 buffer_size);
        virtual int32   DecodePPS(uint8 *bitstream, int32 buffer_size);
        virtual int32   DecodeAVCSlice(uint8 *bitstream, int32 *buffer_size);
        virtual bool    GetDecOutput(int *indx, int *release, AVCFrameIO* output);
        virtual void    GetVideoDimensions(int32 *width, int32 *height, int32 *top, int32 *left, int32 *bottom, int32 *right);

    private:
        PVAVCDecoder();

        bool Construct(void);
        AVCHandle iAvcHandle;
        uint8*  iDPB;
        bool*   iFrameUsed;
        uint8** iFramePtr;
        int     iNumFrames;
};

#endif
