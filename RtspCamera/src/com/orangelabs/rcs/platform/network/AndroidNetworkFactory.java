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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Android network factory
 * 
 * @author jexa7410
 */
public class AndroidNetworkFactory extends NetworkFactory {

	/**
	 * Returns the local IP address
	 *
	 * @return IP address
	 */
	public String getLocalIpAddress() {
		try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = (NetworkInterface)en.nextElement();
	            for (Enumeration<InetAddress> addr = intf.getInetAddresses(); addr.hasMoreElements();) {
	                InetAddress inetAddress = (InetAddress)addr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
	            }
	        }
	        return null;
		} catch(Exception e) {
			return null;
		}
	}

    /**
     * Create a datagram connection
     * 
     * @return Datagram connection
     */
	public DatagramConnection createDatagramConnection() {
		return new AndroidDatagramConnection();
	}

    /**
     * Create a socket client connection
     * 
     * @return Socket connection
     */
	public SocketConnection createSocketClientConnection() {
		return new AndroidSocketConnection();
	}

    /**
     * Create a socket server connection
     * 
     * @return Socket server connection
     */
	public SocketServerConnection createSocketServerConnection() {
		return new AndroidSocketServerConnection();
	}

    /**
     * Create an HTTP connection
     * 
     * @return HTTP connection
     */
	public HttpConnection createHttpConnection() {
		return new AndroidHttpConnection();
	}
}
