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

package com.orangelabs.rcs.utils.logger;

import com.orangelabs.rcs.platform.logger.AndroidAppender;

/**
 * Logger
 * 
 * @author jexa7410
 */
public class Logger {
	/**
	 * Trace ON
	 */
	public static boolean TRACE_ON = true;

	/**
	 * Trace OFF
	 */
	public static boolean TRACE_OFF = false;

	/**
	 * DEBUG level
	 */
	public static int DEBUG_LEVEL = 0;

	/**
	 * INFO level
	 */
	public static int INFO_LEVEL = 1;
	
	/**
	 * WARN level
	 */
	public static int WARN_LEVEL = 2;

	/**
	 * ERROR level
	 */
	public static int ERROR_LEVEL = 3;

	/**
	 * FATAL level
	 */
	public static int FATAL_LEVEL = 4;
	
	/**
	 * Trace flag
	 */
	public static boolean activationFlag = TRACE_ON;

	/**
	 * Trace level
	 */
	public static int traceLevel = DEBUG_LEVEL;
	
	/**
	 * List of appenders
	 */
	private static Appender[] appenders = new Appender[] { 
		new AndroidAppender()
	};
	
	/**
	 * Classname
	 */
	private String classname;
	
	/**
	 * Constructor
	 * 
	 * @param classname Classname
	 */
	private Logger(String classname) {
		int index = classname.lastIndexOf('.');
		if (index != -1) {
			this.classname = classname.substring(index+1);
		} else {
			this.classname = classname;		
		}
	}
	
	/**
	 * Is logger activated
	 * 
	 * @return boolean
	 */
	public boolean isActivated() {
		return (activationFlag == TRACE_ON);
	}

	/**
	 * Debug trace
	 * 
	 * @param trace Trace
	 */
	public void debug(String trace) {
		printTrace(trace, DEBUG_LEVEL);
	}

	/**
	 * Info trace
	 * 
	 * @param trace Trace
	 */
	public void info(String trace) {
		printTrace(trace, INFO_LEVEL);		
	}
	
	/**
	 * Warning trace
	 * 
	 * @param trace Trace
	 */
	public void warn(String trace) {
		printTrace(trace, WARN_LEVEL);		
	}
	
	/**
	 * Error trace
	 * 
	 * @param trace Trace
	 */
	public void error(String trace) {
		printTrace(trace, ERROR_LEVEL);	
	}

	/**
	 * Error trace
	 * 
	 * @param trace Trace
	 * @param e Exception
	 */
	public void error(String trace, Throwable e) {
		printTrace(trace, ERROR_LEVEL);
		e.printStackTrace();
	}
	
	/**
	 * Fatal trace
	 * 
	 * @param trace Trace
	 */
	public void fatal(String trace) {
		printTrace(trace, FATAL_LEVEL);	
	}

	/**
	 * Fatal trace
	 * 
	 * @param trace Trace
	 * @param e Exception
	 */
	public void fatal(String trace, Throwable e) {
		printTrace(trace, FATAL_LEVEL);	
		e.printStackTrace();
	}

	/**
	 * Print a trace
	 * 
	 * @param trace Trace
	 * @param level Trace level
	 */
	private void printTrace(String trace, int level) {
		if ((appenders != null) && (level >= traceLevel)) {
			for(int i=0; i < appenders.length; i++) {
				appenders[i].printTrace(classname, level, trace);
			}
		}
	}
	
	/**
	 * Set the list of appenders
	 * 
	 * @param appenders List of appenders
	 */
	public static void setAppenders(Appender[] appenders) {
		Logger.appenders = appenders;
	}

	/**
	 * Create a static instance
	 * 
	 * @param classname Classname
	 * @return Instance
	 */
	public static synchronized Logger getLogger(String classname) {
		return new Logger(classname);
	}
	
	/**
	 * Get the current appenders
	 * 
	 * @return Array of appender
	 */
	public static synchronized Appender[] getAppenders() {
		return appenders;
	}
}
