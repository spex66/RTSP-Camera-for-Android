package de.kp.net;

import java.io.IOException;
import java.util.Vector;

public class RtpSender {

	private static RtpSender instance = new RtpSender();
	
	private Vector<RtpSocket> receivers;
	
	private RtpSender() {
		receivers = new Vector<RtpSocket>();
	}
	
	public int getReceiverCount() {
		return receivers.size();
	}
	
	public static RtpSender getInstance() {
		if (instance == null) instance = new RtpSender();
		return instance;
	}
	
	public void addReceiver(RtpSocket receiver) {
		receivers.add(receiver);
	}
	
	public void removeReceiver(RtpSocket receiver) {
		receivers.remove(receiver);
	}
	
	public synchronized void send(RtpPacket rtpPacket) throws IOException {

		for (RtpSocket receiver:receivers) {
			receiver.send(rtpPacket);
		}
		
	}

	public void stop() {
		// TODO
	}
}
