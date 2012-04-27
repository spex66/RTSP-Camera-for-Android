/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.orangelabs.rcs.platform.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


/**
 * Android socket connection
 * 
 * @author jexa7410
 */
public class AndroidSocketConnection implements SocketConnection {
	/**
	 * Socket connection
	 */
	private Socket socket = null;
	
	/**
	 * Constructor
	 */
	public AndroidSocketConnection() {
	}
	
	/**
	 * Constructor
	 * 
	 * @param socket Socket
	 */
	public AndroidSocketConnection(Socket socket) {
		this.socket = socket;
	}

	/**
	 * Open the socket
	 * 
	 * @param remoteAddr Remote address
	 * @param remotePort Remote port
	 * @throws IOException
	 */
	public void open(String remoteAddr, int remotePort) throws IOException {
		socket = new Socket(remoteAddr, remotePort);
	}

	/**
	 * Close the socket
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (socket != null) {
			socket.close();
			socket = null;
		}		
	}
	
	/**
	 * Returns the socket input stream
	 * 
	 * @return Input stream
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException {
		if (socket != null) {
			return socket.getInputStream();
		} else {
			throw new IOException("Connection not openned");
		}
	}
	
	/**
	 * Returns the socket output stream
	 * 
	 * @return Output stream
	 * @throws IOException
	 */
	public OutputStream getOutputStream() throws IOException {
		if (socket != null) {
			return socket.getOutputStream();
		} else {
			throw new IOException("Connection not openned");
		}
	}
	
	/**
	 * Returns the remote address of the connection
	 * 
	 * @return Address
	 * @throws IOException
	 */
	public String getRemoteAddress() throws IOException {
		if (socket != null) {
			return socket.getInetAddress().getHostAddress();
		} else {
			throw new IOException("Connection not openned");
		}
	}
	
	/**
	 * Returns the remote port of the connection
	 * 
	 * @return Port
	 * @throws IOException
	 */
	public int getRemotePort() throws IOException {
		if (socket != null) {
			return socket.getPort();
		} else {
			throw new IOException("Connection not openned");
		}
	}

	/**
	 * Returns the local address of the connection
	 * 
	 * @return Address
	 * @throws IOException
	 */
	public String getLocalAddress() throws IOException {
		if (socket != null) {
			return socket.getLocalAddress().getHostAddress();
		} else {
			throw new IOException("Connection not openned");
		}
	}

	/**
	 * Returns the local port of the connection
	 * 
	 * @return Port
	 * @throws IOException
	 */
	public int getLocalPort() throws IOException {
		if (socket != null) {
			return socket.getLocalPort();
		} else {
			throw new IOException("Connection not openned");
		}
	}
	
	/**
	 * Get the timeout for this socket during which a reading
	 * operation shall block while waiting for data
	 * 
	 * @return Timeout in milliseconds
	 * @throws IOException
	 */
	public int getSoTimeout() throws IOException {
		if (socket != null) {
			return socket.getSoTimeout();
		} else {
			throw new IOException("Connection not openned");
		}
	}

	/**
	 * Set the timeout for this socket during which a reading
	 * operation shall block while waiting for data
	 * 
	 * @param timeout Timeout in milliseconds
	 * @throws IOException
	 */
	public void setSoTimeout(int timeout) throws IOException {
		if (socket != null) {
			socket.setSoTimeout(timeout);
		} else {
			throw new IOException("Connection not openned");
		}
	}
}
