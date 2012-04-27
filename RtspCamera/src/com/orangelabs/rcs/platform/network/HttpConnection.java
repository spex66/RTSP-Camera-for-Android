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

/**
 * HTTP connection
 * 
 * @author jexa7410
 */
public interface HttpConnection {
	/**
	 * GET method
	 */
	public final static String GET_METHOD = "GET";
	
	/**
	 * POST method
	 */
	public final static String POST_METHOD = "POST";

	/**
	 * Open the HTTP connection
	 * 
	 * @param url Remote URL
	 * @throws IOException
	 */
	public void open(String url) throws IOException;

	/**
	 * Close the HTTP connection
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;
	
	/**
	 * HTTP GET request
	 * 
	 * @return Response
	 * @throws IOException
	 */
	public ByteArrayOutputStream get() throws IOException;
	
	/**
	 * HTTP POST request
	 * 
	 * @return Response
	 * @throws IOException
	 */
	public ByteArrayOutputStream post() throws IOException;
}
