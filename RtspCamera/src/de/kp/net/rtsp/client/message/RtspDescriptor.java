package de.kp.net.rtsp.client.message;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class RtspDescriptor {

	private static String SEP = "\r\n";
	
	private ArrayList<RtspMedia> mediaList;
	
	public RtspDescriptor(String descriptor) {
		
		// initialize media list
		mediaList = new ArrayList<RtspMedia>();
		
		RtspMedia mediaItem = null;
		
		try {
	    	StringTokenizer tokenizer = new StringTokenizer(descriptor, SEP);
	    	while (tokenizer.hasMoreTokens()) {
	    		
	    		String token = tokenizer.nextToken();
	    		if (token.startsWith("m=")) {
	    			// a new media item is detected
	    			mediaItem = new RtspMedia(token); 
    				mediaList.add(mediaItem);
	    			
	    			
	    		} else if (token.startsWith("a=")) {
	    			mediaItem.setAttribute(token);	    			
	    		}
	
	    	}
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public List<RtspMedia> getMediaList() {
		return mediaList;
	}
	
}
