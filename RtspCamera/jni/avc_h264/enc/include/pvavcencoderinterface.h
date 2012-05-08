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
#ifndef PVAVCENCODERINTERFACE_H_INCLUDED
#define PVAVCENCODERINTERFACE_H_INCLUDED

#ifndef OSCL_BASE_H_INCLUDED
#include "oscl_base.h"
#endif

#define AVC_MAX_LAYER 1

/** General returned values. */
enum TAVCEI_RETVAL
{
    EAVCEI_SUCCESS,
    EAVCEI_FAIL,    // upon receiving fail, encoder should be reset

    /** Encode return values */
    EAVCEI_FRAME_DROP,    // current frame is dropped, send in a new frame with new timestamp
    EAVCEI_NOT_READY,  // the previous frame is still being processed
    EAVCEI_INPUT_ERROR, // error with input buffers

    /** GetOutput return values */
    EAVCEI_MORE_DATA,  // there are more data to be retrieve (multiple fragments of a NAL)
    EAVCEI_MORE_NAL     // there is more NAL to be retrieved

} ;

/** Contains supported input format */
enum TAVCEIVideoFormat
{
    EAVCEI_VDOFMT_RGB24,
    EAVCEI_VDOFMT_RGB12,
    EAVCEI_VDOFMT_YUV420,
    EAVCEI_VDOFMT_UYVY,
    EAVCEI_VDOFMT_YUV420SEMIPLANAR
};

/** Type of contents for optimal encoding mode. */
enum TAVCEIEncodingMode
{
    /** Content is encoded as fast as possible with error protection */
    EAVCEI_ENCMODE_TWOWAY,

    /** Content is encoded as fast as possible without error protection */
    EAVCEI_ENCMODE_RECORDER,

    /** Content is encoded with better quality (slow) with error protection */
    EAVCEI_ENCMODE_STREAMING,

    /** Content is encoded with better quality (slow) without error protection */
    EAVCEI_ENCMODE_DOWNLOAD
};

/** Rate control type. */
enum TAVCEIRateControlType
{
    /** Constant quality, variable bit rate, fixed quantization level. */
    EAVCEI_RC_CONSTANT_Q,

    /** Short-term constant bit rate control. */
    EAVCEI_RC_CBR_1,

    /** Long-term constant bit rate control. */
    EAVCEI_RC_VBR_1
};

/** Targeted profile to encode. */
enum TAVCEIProfile
{
    /* Non-scalable profile */
    EAVCEI_PROFILE_DEFAULT,
    EAVCEI_PROFILE_BASELINE,
    EAVCEI_PROFILE_MAIN,
    EAVCEI_PROFILE_EXTENDED,
    EAVCEI_PROFILE_HIGH,
    EAVCEI_PROFILE_HIGH10,
    EAVCEI_PROFILE_HIGH422,
    EAVCEI_PROFILE_HIGH444
};

/** Targeted level to encode. */
enum TAVCEILevel
{
    EAVCEI_LEVEL_AUTODETECT,
    EAVCEI_LEVEL_1,
    EAVCEI_LEVEL_1B,
    EAVCEI_LEVEL_11,
    EAVCEI_LEVEL_12,
    EAVCEI_LEVEL_13,
    EAVCEI_LEVEL_2,
    EAVCEI_LEVEL_21,
    EAVCEI_LEVEL_22,
    EAVCEI_LEVEL_3,
    EAVCEI_LEVEL_31,
    EAVCEI_LEVEL_32,
    EAVCEI_LEVEL_4,
    EAVCEI_LEVEL_41,
    EAVCEI_LEVEL_42,
    EAVCEI_LEVEL_5,
    EAVCEI_LEVEL_51,
};

/** Output format */
enum TAVCEIOutputFormat
{
    /** output in byte stream format according to Annex B */
    EAVCEI_OUTPUT_ANNEXB,

    /** output for MP4 file format */
    EAVCEI_OUTPUT_MP4,

    /** output in RTP format according to RFC 3984 */
    EAVCEI_OUTPUT_RTP
};


/** This structure contains encoder settings. */
struct TAVCEIEncodeParam
{
    /** Specifies an  ID that will be used to specify this encoder while returning
    the bitstream in asynchronous mode. */
    uint32              iEncodeID;

    /** Specifies the targeted profile, and will also specifies available tools for iEncMode.
    If default is used, encoder will choose its own preferred profile. If autodetect is used, encoder
    will check other settings and choose the right profile that doesn't have any conflicts. */
    TAVCEIProfile       iProfile;

    /** Specifies the target level  When present,
    other settings will be checked against the range allowable by this target level.
    Fail will returned upon Initialize call. If not known, users must set it to autodetect. Encoder will
    calculate the right level that doesn't conflict with other settings. */
    TAVCEILevel         iLevel;

    /** Specifies whether base only (iNumLayer = 1) or base + enhancement layer
    (iNumLayer =2 ) is to be used. */
    int32               iNumLayer;

