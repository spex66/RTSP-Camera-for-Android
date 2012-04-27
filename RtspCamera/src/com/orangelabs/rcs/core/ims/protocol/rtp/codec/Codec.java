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

package com.orangelabs.rcs.core.ims.protocol.rtp.codec;

import com.orangelabs.rcs.core.ims.protocol.rtp.format.Format;
import com.orangelabs.rcs.core.ims.protocol.rtp.util.Buffer;

/**
 * Abstract codec
 * 
 * @author jexa7410
 */
public abstract class Codec {

	/**
	 * The input buffer was converted successfully to output
	 */
	public static final int BUFFER_PROCESSED_OK = 0;

	/**
	 * The input buffer could not be handled
	 */
	public static final int BUFFER_PROCESSED_FAILED = 1 << 0;

	/**
	 * The input buffer chunk was not fully consumed
	 */
	public static final int INPUT_BUFFER_NOT_CONSUMED = 1 << 1;

	/**
	 * The output buffer chunk was not filled
	 */
	public static final int OUTPUT_BUFFER_NOT_FILLED = 1 << 2;

	/**
	 * Input format
	 */
	private Format inputFormat;

	/**
	 * Ouput format
	 */
	private Format outputFormat;

	/**
	 * Set the input format
	 * 
	 * @param input Input format
	 * @return New format
	 */
	public Format setInputFormat(Format input) {
		inputFormat = input;
		return input;
	}

	/**
	 * Set the output format
	 * 
	 * @param output Output format
	 * @return New format
	 */
	public Format setOutputFormat(Format output) {
		outputFormat = output;
		return output;
	}

	/**
	 * Return the input format
	 * 
	 * @return Format
	 */
	public Format getInputFormat() {
		return inputFormat;
	}

	/**
	 * Return the output format
	 * 
	 * @return Format
	 */
	public Format getOutputFormat() {
		return outputFormat;
	}

	/**
	 * Reset the codec
	 */
	public void reset() {
	}

	/**
	 * Open the codec
	 */
	public void open() {
	}

	/**
	 * Close the codec
	 */
	public void close() {
	}

	/**
	 * Test if it's the end of media
	 * 
	 * @return Boolean
	 */
	protected boolean isEOM(Buffer inputBuffer) {
		return inputBuffer.isEOM();
	}

	/**
	 * Propagate EOM to the ouput buffer
	 * 
	 * @param outputBuffer Ouput buffer
	 */
	protected void propagateEOM(Buffer outputBuffer) {
		updateOutput(outputBuffer, getOutputFormat(), 0, 0);
		outputBuffer.setEOM(true);
	}

	/**
	 * Update the ouput buffer informations
	 * 
	 * @param outputBuffer Ouput buffer
	 * @param format Ouput format
	 * @param length Ouput length
	 * @param offset Ouput offset
	 */
	protected void updateOutput(Buffer outputBuffer, Format format, int length,
			int offset) {
		outputBuffer.setFormat(format);
		outputBuffer.setLength(length);
		outputBuffer.setOffset(offset);
	}

	/**
	 * Check the input buffer
	 * 
	 * @param inputBuffer Input buffer
	 * @return Boolean
	 */
	protected boolean checkInputBuffer(Buffer inputBuffer) {
		boolean fError = !isEOM(inputBuffer)
				&& (inputBuffer == null || inputBuffer.getFormat() == null);
		return !fError;
	}

	/**
	 * Validate that the Buffer's data size is at least newSize
	 * 
	 * @return Array with sufficient capacity
	 */
	protected byte[] validateByteArraySize(Buffer buffer, int newSize) {
		byte[] typedArray = (byte[]) buffer.getData();
		if (typedArray != null) {
			if (typedArray.length >= newSize) {
				return typedArray;
			}

			byte[] tempArray = new byte[newSize];
			System.arraycopy(typedArray, 0, tempArray, 0, typedArray.length);
			typedArray = tempArray;
		} else {
			typedArray = new byte[newSize];
		}

		buffer.setData(typedArray);
		return typedArray;
	}

	/**
	 * Performs the media processing defined by this codec
	 * 
	 * @param input The buffer that contains the media data to be processed
	 * @param output The buffer in which to store the processed media data
	 * @return Processing result
	 */
	public abstract int process(Buffer input, Buffer output);
}
