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
import com.orangelabs.rcs.core.ims.protocol.rtp.stream.ProcessorInputStream;
import com.orangelabs.rcs.core.ims.protocol.rtp.stream.ProcessorOutputStream;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Media processor. A processor receives an input stream, use a codec chain
 * to filter the data before to send it to the output stream.
 *
 * @author jexa7410
 */
public class Processor extends Thread {
	/**
	 * Processor input stream
	 */
	private ProcessorInputStream inputStream;

	/**
	 * Processor output stream
	 */
	private ProcessorOutputStream outputStream;

	/**
	 * Codec chain
	 */
	private CodecChain codecChain;

	/**
	 * Processor status flag
	 */
	private boolean interrupted = false;

    /**
     * bigger Sequence Number
     */
    private long bigSeqNum = 0;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     *
     * @param inputStream Input stream
     * @param outputStream Output stream
     * @param codecs List of codecs
     */
	public Processor(ProcessorInputStream inputStream, ProcessorOutputStream outputStream, Codec[] codecs) {
        super();

		this.inputStream = inputStream;
        this.outputStream = outputStream;

		// Create the codec chain
		codecChain = new CodecChain(codecs, outputStream);

    	if (logger.isActivated()) {
    		logger.debug("Media processor created");
        }
	}

	/**
	 * Start processing
	 */
	public void startProcessing() {
		if (logger.isActivated()) {
			logger.debug("Start media processor");
		}
		interrupted = false;
        bigSeqNum = 0;
        start();
	}

	/**
	 * Stop processing
	 */
	public void stopProcessing() {
		if (logger.isActivated()) {
			logger.debug("Stop media processor");
		}
		interrupted = true;

		// Close streams
		outputStream.close();
		inputStream.close();
	}

	/**
	 * Background processing
	 */
	public void run() {
		try {
			if (logger.isActivated()) {
				logger.debug("Processor processing is started");
			}

			// Start processing
			while (!interrupted) {
				// Read data from the input stream
				Buffer inBuffer = inputStream.read();
				if (inBuffer == null) {
					interrupted = true;
					if (logger.isActivated()) {
						logger.debug("Processing terminated: null data received");
					}
					break;
				}

                // Drop the old packet
                long seqNum = inBuffer.getSequenceNumber();
                                
                if (seqNum + 3 > bigSeqNum) {
                    if (seqNum > bigSeqNum) {
                        bigSeqNum = seqNum;
                    }

                    // Codec chain processing
                    int result = codecChain.process(inBuffer);
                    if ((result != Codec.BUFFER_PROCESSED_OK)
                            && (result != Codec.OUTPUT_BUFFER_NOT_FILLED)) {
                        interrupted = true;
                        if (logger.isActivated()) {
                            logger.error("Codec chain processing error: " + result);
                        }
                        break;
                    }
                }
			}
		} catch (Exception e) {
			if (!interrupted) {
				if (logger.isActivated()) {
					logger.error("Processor error", e);
				}
			} else {
				if (logger.isActivated()) {
					logger.debug("Processor processing has been terminated");
				}
			}
		}
	}

    /**
     * Returns the input stream
     *
     * @return Stream
     */
	public ProcessorInputStream getInputStream() {
		return inputStream;
	}

    /**
     * Returns the output stream
     *
     * @return Stream
     */
	public ProcessorOutputStream getOutputStream() {
		return outputStream;
	}
}