    /** Specifies the width in pixels of the encoded frames. IFrameWidth[0] is for
    base layer and iFrameWidth[1] is for enhanced layer. */
    int                 iFrameWidth[AVC_MAX_LAYER];

    /** Specifies the height in pixels of the encoded frames. IFrameHeight[0] is for
    base layer and iFrameHeight[1] is for enhanced layer. */
    int                 iFrameHeight[AVC_MAX_LAYER];

    /** Specifies the cumulative bit rate in bit per second. IBitRate[0] is for base
    layer and iBitRate[1] is for base+enhanced layer.*/
    int                 iBitRate[AVC_MAX_LAYER];

    /** Specifies the cumulative frame rate in frame per second. IFrameRate[0] is for
    base layer and iFrameRate[1] is for base+enhanced layer. */
    OsclFloat               iFrameRate[AVC_MAX_LAYER];

    /** Specifies the encoding mode. This translates to the complexity of encoding modes and
    error resilient tools.
    */
    TAVCEIEncodingMode  iEncMode;

    /** Specifies that SPS and PPS are retrieved first and sent out-of-band */
    bool                iOutOfBandParamSet;

    /** Specifies the desired output format. */
    TAVCEIOutputFormat  iOutputFormat;

    /** Specifies the packet size in bytes which represents the desired number of bytes per NAL.
    If this number is set to 0, the encoder will encode the entire slice group as one NAL. */
    uint32              iPacketSize;

    /** Specifies the rate control algorithm among one of the following constant Q,
    CBR and VBR. .*/
    TAVCEIRateControlType iRateControlType;

    /** Specifies the VBV buffer size which determines the end-to-end delay between the
    encoder and the decoder.  The size is in unit of seconds. For download application,
    the buffer size can be larger than the streaming application. For 2-way application,
    this buffer shall be kept minimal. For a special case, in VBR mode, iBufferDelay will
    be set to -1 to allow buffer underflow. */
    float               iBufferDelay;

    /** Specifies the initial quantization parameter for the first I-frame. If constant Q
    rate control is used, this QP will be used for all the I-frames. This number must be
    set between 1 and 31, otherwise, Initialize() will fail. */
    int                 iIquant[AVC_MAX_LAYER];

    /** Specifies the initial quantization parameter for the first P-frame. If constant Q
    rate control is used, this QP will be used for all the P-frames. This number must be
    set between 1 and 31, otherwise, Initialize() will fail. */
    int                 iPquant[AVC_MAX_LAYER];

    /** Specifies the initial quantization parameter for the first B-frame. If constant Q
    rate control is used, this QP will be used for all the B-frames. This number must be
    set between 1 and 31, otherwise, Initialize() will fail. */
    int                 iBquant[AVC_MAX_LAYER];

    /** Specifies automatic scene detection where I-frame will be used the the first frame
    in a new scene. */
    bool                iSceneDetection;

    /** Specifies the maximum period in seconds between 2 INTRA frames. An INTRA mode is
    forced to a frame once this interval is reached. When there is only one I-frame is present
    at the beginning of the clip, iIFrameInterval should be set to -1. For all I-frames coding
    this number should be set to 0. */
    int32               iIFrameInterval;

    /** According to iIFrameInterval setting, the minimum number of intra MB per frame is
    optimally calculated for error resiliency. However, when iIFrameInterval is set to -1,
    iNumIntraMBRefresh must be specified to guarantee the minimum number of intra
    macroblocks per frame.*/
    uint32              iNumIntraMBRefresh;


    /** Specifies the duration of the clip in millisecond, needed for VBR encode. Set to 0 if unknown.*/
    int32               iClipDuration;

    /** Specify FSI Buffer input */
    uint8*              iFSIBuff;

    /** Specify FSI Buffer Length */
    int                 iFSIBuffLength;

};


/** Structure for input format information */
struct TAVCEIInputFormat
{
    /** Contains the width in pixels of the input frame. */
    int32           iFrameWidth;

    /** Contains the height in pixels of the input frame. */
    int32           iFrameHeight;

    /** Contains the input frame rate in the unit of frame per second. */
    OsclFloat           iFrameRate;

    /** Contains Frame Orientation. Used for RGB input. 1 means Bottom_UP RGB, 0 means Top_Down RGB, -1 for video formats other than RGB*/
    int             iFrameOrientation;

    /** Contains the format of the input video, e.g., YUV 4:2:0, UYVY, RGB24, etc. */
    TAVCEIVideoFormat   iVideoFormat;
};


/** Contains the input data information */
struct TAVCEIInputData
{
    /** Pointer to an input frame buffer in input source format.*/
    uint8*      iSource;

    /** The corresponding time stamp of the input frame. */
    uint32      iTimeStamp;
};

/** Contains the output data information */
struct TAVCEIOutputData
{
    /** Pointer to the encoded bitstream buffer. */
    uint8*          iBitstream;

    /** The size in bytes of iBStream. */
    int32           iBitstreamSize;

    /** The time stamp of the encoded frame according to the bitstream. */
    uint32          iTimeStamp;

    /** Set to true if this is a fragment of a NAL */
    bool            iFragment;

