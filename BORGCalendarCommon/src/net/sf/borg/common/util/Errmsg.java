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
 
Copyright 2003 by ==Quiet==
 */

package net.sf.borg.common.util;

import javax.swing.JOptionPane;



// Errmsg provides standard handling of errors and notices with an option to
// output to screen vs. console.
// output of an error to the screen includes stack trace
public class Errmsg
{
    static
    {
        Version.addVersion("$Id$");
    }

    private static boolean console_ = false;   // error to stdout only
    
    // set flag for output to screen vs. console
	public static void console( boolean c )
    {
        console_ = c;
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
            return;
        }
        
        Object options[] = new Object[1];
        options[0] = Resource.getResourceString("OK");
        
        String showstack = Prefs.getPref(PrefName.STACKTRACE);
        if( showstack.equals("true"))
        {
            // pop up a window showing the exception and an option
            // to see a stack trace
            options = new Object[2];
            options[0] = Resource.getResourceString("OK");
            options[1] = Resource.getResourceString("Show_Stack_Trace");
        
        }
        
        int option = JOptionPane.showOptionDialog(null, e.toString(), Resource.getResourceString("Error"),
        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
        null, options, options[0]);
        
        if( option == 1 )
        {
            // show the stack trace
            java.io.ByteArrayOutputStream bao = new java.io.ByteArrayOutputStream();
            java.io.PrintStream ps = new java.io.PrintStream(bao);
            e.printStackTrace(ps);
            JOptionPane.showMessageDialog(null, bao.toString(), Resource.getResourceString("Stack_Trace"),
            JOptionPane.ERROR_MESSAGE);
            // dump to console too for cut & paste
            System.out.println(bao.toString());
        }
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
        JOptionPane.showMessageDialog(null, s, Resource.getResourceString("Notice"), JOptionPane.INFORMATION_MESSAGE);
        
    }
    
}
