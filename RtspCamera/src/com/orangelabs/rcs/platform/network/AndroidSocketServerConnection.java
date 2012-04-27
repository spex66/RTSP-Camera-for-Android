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
import java.net.ServerSocket;
import java.net.Socket;

import com.orangelabs.rcs.utils.logger.Logger;


/**
 * Android socket connection
 * 
 * @author jexa7410
 */
public class AndroidSocketServerConnection implements SocketServerConnection {
	/**
	 * Socket server connection
	 */
	private ServerSocket acceptSocket = null; 

	/**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
	 * Constructor
	 */
	public AndroidSocketServerConnection() {
	}

	/**
	 * Open the socket
	 * 
	 * @param port Local port
	 * @throws IOException
	 */
	public void open(int port) throws IOException {
		acceptSocket = new ServerSocket(port);
	}

	/**
	 * Close the socket
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (acceptSocket != null) {
			acceptSocket.close();
			acceptSocket = null;		
		}
	}
	
	/**
	 * Accept connection
	 * 
	 * @return Socket connection
	 * @throws IOException
	 */
	public SocketConnection acceptConnection() throws IOException {
		if (acceptSocket != null) { 
			if (logger.isActivated()) {
				logger.debug("Socket serverSocket is waiting for incoming connection");
			}
			Socket socket = acceptSocket.accept();		
			return new AndroidSocketConnection(socket);
		} else {
			throw new IOException("Connection not openned");
		}
	}
}
