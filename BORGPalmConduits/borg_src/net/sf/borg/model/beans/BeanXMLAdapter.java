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
package net.sf.borg.model.beans;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import net.sf.borg.common.VMap;
import net.sf.borg.common.XTree;

/**
 *
 * @author  mbb
 */
// BeanXMLAdapters are used by BeanDB to convert KeyedBeans to and from Row objects
// or XML
abstract public class BeanXMLAdapter
{
    
    abstract public XTree toXml( KeyedBean bean );
    abstract public KeyedBean fromXml( XTree xt );
    
    private static SimpleDateFormat normalDateFormat_ = new SimpleDateFormat( "MM/dd/yy hh:mm aa" );
    protected static String toString( Date d )
    {
        return( normalDateFormat_.format(d) );
    }
    
    protected static String toString( boolean b )
    {
        if( b ) return("true");
        return("false");
    }
    
    protected static Date toDate( String s )
    {
        
        try{
            Date d = normalDateFormat_.parse(s);
            return(d);
        }
        catch( Exception e )
        {
            return( null );
        }
    }
    
    protected static boolean toBoolean( String s )
    {
        if( s.equals("true") ) return( true );
        return( false );
    }
   
    protected static String toString( Integer i )
    {
        return(i.toString());
    }
    
    protected static Integer toInteger( String s )
    {
        Integer i = null;
        try{
            i = Integer.decode(s);
        }
        catch ( Exception e )
        {
            return(null);
        }
        return( i );
    }
    
    protected static int toInt( String s )
    {
        int i = 0;
        try{
            i = Integer.parseInt(s);
        }
        catch ( Exception e )
        {
            return(0);
        }
        return( i );
    }
    // yuck
    protected static String toString( Vector v )
    {
        VMap vm = new VMap();
        for( int i = 0; i < v.size(); i++ )
            vm.put( v.elementAt(i), "v" );
        
        return(vm.passivate());
    }
    
    protected static Vector toVector( String s )
    {
        if( s == null || s.equals("") )
            return( null );
        VMap vm = new VMap();
        vm.activate(s);
        Vector v = new Vector( vm.keySet() );
        
        return( v );
    }
}

