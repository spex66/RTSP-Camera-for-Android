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
 * RCTP SDES packet
 * 
 * @author jexa7410
 */
public class RtcpSdesPacket extends RtcpPacket {

	public RtcpSdesBlock sdes[];

	public RtcpSdesPacket(RtcpPacket parent) {
		super(parent);
		super.type = 202;
	}

	public RtcpSdesPacket(RtcpSdesBlock sdes[]) {
		this.sdes = sdes;
		if (sdes.length > 31) {
			throw new IllegalArgumentException("Too many SDESs");
		} else {
			return;
		}
	}

	public int calcLength() {
		int len = 4;
		for (int i = 0; i < sdes.length; i++) {
			int sublen = 5;
			for (int j = 0; j < sdes[i].items.length; j++) {
				sublen += 2 + sdes[i].items[j].data.length;
			}

			sublen = sublen + 3 & -4;
			len += sublen;
		}

		return len;
	}

	public void assemble(DataOutputStream out) throws IOException {
		out.writeByte(128 + sdes.length);
		out.writeByte(202);
		out.writeShort(calcLength() - 4 >> 2);
		for (int i = 0; i < sdes.length; i++) {
			out.writeInt(sdes[i].ssrc);
			int sublen = 0;
			for (int j = 0; j < sdes[i].items.length; j++) {
				out.writeByte(sdes[i].items[j].type);
				out.writeByte(sdes[i].items[j].data.length);
				out.write(sdes[i].items[j].data);
				sublen += 2 + sdes[i].items[j].data.length;
			}

			for (int j = (sublen + 4 & -4) - sublen; j > 0; j--) {
				out.writeByte(0);
			}

		}
	}
}
