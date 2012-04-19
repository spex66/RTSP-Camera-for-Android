package de.kp.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;
import de.kp.net.protocol.Describe;
import de.kp.net.protocol.Options;
import de.kp.net.protocol.Parser;
import de.kp.net.protocol.Pause;
import de.kp.net.protocol.Play;
import de.kp.net.protocol.RtspConstants;
import de.kp.net.protocol.RtspError;
import de.kp.net.protocol.RtspResponse;
import de.kp.net.protocol.Setup;
import de.kp.net.protocol.Teardown;

public class RtspServer implements Runnable {

	
	private ServerSocket serverSocket;
	private boolean stopped = false;
	private String TAG = "RtspServer";

	public RtspServer(int port) throws IOException {		
	    this.serverSocket = new ServerSocket(port);	  
	}

	public void run() {
	    
	    /*
	     * In order to communicate with different clients,
	     * we construct a thread for each client that is
	     * connected.
	     */
	    while (this.stopped == false) {
	    	
			try {
				Socket  clientSocket = this.serverSocket.accept();
				
				Log.v(TAG , "accepted");

		    	new ServerThread(clientSocket);
				Log.v(TAG , "ServerThread started");

			} catch (IOException e) {
				e.printStackTrace();
			}
	    
	    }
		
	}

	public void stop() {
		this.stopped = true;
	}
	
	private class ServerThread extends Thread {
		
		/*
		 * response to RTSP client
		 */
		private RtspResponse rtspResponse;
		
		private String contentBase = "";
		
		/*
		 * input and output stream buffer for TCP connection; 
		 * UDP response are sent through DatagramSocket
		 */
		private BufferedReader rtspBufferedReader;
		private BufferedWriter rtspBufferedWriter;

		private int rtspState;
		
		// Sequence number of RTSP messages within the session	
		private int cseq = 0;	
		
		private int clientPort;
		
		// remote (client) address
		private InetAddress clientAddress;
		
		/*
		 * This datagram socket is used to send UDP
		 * packets to the clientIPAddress
		 */
		private RtpSocket rtpSocket;

		private final Socket clientSocket;
	    public ServerThread(Socket socket) {
	    	
	    	this.clientSocket = socket;
	    	
	    	// register IP address of requesting client
	    	this.clientAddress = this.clientSocket.getInetAddress();
	    	
	    	start();
	    
	    }
	    
	    public void run() {
	    	
	    	// prepare server response
	    	String response = "";
	    	
	    	try {

	    		// Set input and output stream filters
	    		rtspBufferedReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()) );
	    		rtspBufferedWriter = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()) );

	    		boolean setup = false;
	    		 
	    		while (setup == false) {
	    			
	    			// determine request type and also provide
	    			// server response
	    			int requestType = getRequestType();

	    			// send response
	    			response = rtspResponse.toString();
	    			
					Log.v(TAG , "Request: " + requestType + "\nresp: \n" + response);


	    			rtspBufferedWriter.write(response);
		    		rtspBufferedWriter.flush();

	    			if (requestType == RtspConstants.SETUP) {
	    			    
	    				setup = true;

	    			    // update RTSP state
	    			    rtspState = RtspConstants.READY;
	    				
	    				// in case of a setup request, we create a new RtpSocket 
	    				// instance used to send RtpPacket
	    				this.rtpSocket = new RtpSocket(this.clientAddress, this.clientPort);
	    				
	    				// this RTP socket is registered as RTP receiver to also
	    				// receive the streaming video of this device
	    				RtpSender.getInstance().addReceiver(this.rtpSocket);

	    			}
	    			
	    		}

	    		while (true) {

	    			// pares incoming request to decide how to proceed
	    			int requestType = getRequestType();

	    			// send response
	    			response = rtspResponse.toString();

	    			rtspBufferedWriter.write(response);
		    		rtspBufferedWriter.flush();
	    			
	    			if ((requestType == RtspConstants.PLAY) && (rtspState == RtspConstants.READY)) {	    				
	    				this.rtpSocket.suspend(false);
	    				
	    			} else if ((requestType == RtspConstants.PAUSE) && (rtspState == RtspConstants.PLAYING)) {
	    				
	    				// suspend RTP socket from sending video packets
	    				this.rtpSocket.suspend(true);
	    				
	    			} else if (requestType == RtspConstants.TEARDOWN) {

	    				// this RTP socket is removed from the RTP Sender
//	    				RtpSender.getInstance().removeReceiver(this.rtpSocket);
	    				
	    			}

	    		}
	      
	    	} catch(Throwable t) {
	    		t.printStackTrace();
	    		
	    		System.out.println("Caught " + t + " - closing thread");
	      
	    	}
	    }
	    
	    private int getRequestType() throws Exception {
	  	
	    	int requestType = -1;

	    	String requestLine = "";
            try {
            	requestLine = Parser.readRequest(rtspBufferedReader);
        
            } catch (IOException e) {
                e.printStackTrace();
        
            }

			Log.d(TAG , "getRequest: " + requestLine);

            requestType = Parser.getRequestType(requestLine);


            if (contentBase.isEmpty()) {
                contentBase = Parser.getContentBase(requestLine);
            }

            if (!requestLine.isEmpty()) {
                cseq = Parser.getCseq(requestLine);
            }

            if (requestType == RtspConstants.OPTIONS) {
        		rtspResponse = new Options(cseq);


            } else if (requestType == RtspConstants.DESCRIBE) {
                buildDescribeResponse(requestLine);

            } else if (requestType == RtspConstants.SETUP) {
                buildSetupResponse(requestLine);
		                
            } else if (requestType == RtspConstants.PAUSE) {
                rtspResponse = new Pause(cseq);
		
            } else if (requestType == RtspConstants.TEARDOWN) {
                rtspResponse = new Teardown(cseq);
 
            } else if (requestType == RtspConstants.PLAY) {
                rtspResponse = new Play(cseq);	                
                ((Play) rtspResponse).setRange(Parser.getRangePlay(requestLine));

            } else {
	        	if( requestLine.isEmpty()){
	        		rtspResponse = new RtspError(cseq);
            
	        	} else {
                    rtspResponse = new RtspError(cseq);
	        	}
         
            }

            return requestType;
	    
	    }

	    private void buildSetupResponse(String requestLine) throws Exception {
	        
	    	rtspResponse = new Setup(cseq);
	        
	    	// client port
	    	clientPort = Parser.getClientPort(requestLine);	            
	    	((Setup) rtspResponse).setClientPort(clientPort);
	    	
	    	// transport protocol
            ((Setup) rtspResponse).setTransportProtocol(Parser.getTransportProtocol(requestLine));
            
            // session type
            ((Setup) rtspResponse).setSessionType(Parser.getSessionType(requestLine));

            ((Setup) rtspResponse).setClientIP(this.clientAddress.getHostAddress());
	            
            int[] interleaved = Parser.getInterleavedSetup(requestLine);
            if(interleaved != null){
                ((Setup) rtspResponse).setInterleaved(interleaved);
            }

	    }

       private void buildDescribeResponse(String requestLine) throws Exception{
                
    	   rtspResponse = new Describe(cseq);
                
    	   String fileName = Parser.getFileName(requestLine);
           boolean isFile  = Parser.isFile(fileName);

           if (isFile) {
               
        	   fileName = RtspConstants.DIR_MULTIMEDIA + fileName;
               ((Describe)rtspResponse).setFileName(fileName);
                
           } else {        	   
        	   ((Describe) rtspResponse).setFileName(fileName);
        	   
            }
                
            ((Describe)rtspResponse).setContentBase(contentBase);
        }

	}

}
