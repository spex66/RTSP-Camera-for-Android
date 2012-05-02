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

package de.kp.net.rtp.stream;

import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtcpSession;
import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtpPacket;
import com.orangelabs.rcs.core.ims.protocol.rtp.stream.ProcessorOutputStream;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Packet;
import com.orangelabs.rcs.utils.logger.Logger;

import de.kp.net.rtp.RtpSender;

import java.io.IOException;

/**
 * RTP output stream
 *
 * @author Peter Arwanitis (arwanitis@dr-kruscheundpartner.de)
 * @author Stefan Krusche  (krusche@dr-kruscheundpartner.de)
 * 
 */
public class RtpOutputStream implements ProcessorOutputStream {
 
    /**
     * Sequence number
     */
	private int seqNumber = 0;

    /**
     * RTCP Session
     */
    private RtcpSession rtcpSession = null;

    /**
     * The logger
     */
	private final Logger logger = Logger.getLogger(this.getClass().getName());

    public RtpOutputStream() {	

    	// Used to build SSCR 
    	rtcpSession = new RtcpSession(true, 16000);

    }

    public void open() throws Exception {
    }

    public void close() {
	}

    /**
     * Write to the stream without blocking
     *
     * @param buffer Input buffer
     * @throws IOException
     */
    public void write(Buffer buffer) throws IOException {
		
		// Build a RTP packet
    	RtpPacket packet = buildRtpPacket(buffer);
    	if (packet == null) return;

    	// Assemble RTP packet
    	int size = packet.calcLength();
    	packet.assemble(size);

    	// Send the RTP packet to the remote destination
    	transmit(packet);
    
    }

    /**
     * Build a RTP packet
     *
     * @param buffer Input buffer
     * @return RTP packet
     */
	private RtpPacket buildRtpPacket(Buffer buffer) {

		byte data[] = (byte[])buffer.getData();
		if (data == null) return null;

		Packet packet = new Packet();
		packet.data = data;
		
		packet.offset = 0;
		packet.length = buffer.getLength();

		RtpPacket rtpPacket = new RtpPacket(packet);
		if ((buffer.getFlags() & 0x800) != 0) {
			rtpPacket.marker = 1;
		
		} else {
			rtpPacket.marker = 0;
		
		}

		rtpPacket.payloadType = buffer.getFormat().getPayload();
		rtpPacket.seqnum = seqNumber++;
	
		rtpPacket.timestamp = buffer.getTimeStamp();
        rtpPacket.ssrc      = rtcpSession.SSRC;
		
        rtpPacket.payloadoffset = buffer.getOffset();
		rtpPacket.payloadlength = buffer.getLength();
		
		return rtpPacket;
	
	}

    /**
     * Transmit a RTCP compound packet to the remote destination
     *
     * @param packet RTP packet
     * @throws IOException
     */
	private void transmit(Packet packet) {

		// Prepare data to be sent
		byte[] data = packet.data;
		
		if (packet.offset > 0) {
			System.arraycopy(data, packet.offset, data = new byte[packet.length], 0, packet.length);
		}

		// broadcast data
    	try {
			RtpSender.getInstance().send(data);

    	} catch (IOException e) {

    		e.printStackTrace();
			if (logger.isActivated()) {
				logger.error("Can't broadcast the RTP packet", e);
			}
		
    	}
    	
	}

}
