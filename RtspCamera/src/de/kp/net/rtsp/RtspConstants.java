package de.kp.net.rtsp;

public class RtspConstants {

	// rtsp states
	public static int INIT 		= 0;
	public static int READY 	= 1;
	public static int PLAYING 	= 2;
	
	// rtsp message types
	public static int OPTIONS 	= 3;
	public static int DESCRIBE 	= 4;
	public static int SETUP 	= 5;
	public static int PLAY 		= 6;
	public static int PAUSE 	= 7;
	public static int TEARDOWN 	= 8;
	
	// the payload type is part of the SDP description
	// sent back as an answer to a DESCRIBE request.
	
	// android actually supports video streaming from
	// the camera using H.263-1998
	public static int RTP_PAYLOADTYPE = 98; // dynamic range

	public static String H263_1998 = "H263-1998/90000";
	
	// default client ports for audio and video streaming;
	// the port is usually provided with an RTSP request
    public static final int CLIENT_AUDIO_PORT = 2000;
    public static final int CLIENT_VIDEO_PORT = 4000;

	public static String SERVER_IP = "spexhd2:8080";
	
	public static String SERVER_NAME    = "KuP RTSP Server";
    public static String SERVER_VERSION = "0.1";
    
    public static int PORT_BASE = 3000;
    public static int[] PORTS_RTSP_RTP = {PORT_BASE, (PORT_BASE + 1)};
    
	public static final String DIR_MULTIMEDIA = "../";
	
	// tags for logging
	public static String SERVER_TAG = "RtspServer";

}
