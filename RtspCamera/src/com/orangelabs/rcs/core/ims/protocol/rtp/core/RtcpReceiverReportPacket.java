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

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * RCTP RR packet
 * 
 * @author jexa7410
 */
public class RtcpReceiverReportPacket extends RtcpPacket {
	public int ssrc;
	public RtcpReport[] reports;

	public RtcpReceiverReportPacket(int i, RtcpReport[] rtcpreportblocks) {
		ssrc = i;
		reports = rtcpreportblocks;
		if (rtcpreportblocks.length > 31)
			throw new IllegalArgumentException("Too many reports");
	}

	public RtcpReceiverReportPacket(RtcpPacket rtcppacket) {
		super(rtcppacket);
		type = 201;
	}

	public void assemble(DataOutputStream dataoutputstream) throws IOException {
		dataoutputstream.writeByte(128 + reports.length);
		dataoutputstream.writeByte(201);
		dataoutputstream.writeShort(1 + reports.length * 6);
		dataoutputstream.writeInt(ssrc);
		for (int i = 0; i < reports.length; i++) {
			dataoutputstream.writeInt(reports[i].ssrc);
			dataoutputstream.writeInt((reports[i].packetslost & 0xffffff)
					+ (reports[i].fractionlost << 24));
			dataoutputstream.writeInt((int) reports[i].lastseq);
			dataoutputstream.writeInt(reports[i].jitter);
			dataoutputstream.writeInt((int) reports[i].lsr);
			dataoutputstream.writeInt((int) reports[i].dlsr);
		}
	}

	public int calcLength() {
		return 8 + reports.length * 24;
	}
}
