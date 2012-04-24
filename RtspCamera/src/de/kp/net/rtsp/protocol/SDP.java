package de.kp.net.rtsp.protocol;

import java.net.UnknownHostException;

import de.kp.net.MediaConstants;
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
		
		buf.append(getSDPVideo(1));

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
	
	private StringBuffer getSDPVideo(int track){
		
		StringBuffer buf = new StringBuffer();
		
		buf.append("m=video " + clientVideoPort + RtspConstants.SEP + "RTP/AVP " + RtspConstants.RTP_PAYLOADTYPE + RtspResponse.CRLF);		
		// TODO:
//		buf.append("a=rtpmap:" + RtspConstants.RTP_PAYLOADTYPE + RtspConstants.SEP + RtspConstants.H263_1998 + RtspResponse.CRLF);
		buf.append("a=rtpmap:" + RtspConstants.RTP_PAYLOADTYPE + RtspConstants.SEP + RtspConstants.H264 + RtspResponse.CRLF);

		// TODO
		//buf.append("a=fmtp:98 packetization-mode=1;profile-level-id=420020;sprop-parameter-sets=J0IAIKaAoD0Q,KM48gA==;" + RtspResponse.CRLF); // 640x480 20fps
//		buf.append("a=fmtp:98 packetization-mode=1;profile-level-id=420020;sprop-parameter-sets=J0IAINoLExA,KM48gA==;" + RtspResponse.CRLF); // 176x144 15fps
		buf.append("a=fmtp:98 packetization-mode=1;profile-level-id=420020;sprop-parameter-sets=J0IAIKaCxMQ=,KM48gA==;" + RtspResponse.CRLF); // 176x144 20fps
//		buf.append("a=fmtp:98 packetization-mode=1;profile-level-id=420020;sprop-parameter-sets=J0IAINoFB8Q=,KM48gA==;" + RtspResponse.CRLF); // 320x240 10fps

		// additional information for android video
		// view, due to extended checking mechanism
		buf.append("a=framesize:" + RtspConstants.RTP_PAYLOADTYPE + RtspConstants.SEP + RtspConstants.WIDTH + "-" + RtspConstants.HEIGHT + RtspResponse.CRLF);
		buf.append("a=control:trackID=" + String.valueOf(track));
		
		
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