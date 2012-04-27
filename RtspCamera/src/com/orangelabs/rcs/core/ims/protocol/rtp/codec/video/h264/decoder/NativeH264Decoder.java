/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.decoder;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.decoder.VideoSample;

public class NativeH264Decoder
{

    public NativeH264Decoder()
    {
    }

    public static native int InitDecoder();

    public static native int DeinitDecoder();

    public static synchronized native int DecodeAndConvert(byte abyte0[], int ai[]);

    public static native int InitParser(String s);

    public static native int DeinitParser();

    public static native int getVideoLength();

    public static native int getVideoWidth();

    public static native int getVideoHeight();

    public static native String getVideoCoding();

    public static native VideoSample getVideoSample(int ai[]);

    static 
    {
        String libname = "H264Decoder";
        try
        {
            System.loadLibrary(libname);
        }
        catch(Exception exception) { }
    }
}
