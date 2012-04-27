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

import com.orangelabs.rcs.platform.FactoryException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * File factory
 * 
 * @author jexa7410
 */
public abstract class FileFactory {
	/**
	 * Current platform factory
	 */
	private static FileFactory factory = null;
	
	/**
	 * Load the factory
	 * 
	 * @param classname Factory classname
	 * @throws Exception
	 */
	public static void loadFactory(String classname) throws FactoryException {
		if (factory != null) {
			return;
		}
		
		try {
			factory = (FileFactory)Class.forName(classname).newInstance();
		} catch(Exception e) {
			throw new FactoryException("Can't load the factory " + classname);
		}
	}
	
	/**
	 * Returns the current factory
	 * 
	 * @return Factory
	 */
	public static FileFactory getFactory() {
		return factory;
	}

	/**
	 * Open a file input stream
	 * 
	 * @param url URL
	 * @return Input stream
	 * @throws IOException
	 */
	public abstract InputStream openFileInputStream(String url) throws IOException;

	/**
	 * Open a file output stream
	 * 
	 * @param url URL
	 * @return Output stream
	 * @throws IOException
	 */
	public abstract OutputStream openFileOutputStream(String url) throws IOException;
	
	/**
	 * Returns the description of a file
	 * 
	 * @param url URL of the file
	 * @return File description
	 * @throws IOException
	 */
	public abstract FileDescription getFileDescription(String url) throws IOException;
	
	/**
	 * Returns the root directory for photos
	 * 
	 *  @return Directory path
	 */
	public abstract String getPhotoRootDirectory();

	/**
	 * Returns the root directory for videos
	 * 
	 *  @return Directory path
	 */
	public abstract String getVideoRootDirectory();
	
	/**
	 * Returns the root directory for files
	 * 
	 *  @return Directory path
	 */
	public abstract String getFileRootDirectory();
	
	/**
	 * Update the media storage
	 * 
	 * @param url New URL to be added
	 */
	public abstract void updateMediaStorage(String url);	
	
	/**
	 * Returns whether a file exists or not
	 * 
	 * @param url Url of the file to check
	 * @return File existence
	 */
	public abstract boolean fileExists(String url);
	
	/**
	 * Create a directory if not already exist
	 * 
	 * @param path Directory path
	 * @return true if the directory exists or is created
	 */
	public static boolean createDirectory(String path) {
		File dir = new File(path); 
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
                return false; 
			}
		}
        return true;
	}	
}
