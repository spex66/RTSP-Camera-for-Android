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

import com.orangelabs.rcs.platform.network.DatagramConnection;
import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.utils.logger.Logger;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;

/**
 * RTCP packet transmitter
 *
 * @author jexa7410
 */
public class RtcpPacketTransmitter extends Thread {
    /**
	 * Remote address
	 */
	private String remoteAddress;

    /**
	 * Remote port
	 */
	private int remotePort;

	/**
	 * Statistics
	 */
	private RtcpStatisticsTransmitter stats = new RtcpStatisticsTransmitter();

	/**
	 * Datagram connection
	 */
	public DatagramConnection datagramConnection = null;

    /**
     * RTCP Session
     */
    private RtcpSession rtcpSession = null;

    /**
     * Flag used to determine when to terminate after sending a BYE
     */
    private boolean waitingForByeBackoff = false;

    /**
     * Flag used to properly close
     */
    private boolean closed = false;

    /**
     * Random value
     */
    private Random rand = new Random();

    /**
     * The logger
     */
	private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     *
     * @param address Remote address
     * @param port Remote port
     * @param rtcpSession the RTCP session
     * @throws IOException
     */
    public RtcpPacketTransmitter(String address, int port, RtcpSession rtcpSession)
            throws IOException {
        super();

        this.remoteAddress = address;
        this.remotePort = port;
        this.rtcpSession = rtcpSession;

        // Open the connection
        datagramConnection = NetworkFactory.getFactory().createDatagramConnection();
        datagramConnection.open();

        if (logger.isActivated()) {
            logger.debug("RTCP transmitter connected to " + remoteAddress + ":" + remotePort);
        }
    }

    /**
     * Constructor - used for SYMETRIC_RTP
     *
     * @param address Remote address
     * @param port Remote port
     * @param rtcpSession the RTCP session
     * @param DatagramConnection datagram connection of the RtpPacketReceiver
     * @throws IOException
     */
    public RtcpPacketTransmitter(String address, int port, RtcpSession rtcpSession,
            DatagramConnection connection) throws IOException {
        super();

        this.remoteAddress = address;
        this.remotePort = port;
        this.rtcpSession = rtcpSession;

        // Open the connection
        if (connection != null) {
            this.datagramConnection = connection;
        } else {
            this.datagramConnection = NetworkFactory.getFactory().createDatagramConnection();
            this.datagramConnection.open();
        }

        if (logger.isActivated()) {
            logger.debug("RTCP transmitter connected to " + remoteAddress + ":" + remotePort);
        }
    }

    /**
     * Close the transmitter
     *
     * @throws IOException
     */
	public void close() throws IOException {
        rtcpSession.isByeRequested = true;
        closed = true;
        // Close the datagram connection
		if (datagramConnection != null) {
			datagramConnection.close();
		}
		if (logger.isActivated()) {
            logger.debug("RTCP transmitter closed");
		}
	}

	/**
	 * Background processing
	 */
	public void run() {
		try {
            // Send a SDES packet
            // sendSdesPacket();

            boolean terminate = false;
            while (!terminate) {
                try {
                    // Wait the RTCP report interval.
                    Thread.sleep((long)rtcpSession.getReportInterval());

                    // Right time to send a RTCP packet or reschedule ?
                    if ((rtcpSession.timeOfLastRTCPSent + rtcpSession.T) <= rtcpSession
                            .currentTime()) {
                        // We know that it is time to send a RTCP packet, is it
                        // a BYE packet
                        if ((rtcpSession.isByeRequested && waitingForByeBackoff)) {
                            // If it is bye then did we ever sent anything
                            if (rtcpSession.timeOfLastRTCPSent > 0
                                    && rtcpSession.timeOfLastRTPSent > 0) {
                                rtcpSession.getMySource().activeSender = false;
                                rtcpSession.timeOfLastRTCPSent = rtcpSession.currentTime();
                            } else {
                                // We never sent anything and we have to quit :(
                                // do not send BYE
                                terminate = true;
                            }
                        } else {
                            if (!closed) {
                                transmit(assembleRtcpPacket());
                                if (rtcpSession.isByeRequested && !waitingForByeBackoff) {
                                    // We have sent a BYE packet, so terminate
                                    terminate = true;
                                } else {
                                    rtcpSession.timeOfLastRTCPSent = rtcpSession.currentTime();
                                }
                            } else {
                                terminate = true;
                            }

                        }
                    }
                    waitingForByeBackoff = false;

                } catch (InterruptedException e) {
                    waitingForByeBackoff = true;
                    rtcpSession.isByeRequested = true;
                }
            }
		} catch (Exception e) {
			if (logger.isActivated()) {
                logger.error("Can't send the RTCP packet", e);
			}
		}
	}

