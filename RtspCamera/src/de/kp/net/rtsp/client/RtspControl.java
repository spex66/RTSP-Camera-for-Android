package de.kp.net.rtsp.client;

import java.net.URI;

import de.kp.net.rtsp.RtspConstants;
import de.kp.net.rtsp.client.api.RequestListener;
import de.kp.net.rtsp.client.api.Request;
import de.kp.net.rtsp.client.api.Response;
import de.kp.net.rtsp.client.message.RtspDescriptor;

public class RtspControl implements RequestListener {

	// reference to the RTSP client
	private RtspClient client;

	// flag to indicate whether there is a connection
	// established to a remote RTSP server
	private boolean connected = false;

	// reference to the RTSP server URI
	private URI uri;

	private int clientPort;

	private String resource;
	
	// reference to the SDP file returned as a response
	// to a DESCRIBE request
	
	private String descriptor;
	
	private int state;
	
	public RtspControl(URI serverUri, int clientPort, String resource) {
				
		// register RTSP server uri
		this.uri = serverUri;
		
		// register client port where to receive RTP packets
		this.clientPort = clientPort;
				
		// register resource
		this.resource = resource;
		
		// initialize the RTSP communication
		this.client = new RtspClient();
		this.client.setRequestListener(this);
		
		this.state = RtspConstants.UNDEFINED;
		
		try {
			
			// the OPTIONS request is used to invoke and
			// test the connection to the RTSP server,
			// specified with the URI provided
			
			this.client.options("*", uri);

		} catch (Exception e) {
			onError(this.client, e);
		}

	}
	
	public void play() {

		if ((this.client == null) || (this.connected == false)) return;

		if (this.state == RtspConstants.READY) {
			this.client.play();		
		}
	
	}
	
	public void pause() {

		if ((this.client == null) || (this.connected == false)) return;

		if (this.state == RtspConstants.PLAYING) {
			this.client.pause();		
		}

	}
	
	public void stop() {
		
		if ((this.client == null) || (this.connected == false)) return;
		
		// send TEARDOWN request
		this.client.teardown();
		
	}

	public RtspDescriptor getDescriptor() {
		return new RtspDescriptor(this.descriptor);
	}
	
	@Override
	public void onError(RtspClient client, Throwable error) {

		if ((this.client != null) && (this.connected == true)) {
			this.client.teardown();
		}
 		
		this.state = RtspConstants.UNDEFINED;
		this.connected = false;
		
		this.client = null;
		
	}

	@Override
	public void onDescriptor(RtspClient client, String descriptor) {

		// register SDP file
		this.descriptor = descriptor;
		
	}

	public void onFailure(RtspClient client, Request request, Throwable cause) {

		if ((this.client != null) && (this.connected == true)) {
			this.client.teardown();
		}
 		
		this.state = RtspConstants.UNDEFINED;
		this.connected = false;
		
		this.client = null;
		
	}

	public void onSuccess(RtspClient client, Request request, Response response) {

		try {

			if ((this.client != null) && (response.getStatusCode() == 200)) {
				
				Request.Method method = request.getMethod();
				if (method == Request.Method.OPTIONS) {
					// the response to an OPTIONS request
					this.connected = true;
					
					// send DESCRIBE request
					this.client.describe(this.uri);
				
				} else if (method == Request.Method.DESCRIBE) {
					
					// set state to INIT
					this.state = RtspConstants.INIT;
					
					// send SETUP request
					this.client.setup(uri, clientPort, resource);

				} else if (method == Request.Method.SETUP) {
					
					// set state to READY
					this.state = RtspConstants.READY;

				} else if (method == Request.Method.PLAY) {
					
					// set state to PLAYING
					this.state = RtspConstants.PLAYING;

				} else if (method == Request.Method.PAUSE) {
					
					// set state to READY
					this.state = RtspConstants.READY;

				} else if (method == Request.Method.TEARDOWN) {

					this.connected = false;
					
					// set state to UNDEFINED
					this.state = RtspConstants.UNDEFINED;

				}
			
			} else {
				
			}
			
		} catch (Exception e) {
			onError(this.client, e);
			
		}
		
	}
}
