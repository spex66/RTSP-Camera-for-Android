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

package de.kp.net.rtp.recorder;

import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.H263Config;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.encoder.NativeH263Encoder;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.encoder.NativeH263EncoderParams;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.H264Config;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.encoder.NativeH264Encoder;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.H263VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.H264VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaException;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaInput;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaSample;
import com.orangelabs.rcs.service.api.client.media.IMediaEventListener;
import com.orangelabs.rcs.service.api.client.media.IMediaPlayer;
import com.orangelabs.rcs.service.api.client.media.MediaCodec;
import com.orangelabs.rcs.service.api.client.media.video.VideoCodec;
import com.orangelabs.rcs.utils.FifoBuffer;
import com.orangelabs.rcs.utils.logger.Logger;

import android.hardware.Camera;
import android.os.SystemClock;
import android.util.Log;

import java.util.Vector;

/**
 * Live RTP video player. Supports only H.263 and H264 QCIF formats.
 */
public class RtspVideoRecorder extends IMediaPlayer.Stub implements Camera.PreviewCallback {

    /**
     * List of supported video codecs
     */
    public static MediaCodec[] supportedMediaCodecs = {
            new VideoCodec(H264Config.CODEC_NAME, H264VideoFormat.PAYLOAD, H264Config.CLOCK_RATE, H264Config.CODEC_PARAMS,
                    H264Config.FRAME_RATE, H264Config.BIT_RATE, H264Config.VIDEO_WIDTH,
                    H264Config.VIDEO_HEIGHT).getMediaCodec(),
            new VideoCodec(H263Config.CODEC_NAME, H263VideoFormat.PAYLOAD, H263Config.CLOCK_RATE, H263Config.CODEC_PARAMS,
                    H263Config.FRAME_RATE, H263Config.BIT_RATE, H263Config.VIDEO_WIDTH,
                    H263Config.VIDEO_HEIGHT).getMediaCodec()
    };

    /**
     * Selected video codec
     */
    private VideoCodec selectedVideoCodec = null;

    /**
     * Video format
     */
    private VideoFormat videoFormat;

    /**
     * Local RTP port
     */
    private int localRtpPort;

    /**
     * RTP sender session
     */
    private MediaRtpSender rtpMediaSender = null;

    /**
     * RTP media input
     */
    private MediaRtpInput rtpInput = null;

    /**
     * Last video frame
     */
    private CameraBuffer frameBuffer = null;

    /**
     * Is player opened
     */
    private boolean opened = false;

    /**
     * Is player started
     */
    private boolean started = false;

    /**
     * Video start time
     */
    private long videoStartTime = 0L;

    /**
     * Media event listeners
     */
    private Vector<IMediaEventListener> listeners = new Vector<IMediaEventListener>();

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

	private String TAG = "RtspVideoRecorder";

    /**
     * Constructor
     */
    public RtspVideoRecorder() {
    }

    /**
     * Constructor. Force a video codec.
     *
     * @param codec Video codec
     */
    public RtspVideoRecorder(VideoCodec codec) {
        // Set the media codec
        setMediaCodec(codec.getMediaCodec());
    }

    /**
     * Constructor. Force a video codec.
     *
     * @param codec Video codec name
     */
    public RtspVideoRecorder(String codec) {
        // Set the media codec
        for (int i = 0; i < supportedMediaCodecs.length ; i++) {
            if (codec.toLowerCase().contains(supportedMediaCodecs[i].getCodecName().toLowerCase())) {
                setMediaCodec(supportedMediaCodecs[i]);
                break;
            }
        }
    }

    /**
     * Returns the local RTP port
     *
     * @return Port
     */
    public int getLocalRtpPort() {
        return localRtpPort;
    }

    /**
     * Return the video start time
     *
     * @return Milliseconds
     */
    public long getVideoStartTime() {
        return videoStartTime;
    }

    /**
     * Is player opened
     *
     * @return Boolean
     */
    public boolean isOpened() {
        return opened;
    }

    /**
     * Is player started
     *
     * @return Boolean
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Open the player
     *
     * @param remoteHost Remote host
     * @param remotePort Remote port
     */
    public void open(String remoteHost, int remotePort) {
    	// This is an interface method, that is no longer
    	// used with the actual context
    }
    
