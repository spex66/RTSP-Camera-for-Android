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

/**
 * Datagram connection
 * 
 * @author jexa7410
 */
public interface DatagramConnection {
	/**
	 * Default datagram packet size
	 */
	public static int DEFAULT_DATAGRAM_SIZE = 4096 * 8;
	
	/**
	 * Open the datagram connection
	 * 
	 * @throws IOException
	 */
	public void open() throws IOException;
	
	/**
	 * Open the datagram connection
	 * 
	 * @param port Local port
	 * @throws IOException
	 */
	public void open(int port) throws IOException;

	/**
	 * Close the datagram connection
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;
	
	/**
	 * Send data
	 * 
	 * @param remoteAddr Remote address
	 * @param remotePort Remote port
	 * @param data Data as byte array
	 * @throws IOException
	 */
	public void send(String remoteAddr, int remotePort, byte[] data) throws IOException;
	
	/**
	 * Receive data
	 * 
	 * @return Byte array
	 * @throws IOException
	 */
	public byte[] receive() throws IOException;
	
	/**
	 * Receive data with a specific buffer size
	 * 
	 * @param bufferSize Buffer size 
	 * @return Byte array
	 * @throws IOException
	 */
	public byte[] receive(int bufferSize) throws IOException;	
	
	/**
	 * Returns the local address
	 * 
	 * @return Address
	 * @throws IOException
	 */
	public String getLocalAddress() throws IOException;

	/**
	 * Returns the local port
	 * 
	 * @return Port
	 * @throws IOException
	 */
	public int getLocalPort() throws IOException;
}
