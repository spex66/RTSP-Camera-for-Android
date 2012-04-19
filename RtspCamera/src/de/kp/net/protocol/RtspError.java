package de.kp.net.protocol;

public class RtspError extends RtspResponse {

    public RtspError(int cseq) {
        super(cseq);
    }

    protected void generateBody() {
    }

}
