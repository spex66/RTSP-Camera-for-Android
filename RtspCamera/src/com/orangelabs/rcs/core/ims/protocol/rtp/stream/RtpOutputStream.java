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
import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtcpPacketTransmitter;
import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtcpSession;
import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtpPacketReceiver;
import com.orangelabs.rcs.core.ims.protocol.rtp.core.RtpPacketTransmitter;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;
import com.orangelabs.rcs.utils.logger.Logger;

import de.kp.net.rtp.RtpSender;

import java.io.IOException;

/**
 * RTP output stream
 *
 * @author jexa7410
 */
public class RtpOutputStream implements ProcessorOutputStream {
    /**
     * Remote address
     */
    private String remoteAddress;

    /**
     * Remote port
     */
    private int remotePort;

    /**
     * Local port
     */
    private int localRtpPort = -1;

    /**
     * RTP receiver
     */
    private RtpPacketReceiver rtpReceiver = null;

    /**
     * RTCP receiver
     */
    private RtcpPacketReceiver rtcpReceiver = null;

	/**
	 * RTP transmitter
	 */
	private RtpPacketTransmitter rtpTransmitter =  null;

	/**
	 * RTCP transmitter
	 */
	private RtcpPacketTransmitter rtcpTransmitter =  null;

    /**
     * RTCP Session
     */
    private RtcpSession rtcpSession = null;

    /**
     * RTP Input stream
     */
    private RtpInputStream rtpInputStream = null;

    /**
     * The logger
     */
	private final Logger logger = Logger.getLogger(this.getClass().getName());

    public RtpOutputStream() {	
    }

    /**
     * Constructor
     *
     * @param remoteAddress Remote address
     * @param remotePort Remote port
     */
    public RtpOutputStream(String remoteAddress, int remotePort) {
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;

        rtcpSession = new RtcpSession(true, 16000);
    }

    /**
     * Constructor
     *
     * @param remoteAddress Remote address
     * @param remotePort Remote port
     * @param rtpInputStream RTP input stream
     */
    public RtpOutputStream(String remoteAddress, int remotePort, RtpInputStream rtpInputStream) {
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.rtpInputStream = rtpInputStream;

        rtcpSession = new RtcpSession(true, 16000);
    }
    
    /**
     * Constructor
     *
     * @param remoteAddress Remote address
     * @param remotePort Remote port
     */
    public RtpOutputStream(String remoteAddress, int remotePort, int localRtpPort) {
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
        this.localRtpPort = localRtpPort;

        rtcpSession = new RtcpSession(true, 16000);
    }

    /**
     * Open the output stream
     *
     * @throws Exception
     */
    public void open() throws Exception {

    	if (localRtpPort != -1) {
            // Create the RTP receiver
            rtpReceiver = new RtpPacketReceiver(localRtpPort, rtcpSession);
            // Create the RTCP receiver
            rtcpReceiver = new RtcpPacketReceiver(localRtpPort + 1, rtcpSession);
            rtcpReceiver.start();

            // Create the RTP transmitter
            rtpTransmitter = new RtpPacketTransmitter(remoteAddress,
            		remotePort,
            		rtcpSession,
                    rtpReceiver.getConnection());
            
            // Create the RTCP transmitter
            rtcpTransmitter = new RtcpPacketTransmitter(remoteAddress,
            		remotePort + 1,
                    rtcpSession,
                    rtcpReceiver.getConnection());
            rtcpTransmitter.start();
        } else if (rtpInputStream != null) { 
            // Create the RTP transmitter
            rtpTransmitter = new RtpPacketTransmitter(remoteAddress, remotePort, rtcpSession,
                    rtpInputStream.getRtpReceiver().getConnection());
            // Create the RTCP transmitter
            rtcpTransmitter = new RtcpPacketTransmitter(remoteAddress, remotePort + 1, rtcpSession,
                    rtpInputStream.getRtpReceiver().getConnection());
        } else {
            // Create the RTP transmitter
            rtpTransmitter = new RtpPacketTransmitter(remoteAddress, remotePort, rtcpSession);
            // Create the RTCP transmitter
            rtcpTransmitter = new RtcpPacketTransmitter(remoteAddress, remotePort + 1, rtcpSession);
        }
    }

    /**
     * Close the output stream
     */
    public void close() {
		try {
			// Close the RTP transmitter
            if (rtpTransmitter != null)
				rtpTransmitter.close();

            // Close the RTCP transmitter
            if (rtcpTransmitter != null)
                rtcpTransmitter.close();

            // Close the RTP receiver
            if (rtpReceiver != null)
                rtpReceiver.close();

            // Close the RTCP receiver
            if (rtcpReceiver != null)
                rtcpReceiver.close();
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Can't close correctly RTP ressources", e);
			}
		}
	}

    /**
     * Write to the stream without blocking
     *
     * @param buffer Input buffer
     * @throws IOException
     */
    public void write(Buffer buffer) throws IOException {
    	
    	byte[] data = (byte[])buffer.getData();
    	if (data == null) return;
    	
    	RtpSender.getInstance().send(data);
		//rtpTransmitter.sendRtpPacket(buffer);
    
    }
}
