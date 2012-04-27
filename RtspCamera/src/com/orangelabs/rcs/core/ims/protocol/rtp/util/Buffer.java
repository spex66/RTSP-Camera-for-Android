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

package com.orangelabs.rcs.core.ims.protocol.rtp.util;

import com.orangelabs.rcs.core.ims.protocol.rtp.format.Format;

/**
 * Buffer
 * 
 * @author jexa7410
 */
public class Buffer {
	/**
	 * Indicates that this buffer marks the end of media for the
	 * data stream
	 */
	public final static int FLAG_EOM = (1 << 0);

	/**
	 * Indicates that the media data should be ignored
	 */
	public final static int FLAG_DISCARD = (1 << 1);

	/**
	 * Indicates that the buffer carries a time stamp that's relative to
	 * the SystemTimeBase. This flag is generally set for data transferred
	 * from hardware capture source that uses the system clock.
	 */
	public final static int FLAG_SYSTEM_TIME = (1 << 7);

	/**
	 * This is a marker bit for RTP
	 */
	public final static int FLAG_RTP_MARKER = (1 << 11);

	/**
	 * Indicates that the buffer carries a time stamp that's in RTP (NTP)
	 * time units
	 */
	public final static int FLAG_RTP_TIME = (1 << 12);

	/**
	 * Indicates that the data is arriving from a live (real-time) source.
	 */
	public final static int FLAG_LIVE_DATA = (1 << 15);

	/**
	 * Default value if the time stamp of the media is not known
	 */
	public final static long TIME_UNKNOWN = -1L;

	/**
	 * Default value if the sequence number is not known
	 */
	public final static long SEQUENCE_UNKNOWN = Long.MAX_VALUE - 1;

	/**
	 * The time stamp of the data in nanoseconds
	 */
	protected long timeStamp = TIME_UNKNOWN;

	/**
	 * The format of the data chunk
	 */
	protected Format format = null;

	/**
	 * States how many samples are valid in the array of data
	 */
	protected int length = 0;

    /** 
     * Starting point (offset) into the array where the valid data begins
     */ 
    protected int offset = 0;

    /**
	 * A flag mask that describes the boolean attributes of the buffer
	 */
	protected int flags = 0;

    /**
     * The duration of the data in the buffer in nanoseconds
     */
    protected long duration = TIME_UNKNOWN;    
    
	/**
	 * Media data chunk
	 */
	protected Object data = null;

	/**
	 * The sequence number
	 */
	protected long sequenceNumber = SEQUENCE_UNKNOWN;

	/**
	 * Get the data format
	 * 
	 * @return Format
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * Set the data format
	 * 
	 * @param format New format
	 */
	public void setFormat(Format format) {
		this.format = format;
	}

	/**
	 * Get the flag mask
	 * 
	 * @return Flag
	 */
	public int getFlags() {
		return flags;
	}

	/**
	 * Set the flag mask
	 * 
	 * @param flags New flags
	 */
	public void setFlags(int flags) {
		this.flags = flags;
	}

	/**
	 * Check if it's the end of the media stream
	 * 
	 * @return Boolean
	 */
	public boolean isEOM() {
		return (flags & FLAG_EOM) != 0;
	}

	/**
	 * Set the EOM flag
	 * 
	 * @param eom EOM status flag
	 */
	public void setEOM(boolean eom) {
		if (eom)
			flags |= FLAG_EOM;
		else
			flags &= ~FLAG_EOM;
	}
	
	/**
	 * Check if the RTP marker is set
	 * 
	 * @return Boolean
	 */
	public boolean isRTPMarkerSet() {
		return (flags & FLAG_RTP_MARKER) != 0;
	}

	/**
	 * Set the RTP marker
	 * 
	 * @param marker RTP marker flag
	 */
	public void setRTPMarker(boolean marker) {
		if (marker)
			flags |= FLAG_RTP_MARKER;
		else
			flags &= ~FLAG_RTP_MARKER;
	}

	/**
	 * Check whether or not this buffer is to be discarded
	 * 
	 * @return Boolean
	 */
	public boolean isDiscard() {
		return (flags & FLAG_DISCARD) != 0;
	}

	/**
	 * Set the discard flag
	 * 
	 * @param discard Discard flag.
	 */
	public void setDiscard(boolean discard) {
		if (discard)
			flags |= FLAG_DISCARD;
		else
			flags &= ~FLAG_DISCARD;
	}

	/**
	 * Get the internal data that holds the media chunk
	 * 
	 * @return Data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Set the internal data that holds the media chunk
	 * 
	 * @param data Data
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * Get the length of the valid data in the buffer
	 * 
	 * @return The length of the valid data
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Set the length of the valid data stored in the buffer
	 * 
	 * @param length The length of the valid data
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * Get the offset into the data array where the valid data begins
	 * 
	 * @return Offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Set the offset
	 * 
	 * @param offset The starting point for the valid data
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * Get the time stamp
	 * 
	 * @return Time stamp in nanoseconds.
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Set the time stamp
	 * 
	 * @param timeStamp Time stamp in nanoseconds
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * Get the duration
	 * 
	 * @return Duration in nanoseconds
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Set the duration
	 * 
	 * @param duration Duration
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * Set the sequence number
	 * 
	 * @param number Sequence number
	 */
	public void setSequenceNumber(long number) {
		sequenceNumber = number;
	}

	/**
	 * Ges the sequence number
	 * 
	 * @return Sequence number
	 */
	public long getSequenceNumber() {
		return sequenceNumber;
	}
}
