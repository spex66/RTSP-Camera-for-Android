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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import de.kp.net.rtsp.client.api.Message;
import de.kp.net.rtsp.client.api.MessageFactory;
import de.kp.net.rtsp.client.api.Response;
import de.kp.net.rtsp.client.header.CSeqHeader;
import de.kp.net.rtsp.client.header.RtspContent;
import de.kp.net.rtsp.client.header.ContentEncodingHeader;
import de.kp.net.rtsp.client.header.ContentLengthHeader;
import de.kp.net.rtsp.client.header.ContentTypeHeader;
import de.kp.net.rtsp.client.header.RtspHeader;
import de.kp.net.rtsp.client.header.SessionHeader;
import de.kp.net.rtsp.client.header.TransportHeader;
import de.kp.net.rtsp.client.request.RtspDescribeRequest;
import de.kp.net.rtsp.client.request.RtspOptionsRequest;
import de.kp.net.rtsp.client.request.RtspPauseRequest;
import de.kp.net.rtsp.client.request.RtspPlayRequest;
import de.kp.net.rtsp.client.request.RtspRequest;
import de.kp.net.rtsp.client.request.RtspSetupRequest;
import de.kp.net.rtsp.client.request.RtspTeardownRequest;
import de.kp.net.rtsp.client.response.RtspResponse;

public class RtspMessageFactory implements MessageFactory {
	
	private static Map<String, Constructor<? extends RtspHeader>> headerMap;
	private static Map<RtspRequest.Method, Class<? extends RtspRequest>> requestMap;

	static {
		
		headerMap  = new HashMap<String, Constructor<? extends RtspHeader>>();
		requestMap = new HashMap<RtspRequest.Method, Class<? extends RtspRequest>>();

		try {
	
			putHeader(ContentEncodingHeader.class);
			putHeader(ContentLengthHeader.class);
			putHeader(ContentTypeHeader.class);
			putHeader(CSeqHeader.class);
			putHeader(SessionHeader.class);
			putHeader(TransportHeader.class);

			requestMap.put(RtspRequest.Method.OPTIONS, 	RtspOptionsRequest.class);
			requestMap.put(RtspRequest.Method.SETUP, 	RtspSetupRequest.class);
			requestMap.put(RtspRequest.Method.TEARDOWN, RtspTeardownRequest.class);
			requestMap.put(RtspRequest.Method.DESCRIBE, RtspDescribeRequest.class);
			requestMap.put(RtspRequest.Method.PLAY, 	RtspPlayRequest.class);
			requestMap.put(RtspRequest.Method.PAUSE, 	RtspPauseRequest.class);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void putHeader(Class<? extends RtspHeader> cls) throws Exception {
		headerMap.put(cls.getDeclaredField("NAME").get(null).toString().toLowerCase(), cls.getConstructor(String.class));
	}


	/**
	 * This method handles RTSP server responses
	 */
	public void incomingMessage(MessageBuffer buffer) throws Exception {

		ByteArrayInputStream in = new ByteArrayInputStream(buffer.getData(), buffer.getOffset(), buffer.getLength());
		
		int initial = in.available();
		Message message = null;

		try {
			// message line.
			String line = readLine(in);
			if (line.startsWith(Message.RTSP_TOKEN)) {
				message = new RtspResponse(line);
			
			} else {
				
				RtspRequest.Method method = null;
				try {
					method = RtspRequest.Method.valueOf(line.substring(0, line.indexOf(' ')));
				
				} catch (IllegalArgumentException ilae) {
				}
				
				Class<? extends RtspRequest> cls = requestMap.get(method);
				if (cls != null)
					message = cls.getConstructor(String.class).newInstance(line);
				
				else
					message = new RtspRequest(line);
			
			}

			while (true)
			{
				line = readLine(in);
				if (in == null)
					throw new Exception();
				if (line.length() == 0)
					break;
				Constructor<? extends RtspHeader> c = headerMap.get(line.substring(0,
						line.indexOf(':')).toLowerCase());
				if (c != null)
					message.addHeader(c.newInstance(line));
				else
					message.addHeader(new RtspHeader(line));
			}
			buffer.setMessage(message);

			try
			{
				int length = ((ContentLengthHeader) message
						.getHeader(ContentLengthHeader.NAME)).getValue();
				if (in.available() < length)
					throw new Exception();
				RtspContent content = new RtspContent();
				content.setDescription(message);
				byte[] data = new byte[length];
				in.read(data);
				content.setBytes(data);
				message.setEntityMessage(new RtspEntityMessage(message, content));
			} catch (Exception e)
			{
			}

		} catch (Exception e)
		{
			throw new Exception(e);
		} finally
		{
			buffer.setused(initial - in.available());
			try
			{
				in.close();
			} catch (IOException e)
			{
			}
		}
	}

	@Override
	public RtspRequest outgoingRequest(String uri, RtspRequest.Method method, int cseq, RtspHeader... extras) throws URISyntaxException {
		
		Class<? extends RtspRequest> cls = requestMap.get(method);
		RtspRequest message;
		
		try {
			message = cls != null ? cls.newInstance() : new RtspRequest();
		
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		message.setLine(method, uri);
		fillMessage(message, cseq, extras);

		return message;
	}

	@Override
	public RtspRequest outgoingRequest(RtspContent body, String uri, RtspRequest.Method method, int cseq, RtspHeader... extras) throws URISyntaxException {
		
		Message message = outgoingRequest(uri, method, cseq, extras);
		return (RtspRequest) message.setEntityMessage(new RtspEntityMessage(message, body));
		
	}

	@Override
	public Response outgoingResponse(int code, String text, int cseq, RtspHeader... extras) {

		RtspResponse message = new RtspResponse();
		message.setLine(code, text);
		
		fillMessage(message, cseq, extras);
		return message;
		
	}

	@Override
	public Response outgoingResponse(RtspContent body, int code, String text, int cseq, RtspHeader... extras) {
		
		Message message = outgoingResponse(code, text, cseq, extras);
		return (Response) message.setEntityMessage(new RtspEntityMessage(message, body));
	
	}

	private void fillMessage(Message message, int cseq, RtspHeader[] extras) {
		
		message.addHeader(new CSeqHeader(cseq));
		for (RtspHeader h : extras)
			message.addHeader(h);
	
	}

	private String readLine(InputStream in) throws IOException {

		int ch = 0;
		
		StringBuilder b = new StringBuilder();
		for (ch = in.read(); ch != -1 && ch != 0x0d && ch != 0x0a; ch = in.read())
			b.append((char) ch);
		
		if (ch == -1)
			return null;
		
		in.read();
		return b.toString();
	}
}
