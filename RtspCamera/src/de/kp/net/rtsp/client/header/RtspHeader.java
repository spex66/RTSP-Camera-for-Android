package de.kp.net.rtsp.client.header;
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

public class RtspHeader {
	
	private String name;
	private String value;

	/**
	 * Constructs a new header.
	 * 
	 * @param header
	 *          if the character ':' (colon) is not found, it will be the name of
	 *          the header. Otherwise, this constructor parses the header line.
	 */
	public RtspHeader(String header) {
		
		int colon = header.indexOf(':');
		if(colon == -1)
			name = header;
		
		else {
			name = header.substring(0, colon);
			value = header.substring(++colon).trim();
		}
	}
	
	public RtspHeader(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getRawValue() {
		return value;
	}

	public void setRawValue(String value) {
		this.value = value;
	}

	public String toString() {
		return name + ": " + value;
	}

	public boolean equals(Object obj) {
		
		if(super.equals(obj))
			return true;
		
		if(obj instanceof String)
			return getName().equals(obj);
		
		if(obj instanceof RtspHeader)
			return getName().equals(((RtspHeader) obj).getName());
		
		return false;
	
	}

	protected final void checkName(String expected) throws Exception {

		if(expected.equalsIgnoreCase(getName()) == false)
			throw new Exception("[Header Mismatch] - Expected: " + expected + " Retrieved: " + getName());

	}

	protected final void setName(String name) {
		value = this.name;
		this.name = name;
	}
	
}
