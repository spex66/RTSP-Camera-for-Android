package de.kp.net.rtsp.client.header;
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

public class RtspContent {
	
	private String type;

	private String encoding;

	private byte[] content;

	public void setDescription(Message message) throws Exception {
		
		type = message.getHeader(ContentTypeHeader.NAME).getRawValue();
		try {
			encoding = message.getHeader(ContentEncodingHeader.NAME).getRawValue();
		
		} catch(Exception e) {
		}
		
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public byte[] getBytes() {
		return content;
	}

	public void setBytes(byte[] content) {
		this.content = content;
	}
	
}
