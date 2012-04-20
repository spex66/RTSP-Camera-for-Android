package de.kp.net.rtsp.protocol;

public class Options extends RtspResponse {

    public Options(int cseq) {
        super(cseq);
    }
    
    protected void generateBody() {
        this.body = "Public:DESCRIBE,SETUP,TEARDOWN,PLAY,PAUSE"/*+SL*/;
    }

}
