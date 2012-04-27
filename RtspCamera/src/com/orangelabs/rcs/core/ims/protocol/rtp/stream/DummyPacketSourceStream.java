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

import com.orangelabs.rcs.core.ims.protocol.rtp.format.DummyFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.Format;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.SystemTimeBase;
import com.orangelabs.rcs.utils.FifoBuffer;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Dummy packet source stream (used to pass NAT)
 * 
 * @author jexa7410
 */
public class DummyPacketSourceStream extends Thread implements ProcessorInputStream {
	/**
	 * Source period (in seconds)
	 */
	public static int DUMMY_SOURCE_PERIOD = 15;
	
	/**
	 * Input format
	 */
	private DummyFormat format = new DummyFormat();
 
    /**
     * Time base
     */
    private SystemTimeBase systemTimeBase = new SystemTimeBase();

    /**
     * Sequence number
     */
    private long seqNo = 0;

    /**
     * Message buffer
     */
	private FifoBuffer fifo = new FifoBuffer();

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Interruption flag
     */
    private boolean interrupted = false;
    
    /**
	 * Constructor
	 */
	public DummyPacketSourceStream() {
	}
    
    /**
	 * Open the input stream
	 * 
     * @throws Exception
	 */	
    public void open() throws Exception {
    	start();
		if (logger.isActivated()) {
			logger.debug("Dummy source stream openned");
		}
	}    	
	
    /**
     * Close the input stream
     */
    public void close() {
    	interrupted = true;
    	try {
    		fifo.close();
    	} catch(Exception e) {
            // Intentionally blank
    	}
		if (logger.isActivated()) {
			logger.debug("Dummy source stream closed");
		}
    }
    
    /**
     * Format of the data provided by the source stream
     * 
     * @return Format
     */
    public Format getFormat() {
    	return format;
    }
    
    /**
     * Background processing
     */
    public void run() {
    	while(!interrupted) {
	    	try {
	    		// Build a new dummy packet
	    	    Buffer packet = new Buffer();
	    	    packet.setData(new byte[0]);   	
	    	    packet.setLength(0);
	    	    packet.setFormat(format);
	    	    packet.setSequenceNumber(seqNo++);
	    	    packet.setFlags(Buffer.FLAG_SYSTEM_TIME | Buffer.FLAG_LIVE_DATA);
	        	packet.setTimeStamp(systemTimeBase.getTime());    	    	

	        	// Post the packet in the FIFO
	        	fifo.addObject(packet);
	        	
	    		// Make a pause
	    		Thread.sleep(DUMMY_SOURCE_PERIOD * 1000);
	    	} catch(Exception e) {
	    		if (logger.isActivated()) {
	    			logger.error("Dummy packet source has failed", e);
	    		}
	    	}
    	}    
    }

    /**
     * Read from the stream
     * 
     * @return Buffer
     * @throws Exception
     */
    public Buffer read() throws Exception {
    	// Read the FIFO the buffer	        	    	
    	Buffer buffer = (Buffer)fifo.getObject();  
    	return buffer;  
    }
}
