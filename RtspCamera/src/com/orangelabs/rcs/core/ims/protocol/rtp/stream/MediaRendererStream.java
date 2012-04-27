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

import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaOutput;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaSample;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Media renderer stream 
 * 
 * @author jexa7410
 */
public class MediaRendererStream implements ProcessorOutputStream {
	/**
     * Media renderer
     */
	private MediaOutput renderer;
    
    /**
	 * The logger
	 */
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor
	 * 
     * @param renderer Media renderer
	 */
	public MediaRendererStream(MediaOutput renderer) {
		this.renderer = renderer;
	}

	/**
	 * Open the output stream
	 * 
     * @throws Exception
	 */	
    public void open() throws Exception {
    	try {
	    	renderer.open();
			if (logger.isActivated()) {
				logger.debug("Media renderer stream openned");
			}
		} catch(Exception e) {
			if (logger.isActivated()) {
				logger.error("Media renderer stream failed", e);
			}
			throw e; 
		}
    }

    /**
     * Close the output stream
     */
    public void close() {
		renderer.close();
		if (logger.isActivated()) {
			logger.debug("Media renderer stream closed");
		}    	
    }
        
    /**
     * Write to the stream without blocking
     * 
     * @param buffer Input buffer 
     * @throws Exception
     */
    public void write(Buffer buffer) throws Exception {
    	MediaSample sample = new MediaSample((byte[])buffer.getData(), buffer.getTimeStamp());
    	renderer.writeSample(sample);
    }
}
