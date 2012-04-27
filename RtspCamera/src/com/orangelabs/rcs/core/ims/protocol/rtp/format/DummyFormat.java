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

package com.orangelabs.rcs.core.ims.protocol.rtp.format;

/**
 * Dummy format 
 * 
 * @author jexa7410
 */
public class DummyFormat extends Format {
	
	/**
	 * Encoding name
	 */
	public static final String ENCODING = "dummy";
	
	/**
	 * Payload type
	 */
	public static final int PAYLOAD = 12;

	/**
	 * Constructor
	 */
	public DummyFormat() {
		super(ENCODING, PAYLOAD);
	}

	/**
	 * Get the size of a chunk of data from the source
	 * 
	 * @return The minimum size of the buffer needed to read a chunk of data
	 */
    public int getDataChunkSize() {
    	return 0;
    }
}
