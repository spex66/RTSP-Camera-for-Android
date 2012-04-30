package de.kp.net.rtsp.client;
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

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import de.kp.net.rtsp.client.api.RequestListener;
import de.kp.net.rtsp.client.api.Message;
import de.kp.net.rtsp.client.api.MessageFactory;
import de.kp.net.rtsp.client.api.Request;
import de.kp.net.rtsp.client.api.Response;
import de.kp.net.rtsp.client.api.Transport;
import de.kp.net.rtsp.client.api.TransportListener;
import de.kp.net.rtsp.client.header.RtspHeader;
import de.kp.net.rtsp.client.header.SessionHeader;
import de.kp.net.rtsp.client.header.TransportHeader;
import de.kp.net.rtsp.client.header.TransportHeader.LowerTransport;
import de.kp.net.rtsp.client.message.MessageBuffer;
import de.kp.net.rtsp.client.message.RtspMessageFactory;
import de.kp.net.rtsp.client.request.RtspOptionsRequest;
import de.kp.net.rtsp.client.request.RtspRequest;

public class RtspClient implements TransportListener {
	
	private Transport transport;

	private MessageFactory messageFactory;

	private MessageBuffer messageBuffer;

	private volatile int cseq;

	private SessionHeader session;

	/**
	 * URI kept from last setup.
	 */
	private URI uri;

	private Map<Integer, RtspRequest> outstanding;

	private RequestListener clientListener;

	public RtspClient() {

		cseq = 0;

		messageFactory = new RtspMessageFactory();
		messageBuffer  = new MessageBuffer();

		outstanding = new HashMap<Integer, RtspRequest>();

	}

	public Transport getTransport() {
		return transport;
	}

	public void setSession(SessionHeader session) {
		this.session = session;
	}

	public MessageFactory getMessageFactory() {
		return messageFactory;
	}

	public URI getURI() {
		return uri;
	}

	public void options(String uri, URI endpoint) {
		
		try {
			
			RtspOptionsRequest message = (RtspOptionsRequest) messageFactory.outgoingRequest(uri, RtspRequest.Method.OPTIONS, nextCSeq());
			// if (getTransport().isConnected() == false) message.addHeader(new RtspHeader("Connection", "close"));			
			send(message, endpoint);
		
		} catch(Exception e) {
			if(clientListener != null) clientListener.onError(this, e);
		}
	}

	public void play() {

		try {
			send(messageFactory.outgoingRequest(uri.toString(), RtspRequest.Method.PLAY, nextCSeq(), session));
		
		} catch(Exception e) {			
			if(clientListener != null) clientListener.onError(this, e);
		
		}
	}

	public void pause() {
		
		try {
			send(messageFactory.outgoingRequest(uri.toString(), RtspRequest.Method.PAUSE, nextCSeq(), session));
		
		} catch(Exception e) {			
			if(clientListener != null) clientListener.onError(this, e);
		}
	}

	public void record() throws IOException {
		throw new UnsupportedOperationException("Recording is not supported in current version.");
	}

	public void setRequestListener(RequestListener listener) {
		clientListener = listener;
	}

	public RequestListener getRequestListener() {
		return clientListener;
	}

	public void setTransport(Transport transport) {
		this.transport = transport;
		transport.setTransportListener(this);
	}

	public void describe(URI uri, String resource) {

		this.uri = uri;
		
		String finalURI = uri.toString();		
		if ((resource != null) && (resource.equals("*") == false))
			finalURI += '/' + resource;
		
		try {
			send(messageFactory.outgoingRequest(finalURI, RtspRequest.Method.DESCRIBE, nextCSeq(), new RtspHeader("Accept", "application/sdp")));
		
		} catch(Exception e) {
			if(clientListener != null) clientListener.onError(this, e);
		}
	}

	public void setup(URI uri, int localPort) {
		
		this.uri = uri;
		try {
			
			String portParam = "client_port=" + localPort + "-" + (1 + localPort);
			send(getSetup(uri.toString(), localPort, new TransportHeader(LowerTransport.DEFAULT, "unicast", portParam), session));
		
		} catch(Exception e) {
			if(clientListener != null) clientListener.onError(this, e);
		}
	}

	public void setup(URI uri, int localPort, String resource) {
		
		this.uri = uri;
		try {
			
			String portParam = "client_port=" + localPort + "-" + (1 + localPort);
			String finalURI = uri.toString();
			
			if ((resource != null) && (resource.equals("*") == false))
				finalURI += '/' + resource;
			
			send(getSetup(finalURI, localPort, new TransportHeader(LowerTransport.DEFAULT, "unicast", portParam), session));
		
		} catch(Exception e) {
			if(clientListener != null) clientListener.onError(this, e);
		}
	}

	public void teardown() {
		
		if(session == null)
			return;
		
		try {
			send(messageFactory.outgoingRequest(uri.toString(), RtspRequest.Method.TEARDOWN, nextCSeq(), session, new RtspHeader("Connection", "close")));
		
		} catch(Exception e) {
			if(clientListener != null) clientListener.onError(this, e);
		
		}
	}

	public void dataReceived(Transport t, byte[] data, int size) throws Throwable {
		
		messageBuffer.addData(data, size);
		while(messageBuffer.getLength() > 0)
			try
			{
				messageFactory.incomingMessage(messageBuffer);
				messageBuffer.discardData();
				Message message = messageBuffer.getMessage();
				if(message instanceof RtspRequest)
					send(messageFactory.outgoingResponse(405, "Method Not Allowed",
							message.getCSeq().getValue()));
				else
				{
					RtspRequest request = null;
					synchronized(outstanding)
					{
						request = outstanding.remove(message.getCSeq().getValue());
					}
					Response response = (Response) message;
					request.handleResponse(this, response);
					clientListener.onSuccess(this, request, response);
				}
			} catch(Exception e)
			{
				messageBuffer.discardData();
				if(clientListener != null)
					clientListener.onError(this, e.getCause());
			}
	}

	@Override
	public void dataSent(Transport t) throws Throwable
	{
	}

	@Override
	public void error(Transport t, Throwable error) {
		clientListener.onError(this, error);
	}

	@Override
	public void error(Transport t, Message message, Throwable error)
	{
		clientListener.onFailure(this, (RtspRequest) message, error);
	}

	@Override
	public void remoteDisconnection(Transport t) throws Throwable
	{
		synchronized(outstanding)
		{
			for(Map.Entry<Integer, RtspRequest> request : outstanding.entrySet())
				clientListener.onFailure(this, request.getValue(),
						new SocketException("Socket has been closed"));
		}
	}

	public int nextCSeq() {
		return cseq++;
	}

	public void send(Message message) throws Exception {
		send(message, uri);
	}

	private void send(Message message, URI endpoint) throws Exception
	{
		if(!transport.isConnected())
			transport.connect(endpoint);

		if(message instanceof RtspRequest)
		{
			RtspRequest request = (RtspRequest) message;
			synchronized(outstanding)
			{
				outstanding.put(message.getCSeq().getValue(), request);
			}
			try
			{
				transport.sendMessage(message);
			} catch(IOException e)
			{
				clientListener.onFailure(this, request, e);
			}
		} else
			transport.sendMessage(message);
	}

	private Request getSetup(String uri, int localPort, RtspHeader... headers) throws URISyntaxException {		
		return getMessageFactory().outgoingRequest(uri, RtspRequest.Method.SETUP, nextCSeq(),
				headers);
	}

	@Override
	public void connected(Transport t) throws Throwable {
		// TODO Auto-generated method stub
		
	}
}