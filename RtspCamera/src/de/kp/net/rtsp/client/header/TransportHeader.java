package de.kp.net.rtsp.client.header;
/*
   Copyright 2010 Voice Technology Ind. e Com. Ltda.
 
   This file is part of RTSPClientLib.

    RTSPClientLib is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    RTSPClientLib is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with RTSPClientLib.  If not, see <http://www.gnu.org/licenses/>.

*/

import java.util.Arrays;
import java.util.List;


/**
 * Models a "Transport" header from RFC 2326. According to specification, there may be parameters, which will be inserted as a list of strings, which follow below:
 * <code>
   parameter           =    ( "unicast" | "multicast" )
                       |    ";" "destination" [ "=" address ]
                       |    ";" "interleaved" "=" channel [ "-" channel ]
                       |    ";" "append"
                       |    ";" "ttl" "=" ttl
                       |    ";" "layers" "=" 1*DIGIT
                       |    ";" "port" "=" port [ "-" port ]
                       |    ";" "client_port" "=" port [ "-" port ]
                       |    ";" "server_port" "=" port [ "-" port ]
                       |    ";" "ssrc" "=" ssrc
                       |    ";" "mode" = <"> 1\#mode <">
   ttl                 =    1*3(DIGIT)
   port                =    1*5(DIGIT)
   ssrc                =    8*8(HEX)
   channel             =    1*3(DIGIT)
   address             =    host
   mode                =    <"> *Method <"> | Method
   </code>
 * @author paulo
 *
 */
public class TransportHeader extends RtspHeader {
	
	public static final String NAME = "Transport";

	public static enum LowerTransport {
		TCP, UDP, DEFAULT
	};

	private LowerTransport transport;

	private List<String> parameters;

	public TransportHeader(String header)
	{
		super(header);
		String value = getRawValue();
		if(!value.startsWith("RTP/AVP"))
			throw new IllegalArgumentException("Missing RTP/AVP");
		int index = 7;
		if(value.charAt(index) == '/')
		{
			switch(value.charAt(++index))
			{
			case 'T':
				transport = LowerTransport.TCP;
				break;
			case 'U':
				transport = LowerTransport.UDP;
				break;
			default:
				throw new IllegalArgumentException("Invalid Transport: "
						+ value.substring(7));
			}
			index += 3;
		} else
			transport = LowerTransport.DEFAULT;
		if(value.charAt(index) != ';' && index != value.length())
			throw new IllegalArgumentException("Parameter block expected");
		addParameters(value.substring(++index).split(";"));
	}

	public TransportHeader(LowerTransport transport, String... parameters)
	{
		super(NAME);
		this.transport = transport;
		addParameters(parameters);
	}

	public String getParameter(String part)
	{
		for(String parameter : parameters)
			if(parameter.startsWith(part))
				return parameter;
		throw new IllegalArgumentException("No such parameter named " + part);
	}

	void addParameters(String[] parameterList)
	{
		if(parameters == null)
			parameters = Arrays.asList(parameterList);
		else
			parameters.addAll(Arrays.asList(parameterList));
	}

	LowerTransport getTransport()
	{
		return transport;
	}

	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder(NAME).append(": ").append("RTP/AVP");
		if(transport != LowerTransport.DEFAULT)
			buffer.append('/').append(transport);
		for(String parameter : parameters)
			buffer.append(';').append(parameter);
		return buffer.toString();
	}
}
