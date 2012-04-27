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

package com.orangelabs.rcs.core.ims.protocol.rtp.stream;



import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtcpPacketReceiver;
import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtcpSession;
import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtpPacket;
import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtpPacketReceiver;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.Format;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * RTP input stream
 *
 * @author jexa7410
 */
public class RtpInputStream implements ProcessorInputStream {
    /**
     * Local port
     */
    private int localPort;

	/**
	 * RTP receiver
	 */
	private RtpPacketReceiver rtpReceiver =  null;

	/**
	 * RTCP receiver
	 */
	private RtcpPacketReceiver rtcpReceiver =  null;

    /**
     * Input buffer
     */
	private Buffer buffer = new Buffer();

    /**
     * Input format
     */
	private Format inputFormat = null;

    /**
     * RTCP Session
     */
    private RtcpSession rtcpSession = null;

	/**
	 * The logger
	 */
	private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     *
     * @param localPort Local port
     * @param inputFormat Input format
     */
    public RtpInputStream(int localPort, Format inputFormat) {
		this.localPort = localPort;
		this.inputFormat = inputFormat;

        rtcpSession = new RtcpSession(false, 16000);
    }

    /**
     * Open the input stream
     *
     * @throws Exception
     */
    public void open() throws Exception {

    	// Create the RTP receiver
        rtpReceiver = new RtpPacketReceiver(localPort, rtcpSession);
    	// Create the RTCP receiver
        rtcpReceiver = new RtcpPacketReceiver(localPort + 1, rtcpSession);
        rtcpReceiver.start();
    }

    /**
     * Close the input stream
     */
    public void close() {
		try {
			// Close the RTP receiver
			if (rtpReceiver != null) {
				rtpReceiver.close();
			}

			// Close the RTCP receiver
			if (rtcpReceiver != null) {
				rtcpReceiver.close();
			}
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't close correctly RTP ressources", e);
			}
		}
	}

    /**
     * Returns the RTP receiver
     *
     * @return RTP receiver
     */
    public RtpPacketReceiver getRtpReceiver() {
    	return rtpReceiver;
    }

    /**
     * Returns the RTCP receiver
     *
     * @return RTCP receiver
     */
    public RtcpPacketReceiver getRtcpReceiver() {
        return rtcpReceiver;
    }

    /**
     * Read from the input stream without blocking
     *
     * @return Buffer
     * @throws Exception
     */
    public Buffer read() throws Exception {
    	// Wait and read a RTP packet
    	RtpPacket rtpPacket = rtpReceiver.readRtpPacket();
    	if (rtpPacket == null) {
    		return null;
    	}

    	// Create a buffer
        buffer.setData(rtpPacket.data);
        buffer.setLength(rtpPacket.payloadlength);
        buffer.setOffset(0);
        buffer.setFormat(inputFormat);
    	buffer.setSequenceNumber(rtpPacket.seqnum);
    	buffer.setFlags(Buffer.FLAG_RTP_MARKER | Buffer.FLAG_RTP_TIME);
    	buffer.setRTPMarker(rtpPacket.marker!=0);
    	buffer.setTimeStamp(rtpPacket.timestamp);

    	// Set inputFormat back to null
    	inputFormat = null;
    	return buffer;
    }
}
