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




// Errmsg provides standard handling of errors and notices with an option to
// output to screen vs. console.
// output of an error to the screen includes stack trace
public class Errmsg
{

    private static boolean console_ = false;   // error to stdout only
    
    // set flag for output to screen vs. console
	public static void console( boolean c )
    {
        console_ = c;
    }
	
	public static boolean console()
	{
		return console_;
	}
    
    // output an exception
    public static void errmsg( Exception e )
    {
        
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
    
    // output an informational notice
    public static void notice( String s )
    {
        
        if( console_ )
        {
            // console only
            System.out.println(s);
            return;
        }
        
        // info popup
        //JOptionPane.showMessageDialog(null, s, Resource.getResourceString("Notice"), JOptionPane.INFORMATION_MESSAGE);
        ScrolledDialog.showNotice(s);
    }
    
}
