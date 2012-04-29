package de.kp.net.rtsp.server.response;

public class RtspOptionsResponse extends RtspResponse {

    public RtspOptionsResponse(int cseq) {
        super(cseq);
    }
    
    protected void generateBody() {
        this.body = "Public:DESCRIBE,SETUP,TEARDOWN,PLAY,PAUSE"/*+SL*/;
    }

}
