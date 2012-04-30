package de.kp.net.rtp;

/*
 * Copyright (C) 2009 The Sipdroid Open Source Project
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of Sipdroid (http://www.sipdroid.org)
 * 
 * Sipdroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.io.IOException;


/**
 * RtpSocket implements a RTP socket for receiving and sending RTP packets.
 * <p>
 * RtpSocket is associated to a DatagramSocket that is used to send and/or
 * receive RtpPackets.
 */
public class RtpSocket {

	/** UDP socket */
	DatagramSocket socket;
	DatagramPacket datagram;
	
	/** Remote address */
	InetAddress remoteAddress;

	/** Remote port */
	int remotePort;

	/** 
	 * An RtpSocket may be suspended from sending or receiving
	 * UDP data packets
	 */
	
	boolean suspended = false;
	
	/** Creates a new RTP socket (sender and receiver) 
	 * @throws SocketException */
	public RtpSocket(InetAddress remoteAddress, int remotePort) throws SocketException {
		
		this.socket = new DatagramSocket();
		this.socket.connect(remoteAddress, remotePort);
		
		this.remoteAddress = remoteAddress;
		this.remotePort    = remotePort;
		
		datagram = new DatagramPacket(new byte[1],1);
	
	}

	/** Creates a new RTP socket (sender and receiver) **/
	public RtpSocket(DatagramSocket socket, InetAddress remoteAddress, int remotePort) {
		
		this.socket = socket;
		
		// initialize receiver address & port
		this.remoteAddress = remoteAddress;
		this.remotePort    = remotePort;
		
		datagram = new DatagramPacket(new byte[1],1);
	
	}

	/** Returns the RTP DatagramSocket */
	public DatagramSocket getSocket() {
		return this.socket;
	}

	/** Receives a RTP packet from this socket */
	public void receive(RtpPacket rtpPacket) throws IOException {

		datagram.setData(rtpPacket.getPacket());
		datagram.setLength(rtpPacket.packet.length);
		
		socket.receive(datagram);
		if (!socket.isConnected())
			socket.connect(datagram.getAddress(), datagram.getPort());
		
		rtpPacket.packet_len = datagram.getLength();
	
	}

	/** Sends a RTP packet from this socket */
	public void send(RtpPacket rtpPacket) throws IOException {

		if (this.suspended == true) return;
		
		datagram.setData(rtpPacket.getPacket());
		datagram.setLength(rtpPacket.getLength());
		
		datagram.setAddress(remoteAddress);
		datagram.setPort(remotePort);
		
		socket.send(datagram);
	
	}

	/** Sends a RTP packet from this socket */
	public void send(byte[] data) throws IOException {

		if (this.suspended == true) return;
		
		datagram.setData(data);
		datagram.setLength(data.length);
		
		datagram.setAddress(remoteAddress);
		datagram.setPort(remotePort);
		
		socket.send(datagram);
	
	}
	
	public void suspend(boolean suspended) {
		this.suspended = suspended;
	}
	
	/** Closes this socket */
	public void close() { // socket.close();
	}

}
