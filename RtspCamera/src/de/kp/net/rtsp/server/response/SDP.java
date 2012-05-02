package de.kp.net.rtsp.server.response;

import java.net.UnknownHostException;

import de.kp.net.rtsp.RtspConstants;
import de.kp.net.rtsp.RtspConstants.VideoEncoder;

public class SDP {

	// the default file name
	private String fileName = "kupdroid";
	
	private int audioClientPort = RtspConstants.CLIENT_AUDIO_PORT;
	private int clientVideoPort = RtspConstants.CLIENT_VIDEO_PORT;

	private VideoEncoder encoder;

	public SDP(String fileName, VideoEncoder encoder) {

		this.fileName = fileName;
		this.encoder  = encoder;
	
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
		// filename contains leading slash
		buf.append("s="  + fileName.substring(1) + RtspResponse.CRLF);
		
		int track = 1;
		buf.append(getSDPVideo(track));

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
		
		StringBuffer sb = new StringBuffer();
				
		// H263 encoding
		if (encoder.equals(VideoEncoder.H263_ENCODER)) {
			// cross encoder properties
			sb.append("m=video " + clientVideoPort + RtspConstants.SEP + "RTP/AVP " + RtspConstants.RTP_H263_PAYLOADTYPE + RtspResponse.CRLF);	
			// set to H263-2000
			sb.append("a=rtpmap:" + RtspConstants.RTP_H263_PAYLOADTYPE + RtspConstants.SEP + RtspConstants.H263_2000 + RtspResponse.CRLF);

			// additional information for android video view, due to extended checking mechanism
			sb.append("a=framesize:" + RtspConstants.RTP_H263_PAYLOADTYPE + RtspConstants.SEP + RtspConstants.WIDTH + "-" + RtspConstants.HEIGHT + RtspResponse.CRLF);

		} else if (encoder.equals(VideoEncoder.H264_ENCODER)) {
			// cross encoder properties
			sb.append("m=video " + clientVideoPort + RtspConstants.SEP + "RTP/AVP " + RtspConstants.RTP_H264_PAYLOADTYPE + RtspResponse.CRLF);	

			sb.append("a=rtpmap:" + RtspConstants.RTP_H264_PAYLOADTYPE + RtspConstants.SEP + RtspConstants.H264 + RtspResponse.CRLF);
			//buf.append("a=fmtp:98 packetization-mode=1;profile-level-id=420020;sprop-parameter-sets=J0IAIKaAoD0Q,KM48gA==;" + RtspResponse.CRLF); // 640x480 20fps
//			buf.append("a=fmtp:98 packetization-mode=1;profile-level-id=420020;sprop-parameter-sets=J0IAINoLExA,KM48gA==;" + RtspResponse.CRLF); // 176x144 15fps
			sb.append("a=fmtp:98 packetization-mode=1;profile-level-id=420020;sprop-parameter-sets=J0IAIKaCxMQ=,KM48gA==;" + RtspResponse.CRLF); // 176x144 20fps
//			buf.append("a=fmtp:98 packetization-mode=1;profile-level-id=420020;sprop-parameter-sets=J0IAINoFB8Q=,KM48gA==;" + RtspResponse.CRLF); // 320x240 10fps
			
			// additional information for android video view, due to extended checking mechanism
			sb.append("a=framesize:" + RtspConstants.RTP_H264_PAYLOADTYPE + RtspConstants.SEP + RtspConstants.WIDTH + "-" + RtspConstants.HEIGHT + RtspResponse.CRLF);
		}

		sb.append("a=control:trackID=" + String.valueOf(track));
		return sb;
	
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