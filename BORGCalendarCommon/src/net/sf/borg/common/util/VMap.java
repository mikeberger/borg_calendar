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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

// VMap is just a HashMap that can convert to and from a String
// format that I like. It also exports to XML
// format is VMAP{ENTRY1=VALUE1;ENTRY2=VALUE2;....}
public class VMap extends HashMap {
    static
    {
        Version.addVersion("$Id$");
    }    
    public String passivate() {
        StringBuffer buf = new StringBuffer();
        buf.append("VMAP{");
        Iterator it = entrySet().iterator();
        while( it.hasNext() ) {
            Entry me = (Entry) it.next();
            if( me.getValue() == null || me.getValue().equals(""))
            	continue;
            buf.append((String)me.getKey() + "=");
            String val = (String) me.getValue();
            for( int i = 0; i < val.length(); i++ ) {
                char c = val.charAt(i);
                if( c == '\\' || c == ';' )
                    buf.append( '\\' );
                buf.append(c);
            }
            buf.append(';');
        }
        buf.append('}');
        return( buf.toString() );
    }
    
    public String toXml() {
        StringBuffer buf = new StringBuffer();
        buf.append("<VMAP>");
        Iterator it = entrySet().iterator();
        while( it.hasNext() ) {
            Entry me = (Entry) it.next();
            if( me.getValue() == null || me.getValue().equals(""))
            	continue;
            buf.append("<" + (String)me.getKey() + ">");
            String val = (String) me.getValue();
            byte b[] = val.getBytes();
            for( int i = 0; i < b.length; i++ ) {
                
                if( b[i] == '<' )
                    buf.append( "&lt;" );
                else if( b[i] == '>' )
                    buf.append( "&gt;" );
                else if( b[i] == '&' )
                    buf.append( "&amp;" );
                else
                    buf.append((char)b[i]);
            }
            
            buf.append("</" + (String)me.getKey() + ">");
        }
        
        buf.append("</VMAP>");
        return( buf.toString() );
    }
    
    public void activate(String s) {
        clear();
        StringBuffer buf = new StringBuffer();
        String key = "";
        String val;
        
        int start = 0;
        int end = s.length();
        
        if( s.startsWith("VMAP{") )
            start = 5;
        
        if( s.endsWith("}") )
            end = s.length() - 1;
        
        boolean in_data = false;
        
        for( int i = start; i < end; i++ ) {
            char c = s.charAt(i);
            if( c == '\\' ) {
                i++;
                buf.append(s.charAt(i));
                continue;
            }
            
            
            if( c == '=' && !in_data ) {
                in_data = true;
                key = buf.toString();
                buf.setLength(0);
                continue;
            }
            
            if( c == ';' ) {
                in_data = false;
                val = buf.toString();
                buf.setLength(0);
                put(key,val);
                //System.out.println(key + "|" + val );
                continue;
            }
            
            buf.append(c);
            
        }
    }
}
