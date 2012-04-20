package de.kp.net.rtsp.protocol;

public class Play extends RtspResponse {

    protected String range = "";
   
    public Play(int cseq) {
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
