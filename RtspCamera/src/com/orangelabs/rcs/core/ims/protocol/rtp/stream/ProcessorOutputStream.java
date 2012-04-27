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

package com.orangelabs.rcs.core.ims.protocol.rtp.stream;

import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;

/**
 * Processor output stream
 */
public interface ProcessorOutputStream {
    /**
	 * Open the output stream
	 * 
     * @throws Exception
	 */	
    public void open() throws Exception;

    /**
     * Close from the output stream
     */
    public void close();
    
    /**
     * Write to the stream without blocking
     * 
     * @param buffer Input buffer
     * @throws Exception
     */
    public void write(Buffer buffer) throws Exception;
}
