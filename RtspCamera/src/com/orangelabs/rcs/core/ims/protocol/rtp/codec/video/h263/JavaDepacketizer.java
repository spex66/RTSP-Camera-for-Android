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

package com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263;

import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.VideoCodec;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.Format;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;

/**
 * Reassembles H263+ RTP packets into H263+ frames, as per RFC 4629
 * Complete frames are sent to decoder once reassembled
 */
public class JavaDepacketizer extends VideoCodec {

	/**
	 * Collection of frameAssemblers.
	 * Allows the construction of several frames if incoming packets are ouf of order
	 */
	FrameAssemblerCollection assemblersCollection = new FrameAssemblerCollection();
	
	/**
	 * Max frame size to give for next module, as some decoder have frame size limits
	 */
	private static int MAX_H263P_FRAME_SIZE = 8192;

	/**
	 * Constructor
	 */
	public JavaDepacketizer(){
	}
	
	/**
	 * Performs the media processing defined by this codec
	 * 
	 * @param input The buffer that contains the media data to be processed
	 * @param output The buffer in which to store the processed media data
	 * @return Processing result
	 */
	public int process(Buffer input, Buffer output){
		if (!input.isDiscard())	{			
			assemblersCollection.put(input);
			
			if (assemblersCollection.getLastActiveAssembler().complete()){
				assemblersCollection.getLastActiveAssembler().copyToBuffer(output);
				assemblersCollection.removeOldestThan(input.getTimeStamp());
				return BUFFER_PROCESSED_OK;
			}else{	
				output.setDiscard(true);
				return OUTPUT_BUFFER_NOT_FILLED;
			}		
		}else{
			output.setDiscard(true);
			return OUTPUT_BUFFER_NOT_FILLED;
		}		
	}
	
	/**
	 * Used to assemble fragments with the same timestamp into a single frame.
	 */
	static class FrameAssembler{
		private boolean rtpMarker = false; // have we received the RTP marker that signifies the end of a frame?
		private byte[] reassembledData = null;
		private long timeStamp = -1;
		private Format format = null;

		/**
		 * Add the buffer (which contains a fragment) to the assembler.
		 */
		public void put(Buffer buffer){
			// Read rtpMarker
			rtpMarker = (buffer.isRTPMarkerSet());

			if (buffer.getLength() <= 2){
				return; // no actual data in buffer, no need to keep.  Typically happens when RTP marker is set.
			}
			
			byte[] currentRtpPacketData = ((byte[])buffer.getData());
		    H263RtpHeader h263PRtpHeader = new H263RtpHeader(currentRtpPacketData);
		    
		    int headerSize = h263PRtpHeader.HEADER_SIZE;
		    if (h263PRtpHeader.V) {
        	    // There's an extra VRC byte at the end of the header:
        	    ++headerSize;
        	  }
        	  
        	  if (h263PRtpHeader.PLEN > 0) {
        	    // There's an extra picture header at the end:
        		  headerSize += h263PRtpHeader.PLEN;
        	  }

        	  if (h263PRtpHeader.P) {
        	    // Prepend two zero bytes to the start of the payload proper
        	    // Hack: Do this by shrinking header by 2 bytes
        		headerSize -= 2;
        	    currentRtpPacketData[headerSize] = 0x00;
        	    currentRtpPacketData[headerSize+1] = 0x00;
        	  }

        	  if (reassembledData == null){
        		  // First packet
        		  timeStamp = buffer.getTimeStamp();
        		  format = buffer.getFormat();
        		
        		  // Copy packet data to reassembledData
        		  reassembledData = new byte[currentRtpPacketData.length-headerSize];
        		  System.arraycopy(currentRtpPacketData, headerSize, reassembledData, 0, currentRtpPacketData.length-headerSize);
        	  } else {
    			// Concatenate new data to reassembledData
        		byte[] data = new byte[reassembledData.length+buffer.getLength()];
       			System.arraycopy(reassembledData, 0, data, 0, reassembledData.length);
       			System.arraycopy(currentRtpPacketData, headerSize, data, reassembledData.length, buffer.getLength());
    			// Copy data to reassembledData
    			reassembledData = new byte[data.length];
    			System.arraycopy(data, 0, reassembledData, 0, data.length);
      		}        	  
		}
		
		/**
		 * Is the frame complete?
		 */
		public boolean complete(){	
			if (!rtpMarker){
				return false;	// need an rtp marker to signify end
			}
			if (reassembledData.length <= 0){
				return false;	// need data beyond the header
			}
			// TODO: if some of the last ones come in after the marker, there will be blank squares in the lower right.
			return true;
		}	
		
