package de.kp.net.rtsp.server.response;

import java.net.UnknownHostException;

import de.kp.net.rtsp.RtspConstants.VideoEncoder;

public class RtspDescribeResponse extends RtspResponse {

    protected String rtpSession  = "";
    protected String contentBase = "";

    private String fileName;
    private VideoEncoder encoder;
    
    public RtspDescribeResponse(int cseq) {
        super(cseq);
    }

    protected void generateBody() {
            
    	SDP sdp = new SDP(fileName, encoder);

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

    public void setVideoEncoder(VideoEncoder encoder) {
    	this.encoder = encoder;
    }
}
