package de.kp.net;

import android.content.Context;
import android.net.Uri;
import android.widget.MediaController;
import android.widget.VideoView;

public class RtspClient {

	private static String RTSP = "rtsp://";
	
	public RtspClient() {
	}
	
	public void getRemoteVideo(Context context, String remoteAddress, int remotePort, String remoteDomain) {
	    
		VideoView videoView = new VideoView(context);

		// set video URI: a domain may be 'ames' or any other string
		Uri videoURI = Uri.parse(RTSP + remoteAddress + "/" + remotePort + "/" + remoteDomain);
		videoView.setVideoURI(videoURI);
				
		// set media controller
		MediaController mediaController = new MediaController(context);
		videoView.setMediaController(mediaController);
		
		//videoView.setOnErrorListener();
		
		videoView.requestFocus();
		videoView.start();
	}
			
} 