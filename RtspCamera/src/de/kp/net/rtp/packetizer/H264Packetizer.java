package de.kp.net.rtp.packetizer;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import de.kp.net.rtp.RtpPacket;
import de.kp.net.rtp.RtpSender;
import de.kp.net.rtsp.RtspConstants;

import android.os.SystemClock;
import android.util.Log;

public class H264Packetizer extends AbstractPacketizer implements Runnable {

	private final int packetSize = 1400;
	
	private long oldtime = SystemClock.elapsedRealtime(), delay = 20;
	private long latency, oldlat = oldtime;
	
	private int available = 0, oldavailable = 0, nalUnitLength = 0, numberNalUnit = 0, len = 0;
	private H264Fifo fifo = new H264Fifo(500000);

	protected InputStream fis = null;

	protected byte[] buffer = new byte[16384 * 2];

	protected final int rtpHeaderLength = 12; // Rtp header length
	private String TAG = "H264Packetizer";

	public H264Packetizer(InputStream fis) throws SocketException {
		this.fis = fis;
		this.rtpSender = RtpSender.getInstance();
	}

	public void run() {

		int seqn = 0;
		byte[] buffer = new byte[16384*2];

		RtpPacket rtpPacket = new RtpPacket(buffer, 0);
		rtpPacket.setPayloadType(RtspConstants.RTP_H264_PAYLOADTYPE);

		// skip the mpeg4 header

		try {

			// skip all atoms preceding mdat atom
			skipMDAT();

			// some phones do not set length correctly when stream is not
			// seekable, still we need to skip the header
			if (len <= 0) {
				while (true) {
					while (fis.read() != 'm')
						;
					fis.read(buffer, rtpHeaderLength, 3);
					if (buffer[rtpHeaderLength] == 'd' && buffer[rtpHeaderLength + 1] == 'a' && buffer[rtpHeaderLength + 2] == 't')
						break;
				}
			}
			len = 0;

		} catch (IOException e) {
			Log.w(TAG , e.getMessage());
			return;
		}

		while (running) {
			
			/* If there are NAL units in the FIFO ready to be sent, we send one */
			// send();

			/*
			 * Read a NAL unit in the FIFO and send it If it is too big, we
			 * split it in FU-A units (RFC 3984)
			 */
			int sum = 1, len = 0, nalUnitLength;

			if (numberNalUnit != 0) {

				/* Read nal unit length (4 bytes) and nal unit header (1 byte) */
				len = fifo.read(buffer, rtpHeaderLength, 5);
				nalUnitLength = (buffer[rtpHeaderLength + 3] & 0xFF) + (buffer[rtpHeaderLength + 2] & 0xFF) * 256
						+ (buffer[rtpHeaderLength + 1] & 0xFF) * 65536;

//				 Log.d(TAG ,"send- NAL unit length: " + nalUnitLength);

				// rsock.updateTimestamp(SystemClock.elapsedRealtime() * 90);
				rtpPacket.setTimestamp(SystemClock.elapsedRealtime() * 90);

				/* Small nal unit => Single nal unit */
				if (nalUnitLength <= packetSize - rtpHeaderLength - 2) {

					buffer[rtpHeaderLength] = buffer[rtpHeaderLength + 4];
					len = fifo.read(buffer, rtpHeaderLength + 1, nalUnitLength - 1);

					rtpPacket.setMarker(true);

					try {

						rtpPacket.setSequenceNumber(seqn++);
						rtpPacket.setPayloadLength(nalUnitLength);
					
						rtpSender.send(rtpPacket);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				/* Large nal unit => Split nal unit */
				else {

					/* Set FU-A indicator */
					buffer[rtpHeaderLength] = 28;
					buffer[rtpHeaderLength] += (buffer[rtpHeaderLength + 4] & 0x60) & 0xFF; // FU indicator
					// NRI
					// buffer[rtphl] += 0x80;

					/* Set FU-A header */
					buffer[rtpHeaderLength + 1] = (byte) (buffer[rtpHeaderLength + 4] & 0x1F); // FU header
					// type
					buffer[rtpHeaderLength + 1] += 0x80; // Start bit

					while (sum < nalUnitLength) {

						if (!running)
							break;
						
						len = fifo.read(buffer, 
								rtpHeaderLength + 2, 
								nalUnitLength - sum > packetSize - rtpHeaderLength - 2 ? packetSize - rtpHeaderLength - 2 : nalUnitLength - sum);
						sum += len;
						if (len < 0)
							break;

						/* Last packet before next NAL */
						if (sum >= nalUnitLength) {
							// End bit on
							buffer[rtpHeaderLength + 1] += 0x40;

							// rsock.markNextPacket();
							rtpPacket.setMarker(true);

						}

						try {
							
							// rsock.send(len + rtpHeaderLength + 2);
							rtpPacket.setSequenceNumber(seqn++);
							rtpPacket.setPayloadLength(len + 2);

							rtpSender.send(rtpPacket);
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						/* Switch start bit */
						buffer[rtpHeaderLength + 1] = (byte) (buffer[rtpHeaderLength + 1] & 0x7F);

//						Log.d(TAG,"send--- FU-A unit, end:"+(boolean)(sum >= nalUnitLength));

					}

				}

				numberNalUnit--;

//				Log.d(TAG,"NAL UNIT SENT> " + numberNalUnit);
			}

			/*
			 * If the camera has delivered new NAL units we copy them in the
			 * FIFO Then, the delay between two send call is latency/nbNalu
			 * with: latency: how long it took to the camera to output new data
			 * nbNalu: number of NAL units in the FIFO
			 */
			fillFifo();

			try {
				Thread.sleep(delay);

			} catch (InterruptedException e) {
				return;
			}

		}

	}

	// skip all atoms preceeding mdat atom
	
	private void skipMDAT() throws IOException {

		while (true) {
			
			fis.read(buffer, rtpHeaderLength, 8);
			if (buffer[rtpHeaderLength + 4] == 'm' && buffer[rtpHeaderLength + 5] == 'd' && buffer[rtpHeaderLength + 6] == 'a' && buffer[rtpHeaderLength + 7] == 't')
				break;
			
			len = (buffer[rtpHeaderLength + 3] & 0xFF) + (buffer[rtpHeaderLength + 2] & 0xFF) * 256 + (buffer[rtpHeaderLength + 1] & 0xFF) * 65536;
			if (len <= 0)
				break;

			fis.read(buffer, rtpHeaderLength, len - 8);
		
		}

	}
	
	private void fillFifo() {

		try {

			available = fis.available();

			if (available > oldavailable) {

				long now = SystemClock.elapsedRealtime();
				latency = now - oldlat;
				
				oldlat = now;
				oldavailable = available;
			}

			if (numberNalUnit == 0 && available > 4) {
				numberNalUnit = nalUnitLength - len == 0 ? numberNalUnit : numberNalUnit + 1;
			} else
				return;

			while ((available = fis.available()) >= 4) {

				fis.read(buffer, rtpHeaderLength, nalUnitLength - len);
				fifo.write(buffer, rtpHeaderLength, nalUnitLength - len);

				/* Read NAL unit and copy it in the fifo */
				len = fis.read(buffer, rtpHeaderLength, 4);
				nalUnitLength = (buffer[rtpHeaderLength + 3] & 0xFF) + (buffer[rtpHeaderLength + 2] & 0xFF) * 256
						+ (buffer[rtpHeaderLength + 1] & 0xFF) * 65536;
				
				len = fis.read(buffer, rtpHeaderLength + 4, nalUnitLength);
				fifo.write(buffer, rtpHeaderLength, len + 4);

				if (len == nalUnitLength)
					numberNalUnit++;

//				Log.i(TAG,"fifo- available: " + available + ", len: " + len + ", naluLength: " + nalUnitLength);

				if (fis.available() < 4) {

					delay = latency / numberNalUnit;
					oldavailable = fis.available();
//					Log.i(TAG,"fifo- latency: "+latency+", nbNalu: "+numberNalUnit+", delay: "+delay+" avfifo: "+fifo.available());

				}

			}

		}

		catch (IOException e) {
			return;
		}

	}

    // Useful for debug
    protected String printBuffer(int start,int end) {
            String str = "";
            for (int i=start;i<end;i++) str+=","+Integer.toHexString(buffer[i]&0xFF);
            return str;
    }

}
