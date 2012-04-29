package de.kp.net.rtsp.client.api;
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

import de.kp.net.rtsp.client.header.CSeqHeader;
import de.kp.net.rtsp.client.header.RtspHeader;

public interface Message {
	
	static String RTSP_TOKEN = "RTSP/";

	static String RTSP_VERSION = "1.0";

	static String RTSP_VERSION_TOKEN = RTSP_TOKEN + RTSP_VERSION;

	/**
	 * 
	 * @return the Message line (the first line of the message)
	 */
	public String getLine();

	/**
	 * Returns a header, if exists
	 * 
	 * @param name
	 *          Name of the header to be searched
	 * @return value of that header
	 * @throws Exception
	 */
	public RtspHeader getHeader(String name) throws Exception;

	/**
	 * Convenience method to get CSeq.
	 * 
	 * @return
	 */
	public CSeqHeader getCSeq();

	/**
	 * 
	 * @return all headers in the message, except CSeq
	 */
	public RtspHeader[] getHeaders();

	/**
	 * Adds a new header or replaces if one already exists. If header to be added
	 * is a CSeq, implementation MUST keep reference of this header.
	 * 
	 * @param header
	 */
	public void addHeader(RtspHeader header);

	/**
	 * 
	 * @return message as a byte array, ready for transmission.
	 */
	public byte[] getBytes() throws Exception;

	/**
	 * 
	 * @return Entity part of message, it exists.
	 */
	public EntityMessage getEntityMessage();

	/**
	 * 
	 * @param entity
	 *          adds an entity part to the message.
	 * @return this, for easier construction.
	 */
	public Message setEntityMessage(EntityMessage entity);
	
}
