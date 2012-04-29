package de.kp.net.rtsp.server.response;

import java.util.Date;

import de.kp.net.rtsp.RtspConstants;

public abstract class RtspResponse {

    protected String response="";
       
    protected int cseq = 0;
       
   	protected static int session_id = -1;       
   	protected boolean newSessionId = true;
       
	protected String body = "";
       
    /**
     * CR = <US-ASCII CR, carriage return (13)>
     * LF = <US-ASCII LF, linefeed (10)>
     * CRLF = CR LF
     */
    public static final String CRLF  = "\r\n";
    public static final String CRLF2 = "\r\n\r\n";
    public static final String SEP   = " ";
   
    public RtspResponse(int cseq){
            this.cseq = cseq;
    }
       
    protected String getHeader() {

    	StringBuffer sb = new StringBuffer();
    	
    	sb.append("RTSP/1.0" + SEP + "200" + SEP + "OK" + CRLF);
    	sb.append(cseq() +CRLF);
    	
    	sb.append("Date: " + new Date().toGMTString() + CRLF);
    	sb.append("Server: " + getServer() + CRLF);

    	return sb.toString();
    	
    }
       
	protected String cseq() {
        return "CSeq:" + SEP + getCseq();
	}
   
    protected String getResponse() {
        return response;
    }

    protected void setResponse(String response) {
        this.response = response;
    }

    protected String getServer(){
        return RtspConstants.SERVER_NAME + "/" + RtspConstants.SERVER_VERSION;
    }
       
    protected int getCseq() {
        return cseq;
    }

    protected void setCseq(int cseq) {
        this.cseq = cseq;
    }

    protected String getBody() {
        return body;
    }

    protected void setBody(String cuerpo) {
        this.body = cuerpo;
    }

    protected void generate(){
    
    	// note that it is important to close the response
    	// message with 2 CRLFs
    	response += getHeader();
        response += getBody() + CRLF2;

    }
       
    protected abstract void generateBody();
       
    public String toString() {
            
    	generateBody();            
    	generate();
  
    	return response;
    
    }
       
    public void createSessionId(boolean bool){
        newSessionId = bool;
    }

}

