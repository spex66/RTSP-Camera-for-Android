package de.kp.net.rtsp.server.response;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.StringTokenizer;

import de.kp.net.rtsp.RtspConstants;

/**
 * This class provides a parser for incoming RTSP
 * messages and splits them into appropriate parts.
 * 
 * @author Stefan Krusche (krusche@dr-kruscheundpartner.de)
 *
 */
public class Parser {
        
    /**
     * @param rtspBufferedReader
     * @return
     * @throws IOException
     */
    public static String readRequest(BufferedReader rtspBufferedReader) throws IOException {
 
    	String request = new String();

    	boolean endFound = false;
    	int c;

    	while ((c = rtspBufferedReader.read()) != -1) {
        
    		request += (char) c;
    		if (c == '\n') {
            
    			if (endFound) {
    				break;
	 
    			} else {
    				endFound = true;
    			}

    		} else {
    			if (c != '\r') {
    				endFound = false;
    			}
     
    		}

    	}

    	return request;
    
    }

    /**
     * This method determines the request type of an
     * incoming RTSP request.
     * 
     * @param request
     * @return
     */
    public static int getRequestType(String request) {

    	StringTokenizer tokens = new StringTokenizer(request);
        String requestType = "";

        if (tokens.hasMoreTokens()) {
	        requestType = tokens.nextToken();
        }

        if ((new String(requestType)).compareTo("OPTIONS") == 0)
        	return RtspConstants.OPTIONS;
            
        else if ((new String(requestType)).compareTo("DESCRIBE") == 0)
            return RtspConstants.DESCRIBE;
            
        else if ((new String(requestType)).compareTo("SETUP") == 0)
            return RtspConstants.SETUP;
            
        else if ((new String(requestType)).compareTo("PLAY") == 0)
            return RtspConstants.PLAY;
            
        else if ((new String(requestType)).compareTo("PAUSE") == 0)
            return RtspConstants.PAUSE;
            
        else if ((new String(requestType)).compareTo("TEARDOWN") == 0)
            return RtspConstants.TEARDOWN;

        return -1;
    
    }

    /**
     * @param request
     * @return
     */
    public static String getContentBase(String request) {

    	StringTokenizer tokens = new StringTokenizer(request);
        String contentBase = "";
        
        if (tokens.hasMoreTokens()) {
        	contentBase = tokens.nextToken();
            contentBase = tokens.nextToken();
        }

        return contentBase;
    
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    public static int getCseq(String request) throws Exception {
        
    	String ineInput = getLineInput(request, "\r\n", "CSeq");
        String cseq = ineInput.substring(6);
            
        return Integer.parseInt(cseq);
    
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    public static int[] getInterleavedSetup(String request) throws Exception {

    	int[] interleaved = null;
            
    	String lineInput = getLineInput(request, "\r\n", "Transport:");
        String[] parts = lineInput.split("interleaved=");

        int t = parts.length;
        if (t > 1) {
        	
            parts = parts[1].split("-");
            interleaved = new int[2];
            
            interleaved[0] = Integer.parseInt(parts[0]);
            interleaved[1] = Integer.parseInt(parts[1]);
        }

        return interleaved;

    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    public static String getFileName(String request) throws Exception {
                
    	String lineInput = getLineInput(request, " ", "rtsp");
    	URI uri = new URI(lineInput);
    	
    	//String[] parts = lineInput.split("rtsp://" + RtspConstants.SERVER_IP + "/");        
        //String fileName = parts[1];
        
    	String fileName = uri.getPath();
        return fileName;
           
    }

    /**
     * This method retrieves a certain input from an
     * incoming RTSP request, described by a separator
     * and a specific prefix.
     * 
     * @param request
     * @param separator
     * @param prefix
     * @return
     * @throws Exception
     */
    public static String getLineInput(String request, String separator, String prefix) throws Exception {
            
    	StringTokenizer str = new StringTokenizer(request, separator);
        String token = null;

        boolean match = false;
        
        while (str.hasMoreTokens()) {
            token = str.nextToken();
            if (token.startsWith(prefix)) {            	
            	match = true;
                break;
            }
        }

        return (match == true) ? token : null;
    
    }

    /**
     * This method retrieves the client port
     * from an incoming RTSP request.
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public static int getClientPort(String request) throws Exception {

    	String lineInput = getLineInput(request, "\r\n", "Transport:");
    	if (lineInput == null) throw new Exception();
            
    	String[] parts = lineInput.split(";");
        parts[2] = parts[2].substring(12);
            
        String[] ports = parts[2].split("-");
        return Integer.parseInt(ports[0]);
    
    }
 
    /**
     * This method retrieves the transport protocol
     * from an incoming RTSP request.
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public static String getTransportProtocol(String request) throws Exception {
    	
        String lineInput = getLineInput(request, "\r\n", "Transport:");
    	if (lineInput == null) throw new Exception();
        
        String[] parts = lineInput.split(";");
        parts[0] = parts[0].substring(11);
        
        return parts[0];
    
    }

    /**
     * This method retrieves the range from an
     * incoming RTSP request.
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public static String getRangePlay(String request) throws Exception {

    	String lineInput = getLineInput(request, "\r\n", "Range:");
    	if (lineInput == null) {
    		
    		/* 
    		 * Android's video view does not provide
    		 * range information with a PLAY request
    		 */
    		
    		return null;
    		
    	}
        
    	String[] parts = lineInput.split("=");
        return parts[1];
    
    }

    /**
     * 
     * This method determines the session type from an
     * incoming RTSP request.
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public static String getSessionType(String request) throws Exception {
        
    	String lineInput = getLineInput(request, "\r\n", "Transport:");
    	if (lineInput == null) throw new Exception();
        
    	String[] parts = lineInput.split(";");
        return parts[1].trim();
    
    }
        
    /**
     * This method retrieves the user agent from an
     * incoming RTSP request.
     * 
     * @param request
     * @return
     * @throws Exception
     */
    public String getUserAgent(String request) throws Exception{
        
    	String lineInput = getLineInput(request, "\r\n", "User-Agent:");
    	if (lineInput == null) throw new Exception();
        
    	String[] parts = lineInput.split(":");
        return parts[1];
    
    }

}
