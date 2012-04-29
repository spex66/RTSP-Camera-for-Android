package de.kp.net.rtsp.client.response;
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

import de.kp.net.rtsp.client.api.Response;
import de.kp.net.rtsp.client.message.RtspMessage;

public class RtspResponse extends RtspMessage implements Response {
	
	private int status;
	private String text;

	public RtspResponse() {
	}

	public RtspResponse(String line) {
		
		setLine(line);
		line = line.substring(line.indexOf(' ') + 1);
		
		status = Integer.parseInt(line.substring(0, line.indexOf(' ')));
		text = line.substring(line.indexOf(' ') + 1);
	
	}

	@Override
	public int getStatusCode() {
		return status;
	}
	
	@Override
	public String getStatusText() {
		return text;
	}

	@Override
	public void setLine(int statusCode, String statusText) {
	
		status = statusCode;
		text   = statusText;
		
		super.setLine(RTSP_VERSION_TOKEN + ' ' + status + ' ' + text);
	
	}
}
