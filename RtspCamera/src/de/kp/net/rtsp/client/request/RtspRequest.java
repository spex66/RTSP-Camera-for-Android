package de.kp.net.rtsp.client.request;
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

import java.net.URI;
import java.net.URISyntaxException;

import de.kp.net.rtsp.client.RtspClient;
import de.kp.net.rtsp.client.api.Message;
import de.kp.net.rtsp.client.api.Request;
import de.kp.net.rtsp.client.api.Response;
import de.kp.net.rtsp.client.message.RtspMessage;

public class RtspRequest extends RtspMessage implements Request {
	
	private Method method;

	private String uri;

	public RtspRequest() {
	}

	public RtspRequest(String messageLine) throws URISyntaxException {
		String[] parts = messageLine.split(" ");
		setLine(Method.valueOf(parts[0]), parts[1]);
	}

	@Override
	public void setLine(Method method, String uri) throws URISyntaxException {
		
		this.method = method;
		this.uri = new URI(uri).toString();
		;

		super.setLine(method.toString() + ' ' + uri + ' ' + RTSP_VERSION_TOKEN);
	
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public String getURI() {
		return uri;
	}

	@Override
	public void handleResponse(RtspClient client, Response response) {
		
		if (testForClose(client, this) || testForClose(client, response))
			client.getTransport().disconnect();
	
	}

	protected void setURI(String uri) {
		this.uri = uri;
	}
 
	protected void setMethod(Method method) {
		this.method = method;
	}

	private boolean testForClose(RtspClient client, Message message) {
		
		try {
			return message.getHeader("Connection").getRawValue().equalsIgnoreCase("close");
		
		} catch(Exception e) {
			
			// this is an expected exception in case of no
			// connection close in the response message
			
			// client.getRequestListener().onError(client, e);
		}

		return false;
	
	}
}