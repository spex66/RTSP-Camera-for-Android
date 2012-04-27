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
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;

/**
 * Reassembles H263+ RTP packets into H263+ frames, as per RFC 4629
 */
public class JavaPacketizer extends VideoCodec {
	/**
	 * Because packets can come out of order, it is possible that some packets for a newer frame
	 * may arrive while an older frame is still incomplete.  However, in the case where we get nothing
	 * but incomplete frames, we don't want to keep all of them around forever.
	 */
	public JavaPacketizer(){
	}
	
	public int process(Buffer input, Buffer output){		
		if (!input.isDiscard())	{			
			// Add H263+ RTP header
			/*	      0                   1
				      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
				     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
				     |   RR    |P|V|   PLEN    |PEBIT|		+ 2 null bytes
				     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
					----------------------------------------
					|0000 0100|0000 0000|0000 0000|0000 0000|
					----------------------------------------
					Only bit set is P = 1
			*/		
			byte h263header[] = new byte[2];
			h263header[0]= 0x04;
			h263header[1]= 0x00;
			
			byte[] bufferData = (byte[])input.getData();
			byte data[] = new byte[bufferData.length+h263header.length];
			// write h263 payload
			System.arraycopy(h263header, 0, data, 0, h263header.length);
			// Write data
			System.arraycopy(bufferData, 0, data, h263header.length, bufferData.length);
						
			if (data.length > 0){
				// Copy to buffer
				output.setFormat(input.getFormat());
				output.setData(data);
				output.setLength(data.length);
				output.setOffset(0);
				output.setTimeStamp(input.getTimeStamp());
				output.setFlags(Buffer.FLAG_RTP_MARKER | Buffer.FLAG_RTP_TIME);
			}
			return BUFFER_PROCESSED_OK;
		}else{
			output.setDiscard(true);
			return OUTPUT_BUFFER_NOT_FILLED;
		}		
	}
	
}

	
