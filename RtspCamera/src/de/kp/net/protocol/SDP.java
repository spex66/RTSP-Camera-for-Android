package de.kp.net.protocol;

import java.net.UnknownHostException;

public class SDP {

	private String fileName = "kupdroid";
	
	private boolean javasound=false;
	
	private boolean webcam=false;
	
	private boolean screen=false;
	
	private int audioClientPort = RtspConstants.CLIENT_AUDIO_PORT;
	private int clientVideoPort = RtspConstants.CLIENT_VIDEO_PORT;
	
	private String serverIP = "";

	public SDP(String fileName) {

//		this.fileName = fileName;
//		
//		if( fileName.compareToIgnoreCase("sound")==0){
//			javasound=true;
//			this.fileName = "javasound://0";
//		
//		} else {
//			
//			if(fileName.compareToIgnoreCase("webcam")==0){
//				webcam=true;
//				this.fileName = "vfw://0";
//			
//			} else  if(fileName.compareToIgnoreCase("screen")==0){
//				screen=true;
//				this.fileName = "screen://0,0,1280,800/25";
//
//			}
//		}
		
	}

	public String getSdp() throws UnknownHostException {

		StringBuffer buf = new StringBuffer();

		serverIP = RtspConstants.SERVER_IP;
		
		buf.append("v=0" + RtspResponse.CRLF);
//		buf.append("o=" + RtspConstants.SERVER_NAME + RtspResponse.CRLF);
//		buf.append("i=Session Description" + RtspResponse.CRLF);
		//buf.append("e=Marcos Fermin Lobo <uo174203@uniovi.es>" + RtspResponse.CRLF);
		buf.append("s=" + fileName + RtspResponse.CRLF);
//		buf.append("t=0 0" + RtspResponse.CRLF);
//		buf.append("a=charset:ISO-8859-1" + RtspResponse.CRLF);

		
		buf.append(getSDPVideo());

//		if (javasound) {
//			buf.append(getSDPAudio());
//		
//		} else {
//			
//			if (webcam) {
//				buf.append(getSDPWebcam());
//			
//			} else if (screen) {
//				buf.append(getSDPVideo());
//			}
//		
//		}
		

		return buf.toString();
	}
	
	private StringBuffer getSDPAudio(){
		
		StringBuffer buf = new StringBuffer();
		String range = "a=range:npt=0-";
		
		//m=<media> <port> <proto> <fmt>
		buf.append("m=audio " + audioClientPort + " RTP/AVP 14" + RtspResponse.CRLF);
		//a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
		
		buf.append("a=rtpmap:14 MPA/90000" + RtspResponse.CRLF);
		buf.append("a=control:rtsp://");
		
		buf.append(serverIP);
		
		buf.append("/audio" + RtspResponse.CRLF);
		buf.append("a=mimetype: audio/MPA" + RtspResponse.CRLF);
		buf.append(range);
		
		return buf;
	
	}
	
	private StringBuffer getSDPVideo(){
		
		StringBuffer buf = new StringBuffer();
		
		buf.append("m=video " + clientVideoPort +" RTP/AVP " + RtspConstants.RTP_PAYLOADTYPE + RtspResponse.CRLF);
		buf.append("a=rtpmap:" + RtspConstants.RTP_PAYLOADTYPE + " H263-1998/90000" + RtspResponse.CRLF);
		
//		buf.append("a=control:rtsp://");
//		buf.append(RtspConstants.SERVER_IP);
//		
//		buf.append("/video"+RtspResponse.CRLF);
//		buf.append("a=mimetype: video/JPEG"+RtspResponse.CRLF);
		
//		String range = "a=range:npt=0-100";
//		buf.append(range);

		return buf;
	
	}
	
	private StringBuffer getSDPWebcam(){
		
		StringBuffer buf = new StringBuffer();
		String range = "a=range:npt=0-100";
		
		buf.append("m=video " + clientVideoPort + " RTP/AVP 26" + RtspResponse.CRLF);
		buf.append("a=rtpmap:26 JPEG/90000"+RtspResponse.CRLF);
		
		buf.append("a=control:rtsp://");
		buf.append(serverIP);
		
		buf.append("/video" + RtspResponse.CRLF);
		buf.append("a=mimetype: video/JPEG" + RtspResponse.CRLF);
		buf.append(range);
		
		return buf;
	
	}

	public String getFileName() {
		return fileName;
	}

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