    public void open() {

    	if (opened) {
            // Already opened
            return;
        }

        // Check video codec
        if (selectedVideoCodec == null) {
        	
            if (logger.isActivated()) {
                logger.debug("Player error: Video Codec not selected");
            }

            return;

        }

        // Init video encoder
        try {
            if (selectedVideoCodec.getCodecName().equalsIgnoreCase(H264Config.CODEC_NAME)) {
                // H264
                NativeH264Encoder.InitEncoder(selectedVideoCodec.getWidth(), selectedVideoCodec.getHeight(), selectedVideoCodec.getFramerate());

            } else if (selectedVideoCodec.getCodecName().equalsIgnoreCase(H263Config.CODEC_NAME)) {
                // Default H263
                NativeH263EncoderParams params = new NativeH263EncoderParams();
            
                params.setEncFrameRate(selectedVideoCodec.getFramerate());
                params.setBitRate(selectedVideoCodec.getBitrate());
                
                params.setTickPerSrc(params.getTimeIncRes() / selectedVideoCodec.getFramerate());
                params.setIntraPeriod(-1);
                params.setNoFrameSkipped(false);
                
                int result = NativeH263Encoder.InitEncoder(params);
                
                if (result != 1) {
                	
                    if (logger.isActivated()) {
                        logger.debug("Player error: Encoder init failed with error code " + result);
                    }

                    return;

                }
            }
        
        } catch (UnsatisfiedLinkError e) {

        	if (logger.isActivated()) {
                logger.debug("Player error: " + e.getMessage());
            }

            return;

        }

        // Init the RTP layer
        try {

        	rtpInput = new MediaRtpInput();
            rtpInput.open();
            
        	rtpMediaSender = new MediaRtpSender(videoFormat);            
            rtpMediaSender.prepareSession(rtpInput);
        
        } catch (Exception e) {
        	
            if (logger.isActivated()) {
                logger.debug("Player error: " + e.getMessage());
            }
        	
            return;
        }

        // Player is opened
        opened = true;

    }

    /**
     * Close the player
     */
    public void close() {
        if (!opened) {
            // Already closed
            return;
        }
        // Close the RTP layer
        rtpInput.close();
        rtpMediaSender.stopSession();

        try {
            // Close the video encoder
            if (selectedVideoCodec.getCodecName().equalsIgnoreCase(H264Config.CODEC_NAME)) {
                NativeH264Encoder.DeinitEncoder();

            } else if (selectedVideoCodec.getCodecName().equalsIgnoreCase(H263Config.CODEC_NAME)) {
                NativeH263Encoder.DeinitEncoder();
            }
        
        } catch (UnsatisfiedLinkError e) {
            if (logger.isActivated()) {
                logger.error("Can't close correctly the video encoder", e);
            }
        }

        // Player is closed
        opened = false;

    }

    /**
     * Start the player
     */
    public synchronized void start() {
		Log.d(TAG , "start");
   	
        if ((opened == false) || (started == true)) {
            return;
        }

        started = true;

        // Start RTP layer
        rtpMediaSender.startSession();
        
        // Start capture
        captureThread.start();

        // Player is started
        videoStartTime = SystemClock.uptimeMillis();

    }

    /**
     * Stop the player
     */
    public void stop() {
        
    	if ((opened == false) || (started == false)) { 
            return;
        }

        // Stop capture
        try {
            captureThread.interrupt();

        } catch (Exception e) {
        }

        // Player is stopped
        videoStartTime = 0L;
        started = false;

    }

    /**
     * Add a media event listener
     *
     * @param listener Media event listener
     */
    public void addListener(IMediaEventListener listener) {
        listeners.addElement(listener);
    }

    /**
     * Remove all media event listeners
     */
    public void removeAllListeners() {
        listeners.removeAllElements();
    }

    /**
     * Get supported media codecs
     *
     * @return media Codecs list
     */
    public MediaCodec[] getSupportedMediaCodecs() {
        return supportedMediaCodecs;
    }

    /**
     * Get media codec
     *
     * @return Media Codec
     */
    public MediaCodec getMediaCodec() {
        if (selectedVideoCodec == null)
            return null;
        else
            return selectedVideoCodec.getMediaCodec();
    }

