package de.kp.net.rtsp.client;

import java.net.URI;
import de.kp.net.rtsp.RtspConstants;
import de.kp.net.rtsp.client.api.RequestListener;
import de.kp.net.rtsp.client.api.Request;
import de.kp.net.rtsp.client.api.Response;
import de.kp.net.rtsp.client.message.RtspDescriptor;
import de.kp.net.rtsp.client.message.RtspMedia;
import de.kp.net.rtsp.client.transport.TCPTransport;

public class RtspControl implements RequestListener {

	// reference to the RTSP client
	private RtspClient client;

	// flag to indicate whether there is a connection
	// established to a remote RTSP server
	private boolean connected = false;

	// reference to the RTSP server URI
	private URI uri;

	private int port;

	private String resource;
	
	// reference to the SDP file returned as a response
	// to a DESCRIBE request
	private RtspDescriptor rtspDescriptor;

	private int state;

	/**
	 * This constructor is invoked with an uri that
	 * describes the server uri and also a certain
	 * resource
	 */

	public RtspControl(String uri) {	

		int pos = uri.lastIndexOf("/");

		try {

			this.uri      = new URI(uri.substring(0, pos));
			this.resource = uri.substring(pos+1);

			// initialize the RTSP communication
			this.client = new RtspClient();
			this.client.setTransport(new TCPTransport());
			
			this.client.setRequestListener(this);			
			this.state = RtspConstants.UNDEFINED;
			
			// the OPTIONS request is used to invoke and
			// test the connection to the RTSP server,
			// specified with the URI provided
			
			this.client.options("*", this.uri);

		} catch (Exception e) {
			
			if (this.client != null) {
				onError(this.client, e);
				
			} else {
				e.printStackTrace();
				
			}
			
		}
		
	}

	public RtspControl(String uri, String resource) {
				
		try {

			this.uri      = new URI(uri);
			this.resource = resource;

			// initialize the RTSP communication
			this.client = new RtspClient();
			this.client.setTransport(new TCPTransport());
			
			this.client.setRequestListener(this);
			
			this.state = RtspConstants.UNDEFINED;
			
			// the OPTIONS request is used to invoke and
			// test the connection to the RTSP server,
			// specified with the URI provided
			
			this.client.options("*", this.uri);

		} catch (Exception e) {
			
			if (this.client != null) {
				onError(this.client, e);
				
			} else {
				e.printStackTrace();
				
			}
			
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

	public boolean isConnected() {
		return this.connected;
	}
	
	public int getState() {
		return this.state;
	}
	
	public int getClientPort() {
		return this.port;
	}
	
	public RtspDescriptor getDescriptor() {
		return this.rtspDescriptor;
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

	// register SDP file
	public void onDescriptor(RtspClient client, String descriptor) {
		this.rtspDescriptor = new RtspDescriptor(descriptor);		
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
					this.client.describe(this.uri, this.resource);
				
				} else if (method == Request.Method.DESCRIBE) {
					
					// set state to INIT
					this.state = RtspConstants.INIT;
					
					/* 
					 * onSuccess is called AFTER onDescriptor method;
					 * this implies, that a media resource is present
					 * with a certain client port specified by the RTSP
					 * server
					 */
					
					RtspMedia video = this.rtspDescriptor.getFirstVideo();
					if (video != null) {
					
						this.port = Integer.valueOf(video.getTransportPort());
						
						// send SETUP request
						this.client.setup(this.uri, this.port, this.resource);
						
					}

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
