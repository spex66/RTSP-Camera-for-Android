package de.kp.net.protocol;

public class Announce extends RtspResponse {

    public Announce(int cseq) {           
    	super(cseq);
    }

    protected void generateBody() {
    }

}
