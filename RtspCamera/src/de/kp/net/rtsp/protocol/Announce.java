package de.kp.net.rtsp.protocol;

public class Announce extends RtspResponse {

    public Announce(int cseq) {           
    	super(cseq);
    }

    protected void generateBody() {
    }

}
