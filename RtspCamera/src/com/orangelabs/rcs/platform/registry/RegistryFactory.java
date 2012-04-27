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

package com.orangelabs.rcs.platform.registry;

import com.orangelabs.rcs.platform.FactoryException;

/**
 * Application registry factory
 * 
 * @author jexa7410
 */
public abstract class RegistryFactory {
	/**
	 * Current platform factory
	 */
	private static RegistryFactory factory = null;
	
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
			factory = (RegistryFactory)Class.forName(classname).newInstance();
		} catch(Exception e) {
			throw new FactoryException("Can't load the factory " + classname);
		}
	}
	
	/**
	 * Returns the current factory
	 * 
	 * @return Factory
	 */
	public static RegistryFactory getFactory() {
		return factory;
	}
	
	/**
	 * Read a string value in the registry
	 * 
	 * @param key Key name to be read
	 * @param defaultValue Default value
	 * @return String
	 */
	public abstract String readString(String key, String defaultValue);

	/**
	 * Write a string value in the registry
	 * 
	 * @param key Key name to be updated
	 * @param value New value
	 */
	public abstract void writeString(String key, String value);

	/**
	 * Read an integer value in the registry
	 * 
	 * @param key Key name to be read
	 * @param defaultValue Default value
	 * @return Integer
	 */
	public abstract int readInteger(String key, int defaultValue);

	/**
	 * Write an integer value in the registry
	 * 
	 * @param key Key name to be updated
	 * @param value New value
	 */
	public abstract void writeInteger(String key, int value);

	/**
	 * Read a long value in the registry
	 * 
	 * @param key Key name to be read
	 * @param defaultValue Default value
	 * @return Long
	 */
	public abstract long readLong(String key, long defaultValue);

	/**
	 * Write a long value in the registry
	 * 
	 * @param key Key name to be updated
	 * @param value New value
	 */
	public abstract void writeLong(String key, long value);

	/**
	 * Read a boolean value in the registry
	 * 
	 * @param key Key name to be read
	 * @param defaultValue Default value
	 * @return Boolean
	 */
	public abstract boolean readBoolean(String key, boolean defaultValue);

	/**
	 * Write a boolean value in the registry
	 * 
	 * @param key Key name to be updated
	 * @param value New value
	 */
	public abstract void writeBoolean(String key, boolean value);
	
	/**
	 * Remove a parameter in the registry
	 * 
	 * @param key Key name to be removed
	 */
	public abstract void removeParameter(String key);
}
