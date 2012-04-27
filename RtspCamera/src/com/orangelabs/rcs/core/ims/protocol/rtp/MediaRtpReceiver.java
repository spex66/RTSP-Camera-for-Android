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

package com.orangelabs.rcs.core.ims.protocol.rtp;



import com.orangelabs.rcs.core.ims.protocol.rtp.codec.Codec;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.Format;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaOutput;
import com.orangelabs.rcs.core.ims.protocol.rtp.stream.MediaRendererStream;
import com.orangelabs.rcs.core.ims.protocol.rtp.stream.RtpInputStream;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Media RTP receiver
 */
public class MediaRtpReceiver {
    /**
     * Media processor
     */
    private Processor processor = null;

	/**
	 * Local port number (RTP listening port)
	 */
	private int localPort;

    /**
     * RTP Input Stream
     */
    private RtpInputStream inputStream = null;

	/**
	 * The logger
	 */
	private Logger logger =	Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     *
     * @param localPort Local port number
     */
	public MediaRtpReceiver(int localPort) {
		this.localPort = localPort;
	}

    /**
     * Prepare the RTP session
     *
     * @param renderer Media renderer
     * @param format Media format
     * @throws RtpException
     */
    public void prepareSession(MediaOutput renderer, Format format)
            throws RtpException {
    	try {
			// Create the input stream
            inputStream = new RtpInputStream(localPort, format);
    		inputStream.open();
			if (logger.isActivated()) {
				logger.debug("Input stream: " + inputStream.getClass().getName());
			}

            // Create the output stream
        	MediaRendererStream outputStream = new MediaRendererStream(renderer);
    		outputStream.open();
			if (logger.isActivated()) {
				logger.debug("Output stream: " + outputStream.getClass().getName());
			}

        	// Create the codec chain
        	Codec[] codecChain = MediaRegistry.generateDecodingCodecChain(format.getCodec());

            // Create the media processor
    		processor = new Processor(inputStream, outputStream, codecChain);

        	if (logger.isActivated()) {
        		logger.debug("Session has been prepared with success");
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
			logger.info("Start the session");
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
			logger.info("Stop the session");
		}

		// Stop the media processor
		if (processor != null) {
			processor.stopProcessing();
		}
	}

    /**
     * Returns the RTP input stream
     *
     * @return RTP input stream
     */
    public RtpInputStream getInputStream() {
        return inputStream;
    }
}
