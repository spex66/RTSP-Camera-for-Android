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

package com.orangelabs.rcs.service.api.client.media;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/**
 * Media Codec
 * 
 * @author hlxn7157
 */
public class MediaCodec implements Parcelable {
    /**
     * Codec name
     */
    private String codecName;

    /**
     * Codec parameters
     */
    private Hashtable<String, String> parameters = new Hashtable<String, String>();

    /**
     * Constructor
     * 
     * @param codecName Codec name
     */
    public MediaCodec(String codecName) {
        this.codecName = codecName;
    }

    /**
     * Constructor
     * 
     * @param source Parcelable source
     */
    public MediaCodec(Parcel source) {
        this.codecName = source.readString();

        Bundle parametersBundle = source.readBundle();
        Set<String> keys = parametersBundle.keySet();
        Iterator<String> i = keys.iterator();
        while (i.hasNext()) {
            String key = i.next().toString();
            String value = parametersBundle.getString(key);
            this.parameters.put(key, value);
        }
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation
     * 
     * @return Integer
     */
    public int describeContents() {
        return 0;
    }

    /**
     * Write parcelable object
     * 
     * @param dest The Parcel in which the object should be written
     * @param flags Additional flags about how the object should be written
     */
    public void writeToParcel(Parcel dest, int flags) {
        Bundle parametersBundle = new Bundle();
        Enumeration<String> e = parameters.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement().toString();
            parametersBundle.putString(key, parameters.get(key));
        }

        dest.writeString(codecName);
        dest.writeBundle(parametersBundle);
    }

    /**
     * Parcelable creator
     */
    public static final Parcelable.Creator<MediaCodec> CREATOR = new Parcelable.Creator<MediaCodec>() {
        public MediaCodec createFromParcel(Parcel source) {
            return new MediaCodec(source);
        }

        public MediaCodec[] newArray(int size) {
            return new MediaCodec[size];
        }
    };

    /**
     * Get codec name
     * 
     * @return Codec name
     */
    public String getCodecName() {
        return codecName;
    }

    /**
     * Set codec name
     * 
     * @param codecName Codec name
     */
    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    /**
     * Get a codec parameter as string
     * 
     * @param key Parameter key
     * @return Parameter value
     */
    public String getStringParam(String key) {
        if (key != null) {
            return parameters.get(key);
        } else {
            return null;
        }
    }

    /**
     * Get a codec parameter as integer
     * 
     * @param key Parameter key
     * @param defaultValue default value
     * @return Parameter value
     */
    public int getIntParam(String key, int defaultValue) {
        String value = getStringParam(key);
        try {
            return Integer.parseInt(value);
        } catch(Exception e) {
            return defaultValue;
        }
    }

    /**
     * Set a codec parameter
     * 
     * @param key Parameter key
     * @param value Parameter value
     */
    public void setParam(String key, String value) {
        parameters.put(key, value);
    }
}
