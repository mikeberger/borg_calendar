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
package net.sf.borg.model.db.file.mdb;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sf.borg.common.util.VMap;
import net.sf.borg.common.util.Version;

/*
 * Row.java
 *
 * Created on December 31, 2003, 11:27 AM
 */
// Row objects are used to convert some java classes to and from a String for DB storage
// Row also provides get and set access using String field names. Row maps String field names
// to Class types using a Schema
public class Row
{
    static
    {
        Version.addVersion("$Id$");
    }

    private static SimpleDateFormat normalDateFormat_ = new SimpleDateFormat( "MM/dd/yyyy hh:mm aa" );
    private static SimpleDateFormat twoDigitYearFormat_ = new SimpleDateFormat( "MM/dd/yy hh:mm aa" );
   
    static {
    	normalDateFormat_.setLenient(false);
    }
    // ---------------------------------------------------------------------------
    // ROW
    // This class contains a collection of data that can be accessed by field name
    // ---------------------------------------------------------------------------
   
        private VMap map_;          // the actuall row data mapped by field name
        private Schema schema_;     // reference to the  schema 
        private int flags_;         // boolean user flags for the Row are kept
                                    // here - not in the VMap
        private int key_;           // integer key for the row
        
        private SimpleDateFormat dateformat_;
        
        // a Row must be created with respect to a particular Schema
        public Row(Schema sch) {
            
            map_ = new VMap();
            flags_ = 0;
            key_ = 0;
            schema_ = sch;
            dateformat_ = null;
        }
        
        public Row copy() {
            Row nr = new Row(null);
            nr.schema_ = this.schema_;
            nr.flags_ = this.flags_;
            nr.key_ = this.key_;
            nr.map_ = (VMap) this.map_.clone();
            nr.dateformat_ = this.dateformat_;
            
            return(nr);
        }
        
        public void normalize( boolean b )
        {
            if( b )
                dateformat_ = normalDateFormat_;
            else
                dateformat_ = null;
        }
        
        // get/set user flags
        public int getFlags() { return(flags_); }
        public void setFlags( int f ) { flags_ = f;}
        
        public boolean getFlag( int n ){
            int bit = flags_ & (0x01 << (n-1) );
            if( bit != 0 ) return( true );
            return( false );
        }
        
        public void setFlag( int n, boolean val )
        {
            int mask = 0x01 << (n-1);
            if( val )
                flags_ |= mask;
            else
                flags_ &= ~mask;
        }
        
        // get the key
        public int getKey(){ return(key_); }
        public void setKey(int k){ key_ = k; }
        //------------------------------------------------
        // the get functions below provide type specific
        // field access. Internally, all data is kept as
        // String data in a VMap object.
        // These get functions present field data in the correct
        // Java type.
        // 
        // They all use the generic getField to get the field
        // as an object. getField converts the field to
        // the proper object type. The other get functions
        // error if getField converts the type to something
        // other than what the user wants - the user must be
        // asking for the wrong type
        //---------------------------------------------------

        public Date getDate( String field ) throws Exception {
            Object o = getField( field );
            if( o == null ) return( null );
            if( o.getClass() != Date.class )
                throw new Exception( "Type Mismatch on Field " + field + ": " + o.getClass());
            return( (Date) o );
        }
        

        public String getString( String field ) throws Exception {
            Object o = getField( field );
            if( o == null ) return( null );
            if( o.getClass() != java.lang.String.class )
                throw new Exception( "Type Mismatch on Field " + field + ": " + o.getClass());
            return( (String) o );
        }
        
        public Vector getStringVector( String field ) throws Exception {
            Object o = getField( field );
            if( o == null ) return( null );
            if( o.getClass() != Vector.class )
                throw new Exception( "Type Mismatch on Field " + field + ": " + o.getClass());
            return( (Vector) o );
        }
        
        public Integer getInteger( String field ) throws Exception {
            Object o = getField( field );
            if( o == null ) return( null );
            if( o.getClass() != java.lang.Integer.class )
                throw new Exception( "Type Mismatch on Field " + field + ": " + o.getClass());
            return( (Integer) o );
        }
        /** Gets object i from a Row. Can be used to iterate over the items
         * in a row
        */
        public Object getObj(int i ) throws Exception {
            if( i > schema_.numCols() ) return null;
            String field = schema_.getField(i);
            return( getField( field ) );
        }        
        
