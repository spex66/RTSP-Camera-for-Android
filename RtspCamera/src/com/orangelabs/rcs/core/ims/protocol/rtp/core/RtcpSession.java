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

import java.util.Date;
import java.util.Random;

/**
 * RTCP Session Information
 *
 * @author hlxn7157
 */
public class RtcpSession {

    /**
     * minimum time between RTCP message (ms)
     */
    private static final int RTCP_MIN_TIME = 5000;

    /**
     * fraction of RTCP sender messages
     */
    private static final double RTCP_SENDER_BW_FRACTION = 0.25;

    /**
     * fraction of RTCP receiver messages
     */
    private static final double RTCP_RCVR_BW_FRACTION = 0.75;

    /**
     * bandwidth
     */
    private double bandwidth;

    /**
     * rtcp bandwidth
     */
    private double rtcp_bandwidth;

    /**
     * minimum time between RTCP message (ms)
     */
    private int rtcp_min_time;

    /**
     * RTCP average packet size
     */
    private int avgrtcpsize;

    /**
     * no of members
     */
    private int members;

    /**
     * no of senders
     */
    private int senders;

    /**
     * initial state
     */
    private Boolean initial;

    /**
     * is sender ?
     */
    private Boolean isSender;

    /**
     *   True if session instantiator requested a close.
     */
    public boolean isByeRequested = false;

    /**
     *   Time this source last sent an RTP Packet
     */
    public double timeOfLastRTPSent = 0;

    /**
    * The last time an RTCP packet was transmitted.
    */
    public double timeOfLastRTCPSent = 0;

    /**
     * The startup time for the application.
     */
    public long appStartupTime;

    /**
     * Ramdomized time interval for next RTCP transmission.
     */
    public double T = 0;

    /**
     * Synchronization Source identifier for this source.
     */
    public int SSRC;

    /**
     * RTP Source
     */
    RtpSource rtpSource;

    /**
     * The current time.
     */
    public double tc = 0;

    /**
     * Total Number of RTP data packets sent out by this source since starting transmission.
     */
    public long packetCount;

    /**
    * Total Number of payload octets (i.e not including header or padding)
    * sent out by this source since starting transmission.
    */
    public long octetCount;

    /**
     * Initialize the Random Number Generator.
     */
    private Random rnd = new Random();

    /**
     * Constructor.
     *
     * @param isSender is sender
     * @param bandwidth bandwidth (can set 16000 (16kops 128kbps))
     */
    public RtcpSession(boolean isSender, double bandwidth) {
        this.isSender = isSender;
        members = 2;
        senders = 1;
        this.bandwidth = bandwidth;
        rtcp_bandwidth = 0.05 * bandwidth;
        rtcp_min_time = RTCP_MIN_TIME;
        avgrtcpsize = 128;
        initial = true;

        // Initialize the Session level variables
        appStartupTime = currentTime();
        timeOfLastRTCPSent = appStartupTime;
        tc = appStartupTime;
        SSRC = rnd.nextInt();
        packetCount = 0;
        octetCount = 0;

        // Init RTP source
        rtpSource = new RtpSource(SSRC);
    }

    /**
     * Setter of members
     *
     * @param members no of members
     */
    public void setMembers(int members) {
        this.members = members;
    }

    /**
     * Setter of senders
     *
     * @param senders no of senders
     */
    public void setSenders(int senders) {
        this.senders = senders;
    }

    /**
     * Get the interval of RTCP message
     *
     * @return interval
     */
    public double getReportInterval() {
        // Interval
        double t;
        // no. of members for computation
        double n;

        // initial half the min delay for quicker notification
        if (initial) {
            initial = false;
            rtcp_min_time /= 2;
        }

        // If there were active senders, give them at least a minimum share of
        // the RTCP bandwidth. Otherwise all participants share the RTCP
        // bandwidth equally.
        n = members;
        if (senders > 0 && senders < members * RTCP_SENDER_BW_FRACTION) {
            if (isSender) {
                rtcp_bandwidth *= RTCP_SENDER_BW_FRACTION;
                n = senders;
            } else {
                rtcp_bandwidth *= RTCP_RCVR_BW_FRACTION;
                n -= senders;
            }
        }

        // get interval
        t = (double)avgrtcpsize * n / bandwidth;
        if (t < rtcp_min_time)
            t = rtcp_min_time;

        // add noise to avoid traffic bursts
        t *= (Math.random() + 0.5);

        T = t;
        return t;
    }

    /**
     * Update the average RTCP packet size
     *
     * @param size
     */
    public void updateavgrtcpsize(int size) {
        avgrtcpsize = (int)(0.0625 * (double)size + 0.9375 * (double)avgrtcpsize);
    }

    /**
     * Returns a self source object.
     *
     * @return My source object.
     */
    public RtpSource getMySource() {
        return rtpSource;
    }

    /**
     * Returns current time from the Date().getTime() function.
     *
     * @return The current time.
     */
    public long currentTime() {
        tc = (new Date()).getTime();
        return (long)tc;
    }
}
