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

package com.orangelabs.rcs.core.ims.protocol.rtp.core;

/**
 * RTCP utils.
 *
 * @author hlxn7157
 */
public class RtcpPacketUtils {

    /**
     * Convert 64 bit long to n bytes.
     *
     * @param data data
     * @param n desired number of bytes to convert the long to.
     * @return buffer
     */
    public static byte[] longToBytes(long data, int n) {
        byte buf[] = new byte[n];
         for (int i = n - 1; i >= 0; i--) {
            buf[i] = (byte)data;
            data = data >> 8;
        }
        return buf;
    }

    /**
     * Append two byte arrays.
     *
     * @param pck1 first packet.
     * @param pck2 second packet.
     * @return concatenated packet.
     */
    public static byte[] append(byte[] pck1, byte[] pck2) {
        byte packet[] = new byte[pck1.length + pck2.length];
        for (int i = 0; i < pck1.length; i++)
            packet[i] = pck1[i];
        for (int i = 0; i < pck2.length; i++)
            packet[i + pck1.length] = pck2[i];
        return packet;
    }
};