		/**
		 * Assumes that complete() has been called and returns true.
		 */
		private void copyToBuffer(Buffer bDest){
			if (!rtpMarker)
				throw new IllegalStateException();
			if (reassembledData.length <= 0)
				throw new IllegalStateException();	
			
			if (reassembledData.length<=MAX_H263P_FRAME_SIZE){
				// If the frame data can be processed by native module, ie reassembled frame size not too big 
				// Set buffer
				bDest.setData(reassembledData);
				bDest.setLength(reassembledData.length);
				bDest.setOffset(0);
				bDest.setTimeStamp(timeStamp);
				bDest.setFormat(format);
				bDest.setFlags(Buffer.FLAG_RTP_MARKER | Buffer.FLAG_RTP_TIME);
			}
			// Set reassembledData to null
			reassembledData = null;
		}
		
		/**
		 * Get timestamp
		 * 
		 * @return long
		 */
		public long getTimeStamp(){
			return timeStamp;
		}
	}
	
	/**
	 * Used to manage different timestamps, as packets could be coming not in order.
	 * 
	 * Data is an array of FrameAssemblers, sorted by timestamps (oldest is first, newest is last)
	 */
	static class FrameAssemblerCollection{
		final static int NUMBER_OF_ASSEMBLERS = 5;
		private FrameAssembler[] assemblers = new FrameAssembler[NUMBER_OF_ASSEMBLERS];
		private int activeAssembler = 0;
		private int numberOfAssemblers = 0;
		
		/**
		 * Add the buffer (which contains a fragment) to the right assembler.  
		 */
		public void put(Buffer buffer){
			activeAssembler = getAssembler(buffer.getTimeStamp());
			assemblers[activeAssembler].put(buffer);
		}
		
		/**
		 * Get the active frame assembler
		 * 
		 * @return frameAssembler Last active assembler
		 */
		public FrameAssembler getLastActiveAssembler(){
			return assemblers[activeAssembler];
		}
		
		/**
		 * Create a new frame assembler for given timeStamp
		 * 
		 * @param timeStamp
		 * @return assembler number Position of the assembler in the collection
		 */
		public int createNewAssembler(long timeStamp){
			int spot = -1;
			if (numberOfAssemblers< NUMBER_OF_ASSEMBLERS-1){
				// If there's enough space left to create a new assembler
				// We search its spot
				for (int i=0;i<numberOfAssemblers;i++){
					if (timeStamp <assemblers[i].getTimeStamp()){
						spot = i;
					}
				}
				if (spot ==-1){
					spot = numberOfAssemblers;
				}
				numberOfAssemblers++;
				// Decale all assemblers with newest timeStamp to the right
				for (int i=numberOfAssemblers;i>spot;i--){
					assemblers[i] = assemblers[i-1];
				}
				assemblers[spot] = new FrameAssembler();
			}else{
				// Not enough space, we destroy the oldest assembler
				for (int i=1;i<NUMBER_OF_ASSEMBLERS;i++){
					assemblers[i-1]=assemblers[i];
				}
				// Last spot is for the new assembler
				assemblers[NUMBER_OF_ASSEMBLERS-1] = new FrameAssembler();
				spot = numberOfAssemblers;
			}
			return spot;
		}
		
		/**
		 * Get the assembler used for given timestamp
		 *
		 * @param timeStamp
		 * @return  FrameAssembler associated to timeStamp
		 */
		public int getAssembler(long timeStamp){
			int assemblerNumber = -1;
			for (int i=0; i<numberOfAssemblers; i++){
				if (assemblers[i].getTimeStamp() == timeStamp){
					assemblerNumber = i;
				}
			}
			if (assemblerNumber==-1){
				// Given timestamp never used, we create a new assembler
				assemblerNumber = createNewAssembler(timeStamp);
			}
			return assemblerNumber;
		}
		
		/**
		 * Remove oldest FrameAssembler than given timeStamp 
		 * (if given timeStamp has been rendered, then oldest ones are no more of no use)
		 * This also removes given timeStamp
		 * 
		 * @param timeStamp
		 */
		public void removeOldestThan(long timeStamp){
			// Find spot from which to remove
			int spot = numberOfAssemblers-1;
			for (int i=0;i<numberOfAssemblers;i++){
				if (timeStamp <=assemblers[i].getTimeStamp()){
					spot = i;
				}
			}
			// remove all assemblers with oldest timeStamp to the left
			for (int i=numberOfAssemblers;i>spot;i--){
				assemblers[i-1] = assemblers[i];
			}
			numberOfAssemblers -=spot+1;
		}
	}
}

	
