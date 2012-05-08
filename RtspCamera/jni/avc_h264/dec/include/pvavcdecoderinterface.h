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
#ifndef PVAVCDECODERINTERFACE_H_INCLUDED
#define PVAVCDECODERINTERFACE_H_INCLUDED

// includes
#ifndef OSCL_BASE_H_INCLUDED
#include "oscl_base.h"
#endif

#ifndef AVCDEC_API_H_INCLUDED
#include "avcdec_api.h"
#endif

typedef void (*FunctionType_Unbind)(void *, int);
typedef int (*FunctionType_Alloc)(void *, int, uint8 **);
typedef int (*FunctionType_SPS)(void *, uint, uint);
typedef int (*FunctionType_Malloc)(void *, int32, int);
typedef void(*FunctionType_Free)(void *, int);


// PVAVCDecoderInterface pure virtual interface class
class PVAVCDecoderInterface
{
    public:
        virtual ~PVAVCDecoderInterface() {};
        virtual void    CleanUpAVCDecoder(void) = 0;
        virtual void    ResetAVCDecoder(void) = 0;
        virtual int32   DecodeSPS(uint8 *bitstream, int32 buffer_size) = 0;
        virtual int32   DecodePPS(uint8 *bitstream, int32 buffer_size) = 0;
        virtual int32   DecodeAVCSlice(uint8 *bitstream, int32 *buffer_size) = 0;
        virtual bool    GetDecOutput(int *indx, int *release, AVCFrameIO* output) = 0;
        virtual void    GetVideoDimensions(int32 *width, int32 *height, int32 *top, int32 *left, int32 *bottom, int32 *right) = 0;
};

#endif // PVAVCDECODERINTERFACE_H_INCLUDED


