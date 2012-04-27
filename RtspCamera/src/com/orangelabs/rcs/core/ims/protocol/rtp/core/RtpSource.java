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

/**
 * RTP source
 *
 * @author jexa7410
 */
public class RtpSource {
	/**
	 * CNAME value
	 */
    public static String CNAME = "anonymous@127.0.0.1";

	/**
	 * SSRC
	 */
    public int SSRC;

    /**
     * Fraction of RTP data packets from source SSRC lost since the previous
     * SR or RR packet was sent, expressed as a fixed point number with the
     * binary point at the left edge of the field.  To get the actual fraction
     * multiply by 256 and take the integral part
     */
    public double fraction;

    /**
     * Cumulative number of packets lost (signed 24bits).
     */
    public long lost;

    /**
     * Extended highest sequence number received.
     */
    public long last_seq;

    /**
     * Interarrival jitter.
     */
    public long jitter;

    /**
     * Last SR Packet from this source.
     */
    public long lst;

    /**
     * Delay since last SR packet.
     */
    public double dlsr;

    /**
     * Is this source and ActiveSender.
     */
    public boolean activeSender;

    /**
     * Time the last RTCP Packet was received from this source.
     */
    public double timeOfLastRTCPArrival;

    /**
     * Time the last RTP Packet was received from this source.
     */
    public double timeOfLastRTPArrival;

    /**
     * Time the last Sender Report RTCP Packet was received from this source.
     */
    public double timeofLastSRRcvd;

    /**
     * Total Number of RTP Packets Received from this source
     */
    public int noOfRTPPacketsRcvd;

    /**
     * Sequence Number of the first RTP packet received from this source
     */
    public long base_seq;

    /**
     * Number of RTP Packets Expected from this source
     */
    public long expected;

    /**
     * No of  RTP Packets expected last time a Reception Report was sent
     */
    public long expected_prior;

    /**
     * No of  RTP Packets received last time a Reception Report was sent
     */
    public long received_prior;

    /**
     * Highest Sequence number received from this source
     */
    public long max_seq;

    /**
     * Keep track of the wrapping around of RTP sequence numbers, since RTP Seq No. are
     * only 16 bits
     */
    public long cycles;

    /**
     * Since Packets lost is a 24 bit number, it should be clamped at WRAPMAX = 0xFFFFFFFF
     */
    public long WRAPMAX = 0xFFFFFFFF;

    /**
     * Constructor requires an SSRC for it to be a valid source. The constructor initializes
     * all the source class members to a default value
     *
     * @param   sourceSSRC SSRC of the new source
     */
    RtpSource(int sourceSSRC) {
        long time = currentTime();
        SSRC = sourceSSRC;
        fraction = 0;
        lost = 0;
        last_seq = 0;
        jitter = 0;
        lst = 0;
        dlsr = 0;
        activeSender = false;
        timeOfLastRTCPArrival = time;
        timeOfLastRTPArrival = time;
        timeofLastSRRcvd = time;
        noOfRTPPacketsRcvd = 0;
        base_seq = 0;
        expected_prior = 0;
        received_prior = 0;
    }

    /**
     * Returns the extended maximum sequence for a source
     * considering that sequences cycle.
     *
     * @return  Sequence Number
     */

    public long getExtendedMax() {
        return (cycles + max_seq);
    }

    /**
     * This safe sequence update function will try to
     * determine if seq has wrapped over resulting in a
     * new cycle.  It sets the cycle -- source level
     * variable which keeps track of wraparounds.
     *
     * @param seq  Sequence Number
     */
    public void updateSeq(long seq) {
        // If the diferrence between max_seq and seq
        // is more than 1000, then we can assume that
        // cycle has wrapped around.
        if (max_seq == 0)
            max_seq = seq;
        else {
            if (max_seq - seq > 0.5 * WRAPMAX)
                cycles += WRAPMAX;

            max_seq = seq;
        }

    }

    /**
     * Updates the various statistics for this source e.g. Packets Lost, Fraction lost
     * Delay since last SR etc, according to the data gathered since a last SR or RR was sent out.
     * This method is called prior to sending a Sender Report(SR)or a Receiver Report(RR)
     * which will include a Reception Report block about this source.
     */
    public int updateStatistics() {
        // Set all the relevant parameters

        // Calculate the highest sequence number received in an RTP Data Packet
        // from this source
        last_seq = getExtendedMax();

        // Number of Packets lost = Number of Packets expected - Number of
        // Packets actually rcvd
        expected = getExtendedMax() - base_seq + 1;
        lost = expected - noOfRTPPacketsRcvd;

        // Clamping at 0xffffff
        if (lost > 0xffffff)
            lost = 0xffffff;

        // Calculate the fraction lost
        long expected_interval = expected - expected_prior;
        expected_prior = expected;

        long received_interval = noOfRTPPacketsRcvd - received_prior;
        received_prior = noOfRTPPacketsRcvd;

        long lost_interval = expected_interval - received_interval;

        if (expected_interval == 0 || lost_interval <= 0)
            fraction = 0;
        else
            fraction = (lost_interval << 8) / (double) expected_interval;

        // dlsr - express it in units of 1/65336 seconds
        dlsr = (timeofLastSRRcvd - currentTime()) / 65536;

        return 0;
    }

    /**
     * Returns current time from the Date().getTime() function.
     *
     * @return The current time.
     */
    private static long currentTime() {
        return (long)((new Date()).getTime());
    }
}
