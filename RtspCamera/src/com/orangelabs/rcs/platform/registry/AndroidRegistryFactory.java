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

import com.orangelabs.rcs.core.CoreException;
import com.orangelabs.rcs.platform.AndroidFactory;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Android registry factory
 * 
 * @author jexa7410
 */
public class AndroidRegistryFactory extends RegistryFactory {
	/**
	 * RCS registry name
	 */
	public static final String RCS_PREFS = "RCS";

	/**
	 * Shared preference
	 */
	private SharedPreferences preferences;

	/**
     * Constructor
     * 
     * @throws CoreException
     */
	public AndroidRegistryFactory() throws CoreException {
		super();

		if (AndroidFactory.getApplicationContext() == null) {
			throw new CoreException("Application context not initialized");
		}
		
		preferences = AndroidFactory.getApplicationContext().getSharedPreferences(RCS_PREFS, Activity.MODE_PRIVATE);
	}

	/**
	 * Read a string value in the registry
	 * 
	 * @param key Key name to be read
	 * @param defaultValue Default value
	 * @return String
	 */
	public String readString(String key, String defaultValue) {
		return preferences.getString(key, defaultValue);
	}

	/**
	 * Write a string value in the registry
	 * 
	 * @param key Key name to be updated
	 * @param value New value
	 */
	public void writeString(String key, String value) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * Read an integer value in the registry
	 * 
	 * @param key Key name to be read
	 * @param defaultValue Default value
	 * @return Integer
	 */
	public int readInteger(String key, int defaultValue) {
		return preferences.getInt(key, defaultValue);
	}

	/**
	 * Write an integer value in the registry
	 * 
	 * @param key Key name to be updated
	 * @param value New value
	 */
	public void writeInteger(String key, int value) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(key, value);
		editor.commit();		
	}

	/**
	 * Read a long value in the registry
	 * 
	 * @param key Key name to be read
	 * @param defaultValue Default value
	 * @return Long
	 */
	public long readLong(String key, long defaultValue) {
		return preferences.getLong(key, defaultValue);
	}

	/**
	 * Write a long value in the registry
	 * 
	 * @param key Key name to be updated
	 * @param value New value
	 */
	public void writeLong(String key, long value) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(key, value);
		editor.commit();		
	}
	
	/**
	 * Read a boolean value in the registry
	 * 
	 * @param key Key name to be read
	 * @param defaultValue Default value
	 * @return Boolean
	 */
	public boolean readBoolean(String key, boolean defaultValue) {
		return preferences.getBoolean(key, defaultValue);
	}

	/**
	 * Write a boolean value in the registry
	 * 
	 * @param key Key name to be updated
	 * @param value New value
	 */
	public void writeBoolean(String key, boolean value) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}	

	/**
	 * Remove a parameter in the registry
	 * 
	 * @param key Key name to be removed
	 */
	public void removeParameter(String key) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove(key);
		editor.commit();
	}
}
