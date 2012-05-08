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

#ifndef __PVVIDEODECBASE_H
#define __PVVIDEODECBASE_H


// includes
#include <e32std.h>
#include <e32base.h>
#include "mp4dec_api.h"
#include "dspmsgproto1.h"
#include "dspmsgproto2.h"
#include "dspmsgproto4.h"
#include "dsp_msg_proto6.h"
#include "DspMsgProto7.h"
#include "dspmsgproto9.h"
#include "dspmsgproto10.h"
#include "dspmsgproto11.h"
#include "dspmsgproto12.h"
#include "dspmsgproto13.h"
#include "dspmsgproto14.h"
#include "dspmsgproto15.h"
#include "dspmsgproto16.h"
#include "dspmsgproto17.h"
#include "dspmsgproto20.h"

#ifndef OSCL_BASE_H_INCLUDED
#include "oscl_base.h"
#endif

#ifndef VISUAL_HEADER_H_INCLUDED
#include "visual_header.h"
#endif

#ifndef PVVIDEODECODERINTERFACE_H_INCLUDED
#include "pvvideodecoderinterface.h"
#endif

#include "pvzcdt.h"
#define UChar uint8
#define MAX_LAYERS 1

#define USING_SYNC_2STEP

class PVM4VDecoder_DPI : public PVVideoDecoderInterface
{
    public:

        PVM4VDecoder_DPI();
        PVM4VDecoder_DPI(CPVDsp* aDsp);
        ~PVM4VDecoder_DPI();
        static PVM4VDecoder_DPI* New(void);

        IMPORT_C static PVM4VDecoder_DPI* NewL(CPVDsp* aDsp);

        IMPORT_C bool InitVideoDecoder(uint8 *volbuf[], int32 *volbuf_size, int32 nLayers, int32* iWidth, int32* iHeight, int *mode);

////// not implemeneted/////////////////////////////////////////////////////////////////////////////////////////////////////////////
        IMPORT_C bool  GetVolInfo(VolInfo* pVolInfo) {};
        IMPORT_C void   DecPostProcess(uint8 *YUV) {};
        IMPORT_C void  DecSetEnhReference(uint8 *refYUV, uint32 timestamp) {};
        IMPORT_C void   SetReferenceYUV(uint8 *YUV) {};
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        IMPORT_C void   GetVideoDimensions(int32 *display_width, int32 *display_height)
        {
            *display_width = GetVideoWidth();
            *display_height = GetVideoHeight();
        };

#ifdef USING_SYNC_2STEP
        IMPORT_C bool   getSynchResponse(uint32 timestamp[], int32 buffer_size[]);
        IMPORT_C bool   DSPDecoderBusy();
#endif

        IMPORT_C int32  GetVideoWidth(void);
        IMPORT_C int32  GetVideoHeight(void);
        IMPORT_C int32  DPIFreeVideoDecCtrls(void);

        IMPORT_C void   CleanUpVideoDecoder(void);
        IMPORT_C bool   IsIFrame(void);

        IMPORT_C void   SetPostProcType(int32 aMode);

        IMPORT_C bool  DecodeVideoFrame(uint8 *bitstream[], uint32 *timestamp, int32 *buffer_size, uint *use_ext_ts, uint8 *YUV);
        IMPORT_C bool  DecodeVideoFrame(uint8 *bitstream[], uint32 *timestamp, int32 *buffer_size, uint *use_ext_ts, uint8 *YUV, TRequestStatus *aRequestStatus);
        IMPORT_C bool  DecodeVideoFrameAsyncResp(uint32 timestamp[], int32 buffer_size[]);

        IMPORT_C bool   DecodeStillVideoFrame(uint8 *buffer, int32 buf_size, uint8 *YUV);

        IMPORT_C bool   GetStillVideoFrameSize(uint8 *buffer, int32 buf_size, int32 *width, int32 *height);

        IMPORT_C uint8*  GetDecOutputFrame(void);
        IMPORT_C void    GetDecOutputFrame(uint8*);