    /**
     * assemble RTCP packet
     */
    private byte[] assembleRtcpPacket() {
        byte data[] = new byte[0];

        // Sender or receiver packet
        RtpSource s = rtcpSession.getMySource();
        if ((s.activeSender) && (rtcpSession.timeOfLastRTCPSent < rtcpSession.timeOfLastRTPSent)) {
            data = RtcpPacketUtils.append(data, assembleSenderReportPacket());
        } else {
            data = RtcpPacketUtils.append(data, assembleReceiverReportPacket());
        }

        // SDES packets
        Vector<RtcpSdesPacket> repvec = makereports();
        for (int i = 0; i < repvec.size(); i++) {
            if (repvec.elementAt(i).data != null)
                data = RtcpPacketUtils.append(data, repvec.elementAt(i).data);
        }

        // BYE packet
        RtcpByePacket byepacket = null;
        if (rtcpSession.isByeRequested) {
            int ssrc[] = {rtcpSession.SSRC};
            byepacket = new RtcpByePacket(ssrc, null);
            data = RtcpPacketUtils.append(data, byepacket.data);
        }

        return data;
    }

    /**
     * assemble RTCP SR packet
     * @return packet data
     */
    private byte[] assembleSenderReportPacket() {
        final int FIXED_HEADER_SIZE = 4;
        byte V_P_RC = (byte)((RtcpPacket.VERSION << 6) | (RtcpPacket.PADDING << 5) | (0x00));
        byte ss[] = RtcpPacketUtils.longToBytes(rtcpSession.SSRC, 4);
        byte PT[] = RtcpPacketUtils.longToBytes((long)RtcpPacket.RTCP_SR, 1);
        byte NTP_TimeStamp[] = RtcpPacketUtils.longToBytes(rtcpSession.currentTime(), 8);
        short randomOffset = (short)Math.abs(rand.nextInt() & 0x000000FF);
        byte RTP_TimeStamp[] = RtcpPacketUtils.longToBytes((long)rtcpSession.tc
                + randomOffset, 4);
        byte SenderPacketCount[] = RtcpPacketUtils.longToBytes(rtcpSession.packetCount, 4);
        byte SenderOctetCount[] = RtcpPacketUtils.longToBytes(rtcpSession.octetCount, 4);

        // report block
        byte receptionReportBlocks[] = new byte[0];
        receptionReportBlocks = RtcpPacketUtils.append(receptionReportBlocks,
                assembleRTCPReceptionReport());
        byte receptionReports = (byte)(receptionReportBlocks.length / 24);
        V_P_RC = (byte)(V_P_RC | (byte)(receptionReports & 0x1F));

        // Length is 32 bit words contained in the packet -1
        byte length[] = RtcpPacketUtils.longToBytes((FIXED_HEADER_SIZE + ss.length
                + NTP_TimeStamp.length + RTP_TimeStamp.length + SenderPacketCount.length
                + SenderOctetCount.length + receptionReportBlocks.length) / 4 - 1, 2);

        // Build RTCP SR Packet
        byte rtcpSRPacket[] = new byte[1];
        rtcpSRPacket[0] = V_P_RC;
        rtcpSRPacket = RtcpPacketUtils.append(rtcpSRPacket, PT);
        rtcpSRPacket = RtcpPacketUtils.append(rtcpSRPacket, length);
        rtcpSRPacket = RtcpPacketUtils.append(rtcpSRPacket, ss);
        rtcpSRPacket = RtcpPacketUtils.append(rtcpSRPacket, NTP_TimeStamp);
        rtcpSRPacket = RtcpPacketUtils.append(rtcpSRPacket, RTP_TimeStamp);
        rtcpSRPacket = RtcpPacketUtils.append(rtcpSRPacket, SenderPacketCount);
        rtcpSRPacket = RtcpPacketUtils.append(rtcpSRPacket, SenderOctetCount);
        rtcpSRPacket = RtcpPacketUtils.append(rtcpSRPacket, receptionReportBlocks);

        return rtcpSRPacket;
    }

    /**
     * assemble RTCP RR packet
     * @return packet data
     */
    private byte[] assembleReceiverReportPacket() {
        final int FIXED_HEADER_SIZE = 4;
        byte V_P_RC = (byte)((RtcpPacket.VERSION << 6) | (RtcpPacket.PADDING << 5) | (0x00));
        byte ss[] = RtcpPacketUtils.longToBytes(rtcpSession.SSRC, 4);
        byte PT[] = RtcpPacketUtils.longToBytes((long)RtcpPacket.RTCP_RR, 1);

        // report block
        byte receptionReportBlocks[] = new byte[0];
        receptionReportBlocks = RtcpPacketUtils.append(receptionReportBlocks,
                assembleRTCPReceptionReport());
        byte receptionReports = (byte)(receptionReportBlocks.length / 24);
        V_P_RC = (byte)(V_P_RC | (byte)(receptionReports & 0x1F));

        byte length[] = RtcpPacketUtils.longToBytes(
                (FIXED_HEADER_SIZE + ss.length + receptionReportBlocks.length) / 4 - 1, 2);

        // Build RTCP RR Packet
        byte RRPacket[] = new byte[1];
        RRPacket[0] = V_P_RC;
        RRPacket = RtcpPacketUtils.append(RRPacket, PT);
        RRPacket = RtcpPacketUtils.append(RRPacket, length);
        RRPacket = RtcpPacketUtils.append(RRPacket, ss);
        RRPacket = RtcpPacketUtils.append(RRPacket, receptionReportBlocks);
        return RRPacket;
    }

