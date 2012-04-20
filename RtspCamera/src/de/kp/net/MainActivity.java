package de.kp.net;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import de.kp.net.rtp.RtpPacket;
import de.kp.net.rtp.RtpSender;
import de.kp.net.rtsp.RtspConstants;
import de.kp.net.rtsp.RtspServer;

public class MainActivity extends Activity implements OnClickListener, SurfaceHolder.Callback,
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
	private Thread t;
	protected boolean videoQualityHigh = false;
	private RtpSender rtpSender;
	private RtspServer streamer = null;

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
		mMediaRecorder.setVideoFrameRate(20);
		mMediaRecorder.setVideoSize(352, 288);
		// mMediaRecorder.setVideoSize(176, 144);

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

		(t = new Thread() {

			private int fps;
			private boolean change;

			public void run() {

				int frame_size = 1400;
				byte[] buffer = new byte[frame_size + 14];
				buffer[12] = 4;

				RtpPacket rtp_packet = new RtpPacket(buffer, 0);
				int seqn = 0;
				int num, number = 0, src, dest, len = 0, head = 0, lasthead = 0, lasthead2 = 0, cnt = 0, stable = 0;
				long now, lasttime = 0;
				double avgrate = videoQualityHigh ? 45000 : 24000;
				double avglen = avgrate / 20;

				InputStream fis = null;
				try {
					fis = receiver.getInputStream();

				} catch (IOException e1) {

					Log.w(TAG, "No receiver input stream");
					return;
				}

				rtp_packet.setPayloadType(RtspConstants.RTP_PAYLOADTYPE);

				// while (Receiver.listener_video != null && videoValid()) {
				while (true) {
					num = -1;
					try {
						num = fis.read(buffer, 14 + number, frame_size - number);

					} catch (IOException e) {
						Log.w(TAG, e.getMessage());
						break;
					}

					if (num < 0) {
						try {
							sleep(20);
						} catch (InterruptedException e) {
							break;
						}
						continue;
					}
					number += num;
					head += num;
					try {
						now = SystemClock.elapsedRealtime();
						if (lasthead != head + fis.available() && ++stable >= 5 && now - lasttime > 700) {
							if (cnt != 0 && len != 0)
								avglen = len / cnt;
							if (lasttime != 0) {
								fps = (int) ((double) cnt * 1000 / (now - lasttime));
								avgrate = (double) ((head + fis.available()) - lasthead2) * 1000 / (now - lasttime);
							}
							lasttime = now;
							lasthead = head + fis.available();
							lasthead2 = head;
							len = cnt = stable = 0;
						}
					} catch (IOException e1) {
						Log.w(TAG, e1.getMessage());
						break;
					}

					for (num = 14; num <= 14 + number - 2; num++)
						if (buffer[num] == 0 && buffer[num + 1] == 0)
							break;
					if (num > 14 + number - 2) {
						num = 0;
						rtp_packet.setMarker(false);
					} else {
						num = 14 + number - num;
						rtp_packet.setMarker(true);
					}

					rtp_packet.setSequenceNumber(seqn++);
					rtp_packet.setPayloadLength(number - num + 2);
					if (seqn > 10)
						try {

//							if (rtpSender.getReceiverCount() != 0) {
//								Log.d(TAG, "RTP packet sent to RtpSender: " + rtpSender.getReceiverCount());
//							}
							
							rtpSender.send(rtp_packet);
							len += number - num;

						} catch (IOException e) {
							Log.w(TAG, "RTP packet sent failed");
							break;
						}

					if (num > 0) {
						num -= 2;
						dest = 14;
						src = 14 + number - num;
						if (num > 0 && buffer[src] == 0) {
							src++;
							num--;
						}
						number = num;
						while (num-- > 0)
							buffer[dest++] = buffer[src++];
						buffer[12] = 4;

						cnt++;
						try {
							if (avgrate != 0)
								Thread.sleep((int) (avglen / avgrate * 1000));
						} catch (Exception e) {
							break;
						}
						rtp_packet.setTimestamp(SystemClock.elapsedRealtime() * 90);

					} else {
						number = 0;
						buffer[12] = 0;
					}
					if (change) {
						change = false;
						long time = SystemClock.elapsedRealtime();

						try {
							while (fis.read(buffer, 14, frame_size) > 0 && SystemClock.elapsedRealtime() - time < 3000)
								;
						} catch (Exception e) {
						}
						number = 0;
						buffer[12] = 0;
					}
				}

				rtpSender.stop();

				try {
					while (fis.read(buffer, 0, frame_size) > 0)
						;
				} catch (IOException e) {
				}
			}

		}).start();

	}

	private void stopVideoRecording() {

		Log.d(TAG, "stopVideoRecording");

		if (mMediaRecorderRecording || mMediaRecorder != null) {

			// stop thread
			t.interrupt();

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
