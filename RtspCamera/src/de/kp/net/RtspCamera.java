package de.kp.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import android.app.Activity;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import de.kp.net.rtp.AbstractPacketizer;
import de.kp.net.rtp.H264Packetizer;
import de.kp.net.rtp.RtpSender;
import de.kp.net.rtsp.RtspConstants;
import de.kp.net.rtsp.RtspServer;

public class RtspCamera extends Activity implements OnClickListener, SurfaceHolder.Callback,
		MediaRecorder.OnErrorListener {
	private SurfaceView mVideoPreview;
	private SurfaceHolder mSurfaceHolder;
	private LocalServerSocket lss;
	private LocalSocket receiver;
	private LocalSocket sender;
	private MediaRecorder mMediaRecorder;
	private boolean mMediaRecorderRecording = false;
	private String TAG = "RTSPStreamer";
	private Camera mCamera;
	protected boolean videoQualityHigh = false;
	private RtpSender rtpSender;
	private RtspServer streamer = null;
	private AbstractPacketizer videoPacketizer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "onCreate");

		setContentView(R.layout.main);

		// get the reference
		rtpSender = RtpSender.getInstance();

		/*
		 * Video preview initialization
		 */
		mVideoPreview = (SurfaceView) findViewById(R.id.smallcameraview);
		SurfaceHolder holder = mVideoPreview.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	public void onResume() {
		Log.d(TAG, "onResume");

		/*
		 * Starts the RTSP Server
		 */
		try {
			if (streamer == null) {
				// default RTSP port is 554
				streamer = new RtspServer(8080);
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
		receiver = new LocalSocket();
		try {
			lss = new LocalServerSocket("camera2rtsp");

			// InputStream the RTPPackets can be built from
			receiver.connect(new LocalSocketAddress("camera2rtsp"));
			receiver.setReceiveBufferSize(500000);
			receiver.setSendBufferSize(500000);

			// FileDescriptor the Camera can send to
			sender = lss.accept();
			sender.setReceiveBufferSize(500000);
			sender.setSendBufferSize(500000);

		} catch (IOException e1) {
			e1.printStackTrace();
			super.onResume();
			finish();
			return;
		}

		mVideoPreview.setVisibility(View.VISIBLE);

		initializeVideo();

		startVideoRecording();
		super.onResume();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder
	 * )
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		mSurfaceHolder = holder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder
	 * , int, int, int)
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (!mMediaRecorderRecording)
			initializeVideo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.
	 * SurfaceHolder)
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		mSurfaceHolder = null;

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	// initializeVideo() starts preview and prepare media recorder.
	// Returns false if initializeVideo fails
	private boolean initializeVideo() {
		Log.d(TAG, "initializeVideo: " + mMediaRecorderRecording);

		if (mMediaRecorderRecording) {
			Log.v(TAG, "initialized Video");
			return true;
		}

		if (mSurfaceHolder == null) {
			Log.v(TAG, "SurfaceHolder is null");
			return false;
		}

		mMediaRecorderRecording = true;

		Log.v(TAG, "initializeVideo set to true: " + mMediaRecorderRecording);

		if (mMediaRecorder == null)
			mMediaRecorder = new MediaRecorder();
		else
			mMediaRecorder.reset();

		if (mCamera != null) {
			// if (Integer.parseInt(Build.VERSION.SDK) >= 8)
			// VideoCameraNew2.reconnect(mCamera);
			try {
				mCamera.reconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			mCamera.release();
			mCamera = null;

		}

		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

		// route video to LocalSocket
		mMediaRecorder.setOutputFile(sender.getFileDescriptor());

		// Use the same frame rate for both, since internally
		// if the frame rate is too large, it can cause camera to become
		// unstable. We need to fix the MediaRecorder to disable the support
		// of setting frame rate for now.
		
		mMediaRecorder.setVideoFrameRate(RtspConstants.FPS);
//		mMediaRecorder.setVideoEncodingBitRate(RtspConstants.BITRATE);
		
		mMediaRecorder.setVideoSize(Integer.valueOf(RtspConstants.WIDTH), Integer.valueOf(RtspConstants.HEIGHT));
//		 mMediaRecorder.setVideoSize(176, 144);
		
		if (MediaConstants.H264_CODEC) 
			mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		else
			mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
		
		mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

		try {

			mMediaRecorder.prepare();

			mMediaRecorder.setOnErrorListener(this);
			mMediaRecorder.start();

		} catch (IOException exception) {

			releaseMediaRecorder();
			return false;
		}
		return true;
	}

	public void onError(MediaRecorder mr, int what, int extra) {
		// MediaRecorder or MediaPlayer error
		rtpSender.stop();
	}

	private void startVideoRecording() {

		Log.v(TAG, "startVideoRecording");
		
		InputStream fis = null;
		try {
			fis = receiver.getInputStream();

		} catch (IOException e1) {

			Log.w(TAG, "No receiver input stream");
			return;
		}
		boolean videoQualityHigh = true;
		try {
			
			videoPacketizer = new H264Packetizer(fis);
			videoPacketizer.startStreaming();

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void stopVideoRecording() {

		Log.d(TAG, "stopVideoRecording");

		if (mMediaRecorderRecording || mMediaRecorder != null) {

			// stop thread
			videoPacketizer.stopStreaming();
			// t.interrupt();

			if (mMediaRecorderRecording && mMediaRecorder != null) {
				try {
					mMediaRecorder.setOnErrorListener(null);
					mMediaRecorder.setOnInfoListener(null);
					mMediaRecorder.stop();
				} catch (RuntimeException e) {
					Log.e(TAG, "stop fail: " + e.getMessage());
				}

				mMediaRecorderRecording = false;
			}
			releaseMediaRecorder();
		}
	}

	private void releaseMediaRecorder() {

		Log.d(TAG, "Releasing media recorder.");
		if (mMediaRecorder != null) {
			mMediaRecorder.reset();
			if (mCamera != null) {
				// if (Integer.parseInt(Build.VERSION.SDK) >= 8)
				try {
					mCamera.reconnect();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				mCamera.release();
				mCamera = null;
			}
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}

}
