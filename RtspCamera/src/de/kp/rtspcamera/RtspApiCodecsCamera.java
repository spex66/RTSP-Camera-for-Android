package de.kp.rtspcamera;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import android.app.Activity;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import de.kp.net.rtp.RtpSender;
import de.kp.net.rtp.packetizer.AbstractPacketizer;
import de.kp.net.rtp.packetizer.H263Packetizer;
import de.kp.net.rtp.packetizer.H264Packetizer;
import de.kp.net.rtsp.RtspConstants;
import de.kp.net.rtsp.server.RtspServer;

public class RtspApiCodecsCamera extends Activity {

	private String TAG = "RTSPCamera";

	// default RTSP command port is 554
	private int SERVER_PORT = 8080;

	private SurfaceView mVideoPreview;
	private SurfaceHolder mSurfaceHolder;

	// these parameters are used to separate between incoming
	// and outgoing streams
	private LocalServerSocket localSocketServer;
	private LocalSocket receiver;
	private LocalSocket sender;

	private MediaRecorder mediaRecorder;

	private boolean mediaRecorderRecording = false;
	protected boolean videoQualityHigh = false;

	private RtpSender rtpSender;
	private RtspServer streamer = null;
	
	private AbstractPacketizer videoPacketizer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "onCreate");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = getWindow();

        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);		
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 

		setContentView(R.layout.cameraapicodecs);

		// hold the reference
		rtpSender = RtpSender.getInstance();

		/*
		 * Video preview initialization
		 */
		mVideoPreview = (SurfaceView) findViewById(R.id.smallcameraview);
		mSurfaceHolder = mVideoPreview.getHolder();
		mSurfaceHolder.addCallback(surfaceCallback);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
	}

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
	 * MediaRecorder listener
	 */
	private MediaRecorder.OnErrorListener mErrorListener = new MediaRecorder.OnErrorListener() {
		
		public void onError(MediaRecorder mr, int what, int extra) {
			// MediaRecorder or MediaPlayer error
			rtpSender.stop();
		}
	};

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

			initializeVideo();
			startVideoRecording();
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

			stopVideoRecording();

		}
	};

	// initializeVideo() starts preview and prepare media recorder.
	// Returns false if initializeVideo fails
	private void initializeVideo() {
		Log.d(TAG, "initializeVideo: " + mediaRecorderRecording);

		mediaRecorderRecording = true;

		Log.v(TAG, "initializeVideo set to true: " + mediaRecorderRecording);

		if (mediaRecorder == null)
			mediaRecorder = new MediaRecorder();
		else
			mediaRecorder.reset();


		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

		// route video to LocalSocket
		mediaRecorder.setOutputFile(sender.getFileDescriptor());

		// Use the same frame rate for both, since internally
		// if the frame rate is too large, it can cause camera to become
		// unstable. We need to fix the MediaRecorder to disable the support
		// of setting frame rate for now.

		mediaRecorder.setVideoFrameRate(RtspConstants.FPS);
		// mMediaRecorder.setVideoEncodingBitRate(RtspConstants.BITRATE);

		mediaRecorder.setVideoSize(Integer.valueOf(RtspConstants.WIDTH), Integer.valueOf(RtspConstants.HEIGHT));

		mediaRecorder.setVideoEncoder(getMediaEncoder());
		mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

		try {

			mediaRecorder.prepare();
			mediaRecorder.setOnErrorListener(mErrorListener);
			mediaRecorder.start();

		} catch (IOException exception) {
			exception.printStackTrace();
			releaseMediaRecorder();
		}
	}

	private int getMediaEncoder() {
		if (MediaConstants.H264_CODEC == true)
			return MediaRecorder.VideoEncoder.H264;
		return MediaRecorder.VideoEncoder.H263;
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

	 private void stopVideoRecording() {

		  Log.d(TAG, "stopVideoRecording");

		  if (mediaRecorderRecording || mediaRecorder != null) {

		   try {

		    // stop thread
		    videoPacketizer.stopStreaming();

		    if (mediaRecorderRecording && mediaRecorder != null) {
		     try {
		      mediaRecorder.setOnErrorListener(null);
		      mediaRecorder.setOnInfoListener(null);
		      mediaRecorder.stop();
		     } catch (RuntimeException e) {
		      Log.e(TAG, "stop fail: " + e.getMessage());
		     }

		     mediaRecorderRecording = false;
		    }
		   } catch (Exception e) {
		    Log.e(TAG, "stopVideoRecording failed");
		    
		    e.printStackTrace();
		   } finally {
		    releaseMediaRecorder();
		   }
		  }
		 }
	private void releaseMediaRecorder() {

		Log.d(TAG, "Releasing media recorder.");
		if (mediaRecorder != null) {
			mediaRecorder.reset();
			mediaRecorder.release();
			mediaRecorder = null;
		}
	}

}