    /**
     * Set media codec
     *
     * @param mediaCodec Media codec
     */
    public void setMediaCodec(MediaCodec mediaCodec) {
       
    	if (VideoCodec.checkVideoCodec(supportedMediaCodecs, new VideoCodec(mediaCodec))) {
        
    		selectedVideoCodec = new VideoCodec(mediaCodec);
            videoFormat = (VideoFormat) MediaRegistry.generateFormat(mediaCodec.getCodecName());

            // Initialize frame buffer
            if (frameBuffer == null) {
                frameBuffer = new CameraBuffer();
            }

        } else {

            if (logger.isActivated()) {
                logger.debug("Player error: Codec not supported");
            }

        }
    }

    /**
     * Preview frame from the camera
     *
     * @param data Frame
     * @param camera Camera
     */
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (frameBuffer != null)
            frameBuffer.setFrame(data);
    }

    /**
     * Camera buffer
     */
    private class CameraBuffer {
        /**
         * YUV frame where frame size is always (videoWidth*videoHeight*3)/2
         */
        private byte frame[] = new byte[(selectedVideoCodec.getWidth()
                * selectedVideoCodec.getHeight() * 3) / 2];

        /**
         * Set the last captured frame
         *
         * @param frame Frame
         */
        public void setFrame(byte[] frame) {
            this.frame = frame;
        }

        /**
         * Return the last captured frame
         *
         * @return Frame
         */
        public byte[] getFrame() {
            return frame;
        }
    }

    /**
     * Video capture thread
     */
    private Thread captureThread = new Thread() {
        /**
         * Timestamp
         */
        private long timeStamp = 0;

        /**
         * Processing
         */
        public void run() {
//            if (rtpInput == null) {
//                return;
//            }

            int timeToSleep = 1000 / selectedVideoCodec.getFramerate();
            int timestampInc = 90000 / selectedVideoCodec.getFramerate();
            byte[] frameData;
            byte[] encodedFrame;
            long encoderTs = 0;
            long oldTs = System.currentTimeMillis();

            while (started) {
                // Set timestamp
                long time = System.currentTimeMillis();
                encoderTs = encoderTs + (time - oldTs);

                // Get data to encode
                frameData = frameBuffer.getFrame();
                
                // Encode frame
                int encodeResult;
                if (selectedVideoCodec.getCodecName().equalsIgnoreCase(H264Config.CODEC_NAME)) {
                    encodedFrame = NativeH264Encoder.EncodeFrame(frameData, encoderTs);
                    encodeResult = NativeH264Encoder.getLastEncodeStatus();
                } else {
                    encodedFrame = NativeH263Encoder.EncodeFrame(frameData, encoderTs);
                    encodeResult = 0;
                }

                if (encodeResult == 0 && encodedFrame.length > 0) {
                    // Send encoded frame                	
                    rtpInput.addFrame(encodedFrame, timeStamp += timestampInc);
                }

                // Sleep between frames if necessary
                long delta = System.currentTimeMillis() - time;
                if (delta < timeToSleep) {
                    try {
                        Thread.sleep((timeToSleep - delta) - (((timeToSleep - delta) * 10) / 100));
                    } catch (InterruptedException e) {
                    }
                }

                // Update old timestamp
                oldTs = time;
            }
        }
    };

    /**
     * Media RTP input
     */
    private static class MediaRtpInput implements MediaInput {
        /**
         * Received frames
         */
        private FifoBuffer fifo = null;

        /**
         * Constructor
         */
        public MediaRtpInput() {
        }

        /**
         * Add a new video frame
         *
         * @param data Data
         * @param timestamp Timestamp
         */
        public void addFrame(byte[] data, long timestamp) {
            if (fifo != null) {
                fifo.addObject(new MediaSample(data, timestamp));
            }
        }

        /**
         * Open the player
         */
        public void open() {
            fifo = new FifoBuffer();
        }

        /**
         * Close the player
         */
        public void close() {
            if (fifo != null) {
                fifo.close();
                fifo = null;
            }
        }

        /**
         * Read a media sample (blocking method)
         *
         * @return Media sample
         * @throws MediaException
         */
        public MediaSample readSample() throws MediaException {
            try {
                if (fifo != null) {
                    return (MediaSample)fifo.getObject();
                } else {
                    throw new MediaException("Media input not opened");
                }
            } catch (Exception e) {
                throw new MediaException("Can't read media sample");
            }
        }
    }
}
