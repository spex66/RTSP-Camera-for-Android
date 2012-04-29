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

import de.kp.net.rtsp.client.api.EntityMessage;
import de.kp.net.rtsp.client.api.Message;
import de.kp.net.rtsp.client.header.RtspContent;
import de.kp.net.rtsp.client.header.ContentEncodingHeader;
import de.kp.net.rtsp.client.header.ContentLengthHeader;
import de.kp.net.rtsp.client.header.ContentTypeHeader;

public class RtspEntityMessage implements EntityMessage {
	
	private RtspContent content;

	private final Message message;

	public RtspEntityMessage(Message message) {
		this.message = message;
	}
	
	public RtspEntityMessage(Message message, RtspContent body) {
		this(message);
		setContent(body);
	}
	
	@Override
	public Message getMessage() {
		return message;
	};

	public byte[] getBytes() throws Exception {
	
		message.getHeader(ContentTypeHeader.NAME);
		message.getHeader(ContentLengthHeader.NAME);
		
		return content.getBytes();
	
	}

	@Override
	public RtspContent getContent() {
		return content;
	}

	@Override
	public void setContent(RtspContent content) {
		
		if(content == null) throw new NullPointerException();
		this.content = content;
		
		message.addHeader(new ContentTypeHeader(content.getType()));
		if(content.getEncoding() != null)
			message.addHeader(new ContentEncodingHeader(content.getEncoding()));
		
		message.addHeader(new ContentLengthHeader(content.getBytes().length));
	
	}
	
	@Override
	public boolean isEntity() {
		return content != null;
	}
	
}
