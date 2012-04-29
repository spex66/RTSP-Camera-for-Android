package de.kp.net.rtsp.client.transport;
/**
 *	Copyright 2010 Voice Technology Ind. e Com. Ltda.
 *
 *	RTSPClientLib is free software: you can redistribute it and/or 
 *	modify it under the terms of the GNU Lesser General Public License 
 *	as published by the Free Software Foundation, either version 3 of 
 *	the License, or (at your option) any later version.
 *
 *	RTSPClientLib is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details. 
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with this software. If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 *	This class has been adapted to the needs of the RtspCamera project
 *	@author Stefan Krusche (krusche@dr-kruscheundpartner.de)
 *
 */

import de.kp.net.rtsp.client.api.Message;
import de.kp.net.rtsp.client.api.Transport;
import de.kp.net.rtsp.client.api.TransportListener;

class TCPTransportListener implements TransportListener {
	
	private final TransportListener behaviour;

	public TCPTransportListener(TransportListener theBehaviour) {
		behaviour = theBehaviour;
	}

	@Override
	public void connected(Transport t) {
		if (behaviour != null)
			try {
				behaviour.connected(t);
			
			} catch(Throwable error) {
				behaviour.error(t, error);
			}
	}

	@Override
	public void dataReceived(Transport t, byte[] data, int size) {
		
		if (behaviour != null)
			try {
				behaviour.dataReceived(t, data, size);
			
			} catch(Throwable error) {
				behaviour.error(t, error);
			}
	}

	@Override
	public void dataSent(Transport t) {
		// TODO Auto-generated method stub
		if (behaviour != null)
			try {
				behaviour.dataSent(t);
			
			} catch(Throwable error) {
				behaviour.error(t, error);
			}

	}

	@Override
	public void error(Transport t, Throwable error) {
		if (behaviour != null)
			behaviour.error(t, error);
	}

	@Override
	public void error(Transport t, Message message, Throwable error) {
		if(behaviour != null)
			behaviour.error(t, message, error);
	}

	@Override
	public void remoteDisconnection(Transport t) {
		if (behaviour != null)
			try {
				behaviour.remoteDisconnection(t);
			
			} catch(Throwable error) {
				behaviour.error(t, error);
			}
	
	}

}