    /** Set to true if this is the last fragment of a NAL*/
    bool            iLastFragment;

    /** Set to true if this is a key frame */
    bool            iKeyFrame;

    /** Set to true if this is the last NAL of a frame */
    bool            iLastNAL;

    /** Pointer to the reconstructed frame buffer in YUV 4:2:0 domain. */
    uint8           *iFrame;
};


/** \brief  This class is the base class for codec specific interface class.
The users must maintain an instance of the codec specific class throughout
the encoding session.
*/
class PVAVCEncoderInterface
{
    public:
        /** \brief Constructor for PVAVCEncoderInterface class. */
        virtual ~PVAVCEncoderInterface() {};

        /** \brief Initialization function to set the input video format and the
        encoding parameters.
        \parm  aVidInFormat contains input related attributes.
        \parm  aEncParam contains encoding parameters setting.
        \return  fail if there is any errors. Otherwise, the function returns success.*/
        virtual  TAVCEI_RETVAL Initialize(TAVCEIInputFormat* aVidInFormat, TAVCEIEncodeParam* aEncParam) = 0;


        /** \brief Get suggested output buffer size to be allocated such that no frames are dropped.
        \return  Size to be allocated. 0 means the encoder is not initialized. */
        virtual  int32 GetMaxOutputBufferSize() = 0;

        /** \brief This function sends in an input video data structure containing a source
        frame and the associated timestamp. It can start processing such as frame analysis, decision to
        drop or encode.
        \parm  aVidIn contains one frame and other information of input.
        \return one of these, SUCCESS, FRAME_DROP, NOT_READY, INPUT_ERROR, FAIL
        */
        virtual  TAVCEI_RETVAL Encode(TAVCEIInputData* aVidIn) = 0;

        /** \brief This function returns an array of parameter sets (either SPS or PPS, as specified by NAL TYPE
        in the first byte.
        \parm paramSet contains buffer for parameters sets.
        \parm size is for size of the input/output.
        \parm nalType is the NAL type according to the standard.
        \return one of these, SUCCESS, INPUT_ERROR, FAIL
        */
        virtual  TAVCEI_RETVAL GetParameterSet(uint8* paramSet, int32* size, int *nalType) = 0;

        /** \brief This function returns a compressed bitstream.
        \parm   aVidOut is the structure to contain the output information.
        \return one of these, SUCCESS, MORE_DATA, NOT_READY, INPUT_ERROR, FAIL */
        virtual  TAVCEI_RETVAL GetOutput(TAVCEIOutputData* aVidOut, int *aRemainingBytes) = 0;

        /** This function is used to flush all the unencoded frames store inside the encoder (if there exist).
        It is used for random re-positioning. Or free all the input. Note that if users want to flush output
        also, it has to retrieve all the output by calling GetOutput.
        \return  SUCCESS or NOT_READY (if the current frame is being used). */
        virtual  TAVCEI_RETVAL FlushInput() = 0;

        /** This function cleanup the AVCEI allocated resources.
        \return  SUCCESS or FAIL. If fail, exception should be thrown. */
        virtual  TAVCEI_RETVAL CleanupEncoder() = 0;

        /**This function dynamically changes the target bit rate of the encoder
        while encoding. aBitRate[n] is the new accumulate target bit rate of layer n.
        \parm aBitRate is an array of the new target bit rates, size of array is the number of layers.
        \return SUCCESS, INPUT_ERROR or FAIL (if values are invalid) */
        virtual  TAVCEI_RETVAL UpdateBitRate(int32* aBitRate) = 0;

        /** This function dynamically changes the target frame rate of the encoder
        while encoding.
        \parm aFrameRate is an array of new accumulate target frame rate
        \return SUCCESS, INPUT_ERROR or FAIL (if values are invalid) */
        virtual  TAVCEI_RETVAL UpdateFrameRate(OsclFloat* aFrameRate) = 0;

        /** This function dynamically changes the IDR frame update interval while
        encoding to a new value.
        \parm aIFrameInterval is a new value of the IDR-frame interval in millisecond.
        \return SUCCESS or FAIL (if the value is invalid). */
        virtual  TAVCEI_RETVAL UpdateIDRFrameInterval(int32 aIDRFrameInterval) = 0;

        /** This function forces an IDR mode to the next frame to be encoded.
        \return  none. */
        virtual  TAVCEI_RETVAL IDRRequest() = 0;

        /** This function returns the input width of a specific layer
        (not necessarily multiple of 16).
        \param aLayer specifies the layer of interest
        \return  width in pixels. */
        virtual  int32 GetEncodeWidth(int32 aLayer) = 0;

        /** This function returns the input height of a specific layer
        (not necessarily multiple of 16).
        \param aLayer specifies the layer of interest
        \return  height in pixels. */
        virtual  int32 GetEncodeHeight(int32 aLayer) = 0;

        /** This function returns the target encoded frame rate of a specific layer.
        \param aLayer specifies the layer of interest
        \return  frame rate in fps. */

        virtual  OsclFloat GetEncodeFrameRate(int32 aLayer) = 0;

};

#endif
