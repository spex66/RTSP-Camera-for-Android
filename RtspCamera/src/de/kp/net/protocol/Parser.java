package de.kp.net.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Parser {

    public static boolean isFile(String media){
    	
        if (media.compareToIgnoreCase("sound") == 0){
        	return false;
        
        } else {
            
        	if (media.compareToIgnoreCase("webcam") == 0) {
                return false;
            
        	} else {
                if(media.compareToIgnoreCase("screen") == 0){
                    return false;
                }
            }
        }
        
        return true;
        
    }
        
    public static String readRequest(BufferedReader rtspBufferedReader) throws IOException {
 
    	String request = "";

    	boolean endFound = false;
    	int c;

    	request = new String();        
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

    public static int getRequestType(String input) {

    	StringTokenizer tokens = new StringTokenizer(input);
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

    public static String getContentBase(String input) {

    	StringTokenizer tokens = new StringTokenizer(input);
        String contentBase = "";
        
        if (tokens.hasMoreTokens()) {
        	contentBase = tokens.nextToken();
            contentBase = tokens.nextToken();
        }

        return contentBase;
    
    }

    public static int getCseq(String input) throws Exception {
        
    	String readLine = getLineInput(input, "\r\n", "CSeq");
        String cseq = readLine.substring(6);
            
        return Integer.parseInt(cseq);
    
    }

    public static int[] getInterleavedSetup(String input) throws Exception {

    	int[] interleaved = null;
            
    	String clientPortLine = getLineInput(input, "\r\n", "Transport:");
        String[] parts = clientPortLine.split("interleaved=");

        int t = parts.length;
        if (t > 1) {
        	
            parts = parts[1].split("-");
            interleaved = new int[2];
            
            interleaved[0] = Integer.parseInt(parts[0]);
            interleaved[1] = Integer.parseInt(parts[1]);
        }

        return interleaved;

    }

    public static String getFileName(String input) throws Exception {
                
    	String fileLine = getLineInput(input, " ", "rtsp");
        String[] parts = fileLine.split("rtsp://" + RtspConstants.SERVER_IP + "/");
                
        String fileName = parts[1];
        return fileName;
           
    }

    public static String getLineInput(String input, String separator, String prefix) throws Exception {
            
    	StringTokenizer str = new StringTokenizer(input, separator);
        String token = null;
            
        boolean match = false;

        while (str.hasMoreTokens()) {
            token = str.nextToken();
            if (token.startsWith(prefix)) {
                match = true;
                break;
            }
        }

        if (!match) {
            throw new Exception();
        }

        return token;
    
    }

    public static int getClientPort(String input) throws Exception {

    	String clientPortLine = getLineInput(input, "\r\n", "Transport:");
            
    	String[] parts = clientPortLine.split(";");
        parts[2] = parts[2].substring(12);
            
        String[] ports = parts[2].split("-");
        return Integer.parseInt(ports[0]);
    
    }
 
    public static String getTransportProtocol(String input) throws Exception {
    	
        String clientPortLine = getLineInput(input, "\r\n", "Transport:");
        
        String[] parts = clientPortLine.split(";");
        parts[0] = parts[0].substring(11);
        
        return parts[0];
    
    }

    public static String getRangePlay(String input) throws Exception {

    	String rangeLine = getLineInput(input, "\r\n", "Range:");
        
    	String[] parts = rangeLine.split("=");
        return parts[1];
    
    }

    public static String getSessionType(String input) throws Exception {
        
    	String clientPortLine = getLineInput(input, "\r\n", "Transport:");
        
    	String[] parts = clientPortLine.split(";");
        return parts[1].trim();
    
    }
        
        public String getUserAgent(String input) throws Exception{
                String linea = getLineInput(input, "\r\n", "User-Agent:");
                String[] trozos = linea.split(":");
                return trozos[1];
        }

}
