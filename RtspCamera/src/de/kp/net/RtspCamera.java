package de.kp.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import android.app.Activity;
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

import de.kp.net.rtp.RtpSender;
import de.kp.net.rtp.packetizer.AbstractPacketizer;
import de.kp.net.rtp.packetizer.H263Packetizer;
import de.kp.net.rtp.packetizer.H264Packetizer;
import de.kp.net.rtsp.RtspConstants;
import de.kp.net.rtsp.RtspServer;

public class RtspCamera extends Activity implements OnClickListener, SurfaceHolder.Callback, MediaRecorder.OnErrorListener {

	private String TAG = "RTSPCamera";

	// default RTSP port is 554
	private int SERVER_PORT = 8080;
	
	private SurfaceView mVideoPreview;
	private SurfaceHolder mSurfaceHolder;
	
	// these parameters are used to separate between incoming
	// and outgoing streams
	private LocalServerSocket localSocketServer;
	private LocalSocket receiver;
	private LocalSocket sender;
	
	private MediaRecorder mMediaRecorder;
	
	private boolean mMediaRecorderRecording = false;
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

		// starts the RTSP Server

		try {
		
			// initialize video encoder to be used
			// for SDP file generation
			RtspConstants.VideoEncoder rtspVideoEncoder = (MediaConstants.H264_CODEC == true) ? RtspConstants.VideoEncoder.H264_ENCODER : RtspConstants.VideoEncoder.H263_ENCODER;
			
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
		receiver = new LocalSocket();
		try {
			
			localSocketServer = new LocalServerSocket("camera2rtsp");

			// InputStream the RTPPackets can be built from
			receiver.connect(new LocalSocketAddress("camera2rtsp"));
			receiver.setReceiveBufferSize(500000);
			receiver.setSendBufferSize(500000);

			// FileDescriptor the Camera can send to
			sender = localSocketServer.accept();
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
		
		mMediaRecorder.setVideoEncoder(getMediaEncoder());		
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

		try {
		
			// actually H263 over RTP and H264 over RTP is supported
			if (MediaConstants.H264_CODEC == true) {
				videoPacketizer = new H264Packetizer(fis);
			
			} else {
				videoPacketizer = new H263Packetizer(fis);
				
			}

			videoPacketizer.startStreaming();

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unused")
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

	private int getMediaEncoder() {	
		if (MediaConstants.H264_CODEC == true) 
			return MediaRecorder.VideoEncoder.H264;		
		return MediaRecorder.VideoEncoder.H263;
	}

}