        // getField does the real "Get" work
        // it converts the string representations in the VMap to the
        // type thst the user knows the fields by
        private Object getField( String field ) throws Exception {
            
            // get the named field as a String
            String s = (String) map_.get(field);
            
            if( s == null )
                return( null );
            
            // get the type of the field from the schema
            String type = schema_.getType(field);
            
            // can return a String as is
            if( type.equals("String"))
                return( s );
            
            // convert the String to a Data object
            // try to parse the date string using the various
            // formats used by SMDB over the years
            // the first format is the one currently used in SMDB
            // the old ones are for backward compatibility with old DB files
            if( type.equals("Date" )) {
                Date d = new Date();
                if( dateformat_ != null )
                {
                	// determine which format to use
                	// 2 or 4 digit year
                	if( s.indexOf(' ') == 8 )
                	{
                		d = twoDigitYearFormat_.parse(s);
                	}
                	else
                	{
                		d = dateformat_.parse(s);
                	}
                    return(d);
                }
                
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
                try
                { d = df.parse(s); }
                catch( Exception ex ) {
                    DateFormat df2 = DateFormat.getDateInstance(DateFormat.SHORT);
                    try
                    { d = df2.parse(s); }
                    catch( Exception ex2 ) {
                        // convert old format - transition case only - not
                        // 100% correct - only the author's DB is old enough to 
                        // need this (13 years old actually)
                        d.setTime( 1000* (Long.parseLong(s)+60*60*29));
                        GregorianCalendar g = new GregorianCalendar();
                        g.setTime(d);
                        //if( d.getHours() == 23 ) d.setHours(0);
                        if( g.get( Calendar.HOUR ) == 23 ) g.set( Calendar.HOUR, 0 );
                        d = g.getTime();
                        
                    }
                }
                return(d);
            }
            
            // convert string to Integer
            if( type.equals("Integer"))
                return( new Integer(s));
            
            // a StringVector is a list of strings
            // separated by commas - so pull out
            // each String and add them all into a Vector
            // and return
            if( type.equals("StringVector")) {
                StringTokenizer stk = new StringTokenizer(s,",");
                Vector vect = new Vector();
                while (stk.hasMoreTokens()) {
                    String stt = stk.nextToken();
                    if( !stt.equals("") )
                        vect.add(stt);
                }
                return(vect);
            }
            
            throw new Exception("Invalid Type in Schema: " + field + " " + type );
        }
        
        // set field will set a field in the VMap in String form given
        // the users object
        // it will make sure the users object type and the schema agree
        public void setField( String field, Object o ) throws Exception {
            
            String val = "";
            
            // a null object means remove the data from the Row
            if( o == null ) {
                map_.remove(field);
                return;
            }
            
            // verify that the object passed in matches the type identified in the schema
            // for this field
            Class c = o.getClass();
            Class c2 = schema_.getClass(field);
            if( c != c2 )
                throw new Exception("Invalid Object passed to setField: " + field + "=" + c2 + " " + c);
            
            // for Strings, just cast the object, no conversion needed
            if( c == java.lang.String.class) {
                val = (String) o;
            }
            // convert Integer to String
            else if( c == java.lang.Integer.class) {
                val = ((Integer)o).toString();
            }
            // convert date to a String
            else if( c == Date.class) {
                if( dateformat_ != null )
                {
                    val = dateformat_.format((Date) o );
                }
                else{
                    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
                    val = df.format((Date)o);
                }
            }
            // convert a Vector of Strings to a single comma separated String
            else if( c == Vector.class) {
                
		    Vector vect = (Vector)o;
		    for(int i = 0; i < vect.size(); i++) {
			String s = (String)vect.elementAt(i);
			val += s;
			val += ",";
		    }
                
            }
            
            // update the field in the VMap
            map_.remove(field);
            map_.put(field,val);
            
        }
        
        // convert the entire VMap for the row to a single String for DB storage
        public String passivate() {
            return( map_.passivate() );
        }
        
        // convert the Row to XML for exporting
        public String toXml() {
            return( map_.toXml() );
        }
        
        // activate a Row from a String from the DB
        public void activate( String s ) {
            map_.activate(s);
        }
    
    
        
}
