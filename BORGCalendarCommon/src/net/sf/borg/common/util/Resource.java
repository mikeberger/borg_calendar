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

import javax.swing.KeyStroke;

public class Resource
{
	private static String version_ = null;

	public static String getResourceString( String key ) {
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

	public static String getPlainResourceString(String resourceKey)
	{
		ComponentParms parms = parseParms(resourceKey);
		return parms.getText();
	}

	public static String getVersion()
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

	public static ComponentParms parseParms(String resourceKey)
	{
		String parmsText = getResourceString(resourceKey);
		
		if (parmsText.startsWith("Goto"))
			parmsText = parmsText.substring(0);

		String text = parmsText;
		int mnemonic = -1;
		KeyStroke accel = null;
		int pos;
		if ((pos = parmsText.indexOf('|')) != -1)
		{
			text = parmsText.substring(0,pos);
			String parmsTextRem = parmsText.substring(pos+1);
			String mnemonicText = parmsTextRem;

			if ((pos = parmsTextRem.indexOf('|')) != -1)
			{
				mnemonicText = parmsTextRem.substring(0,pos);
				String accelText = parmsTextRem.substring(pos+1);
				accel = KeyStroke.getKeyStroke(accelText);
			}

			if (mnemonicText.length() > 0)
				mnemonic = KeyStroke.getKeyStroke(mnemonicText).getKeyCode();
		}
		return new ComponentParms(text,mnemonic,accel);
	}

////////////////////////////////////////////////////////////////
// nested class ComponentParms

	public static class ComponentParms
	{
		public final int getKeyEvent()
		{
			return keyEvent;
		}
	
		public final KeyStroke getKeyStroke()
		{
			return keyStroke;
		}
	
		public final String getText()
		{
			return text;
		}
	
	// "internal" //
		public ComponentParms(String text, int keyEvent, KeyStroke keyStroke)
		{
			this.text = text;
			this.keyEvent = keyEvent;
			this.keyStroke = keyStroke;
		}
	
	// private //
		private String text;
		private int keyEvent;
		private KeyStroke keyStroke;
	}

// end nested class ComponentParms
////////////////////////////////////////////////////////////////

}
