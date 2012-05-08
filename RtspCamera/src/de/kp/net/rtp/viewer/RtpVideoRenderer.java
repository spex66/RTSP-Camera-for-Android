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

package de.kp.net.rtp.viewer;

import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRegistry;
import com.orangelabs.rcs.core.ims.protocol.rtp.MediaRtpReceiver;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.H263Config;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h263.decoder.NativeH263Decoder;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.H264Config;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.decoder.NativeH264Decoder;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.H263VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.H264VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.format.video.VideoFormat;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaOutput;
import com.orangelabs.rcs.core.ims.protocol.rtp.media.MediaSample;
import com.orangelabs.rcs.platform.network.DatagramConnection;
import com.orangelabs.rcs.platform.network.NetworkFactory;
import com.orangelabs.rcs.service.api.client.media.IMediaEventListener;
import com.orangelabs.rcs.service.api.client.media.IMediaRenderer;
import com.orangelabs.rcs.service.api.client.media.MediaCodec;
import com.orangelabs.rcs.service.api.client.media.video.VideoCodec;
import com.orangelabs.rcs.service.api.client.media.video.VideoSurfaceView;
import com.orangelabs.rcs.utils.logger.Logger;

import de.kp.net.rtsp.RtspConstants;
import de.kp.net.rtsp.client.RtspControl;
import de.kp.net.rtsp.client.message.RtspDescriptor;
import de.kp.net.rtsp.client.message.RtspMedia;

import android.graphics.Bitmap;
import android.os.RemoteException;
import android.os.SystemClock;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

/**
 * Video RTP renderer. Supports only H.263 and H264 QCIF formats.
 *
 * @author jexa7410
 */
public class RtpVideoRenderer extends IMediaRenderer.Stub {

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
     * RTP receiver session
     */
    private MediaRtpReceiver rtpReceiver = null;

    /**
     * RTP media output
     */
    private MediaRtpOutput rtpOutput = null;

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
     * Video surface
     */
    private VideoSurfaceView surface = null;

    /**
     * Media event listeners
     */
    private Vector<IMediaEventListener> listeners = new Vector<IMediaEventListener>();

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Temporary connection to reserve the port
     */
    private DatagramConnection temporaryConnection = null;

    /**
     * RTSP Control
     */
    private RtspControl rtspControl;
    
    /**
     * Constructor Force a RTSP Server Uri
     * @throws Exception 
     */
    
    public RtpVideoRenderer(String uri) throws Exception {
        
        /*
         * The RtspControl opens a connection to an RtspServer, that
         * is determined by the URI provided.
         */
        rtspControl = new RtspControl(uri);    
        
        /*
         * wait unit the rtspControl has achieved status READY; in this 
         * state, an SDP file is present and is ready to get evaluated
         */
        while (rtspControl.getState() != RtspConstants.READY) {
        	; // blocking
        }

        /* 
         * Set the local RTP port: this is the (socket)
         * port, the RtspVideoRenderer is listening to
         * (UDP) RTP packets.
         */
        
    	// localRtpPort = NetworkRessourceManager.generateLocalRtpPort();
    	localRtpPort = rtspControl.getClientPort();
        reservePort(localRtpPort);

        /*
         * The media resources associated with the SDP descriptor are
         * evaluated and the respective video encoding determined
         */
        
        RtspDescriptor rtspDescriptor = rtspControl.getDescriptor();
        List<RtspMedia> mediaList = rtspDescriptor.getMediaList();
        
        if (mediaList.size() == 0) throw new Exception("The session description contains no media resource.");
        RtspMedia videoResource = null;
        
        for (RtspMedia mediaItem:mediaList) {
        	
        	if (mediaItem.getMediaType().equals(RtspConstants.SDP_VIDEO_TYPE)) {
        		videoResource = mediaItem;
        		break;
        	}
        	
        }
        
        if (videoResource == null) throw new Exception("The session description contains no video resource.");
        
        String codec = videoResource.getEncoding();
        if (codec == null) throw new Exception("No encoding provided for video resource.");
        
        // Set the media codec
        for (int i = 0; i < supportedMediaCodecs.length; i++) {
            if (codec.toLowerCase().contains(supportedMediaCodecs[i].getCodecName().toLowerCase())) {
                setMediaCodec(supportedMediaCodecs[i]);
                break;
            }
        }
        
    }
    
