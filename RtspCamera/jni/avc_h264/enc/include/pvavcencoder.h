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
#ifndef PVAVCENCODER_H_INCLUDED
#define PVAVCENCODER_H_INCLUDED

#ifndef PVAVCENCODERINTERFACE_H_INCLUDED
#include "pvavcencoderinterface.h"
#endif

#ifndef AVCENC_API_H_INCLUDED
#include "avcenc_api.h"
#endif

/** AVC encoder class interface. See PVAVCEncoderInterface APIs for
virtual functions definitions. */
class PVAVCEncoder : public PVAVCEncoderInterface
{

    public:
        OSCL_IMPORT_REF static PVAVCEncoder* New(void);
        OSCL_IMPORT_REF virtual ~PVAVCEncoder();

        OSCL_IMPORT_REF virtual TAVCEI_RETVAL Initialize(TAVCEIInputFormat* aVidInFormat, TAVCEIEncodeParam* aEncParam);
        OSCL_IMPORT_REF virtual int32 GetMaxOutputBufferSize();
        OSCL_IMPORT_REF virtual TAVCEI_RETVAL Encode(TAVCEIInputData* aVidIn);
        OSCL_IMPORT_REF virtual TAVCEI_RETVAL GetParameterSet(uint8* paramSet, int32* size, int* nalType);
        OSCL_IMPORT_REF virtual TAVCEI_RETVAL GetOutput(TAVCEIOutputData* aVidOut, int *aRemainingBytes);
        OSCL_IMPORT_REF virtual TAVCEI_RETVAL FlushInput();
        virtual TAVCEI_RETVAL CleanupEncoder();

        OSCL_IMPORT_REF virtual TAVCEI_RETVAL UpdateBitRate(int32* aBitRate);
        OSCL_IMPORT_REF virtual TAVCEI_RETVAL UpdateFrameRate(OsclFloat* aFrameRate);
        OSCL_IMPORT_REF virtual TAVCEI_RETVAL UpdateIDRFrameInterval(int32 aIDRFrameInterval);
        OSCL_IMPORT_REF virtual TAVCEI_RETVAL IDRRequest();

        OSCL_IMPORT_REF virtual int32 GetEncodeWidth(int32 aLayer);
        OSCL_IMPORT_REF virtual int32 GetEncodeHeight(int32 aLayer);
        OSCL_IMPORT_REF virtual OsclFloat GetEncodeFrameRate(int32 aLayer);

        /* for avc encoder lib callback functions */
        int     AVC_DPBAlloc(uint frame_size_in_mbs, uint num_buffers);
        int     AVC_FrameBind(int indx, uint8** yuv);
        void    AVC_FrameUnbind(int indx);

    private:

        PVAVCEncoder();
        bool Construct(void);
        TAVCEI_RETVAL Init(TAVCEIInputFormat *aVidInFormat, TAVCEIEncodeParam *aEncParam, AVCEncParams& aEncOption);

        void CopyToYUVIn(uint8* YUV, int width, int height);

        AVCProfile  mapProfile(TAVCEIProfile in);
        AVCLevel    mapLevel(TAVCEILevel out);

        /* internal enum */
        enum TAVCEncState
        {
            ECreated,
            EInitialized,
            EEncoding
        };

        TAVCEncState    iState;
        uint32      iId;

        /* Pure virtuals from OsclActiveObject implemented in this derived class */
        int     iSrcWidth;
        int     iSrcHeight;
        int     iFrameOrientation;
        OsclFloat       iSrcFrameRate;
        int     iEncWidth;
        int     iEncHeight;
        OsclFloat   iEncFrameRate;
        TAVCEIVideoFormat   iVideoFormat;

        /* variables needed in operation */
        AVCHandle iAvcHandle;
        AVCFrameIO iVidIn;
        uint8*  iYUVIn;
        uint8*  iVideoIn;
        uint8*  iVideoOut;
        uint32  iTimeStamp;
        uint32  iPacketSize;
        uint8*  iOverrunBuffer;
        int     iOBSize;
        AVCEnc_Status iEncStatus;
        bool    iIDR;
        int     iDispOrd;

        uint8*  iDPB;
        bool*   iFrameUsed;
        uint8** iFramePtr;
        int     iNumFrames;

        /* Tables in color coversion */
        uint8 * iY_Table;
        uint16* iCb_Table;
        uint16* iCr_Table;
        uint16* ipCb_Table;
        uint16* ipCr_Table;


        int     iNumLayer;
};

#endif
