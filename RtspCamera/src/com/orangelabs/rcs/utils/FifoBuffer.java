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

package com.orangelabs.rcs.utils;

import java.util.Vector;

/**
 * FIFO buffer
 * 
 * @author JM. Auffret
 */
public class FifoBuffer {
	/**
	 * Number of objects in the buffer
	 */
	private int nbObjects = 0;

	/**
	 * Buffer of objects
	 */
	private Vector<Object> fifo = new Vector<Object>();

	/**
	 * Add an object in the buffer
	 *
	 * @param obj Message
	 */
	public synchronized void addObject(Object obj) {
		fifo.addElement(obj);
		nbObjects++;
		notifyAll();
	}

	/**
	 * Read an object in the buffer. This is a blocking method until an object is read.
	 * 
	 * @return Object
	 */
	public synchronized Object getObject() {
		Object obj = null;
		if (nbObjects == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				// Nothing to do
			}
		}
		if (nbObjects != 0) {
			obj = fifo.elementAt(0);
			fifo.removeElementAt(0);
			nbObjects--;
			notifyAll();
		}
		return obj;
	}

	/**
	 * Read an object in the buffer. This is a blocking method until a timeout
	 * occurs or an object is read.
	 * 
	 * @param timeout Timeout
	 * @return Message
	 */
	public synchronized Object getObject(int timeout) {
		Object obj = null;
		if (nbObjects == 0) {
			try {
				wait(timeout);
			} catch (InterruptedException e) {
				// Nothing to do
			}
		}
		if (nbObjects != 0) {
			obj = fifo.elementAt(0);
			fifo.removeElementAt(0);
			nbObjects--;
			notifyAll();
		}
		return obj;
	}

	/**
	 * Close the buffer
	 */
	public synchronized void close() {
		// Free the semaphore
		this.notifyAll();
	}
}
