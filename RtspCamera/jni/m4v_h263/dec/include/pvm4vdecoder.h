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
#ifndef PVM4VDECODER_H_INCLUDED
#define PVM4VDECODER_H_INCLUDED

#ifndef OSCL_BASE_H_INCLUDED
#include "oscl_base.h"
#endif

#ifndef VISUAL_HEADER_H_INCLUDED
#include "visual_header.h"
#endif

#ifndef PVVIDEODECODERINTERFACE_H_INCLUDED
#include "pvvideodecoderinterface.h"
#endif


class PVM4VDecoder : public PVVideoDecoderInterface
{
    public:
        virtual ~PVM4VDecoder();
        static PVM4VDecoder* New(void);
        /**
        This is the initialization routine of the MPEG-4 video decoder for decoding an nLayers MPEG-4 bitstream (not
        used for H.263 and ShortHeader Modes). Video object layer headers for all layers are passed in through the array
        of buffer, volbuf[].  The size of each header is stored in volbuf_size[]. The iWidth and iHeight fields specify
        the maximum decoded frame dimensions that should be handled by the decoder for H.263 and ShortHeader Modes (does
        not have any effect for MPEG-4 Mode).  When the initialization routine is completed, for an MPEG-4 input
        bitstream the display width and display height will be set to iWidth and iHeight, respectively. The mode
        specifies the elementary bitstream type (0:H.263 and 1:M4V).  This function shall be called before any other
        API's are used.
         */
        virtual bool    InitVideoDecoder(uint8 *volbuf[], int32 *volbuf_size, int32 nLayers, int32* iWidth, int32* iHeight, int *mode);

        /**
        This function frees all the memory used by the decoder library.
         */
        virtual void    CleanUpVideoDecoder(void);

        /**
        This function takes the compressed bitstreams of a multiple layer video and decodes the next YUV 4:2:0 frame to
        be displayed.  The application has to allocate memory for the output frame before this function is called, and
        should be passed into the decoder through yuv parameter. The allocated memory should be WORD aligned. The input
        bitstream is decoded into this passed YUV buffer.  The unpadded (non-multiple of 16) size of the frame can be
        obtained by calling GetVideoDimensions() api.  The input parameter, bitstream[], is an array of buffers that
        stores the next frames to be decoded from all the layers.  The use_ext_timestamp[] parameter tells the decoder
        to use the externally provided system timestamp (1) (ignoring internal bitstream timestamp) or bitstream (0)
        timestamp. The buffer_size[]  parameter for video layers is updated with the remaining number of bytes in each
        layer after consuming a frame worth of data from a particular layer. This is useful if multiple frame data is
        passed into the video decoder at once. The decoder will decode one frame at a time.  If there is no data at the
        time of decoding for layer idx, buffer_size[idx] shall be set to 0, otherwise it shall be set to the number of
        bytes available.  Upon return, this array flags each layer that was used by decoder library.  For example, if
        the buffer of layer idx is used by the library, buffer_size[idx] will be set to 0.  The application has to refill
        the data in this buffer before the decoding of the next frame.  Note that the decoder may use more than one layer
        of the bitstream at the same time (in the case of spatial/SNR scalability).  The function returns FALSE (0) if an
        error has occurred during the decoding process.

        The decoding operation requires at least 2 frame buffers. It is up to the user to manage the handling of frame
        buffers. The frames are always decoded into the YUV-buffer that is passed in using the yuv frame pointer
        parameter. This YUV frame buffer is kept as reference frame for decoding of the next frame. After decoding of
        the frame following the current frame this buffer can be recycled or freed.

         */
        virtual bool    DecodeVideoFrame(uint8 *bitstream[], uint32 *timestamp, int32 *buffer_size, uint *use_ext_timestamp, uint8 *yuv);

        /**
        This function sets the reference frame for the decoder. The user should allocate the memory for the reference
        frame. The size of the reference frame is determined after calling the GetVideoDimensions( ) api. The size
        should be set to ((display_height + 15)/16)*16  x ((display_width + 15)/16)*16.
         */
        virtual void    SetReferenceYUV(uint8 *YUV);

        /**
        This function returns the display width and height of the video bitstream.
         */
        virtual void    GetVideoDimensions(int32 *display_width, int32 *display_height);

        /**
        This function sets the postprocessing type to be used. pp_mode =0 is no postprocessing, pp_mode=1 is deblocking
        only, pp_mode=3 is deblocking + deringing.
         */
        virtual void    SetPostProcType(int32 mode);

        /**
        This function returns the timestamp of the most recently decoded video frame.
         */
        virtual uint32  GetVideoTimestamp(void);

        /**
        This function is used to get VOL header info.Currently only used to get profile and level info.
         */
        virtual bool GetVolInfo(VolInfo* pVolInfo);

        /**
        This function returns profile and level id.
         */
        virtual uint32 GetProfileAndLevel(void);

        /**
        This function returns average bitrate. (bits per sec)
         */
        virtual uint32 GetDecBitrate(void);

        /**
        This function checks whether the last decoded frame is an INTRA frame or not.
         */
        virtual bool    IsIFrame(void);

        /**
        This function performs postprocessing on the current decoded frame and writes the postprocessed frame to the
        *yuv frame. If a separate yuv frame is not used for postprocessed frames NULL pointer can be passed in. In this
        case the postprocessing is done in an internal yuv frame buffer. The pointer to his buffer can be obtained by
        the next GetDecOutputFrame( ) api.
         */
        virtual void    DecPostProcess(uint8 *YUV);

        /**
        This function returns the pointer to the frame to be displayed.
         */
        virtual uint8*  GetDecOutputFrame(void);

        /**
        This function is not used.
         */
        virtual bool    ResetVideoDecoder(void);

        /**
        This function is not used.
         */
        virtual void  DecSetReference(uint8 *refYUV, uint32 timestamp);

        /**
        This function is not used.
         */
        virtual void  DecSetEnhReference(uint8 *refYUV, uint32 timestamp);

    private:
        PVM4VDecoder();
        bool Construct(void);
        void *iVideoCtrls;
};

#endif // PVM4VDECODER_H_INCLUDED

