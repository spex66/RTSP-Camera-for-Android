package de.kp.net;


/**
 * This is the most minimal viewer for RtspCamera app
 * 
 * @author Peter Arwanitis (arwanitis@dr-kruscheundpartner.de)
 *
 */
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class RtspViewerActivity extends Activity {
	/**
	 * hardcoded rtsp path	 
	 */
	private String rtsp = "rtsp://spexhd2:8080/kupandroid.3gp";
	private VideoView mVideoView;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.videoview);
		mVideoView = (VideoView) findViewById(R.id.surface_view);

		mVideoView.setVideoURI(Uri.parse(rtsp));
		mVideoView.setMediaController(new MediaController(this));
		mVideoView.requestFocus();
		
		// automatic start
		mVideoView.start();
	}
}
