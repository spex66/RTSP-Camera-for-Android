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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Android HTTP connection
 * 
 * @author jexa7410
 */
public class AndroidHttpConnection implements HttpConnection {
	/**
	 * HTTP connection
	 */
	private HttpURLConnection connection = null;
	
	/**
	 * Open the HTTP connection
	 * 
	 * @param url Remote URL
	 * @throws IOException
	 */
	public void open(String url) throws IOException {
		URL urlConn = new URL(url);
		connection = (HttpURLConnection)urlConn.openConnection();
		connection.connect();
	}

	/**
	 * Close the HTTP connection
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (connection != null) {
			connection.disconnect();
		}
	}
	
	/**
	 * HTTP GET request
	 * 
	 * @return Response
	 * @throws IOException
	 */
	public ByteArrayOutputStream get() throws IOException {
		if (connection != null) {
			return sendHttpRequest(HttpConnection.GET_METHOD);
		} else {
			throw new IOException("Connection not openned");
		}
	}
	
	/**
	 * HTTP POST request
	 * 
	 * @return Response
	 * @throws IOException
	 */
	public ByteArrayOutputStream post() throws IOException {
		if (connection != null) {
			return sendHttpRequest(HttpConnection.POST_METHOD);
		} else {
			throw new IOException("Connection not openned");
		}
	}
	
	/**
	 * Send HTTP request
	 * 
	 * @param method HTTP method
	 * @return Response
	 * @throws IOException
	 */
	private ByteArrayOutputStream sendHttpRequest(String method) throws IOException {
        connection.setRequestMethod(method);
        int rc = connection.getResponseCode();
        if (rc != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error " + rc);
        }
        
        InputStream inputStream = connection.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
    	int ch;
    	while((ch = inputStream.read()) != -1) {
    		result.write(ch);
    	}
    	inputStream.close();
    	
        return result;		
	}
}
