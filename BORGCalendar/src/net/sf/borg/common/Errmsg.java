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

package net.sf.borg.common;

import net.sf.borg.ui.util.ScrolledDialog;





/**
 * standard error handling for Borg
 */
public class Errmsg
{

	/** output to console only flag */
	private static boolean console_ = false;   // error to stdout only
    
	/**
     * set console output only.
     * 
     */
    public static void console( boolean c )
    {
        console_ = c;
    }
	
    /**
	 * Output an exception to the user.
	 * 
	 * @param e the e
	 */
	public static void errmsg( Exception e )
    {
        
		// treat a warning differently - just show its text
        if( e instanceof Warning )
        {
            notice(e.getMessage());
            return;
        }
        
        if( console_ )
        {
            // just send console output to stdout and exit
            System.out.println(e.toString() );
            e.printStackTrace();
            return;
        }
        
        ScrolledDialog.showError(e);
       
    }
    
    /**
     * output a notice/warning - just shows text
     * 
     * @param s the text to show
     */
    public static void notice( String s )
    {
        
        if( console_ )
        {
            // console only
            System.out.println(s);
            return;
        }
        
        ScrolledDialog.showNotice(s);
    }
    
}
