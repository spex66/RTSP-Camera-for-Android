package de.kp.net.rtsp.protocol;

import java.net.UnknownHostException;

import de.kp.net.rtsp.RtspConstants;

public class SDP {

	// the default file name
	private String fileName = "kupdroid";
	
	private int audioClientPort = RtspConstants.CLIENT_AUDIO_PORT;
	private int clientVideoPort = RtspConstants.CLIENT_VIDEO_PORT;

	public SDP(String fileName) {
		this.fileName = fileName;
	}

	
	/**
	 * This method is used to build a minimal
	 * SDP file description.
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	public String getSdp() throws UnknownHostException {

		StringBuffer buf = new StringBuffer();
		
		buf.append("v=0" + RtspResponse.CRLF);
		buf.append("s="  + fileName + RtspResponse.CRLF);
		
		buf.append(getSDPVideo());

		return buf.toString();
	}
	
	/*
	private StringBuffer getSDPAudio(){
		
		StringBuffer buf = new StringBuffer();
		
		//m=<media> <port> <proto> <fmt>
		buf.append("m=audio " + audioClientPort + " RTP/AVP 14" + RtspResponse.CRLF);
		//a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
		
		buf.append("a=rtpmap:14 MPA/90000" + RtspResponse.CRLF);
		buf.append("a=control:rtsp://" + RtspConstants.SERVER_IP + "/audio" + RtspResponse.CRLF);
		
		buf.append("a=mimetype: audio/MPA" + RtspResponse.CRLF);
		buf.append("a=range:npt=0-");
		
		return buf;
	
	}
	*/
	
	private StringBuffer getSDPVideo(){
		
		StringBuffer buf = new StringBuffer();
		
		buf.append("m=video " + clientVideoPort + " RTP/AVP " + RtspConstants.RTP_PAYLOADTYPE + RtspResponse.CRLF);
		buf.append("a=rtpmap:" + RtspConstants.RTP_PAYLOADTYPE + " " + RtspConstants.H263_1998 + RtspResponse.CRLF);

		return buf;
	
	}
	
	/*
	private StringBuffer getSDPWebcam(){
		
		StringBuffer buf = new StringBuffer();
		
		buf.append("m=video " + clientVideoPort + " RTP/AVP 26" + RtspResponse.CRLF);
		buf.append("a=rtpmap:26 JPEG/90000"+RtspResponse.CRLF);
		
		buf.append("a=control:rtsp://" + RtspConstants.SERVER_IP + "/video" + RtspResponse.CRLF);
		buf.append("a=mimetype: video/JPEG" + RtspResponse.CRLF);
		
		buf.append("a=range:npt=0-100");
		
		return buf;
	
	}
	*/
	
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getClientAudioPort() {
		return audioClientPort;
	}

	public void setClientAudioPort(int clientAudioPort) {
		this.audioClientPort = clientAudioPort;
	}

	public int getClientVideoPort() {
		return clientVideoPort ;
	}

	public void setClientVideoPort(int clientVideoPort) {
		this.clientVideoPort  = clientVideoPort;
	}
	
}