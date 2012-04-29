package de.kp.net.rtsp.client.api;
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

/**
 * Listener for transport events. Implementations of {@link Transport}, when
 * calling a listener method, must catch all errors and submit them to the
 * error() method.
 */
public interface TransportListener {
	
	public void connected(Transport t) throws Throwable;

	public void error(Transport t, Throwable error);

	public void error(Transport t, Message message, Throwable error);

	public void remoteDisconnection(Transport t) throws Throwable;

	public void dataReceived(Transport t, byte[] data, int size) throws Throwable;

	public void dataSent(Transport t) throws Throwable;
}