    /**
     * assemble RTCP Reception report block
     * @return report data
     */
    private byte[] assembleRTCPReceptionReport() {
        byte reportBlock[] = new byte[0];
        RtpSource source = rtcpSession.getMySource();

        source.updateStatistics();
        byte SSRC[] = RtcpPacketUtils.longToBytes((long)source.SSRC, 4);
        byte fraction_lost[] = RtcpPacketUtils.longToBytes((long)source.fraction, 1);
        byte pkts_lost[] = RtcpPacketUtils.longToBytes((long)source.lost, 3);
        byte last_seq[] = RtcpPacketUtils.longToBytes((long)source.last_seq, 4);
        byte jitter[] = RtcpPacketUtils.longToBytes((long)source.jitter, 4);
        byte lst[] = RtcpPacketUtils.longToBytes((long)source.lst, 4);
        byte dlsr[] = RtcpPacketUtils.longToBytes((long)source.dlsr, 4);

        reportBlock = RtcpPacketUtils.append(reportBlock, SSRC);
        reportBlock = RtcpPacketUtils.append(reportBlock, fraction_lost);
        reportBlock = RtcpPacketUtils.append(reportBlock, pkts_lost);
        reportBlock = RtcpPacketUtils.append(reportBlock, last_seq);
        reportBlock = RtcpPacketUtils.append(reportBlock, jitter);
        reportBlock = RtcpPacketUtils.append(reportBlock, lst);
        reportBlock = RtcpPacketUtils.append(reportBlock, dlsr);

        return reportBlock;
    }

	/**
	 * Send a BYE packet
	 */
	public void sendByePacket() {
		// Create a report
	    Vector<RtcpSdesPacket> repvec = makereports();
	    RtcpPacket[] packets = new RtcpPacket[repvec.size() + 1];
	    repvec.copyInto(packets);

	    // Create a RTCP bye packet
	    int ssrc[] = {rtcpSession.SSRC};
	    RtcpByePacket rtcpbyepacket = new RtcpByePacket(ssrc, null);
	    packets[packets.length - 1] = rtcpbyepacket;

		// Create a RTCP compound packet
	    RtcpCompoundPacket cp = new RtcpCompoundPacket(packets);

        rtcpSession.getMySource().activeSender = false;

	    // Send the RTCP packet
		transmit(cp);
	}

	/**
     * Generate a RTCP report
     *
     * @return Vector
     */
	public Vector<RtcpSdesPacket> makereports() {
		Vector<RtcpSdesPacket> packets = new Vector<RtcpSdesPacket>();

		RtcpSdesPacket rtcpsdespacket = new RtcpSdesPacket(new RtcpSdesBlock[1]);
		rtcpsdespacket.sdes[0] = new RtcpSdesBlock();
		rtcpsdespacket.sdes[0].ssrc = rtcpSession.SSRC;

		Vector<RtcpSdesItem> vector = new Vector<RtcpSdesItem>();
		vector.addElement(new RtcpSdesItem(1, RtpSource.CNAME));
		rtcpsdespacket.sdes[0].items = new RtcpSdesItem[vector.size()];
		vector.copyInto(rtcpsdespacket.sdes[0].items);

		packets.addElement(rtcpsdespacket);
		return packets;
	}

    /**
     * Transmit a RTCP compound packet to the remote destination
     *
     * @param packet Compound packet to be sent
     */
	private void transmit(RtcpCompoundPacket packet) {
		// Prepare data to be sent
		byte[] data = packet.data;
		if (packet.offset > 0) {
			System.arraycopy(data, packet.offset,
					data = new byte[packet.length], 0, packet.length);
		}

		// Update statistics
		stats.numBytes += packet.length;
        stats.numPackets++;
        rtcpSession.updateavgrtcpsize(packet.length);
        rtcpSession.timeOfLastRTCPSent = rtcpSession.currentTime();
		// Send data over UDP
		try {
			datagramConnection.send(remoteAddress, remotePort, data);
		} catch(IOException e) {
			if (logger.isActivated()) {
				logger.error("Can't send the RTCP packet", e);
			}
		}
    }

    /**
     * Transmit a RTCP compound packet to the remote destination
     *
     * @param packet Compound packet to be sent
     */
    private void transmit(byte packet[]) {
        // Update statistics
        stats.numBytes += packet.length;
        stats.numPackets++;
        rtcpSession.updateavgrtcpsize(packet.length);
        rtcpSession.timeOfLastRTCPSent = rtcpSession.currentTime();
        // Send data over UDP
        try {
            datagramConnection.send(remoteAddress, remotePort, packet);
        } catch (IOException e) {
            if (logger.isActivated()) {
                logger.error("Can't send the RTCP packet", e);
            }
        }
    }

    /**
     * Returns the statistics of RTCP transmission
     *
     * @return Statistics
     */
	public RtcpStatisticsTransmitter getStatistics() {
		return stats;
	}
}
