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

package com.orangelabs.rcs.platform.file;

/**
 * File description
 * 
 * @author jexa7410
 */
public class FileDescription {
	/**
	 * Name
	 */
	private String name;
	
	/**
	 * Size
	 */
	private long size = -1;
		
	/**
	 * Directory
	 */	
	private boolean directory = false;
	
	/**
	 * Constructor
	 */
	public FileDescription(String name, long size) {
		this.name = name;
		this.size = size;
	}

	/**
	 * Constructor
	 */
	public FileDescription(String name, long size, boolean directory) {
		this.name = name;
		this.size = size;
		this.directory = directory;
	}

	/**
	 * Returns the size of the file
	 * 
	 * @return File size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Returns the name of the file
	 * 
	 * @return File name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Is a directory
	 * 
	 * @return Boolean
	 */
	public boolean isDirectory() {
		return directory;
	}
}
