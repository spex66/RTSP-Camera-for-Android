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

package com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.encoder;


public class NativeH263EncoderParams
{

    public static final int SIMPLE_PROFILE_LEVEL0 = 0;
    public static final int SIMPLE_PROFILE_LEVEL1 = 1;
    public static final int SIMPLE_PROFILE_LEVEL2 = 2;
    public static final int SIMPLE_PROFILE_LEVEL3 = 3;
    public static final int CORE_PROFILE_LEVEL1 = 4;
    public static final int CORE_PROFILE_LEVEL2 = 5;
    public static final int SIMPLE_SCALABLE_PROFILE_LEVEL0 = 6;
    public static final int SIMPLE_SCALABLE_PROFILE_LEVEL1 = 7;
    public static final int SIMPLE_SCALABLE_PROFILE_LEVEL2 = 8;
    public static final int CORE_SCALABLE_PROFILE_LEVEL1 = 10;
    public static final int CORE_SCALABLE_PROFILE_LEVEL2 = 11;
    public static final int CORE_SCALABLE_PROFILE_LEVEL3 = 12;
    public static final int SHORT_HEADER = 0;
    public static final int SHORT_HEADER_WITH_ERR_RES = 1;
    public static final int H263_MODE = 2;
    public static final int H263_MODE_WITH_ERR_RES = 3;
    public static final int DATA_PARTITIONING_MODE = 4;
    public static final int COMBINE_MODE_NO_ERR_RES = 5;
    public static final int COMBINE_MODE_WITH_ERR_RES = 6;
    public static final int CONSTANT_Q = 0;
    public static final int CBR_1 = 1;
    public static final int VBR_1 = 2;
    public static final int CBR_2 = 3;
    public static final int VBR_2 = 4;
    public static final int CBR_LOWDELAY = 5;
    private int encMode;
    private int packetSize;
    private int profile_level;
    private boolean rvlcEnable;
    private int gobHeaderInterval;
    private int numLayers;
    private int timeIncRes;
    private int tickPerSrc;
    private int encHeight;
    private int encWidth;
    private float encFrameRate;
    private int bitRate;
    private int iQuant;
    private int pQuant;
    private int quantType;
    private int rcType;
    private float vbvDelay;
    private boolean noFrameSkipped;
    private int intraPeriod;
    private int numIntraMB;
    private boolean sceneDetect;
    private int searchRange;
    private boolean mv8x8Enable;
    private int intraDCVlcTh;
    private boolean useACPred;

    public NativeH263EncoderParams()
    {
        encMode = 2;
        packetSize = 1024;
        profile_level = 3;
        rvlcEnable = false;
        gobHeaderInterval = 0;
        numLayers = 1;
        timeIncRes = 1000;
        tickPerSrc = 125;
        encHeight = 144;
        encWidth = 176;
        encFrameRate = 8F;
        bitRate = 64000;
        iQuant = 15;
        pQuant = 12;
        quantType = 0;
        rcType = 1;
        vbvDelay = 1.5F;
        noFrameSkipped = false;
        intraPeriod = -1;
        numIntraMB = 0;
        sceneDetect = false;
        searchRange = 4;
        mv8x8Enable = true;
        intraDCVlcTh = 0;
        useACPred = false;
    }

    public int getEncMode()
    {
        return encMode;
    }

    public int getPacketSize()
    {
        return packetSize;
    }

    public int getProfile_level()
    {
        return profile_level;
    }

    public boolean isRvlcEnable()
    {
        return rvlcEnable;
    }

    public int getGobHeaderInterval()
    {
        return gobHeaderInterval;
    }

    public int getNumLayers()
    {
        return numLayers;
    }

    public int getTimeIncRes()
    {
        return timeIncRes;
    }

    public int getTickPerSrc()
    {
        return tickPerSrc;
    }

    public int getEncHeight()
    {
        return encHeight;
    }

    public int getEncWidth()
    {
        return encWidth;
    }

    public float getEncFrameRate()
    {
        return encFrameRate;
    }

    public int getBitRate()
    {
        return bitRate;
    }

    public int getIQuant()
    {
        return iQuant;
    }

    public int getPQuant()
    {
        return pQuant;
    }

    public int getQuantType()
    {
        return quantType;
    }

    public int getRcType()
    {
        return rcType;
    }

    public boolean isNoFrameSkipped()
    {
        return noFrameSkipped;
    }

    public int getIntraPeriod()
    {
        return intraPeriod;
    }

    public int getNumIntraMB()
    {
        return numIntraMB;
    }

    public boolean isSceneDetect()
    {
        return sceneDetect;
    }

    public int getSearchRange()
    {
        return searchRange;
    }

    public boolean isMv8x8Enable()
    {
        return mv8x8Enable;
    }

    public int getIntraDCVlcTh()
    {
        return intraDCVlcTh;
    }

    public boolean isUseACPred()
    {
        return useACPred;
    }

    public void setEncMode(int encMode)
    {
        this.encMode = encMode;
    }

    public void setPacketSize(int packetSize)
    {
        this.packetSize = packetSize;
    }

    public void setProfile_level(int profile_level)
    {
        this.profile_level = profile_level;
    }

    public void setRvlcEnable(boolean rvlcEnable)
    {
        this.rvlcEnable = rvlcEnable;
    }

    public void setGobHeaderInterval(int gobHeaderInterval)
    {
        this.gobHeaderInterval = gobHeaderInterval;
    }

    public void setNumLayers(int numLayers)
    {
        this.numLayers = numLayers;
    }

    public void setTimeIncRes(int timeIncRes)
    {
        this.timeIncRes = timeIncRes;
    }

    public void setTickPerSrc(int tickPerSrc)
    {
        this.tickPerSrc = tickPerSrc;
    }

    public void setEncHeight(int encHeight)
    {
        this.encHeight = encHeight;
    }

    public void setEncWidth(int encWidth)
    {
        this.encWidth = encWidth;
    }

    public void setEncFrameRate(float encFrameRate)
    {
        this.encFrameRate = encFrameRate;
    }

    public void setBitRate(int bitRate)
    {
        this.bitRate = bitRate;
    }

    public void setIQuant(int quant)
    {
        iQuant = quant;
    }

    public void setPQuant(int quant)
    {
        pQuant = quant;
    }

    public void setQuantType(int quantType)
    {
        this.quantType = quantType;
    }

    public void setRcType(int rcType)
    {
        this.rcType = rcType;
    }

    public void setNoFrameSkipped(boolean noFrameSkipped)
    {
        this.noFrameSkipped = noFrameSkipped;
    }

    public void setIntraPeriod(int intraPeriod)
    {
        this.intraPeriod = intraPeriod;
    }

    public void setNumIntraMB(int numIntraMB)
    {
        this.numIntraMB = numIntraMB;
    }

    public void setSceneDetect(boolean sceneDetect)
    {
        this.sceneDetect = sceneDetect;
    }

    public void setSearchRange(int searchRange)
    {
        this.searchRange = searchRange;
    }

    public void setMv8x8Enable(boolean mv8x8Enable)
    {
        this.mv8x8Enable = mv8x8Enable;
    }

    public void setIntraDCVlcTh(int intraDCVlcTh)
    {
        this.intraDCVlcTh = intraDCVlcTh;
    }

    public void setUseACPred(boolean useACPred)
    {
        this.useACPred = useACPred;
    }

    public float getVbvDelay()
    {
        return vbvDelay;
    }

    public void setVbvDelay(float vbvDelay)
    {
        this.vbvDelay = vbvDelay;
    }
}
