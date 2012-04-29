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

import java.util.ArrayList;
import java.util.List;

import de.kp.net.rtsp.client.api.EntityMessage;
import de.kp.net.rtsp.client.api.Message;
import de.kp.net.rtsp.client.header.CSeqHeader;
import de.kp.net.rtsp.client.header.RtspHeader;

public abstract class RtspMessage implements Message {

	private String line;

	private List<RtspHeader> headers;

	private CSeqHeader cseq;
	
	private EntityMessage entity;

	public RtspMessage() {
		headers = new ArrayList<RtspHeader>();
	}

	@Override
	public byte[] getBytes() throws Exception {
		
		getHeader(CSeqHeader.NAME);
		addHeader(new RtspHeader("User-Agent", "RtspClient"));
		
		byte[] message = toString().getBytes();
		if (getEntityMessage() != null) {
			
			byte[] body = entity.getBytes();
			byte[] full = new byte[message.length + body.length];
			
			System.arraycopy(message, 0, full, 0, message.length);
			System.arraycopy(body, 0, full, message.length, body.length);
			
			message = full;
		}
		
		return message;
	
	}

	@Override
	public RtspHeader getHeader(final String name) throws Exception {
		
		int index = headers.indexOf(new Object() {
			@Override
			public boolean equals(Object obj) {
				return name.equalsIgnoreCase(((RtspHeader) obj).getName());
			}
		});
		
		
		if(index == -1) throw new Exception("[Missing Header] " + name);
		
		return headers.get(index);
	
	}

	@Override
	public RtspHeader[] getHeaders() {
		return headers.toArray(new RtspHeader[headers.size()]);
	}

	@Override
	public CSeqHeader getCSeq() {
		return cseq;
	}

	@Override
	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}
	
	@Override
	public void addHeader(RtspHeader header) {
		
		if(header == null) return;
		if(header instanceof CSeqHeader)
			cseq = (CSeqHeader) header;
		
		int index = headers.indexOf(header);
		if(index > -1)
			headers.remove(index);
		else
			index = headers.size();
		
		headers.add(index, header);
	
	}
	
	@Override
	public EntityMessage getEntityMessage() {
		return entity;
	}
	
	@Override
	public Message setEntityMessage(EntityMessage entity) {
		this.entity = entity;
		return this;
	}
	
	@Override
	public String toString() {
		
		StringBuilder buffer = new StringBuilder();
		buffer.append(getLine()).append("\r\n");
		
		for(RtspHeader header : headers)
			buffer.append(header).append("\r\n");
		
		buffer.append("\r\n");
		return buffer.toString();
	
	}
}
