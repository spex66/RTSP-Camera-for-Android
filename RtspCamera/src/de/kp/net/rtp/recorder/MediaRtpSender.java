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

package de.kp.net.rtp.recorder;

import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.rtp.Processor;
import com.orangelabs.rcs.core.ims.protocol.rtp.RtpException;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.Codec;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.Format;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaInput;
import com.orangelabs.rcs.core.ims.protocol.rtp.stream.MediaCaptureStream;
import com.orangelabs.rcs.utils.logger.Logger;

import de.kp.net.rtp.stream.RtpOutputStream;

/**
 * Media RTP sender
 */
public class MediaRtpSender {
	/**
	 * Format
	 */
	private Format format;

    /**
     * Media processor
     */
    private Processor processor = null;

    /**
     * MediaCaptureStream
     */
    MediaCaptureStream inputStream = null;

    /**
     * RTP output stream
     */
    private RtpOutputStream outputStream = null;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public MediaRtpSender(Format format) {
    	this.format = format;
    }

    public void prepareSession(MediaInput player) throws RtpException {
    	
    	try {
    		// Create the input stream
            inputStream = new MediaCaptureStream(format, player);
    		inputStream.open();
			if (logger.isActivated()) {
				logger.debug("Input stream: " + inputStream.getClass().getName());
			}

            // Create the output stream aka the Renderer
            outputStream = new RtpOutputStream();
            // outputStream.open();
			
            if (logger.isActivated()) {
				logger.debug("Output stream: " + outputStream.getClass().getName());
			}

        	// Create the codec chain
        	Codec[] codecChain = MediaRegistry.generateEncodingCodecChain(format.getCodec());

            // Create the media processor
    		processor = new Processor(inputStream, outputStream, codecChain);

        	if (logger.isActivated()) {
        		logger.debug("Broadcast Session has been prepared with success");
            }
        } catch(Exception e) {

        	if (logger.isActivated()) {
        		logger.error("Can't prepare resources correctly", e);
        	}
        	throw new RtpException("Can't prepare resources");
        }
    }

    /**
     * Start the RTP session
     */
    public void startSession() {
    	if (logger.isActivated()) {
    		logger.debug("Start the session");
    	}

    	// Start the media processor
		if (processor != null) {
			processor.startProcessing();
		}
    }

    /**
     * Stop the RTP session
     */
    public void stopSession() {
    	if (logger.isActivated()) {
    		logger.debug("Stop the session");
    	}

    	// Stop the media processor
		if (processor != null) {
			processor.stopProcessing();
		}

        if (outputStream != null)
            outputStream.close();
    }
}
