package de.kp.net.rtsp.server.response;

public class RtspResponseTeardown extends RtspResponse {

    public RtspResponseTeardown(int cseq) {
        super(cseq);
    }

    protected void generar(){
        response += getHeader();
        response += getBody() + CRLF;
    }
    
    protected void generateBody() {
        body += "";
    }

}
