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
/*
 * Schema.java
 *
 * Created on December 31, 2003, 11:29 AM
 */

package net.sf.borg.model.db.file.mdb;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import net.sf.borg.common.util.VMap;
import net.sf.borg.common.util.XTree;





/**
 *
 * @author  mbb
 */

// Class Schema keeps a mapping of field names to types
// It is used by class Row to determine the Class for conversion
// of field data to and from String
// It is used by genMDBObject to generate KeyedBean and FileBeanAdapter classes
public class Schema
{
    // ---------------------------------------------------------------------------
    // SCHEMA
    // ---------------------------------------------------------------------------

    
    // the mapping of type names to java classes
    // these are the types supported by SMDB
    static private HashMap typeMap_;
    static
    {
        typeMap_ = new HashMap();
        typeMap_.put( "String", java.lang.String.class );
        typeMap_.put( "Integer", java.lang.Integer.class );
        typeMap_.put( "Date", Date.class );
        typeMap_.put( "StringVector", Vector.class );
    };
    
    // VMap is pretty much a Java Map. It holds the schema's
    // fieldname to type mapping
    private VMap map_;
    
    private boolean init_ = false;
    
    // gets the type of a given field
    public String getType(String field) throws Exception
    {
        String t = (String)map_.get(field);
        if( t == null ) throw new Exception( "Field: " + field + " not found in schema" );
        return( t );
    }
    
    // gets the class used to represent a given field
    public Class getClass(String field) throws Exception
    {
        return( (Class) typeMap_.get(getType(field)) );
    }
    
    // get the nth field name in the schema
    public String getField(int n )
    {
        if( n > map_.size()) return null;
        
        Object o[] = map_.entrySet().toArray();
        Map.Entry me = (Map.Entry) o[n-1];
        
        return( (String) me.getKey() );
    }
    
    // get the number of fields in the schema
    public int numCols()
    {
        return( map_.size() );
    }
    
    public Schema()
    {
        map_ = new VMap();
    }
    
    // add a field to the schema of a given type
    // error if the schema is already used by a DB - cannot change
    // the schema read from an active DB. you can add to schemas
    // that have yet to be written out to a DB
    // this is because all of the active Row objects from a DB will
    // be using the same schema object
    public void add( String field, String type ) throws Exception
    {
        if( init_ == true )
            throw new Exception( "In-use Schema cannot be changed" );
        map_.put( field, type );
    }
    
    // output schema as text (to be stored in DB in a SCHEMA system Row)
    public String toString()
    {
        
        return( map_.passivate() );
    }
    
    // init a schema from its text form 
    public void set( String txt ) throws Exception
    {
        init_ = true;
        map_.activate(txt);
    }
    
    // this reads a schema from a URL in **XML**
    public void setFromXML( XTree xt ) throws Exception
    {            
               
        for( int i = 1;; i++ )
        {
            XTree field = xt.child("field",i);
            if( !field.exists()  )
                break;
 
            XTree name = field.child("name");
            if( !name.exists() )
                throw new Exception("Cannot parse Schema XML - field found with no name");
            XTree type = field.child("type");
            if( !type.exists() )
                throw new Exception("Cannot parse Schema XML - field " + name.value() + " found with no type");
            add( name.value(), type.value() );
            //System.out.println(name.value() + "--" + type.value() );
        }
    }
    
    
}
