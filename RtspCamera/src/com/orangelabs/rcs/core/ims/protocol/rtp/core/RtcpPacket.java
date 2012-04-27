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

package com.orangelabs.rcs.core.ims.protocol.rtp.core;

import com.orangelabs.rcs.core.ims.protocol.rtp.util.Packet;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Abstract RCTP packet
 *
 * @author jexa7410
 */
public abstract class RtcpPacket extends Packet {
    /**
     *   Version =2
     */
    public static final byte VERSION = 2;

    /**
    *   Padding =0
    */
    public static final byte PADDING = 0;

    /**
     * RTCP SR
     */
    public static final int RTCP_SR = 200;

    /**
     * RTCP RR
     */
    public static final int RTCP_RR = 201;

    /**
     * RTCP SDES
     */
    public static final int RTCP_SDES = 202;

    /**
     * RTCP BYE
     */
    public static final int RTCP_BYE = 203;

    /**
     * RTCP APP
     */
    public static final int RTCP_APP = 204;

    /**
     * RTCP APP
     */
    public static final int RTCP_COMPOUND = -1;

	public Packet base;

	public int type;

	public RtcpPacket() {
	}

	public RtcpPacket(RtcpPacket rtcppacket) {
		super((Packet)rtcppacket);

		base = rtcppacket.base;
	}

	public RtcpPacket(Packet packet) {
		super(packet);

		base = packet;
	}

	public abstract void assemble(DataOutputStream dataoutputstream) throws IOException;

	public abstract int calcLength();
}
