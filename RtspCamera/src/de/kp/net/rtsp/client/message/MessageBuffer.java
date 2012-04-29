package de.kp.net.rtsp.client.message;
/**
 *	Copyright 2010 Voice Technology Ind. e Com. Ltda.
 *
 *	RTSPClientLib is free software: you can redistribute it and/or 
 *	modify it under the terms of the GNU Lesser General Public License 
 *	as published by the Free Software Foundation, either version 3 of 
 *	the License, or (at your option) any later version.
 *
 *	RTSPClientLib is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details. 
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with this software. If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 *	This class has been adapted to the needs of the RtspCamera project
 *	@author Stefan Krusche (krusche@dr-kruscheundpartner.de)
 *
 */

import de.kp.net.rtsp.client.api.Message;

public class MessageBuffer {
	/**
	 * buffer for received data
	 */
	private byte[] data;

	/**
	 * offset for starting useful area
	 */
	private int offset;

	/**
	 * length of useful portion.
	 */
	private int length;

	/**
	 * Used (read) buffer.
	 */
	private int used;

	/**
	 * {@link Message} created during last parsing.
	 */
	private Message message;

	/**
	 * Adds more data to buffer and ensures the sequence [data, newData] is
	 * contiguous.
	 * 
	 * @param newData data to be added to the buffer.
	 */
	public void addData(byte[] newData, int newLength) {
		
		if (data == null) {
			
			data = newData;
			length = newLength;
			offset = 0;
		
		} else {
			
			// buffer seems to be small.
			if((data.length - offset - length) < newLength) {
				// try to sequeeze data at the beginning of the buffer only if current
				// buffer does not overlap
				if(offset >= length && (data.length - length) >= newLength) {
					System.arraycopy(data, offset, data, 0, length);
					offset = 0;
				
				} else { // worst-case scenario, a new buffer will have to be created
					byte[] temp = new byte[data.length + newLength];
					System.arraycopy(data, offset, temp, 0, length);
					offset = 0;
					data = temp;
				}
			}
			// there's room for everything - just copy
			System.arraycopy(newData, 0, data, offset + length, newLength);
			length += newLength;
		}
	}

	/**
	 * Discards used portions of the buffer.
	 */
	public void discardData() {
		offset += used;
		length -= used;
	}

	public byte[] getData() {
		return data;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Message getMessage() {
		return message;
	}

	public void setused(int used) {
		this.used = used;
	}
}