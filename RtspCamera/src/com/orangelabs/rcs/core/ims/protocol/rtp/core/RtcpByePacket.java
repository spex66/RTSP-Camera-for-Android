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
 * RTCP BYE packet
 * 
 * @author jexa7410
 */
public class RtcpByePacket extends RtcpPacket {

	public int ssrc[];
	public byte reason[];

	public RtcpByePacket(RtcpPacket parent) {
		super(parent);
		super.type = 203;
	}

	public RtcpByePacket(int ssrc[], byte reason[]) {
		this.ssrc = ssrc;
		if (reason != null) {
			this.reason = reason;
		} else {
			this.reason = new byte[0];
		}
		if (ssrc.length > 31) {
			throw new IllegalArgumentException("Too many SSRCs");
		} else {
			return;
		}
	}

	public int calcLength() {
		return 4 + (ssrc.length << 2)
				+ (reason.length <= 0 ? 0 : reason.length + 4 & -4);
	}

	public void assemble(DataOutputStream out) throws IOException {
		out.writeByte(128 + ssrc.length);
		out.writeByte(203);
		out.writeShort(ssrc.length
				+ (reason.length <= 0 ? 0 : reason.length + 4 >> 2));
		for (int i = 0; i < ssrc.length; i++) {
			out.writeInt(ssrc[i]);
		}

		if (reason.length > 0) {
			out.writeByte(reason.length);
			out.write(reason);
			for (int i = (reason.length + 4 & -4) - reason.length - 1; i > 0; i--) {
				out.writeByte(0);
			}
		}
	}
}
