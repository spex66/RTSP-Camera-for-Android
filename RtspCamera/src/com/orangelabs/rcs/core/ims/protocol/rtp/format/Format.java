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
 * Abstract format
 * 
 * @author jexa7410
 */
public abstract class Format {
	/**
	 * Unknown payload
	 */
    public static final int UNKNOWN_PAYLOAD = -1;

    /**
     * Codec
     */
    private String codec;

	/**
     * Payload type
     */
    private int payload;

    /**
     * Constructor
     * 
     * @param codec Codec
     * @param payload Payload type
     */
    public Format(String codec, int payload) {
    	this.codec = codec;	
    	this.payload = payload;
    }

    /**
     * Get the codec name
     *
     * @return Name 
     */
    public String getCodec() {
    	return codec;
    }

    /**
     * Get the type of payload
     * 
     * @return Payload type
     */
    public int getPayload() {
    	return payload;
    }
}