        IMPORT_C uint8*  CopyDecOutputFrameToSharedMemBuf(void);

        IMPORT_C bool   ResetVideoDecoder(void);

        IMPORT_C TDspPointer DPIAllocVideoDecCtrls(void);

        IMPORT_C uint32 GetVideoTimestamp(void);

        IMPORT_C uint32 GetProfileAndLevel(void);

        IMPORT_C uint32 GetDecBitrate(void);

        // only port the API's used in PVPlayer 2.0

        IMPORT_C bool   ExtractVolHeader(uint8 *video_buffer, uint8 *vol_header, int32 *vol_header_size);

        IMPORT_C void DecSetReference(uint8 *refYUV, uint32 timestamp);

#if defined USE_PV_TRANSFER_BUFFER
        IMPORT_C inline RPVTransferBuffer& GetTxTransferBuffer()
        {
            return iTxTransferBuffer;
        }
        IMPORT_C inline RPVTransferBuffer& GetRxTransferBuffer()
        {
            return iRxTransferBuffer;
        }
#else
        IMPORT_C inline RTransferBuffer& GetTxTransferBuffer()
        {
            return iTxTransferBuffer;
        }

        IMPORT_C inline RTransferBuffer& GetRxTransferBuffer()
        {
            return iRxTransferBuffer;
        }
#endif

        IMPORT_C inline TInt GetNLayers()
        {
            return iNLayers;
        }

        IMPORT_C inline void SetNLayers(int32 aNLayers)
        {
            iNLayers = aNLayers;
        }

        TDspPointer iVideoCtrls;

    protected:

        void ConstructL(void);
        bool Construct();

        CPVDsp        *iDsp;
        TDspPointer   *iBitstreamDspPointer;
        int32           iNLayers;
        CDspMsgProto7  dspMsgProto7;
        CDspMsgProto15 dspMsgProto15;
        CDspMsgProto16 dspMsgProto16;

    private:

        bool iWaitingBitstream;

        // the order of object declaration is required to ensure the proper sequence of constructor invocation

#if defined USE_PV_TRANSFER_BUFFER
        RPVTransferBuffer iTxTransferBuffer;
        RPVTransferWindow iTxTransferWindow;
        RPVTransferBuffer iRxTransferBuffer;
        RPVTransferWindow iRxTransferWindow;
#else
        RTransferBuffer iTxTransferBuffer;
        RTransferWindow iTxTransferWindow;
        RTransferBuffer iRxTransferBuffer;
        RTransferWindow iRxTransferWindow;
#endif

        RPVTransferBuffer iVideoTransBuf[MAX_LAYERS];
        RPVTransferWindow iVideoTransWin[MAX_LAYERS];
        RPVTransferBuffer iVolHeaderTransBuf[MAX_LAYERS];
        RPVTransferWindow iVolHeaderTransWin[MAX_LAYERS];
        RPVTransferBuffer iDecodeBusyFlagTransBuf;
        RPVTransferWindow iDecodeBusyFlagTransWin;

        unsigned short *iDecodeBusyFlagPtr;
        unsigned char  *iVolHeader[MAX_LAYERS];
        unsigned char  *iVideoBuffer[MAX_LAYERS];

        uint iHeight;
        uint iWidth;

        uint iBufferHeight;
        uint iBufferWidth;

        CDspMsgProto1 dspMsgProto1;
        CDspMsgProto2 dspMsgProto2;
        CDspMsgProto4 dspMsgProto4;
        CDspMsgProto6 dspMsgProto6; //  for SBR
        CDspMsgProto9 dspMsgProto9;
        CDspMsgProto10 dspMsgProto10;
        CDspMsgProto11 dspMsgProto11;
        CDspMsgProto12 dspMsgProto12;
        CDspMsgProto13 dspMsgProto13;
        CDspMsgProto14 dspMsgProto14;
        CDspMsgProto17 dspMsgProto17;
        CDspMsgProto20 dspMsgProto20;

        int32 iCurrVideoTimeStamp;
        uint8* iYuvOutputPtr;
};

#endif
