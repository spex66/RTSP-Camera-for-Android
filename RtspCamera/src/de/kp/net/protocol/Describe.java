package de.kp.net.protocol;

import java.net.UnknownHostException;

public class Describe extends RtspResponse {

    protected String rtpSession  = "";
    protected String contentBase = "";

    private String fileName;
    
    public Describe(int cseq) {
        super(cseq);
    }

    protected void generateBody() {
            
    	SDP sdp = new SDP(fileName);

        String sdpContent = "";
        try {
            sdpContent = CRLF2 + sdp.getSdp();
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        
        body += "Content-base: "+contentBase + CRLF
        + "Content-Type: application/sdp"+ CRLF 
        + "Content-Length: "+ sdpContent.length() + sdpContent;

    }

    public String getContentBase() {
        return contentBase;
    }

    public void setContentBase(String contentBase) {
        this.contentBase = contentBase;
    }
    
    public void setFileName(String fileName) {
    	this.fileName = fileName;
    }
    
}
