package de.kp.net.rtp.packetizer;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import android.os.SystemClock;
import android.util.Log;
import de.kp.net.rtp.RtpPacket;
import de.kp.net.rtp.RtpSender;
import de.kp.net.rtsp.RtspConstants;

public class H263Packetizer extends AbstractPacketizer implements Runnable {

	private String TAG = "H263Sender";

	private boolean videoQualityHigh = true;
	// private int fps;
	
	private boolean change;
	
	public H263Packetizer(InputStream fis) throws SocketException {
		this.fis = fis;
		this.rtpSender = RtpSender.getInstance(); 
	}
	
	public void run() {
		
		int frame_size = 1400;
		byte[] buffer = new byte[frame_size + 14];
		buffer[12] = 4;

		RtpPacket rtpPacket = new RtpPacket(buffer, 0);
		
		int seqn = 0;
		int num, number = 0, src, dest, len = 0, head = 0, lasthead = 0, lasthead2 = 0, cnt = 0, stable = 0;
		
		long now, lasttime = 0;
		
		double avgrate = videoQualityHigh ? 45000 : 24000;
		double avglen = avgrate / 20;

		rtpPacket.setPayloadType(RtspConstants.RTP_H263_PAYLOADTYPE);

		// while (Receiver.listener_video != null && videoValid()) {
		while (running) {

			num = -1;
			try {
				num = fis.read(buffer, 14 + number, frame_size - number);

			} catch (IOException e) {
				Log.w(TAG , e.getMessage());
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
						// fps = (int) ((double) cnt * 1000 / (now - lasttime));
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
				rtpPacket.setMarker(false);
			} else {
				num = 14 + number - num;
				rtpPacket.setMarker(true);
			}

			rtpPacket.setSequenceNumber(seqn++);
			rtpPacket.setPayloadLength(number - num + 2);
			
			if (seqn > 10)
				
				try {
					
					rtpSender.send(rtpPacket);
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
				rtpPacket.setTimestamp(SystemClock.elapsedRealtime() * 90);

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
}