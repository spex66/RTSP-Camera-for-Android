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

package com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264;

/**
 * Default H264 Settings
 *
 * @author hlxn7157
 */
public class H264Config {
    /**
     * H264 Codec Name
     */
    public final static String CODEC_NAME = "h264";

    /**
     * Default clock rate
     */
    public final static int CLOCK_RATE = 90000;

    /**
     * Default codec params
     */
    public final static String CODEC_PARAMS = "profile-level-id=42900B";

    /**
     * Default video width
     */
    public final static int VIDEO_WIDTH = 176;

    /**
     * Default video height
     */
    public final static int VIDEO_HEIGHT = 144;

    /**
     * Default video frame rate
     */
    public final static int FRAME_RATE = 15;

    /**
     * Default video bit rate
     */
    public final static int BIT_RATE = 64000;
}
