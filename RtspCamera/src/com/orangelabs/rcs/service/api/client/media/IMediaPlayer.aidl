package com.orangelabs.rcs.service.api.client.media;

import com.orangelabs.rcs.service.api.client.media.IMediaEventListener;
import com.orangelabs.rcs.service.api.client.media.MediaCodec;

/**
 * Media RTP player
 */
interface IMediaPlayer {
	// Open the player
	void open(in String remoteHost, in int remotePort);

	// Close the player
	void close();

	// Start the player
	void start();

	// Stop the player
	void stop();

	// Returns the local RTP port
	int getLocalRtpPort();

	// Add a media listener
	void addListener(in IMediaEventListener listener);

	// Remove media listeners
	void removeAllListeners();

	// Get supported media codecs
	MediaCodec[] getSupportedMediaCodecs();

	// Get media codec
	MediaCodec getMediaCodec();

	// Set media codec
	void setMediaCodec(in MediaCodec mediaCodec);
}