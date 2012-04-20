package de.kp.net.rtsp.protocol;

public class Pause extends RtspResponse {

    public Pause(int cseq) {
        super(cseq);
    }

    protected void generateBody() {
    }

}
