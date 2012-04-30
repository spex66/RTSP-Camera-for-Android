package de.kp.net.rtsp.client.message;

public class RtspMedia {

	private String mediaType;
	private String mediaFormat;
	
	private String transportPort;
	private String transportProtocol;
	
	private String encoding;
	private String clockrate;
	
	private String framerate;
	
	private static String SDP_CONTROL   = "a=control:";
	private static String SDP_RANGE     = "a=range:";
	private static String SDP_LENGTH    = "a=length:";
	private static String SDP_RTMAP     = "a=rtpmap:";
	private static String SDP_FRAMERATE = "a=framerate:";
	
	public RtspMedia(String line) {
		
		String[] tokens = line.substring(2).split(" "); 
		
		mediaType   = tokens[0];		
		mediaFormat = tokens[3];
	
		transportPort     = tokens[1];
		transportProtocol = tokens[2];
		
	}
	
	public String getMediaType() {
		return mediaType;
	}
	
	public String getFrameRate() {
		return framerate;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	public String getClockrate() {
		return clockrate;
	}
	
	public String getTransportPort() {
		return transportPort;
	}
	
	public void setAttribute(String line) throws Exception {
		
		if (line.startsWith(SDP_CONTROL)) {
			
		} else if (line.startsWith(SDP_RANGE)) {

		} else if (line.startsWith(SDP_LENGTH)) {

		} else if (line.startsWith(SDP_FRAMERATE)) {

			framerate = line.substring(SDP_FRAMERATE.length());
			
		} else if (line.startsWith(SDP_RTMAP)) {
			
			String[] tokens = line.substring(SDP_RTMAP.length()).split(" ");
			
			String payloadType = tokens[0];
			if (payloadType.equals(mediaFormat) == false) throw new Exception("Corrupted Session Description - Payload Type");
			
			if (tokens[1].contains("/")) {

				String[] subtokens = tokens[1].split("/");
				
				encoding  = subtokens[0];
				clockrate = subtokens[1];
				
			} else {
				encoding = tokens[1];
				
			}
			
		}

	}
	
	public String toString() {
		return mediaType + " " + transportPort + " " + transportProtocol + " " + mediaFormat + " " + 
			encoding + "/" + clockrate;
	}
	
}
