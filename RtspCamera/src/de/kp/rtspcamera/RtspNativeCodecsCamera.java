package de.kp.rtspcamera;

import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.orangelabs.rcs.service.api.client.media.video.LiveVideoPlayer;

import de.kp.net.rtsp.RtspConstants;
import de.kp.net.rtsp.server.RtspServer;

public class RtspNativeCodecsCamera extends Activity {

	private String TAG = "RTSPNativeCamera";

	// default RTSP command port is 554
	private int SERVER_PORT = 8080;

	private LiveVideoPlayer outgoingPlayer;

	private SurfaceView mCameraPreview;
	private SurfaceHolder previewHolder;

	private Camera camera;

	private boolean inPreview = false;
	private boolean cameraConfigured = false;

	private int mPreviewWidth = Integer.valueOf(RtspConstants.WIDTH);
	private int mPreviewHeight = Integer.valueOf(RtspConstants.HEIGHT);

	private RtspServer streamer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "onCreate");

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.cameranativecodecs);

		/*
		 * Camera preview initialization
		 */
		mCameraPreview = (SurfaceView) findViewById(R.id.smallcameraview);
		previewHolder = mCameraPreview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		outgoingPlayer = new LiveVideoPlayer("h263-2000");
		outgoingPlayer.open();
		

	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
		

		// starts the RTSP Server

		try {

			// initialize video encoder to be used
			// for SDP file generation
			RtspConstants.VideoEncoder rtspVideoEncoder = (MediaConstants.H264_CODEC == true) ? RtspConstants.VideoEncoder.H264_ENCODER
					: RtspConstants.VideoEncoder.H263_ENCODER;

			if (streamer == null) {
				streamer = new RtspServer(SERVER_PORT, rtspVideoEncoder);
				new Thread(streamer).start();
			}

			Log.d(TAG, "RtspServer started");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * Camera initialization
		 */
		camera = Camera.open();

		super.onResume();

	}

	@Override
	public void onPause() {

		// stop RTSP server
		if (streamer != null)
			streamer.stop();
		streamer = null;

		super.onPause();
	}

	/*
	 * SurfaceHolder callback triple
	 */
	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		/*
		 * Created state: - Open camera - initial call to startPreview() - hook
		 * PreviewCallback() on it, which notifies waiting thread with new
		 * preview data - start thread
		 * 
		 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.
		 * SurfaceHolder )
		 */
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "surfaceCreated");

		}

		/*
		 * Changed state: - initiate camera preview size, set
		 * camera.setPreviewDisplay(holder) - subsequent call to startPreview()
		 * 
		 * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.
		 * SurfaceHolder , int, int, int)
		 */
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			Log.d(TAG, "surfaceChanged");
			initializePreview(w, h);
			startPreview();
		}

		/*
		 * Destroy State: Take care on release of camera
		 * 
		 * @see
		 * android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.
		 * SurfaceHolder)
		 */
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG, "surfaceDestroyed");

			if (inPreview) {
				camera.stopPreview();
			}
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
			
			// stop captureThread
			outgoingPlayer.stop();


			inPreview = false;
			cameraConfigured = false;

		}
	};


	/**
	 * This method checks availability of camera and preview
	 * 
	 * @param width
	 * @param height
	 */
	private void initializePreview(int width, int height) {
		Log.d(TAG, "initializePreview");

		if (camera != null && previewHolder.getSurface() != null) {
			try {
				// provide SurfaceView for camera preview
				camera.setPreviewDisplay(previewHolder);

			} catch (Throwable t) {
				Log.e(TAG, "Exception in setPreviewDisplay()", t);
			}

			if (!cameraConfigured) {

				Camera.Parameters parameters = camera.getParameters();
				parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);

				camera.setParameters(parameters);
				cameraConfigured = true;

			}
		}
	}

	private void startPreview() {
		Log.d(TAG, "startPreview");

		if (cameraConfigured && camera != null) {

			// activate onPreviewFrame()
			// camera.setPreviewCallback(cameraPreviewCallback);
			camera.setPreviewCallback(outgoingPlayer);
			
			// start captureThread
			outgoingPlayer.start();

			camera.startPreview();
			inPreview = true;

		}
	}


	
	public boolean isReady() {
		return this.inPreview;
	}

}
