package de.kp.net.rtsp.server.response;

public class RtspError extends RtspResponse {

    public RtspError(int cseq) {
        super(cseq);
    }

    protected void generateBody() {
    }

}
