package de.kp.net.rtsp.server.response;

import java.util.Random;

import de.kp.net.rtsp.RtspConstants;

public class RtspSetupResponse extends RtspResponse {

    private int clientRTP, clientRTCP;
        
    private String clientIP = "";
        
    private int[] interleaved;        
    private String transportProtocol = "";
        
    private String sessionType = "";

    public RtspSetupResponse(int cseq) {
        super(cseq);
    }

    protected void generateBody() {

    	createSessionId();

    	body += "Session: " + session_id + CRLF + "Transport: " + transportProtocol + ";" + sessionType + ";";
        if (interleaved==null) {
            body += "source=" + RtspConstants.SERVER_IP + ";" + getPortPart();
        
        } else {
            body += getInterleavedPart();
        }

    }
        
    private String getPortPart(){
        
    	String r= "client_port=" + clientRTP + "-" + clientRTCP + ";" + "server_port=" + RtspConstants.PORTS_RTSP_RTP[0] + "-" + RtspConstants.PORTS_RTSP_RTP[1];
	    return r;

    }
        
    private String getInterleavedPart() {
        return "client_ip=" + clientIP + ";interleaved=" + interleaved[0] + "-" + interleaved[1];
    }

    private final void createSessionId() {
 
    	Random r = new Random();
        int id = r.nextInt();
        
        if (id < 0) {
            id *= -1;
        }
        
        if (newSessionId) {
            session_id = id;
        }
    
    }

    public void setClientPort(int port) {
        clientRTP  = port;
        clientRTCP = port + 1;
    }

    public String getTransportProtocol() {
        return transportProtocol;
    }

   public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

     public int[] getInterleaved() {
            return interleaved;
    }

    public void setInterleaved(int[] interleaved) {
        this.interleaved = interleaved;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }
        
}
