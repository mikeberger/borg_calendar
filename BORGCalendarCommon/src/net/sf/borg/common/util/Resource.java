/*
This file is part of BORG.

	BORG is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	BORG is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with BORG; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Copyright 2003 by Mike Berger
 */
package net.sf.borg.common.util;

import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;


public class Resource
{
	private static String version_ = null;

	static public String getResourceString( String key ) {
	    String res = ResourceBundle.getBundle("resource/borg_resource").getString(key);

	    if( res.indexOf("\\n") == -1 )
	        return( res );

	    StringBuffer sb = new StringBuffer();
	    for( int i = 0; i < res.length(); i++) {

	        if( res.charAt(i) == '\\' && (i < res.length() - 1) && res.charAt(i+1) == 'n') {
	            i++;
	            sb.append('\n');
	        }
	        else {
	            sb.append( res.charAt(i));
	        }
	    }

	    //System.out.println(res);
	    //System.out.println( sb.toString());
	    return( sb.toString() );
	}

	static public String getVersion()
	{
		if( version_ == null)
		{
			try {
				// get the version and build info from a properties file in the jar file
				InputStream is = Resource.class.getResource("/properties").openStream();
				Properties props = new Properties();
				props.load(is);
				is.close();
				version_ = props.getProperty("borg.version");
			}
			catch( Exception e ) {
				Errmsg.errmsg(e);
			}
		}

		return( version_ );
	}
}
