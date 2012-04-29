package de.kp.net.rtsp.client.transport;
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
import java.net.Socket;
import java.net.URI;

import de.kp.net.rtsp.client.api.Message;
import de.kp.net.rtsp.client.api.Transport;
import de.kp.net.rtsp.client.api.TransportListener;

class TCPTransportThread extends Thread {
	
	private final TCPTransport transport;

	private volatile TCPTransportListener listener;

	public TCPTransportThread(TCPTransport transport, TransportListener listener) {
		this.transport = transport;
		this.listener  = new TCPTransportListener(listener);
	}

	public TCPTransportListener getListener() {
		return listener;
	}

	public void setListener(TransportListener listener) {
		listener = new TCPTransportListener(listener);
	}

	@Override
	public void run() {
		
		listener.connected(transport);
		
		byte[] buffer = new byte[2048];
		
		int read = -1;
		while(transport.isConnected()) {
			
			try {
				read = transport.receive(buffer);
				if(read == -1)
				{
					transport.setConnected(false);
					listener.remoteDisconnection(transport);
				} else
					listener.dataReceived(transport, buffer, read);
			
			} catch(IOException e) {
				listener.error(transport, e);
			}
		}
	}
}

public class TCPTransport implements Transport {
	
	private Socket socket;

	private TCPTransportThread thread;
	private TransportListener transportListener;

	private volatile boolean connected;

	public TCPTransport() {
	}

	@Override
	public void connect(URI to) throws IOException {
		
		if(connected)
			throw new IllegalStateException("Socket is still open. Close it first");
		
		int port = to.getPort();
		if(port == -1) port = 554;
		
		socket = new Socket(to.getHost(), port);
		
		setConnected(true);
		thread = new TCPTransportThread(this, transportListener);
		thread.start();
	
	}

	@Override
	public void disconnect() {
		
		setConnected(false);
		try {
			socket.close();
		
		} catch(IOException e) {
		}
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public synchronized void sendMessage(Message message) throws Exception {
		
		socket.getOutputStream().write(message.getBytes());
		thread.getListener().dataSent(this);
	
	}

	@Override
	public void setTransportListener(TransportListener listener) {
		transportListener = listener;
		if(thread != null)
			thread.setListener(listener);
	}

	@Override
	public void setUserData(Object data) {
	}

	int receive(byte[] data) throws IOException {
		return socket.getInputStream().read(data);
	}

	void setConnected(boolean connected) {
		this.connected = connected;
	}
}