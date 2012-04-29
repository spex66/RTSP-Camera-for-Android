package de.kp.net.rtsp.server.response;

public class RtspPlayResponse extends RtspResponse {

    protected String range = "";
   
    public RtspPlayResponse(int cseq) {
        super(cseq);
    }

    protected void generateBody() {	
    	this.body += "Session: " + session_id + CRLF + "Range: npt=" + range;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }
      
}