    /**
     * Set the surface to render video
     *
     * @param surface Video surface
     */
    public void setVideoSurface(VideoSurfaceView surface) {
        this.surface = surface;
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
     * Returns the local RTP port
     *
     * @return Port
     */
    public int getLocalRtpPort() {
        return localRtpPort;
    }

    /**
     * Reserve a port.
     *
     * @param port the port to reserve
     */
    private void reservePort(int port) {

    	if (temporaryConnection != null) return;
        try {
            temporaryConnection = NetworkFactory.getFactory().createDatagramConnection();
            temporaryConnection.open(port);

        } catch (IOException e) {
            temporaryConnection = null;
        }

    }

    /**
     * Release the reserved port; this method
     * is invoked while preparing the RTP layer
     */
    private void releasePort() {

    	if (temporaryConnection == null) return;
		try {
            temporaryConnection.close();
    
        } catch (IOException e) {
            temporaryConnection = null;
        }
        
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
     * Open the renderer
     */
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

        try {
            // Init the video decoder
            int result;
            if (selectedVideoCodec.getCodecName().equalsIgnoreCase(H264Config.CODEC_NAME)) {
                result = NativeH264Decoder.InitDecoder();
            
            } else { // default H263
                result = NativeH263Decoder.InitDecoder(selectedVideoCodec.getWidth(), selectedVideoCodec.getHeight());
            
            }
            
            if (result == 0) {
            	           	
                if (logger.isActivated()) {
                    logger.debug("Player error: Decoder init failed with error code " + result);
                }

                return;
            }
        
        } catch (UnsatisfiedLinkError e) {
	
            if (logger.isActivated()) {
                logger.debug("Player error: " + e.getMessage());
            }

            return;
        
        }

        try {

        	// initialize RTP layer
            
        	releasePort();

            rtpOutput = new MediaRtpOutput();
            rtpOutput.open();
            
            rtpReceiver = new MediaRtpReceiver(localRtpPort);
            rtpReceiver.prepareSession(rtpOutput, videoFormat);

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
     * Close the renderer
     */
    public void close() {

    	if (opened == false) return;

    	// Send TEARDOWN request to RTSP Server
    	rtspControl.stop();
    	
        // Close the RTP layer
        rtpReceiver.stopSession();
    	rtpOutput.close();

        // Close the video decoder
    	closeVideoDecoder();

        // Player is closed
        opened = false;

    }

    public void closeVideoDecoder() {

    	try {
            // Close the video decoder
            if (selectedVideoCodec.getCodecName().equalsIgnoreCase(H264Config.CODEC_NAME)) {
                NativeH264Decoder.DeinitDecoder();

            } else { // default H263
                NativeH263Decoder.DeinitDecoder();
            }
        
        } catch (UnsatisfiedLinkError e) {
            if (logger.isActivated()) {
                logger.error("Can't close correctly the video decoder", e);
            }
        
        }
    	
    }
    /**
     * Start the RTP layer (i.e listen to the reserved local
     * port for RTP packets), and send a PLAY request to the
     * RTSP server 
     */
    public void start() {

    	if ((opened == false) || (started == true)) {
            return;
        }
    	
        // Start RTP layer
        rtpReceiver.startSession();

        // Send PLAY request to RTSP Server
        rtspControl.play();
        
        /*
         * wait unit the rtspControl has achieved status PLAYING
         */
        while (rtspControl.getState() != RtspConstants.PLAYING) {
        	; // blocking
        }
        
        
        // Renderer is started
        videoStartTime = SystemClock.uptimeMillis();
        started = true;

    }

    /**
     * Stop the renderer
     */
    public void stop() {

    	if (started == false) return;

    	// Send TEARDOWN request to RTSP Server
    	rtspControl.stop();
 
        // Stop RTP layer
        if (rtpReceiver != null) rtpReceiver.stopSession();

        if (rtpOutput != null) rtpOutput.close();

        // Force black screen
    	surface.clearImage();

        // Close the video decoder
    	closeVideoDecoder();

        // Renderer is stopped
        started = false;
        videoStartTime = 0L;
    
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
     * @return Media codec
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
        
    	} else {
    		
            if (logger.isActivated()) {
                logger.debug("Player error: Codec not supported");
            }
        
    	}
    }

    /**
     * Media RTP output
     */
    private class MediaRtpOutput implements MediaOutput {
        /**
         * Video frame
         */
        private int decodedFrame[];

        /**
         * Bitmap frame
         */
        private Bitmap rgbFrame;

        /**
         * Constructor
         */
        public MediaRtpOutput() {
            
        	decodedFrame = new int[selectedVideoCodec.getWidth() * selectedVideoCodec.getHeight()];
            rgbFrame     = Bitmap.createBitmap(selectedVideoCodec.getWidth(), selectedVideoCodec.getHeight(), Bitmap.Config.RGB_565);
        
        }

        /**
         * Open the renderer
         */
        public void open() {
        }

        /**
         * Close the renderer
         */
        public void close() {
        }

        /**
         * Write a media sample
         *
         * @param sample Sample
         */
		public void writeSample(MediaSample sample) {
			if (selectedVideoCodec.getCodecName().equalsIgnoreCase(H264Config.CODEC_NAME)) {
				if (NativeH264Decoder.DecodeAndConvert(sample.getData(), decodedFrame) == 1) {
					rgbFrame.setPixels(decodedFrame, 0, selectedVideoCodec.getWidth(), 0, 0,
							selectedVideoCodec.getWidth(), selectedVideoCodec.getHeight());

					if (surface != null) {
						surface.setImage(rgbFrame);
					}
				} else {
					System.out.println("MediaRtpOutput.writeSample: cannot decode sample >len:" + sample.getLength());
				}
			} else { // default H263
				if (NativeH263Decoder.DecodeAndConvert(sample.getData(), decodedFrame, sample.getTimeStamp()) == 1) {
					rgbFrame.setPixels(decodedFrame, 0, selectedVideoCodec.getWidth(), 0, 0,
							selectedVideoCodec.getWidth(), selectedVideoCodec.getHeight());
					if (surface != null) {
						surface.setImage(rgbFrame);
					}
				}
			}
		}
    }

	@Override
	public void open(String remoteHost, int remotePort) throws RemoteException {
	}

}

