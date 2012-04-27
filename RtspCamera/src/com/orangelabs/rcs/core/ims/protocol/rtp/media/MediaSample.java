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

package com.orangelabs.rcs.core.ims.protocol.rtp.media;

/**
 * Media sample
 * 
 * @author jexa7410
 */
public class MediaSample {

	/**
	 * Data
	 */
	private byte[] data;
	
	/**
	 * Time stamp
	 */
	private long time;
	
	/**
	 * Constructor
	 * 
	 * @param data Data
	 * @param time Time stamp
	 */
	public MediaSample(byte[] data, long time) {
		this.data = data;
		this.time = time;
	}

	/**
	 * Returns the data sample
	 * 
	 * @return Byte array
	 */
	public byte[] getData() {
		return data;
	}
	
	/**
	 * Returns the length of the data sample
	 * 
	 * @return Data sample length
	 */
	public int getLength() {
		if (data != null) {
			return data.length;
		} else {
			return 0;
		}
	}

	/**
	 * Returns the time stamp of the sample
	 * 
	 * @return Time in microseconds
	 */
	public long getTimeStamp() {
		return time;
	}	
}
