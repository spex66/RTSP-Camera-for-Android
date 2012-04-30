package de.kp.net.rtsp.client.message;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.kp.net.rtsp.RtspConstants;

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
	
	public RtspMedia getFirstVideo() {
		
		RtspMedia video = null;
		for (RtspMedia mediaItem:this.mediaList) {
			
			if (mediaItem.getMediaType().equals(RtspConstants.SDP_VIDEO_TYPE)) {
				video = mediaItem;
				break;
			}
			
		}
		
		return video;
		
	}
	
}
