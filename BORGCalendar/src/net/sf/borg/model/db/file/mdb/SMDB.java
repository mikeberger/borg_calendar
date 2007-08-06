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
package net.sf.borg.model.db.file.mdb;

import java.util.Collection;

import net.sf.borg.common.VMap;
import net.sf.borg.model.db.DBException;



/** Class SMDB uses an MDB to provides a database with
 * the concept of a schema. 
 * SMDB uses MDB to manage a database of Row objects. Row objects
 * are generic data containers that can contain basic Java types.
 *
 */
// SMDB pretty much provides a database where all rows have the same
// schema and are keyed by an integer key. Row data can be accessed by
// field name. SMDB will return rows as Row objects. These objects
// can set and get fields using the field names from the database schema
// The underlying implementation is still text based. SMDB converts Row
// objects to and from a text representation. This allows rows to vary in size,
// and the String and StringVector types therefore have unlimited size.
// Each Row also has the 16 boolean user flags available that MDB supports.

// in addition, SMDB provides DB level options. These are pretty much
// text fields that can be stored not in the Rows, but at a global level
// for the DB file. So a user can store and retrieve an option by name (i.e. "ABC") in a SMDB.
public class SMDB
{
    private MDB db_;
    protected Schema schema_;     // the schema for the DB
    
    protected static final int OPTIONS=2000000005;
    protected static final int SCHEMA=2000000004;

        
    private VMap options_;      // a map of option names to values
    private boolean hasOpts_;   // flag indicating the presence of options
    protected boolean normalize_; // normalize Data
    
    // No schema record was found or else it failed to parse
    // extra BeanDB specific return code for MDBException
    public static final int RET_NO_SCHEMA = 100;
    
    public SMDB(String file, int locktype, boolean shared) throws DBException
    {
        db_ = new MDB(file, locktype, shared);
        

        schema_ = new Schema();
        
        // read the schema, which is kept in a system record
        // keyed using the SCHEMA key
        String s;
        try
        {
            s = db_.readSys( SCHEMA );
        }
        catch( DBException e )
        {
            throw new DBException("Failed to read schema", RET_NO_SCHEMA );
        }
        
        try
        {
            // init the schema object from the schema raw string data
            schema_.set(s);
            
            options_ = new VMap();
            hasOpts_ = false;
            
            // get any options - which are kept in a system record keyed using the OPTIONS key
            
            s = db_.readSys( OPTIONS );
            
            // activate the options map from the raw options string
            options_.activate(s);
            hasOpts_ = true;
            
            String norm = getOption("NORM");
            if( norm != null && norm.equals("Y") )
                normalize_ = true;
            else
                normalize_ = false;
            
            
        }
        catch( DBException me )
        {
            if( me.getRetCode() != DBException.RET_NOT_FOUND )
                throw me;
        }
        catch( Exception e )
        {
            throw new DBException(e.toString(), RET_NO_SCHEMA);
        }      
        
    }
    
    public void setNormalize(boolean b) throws Exception
    {
        normalize_ = b;
        if(b)
            setOption("NORM", "Y");
        else
            setOption("NORM", "N");
    }
    
    public boolean getNormalize()
    {
        return( normalize_);
    }

    // get the Schema
    public Schema getSchema()
    {
        return( schema_ );
    }
    
    // set the schema
    public void setSchema(Schema schema) throws Exception
    {
        db_.updateSys( SCHEMA, schema.toString() );
        schema_ = schema;
    }
    
    
    //static member to add a schema to a DB file
    static public void update_schema( String filename, Schema schema, boolean shared ) throws DBException
    {
        MDB db = new MDB( filename, MDB.ADMIN, shared );
        db.addSys( SCHEMA, schema.toString() );
        db.close();
    }
    
    static public void create(  String dbname,  String filename, int blocksize, Schema schema) throws Exception
    {
        // create a regular MDB database
        MDB.create( dbname, filename, blocksize );
        
        // make it a BeanDB database by adding a schema
        update_schema( filename, schema, false );
    }
    
 
    // gets the raw Option data - not for the end user
    String optString()
    { /* package level access */
        if( !hasOpts_ )
            return(null);
        
        return( options_.toString() );
    }
    
    public Collection optionKeys()
    {
        return( options_.keySet());
    }
    
    /** get user option */
    public String getOption( String oname )
    {
        return( (String) options_.get(oname));
    }
    
    
    /** set a user option */
    public void setOption( String oname, String value ) throws DBException
    {
        options_.put( oname, value );
        
        String s = options_.passivate();
        
        if( hasOpts_ )
        {
            db_.updateSys( OPTIONS, s );
        }
        else
        {
            db_.addSys( OPTIONS, s );
            hasOpts_ = true;
        }      
        
    }
 
    

    // read an Row from the DB given the key
    public Row readRow( int key ) throws DBException
    {
        
        // read the raw String data for the row
        String s = db_.read( key );
        Row sr = new Row(schema_);
        sr.setKey(key);
        
        // activate the Row from the String data
        sr.normalize(normalize_);
        sr.activate(s);
        
        // set any flags in the Row - they do not come from the String data
        sr.setFlags( db_.getFlags(key) );
        
        
        // return a copy of the Row - so that changes to the Row
        // do not update the cached Row
        return sr.copy();
    }
    
    /** Factory method to allocate a new Row for later storage in the SMDB */
    // what makes this needed is the need to associate the schema from the DB
    // to the Row
    public Row newRow()
    {
        return (new Row(schema_));
    }
    
    // add a Row unencrypted
    public void addRow( int key, Row sr ) throws DBException
    {
        addRow(key,sr,false);
    }
    
    // add a Row that has been filled in by the caller
    public void addRow( int key, Row sr, boolean crypt ) throws DBException
    {
        
        // make sure the row reflects the key being added
        sr.setKey(key);
        
        // passivate the SMDB Row into a String
        sr.normalize(normalize_);
        String s = sr.passivate();
        
        
        // add the record using the MDB API
        db_.add( key, sr.getFlags(), s, crypt );
        
    }
    
    // update unencrypted
    public void updateRow( int key, Row sr ) throws DBException, Exception
    {
        updateRow(key,sr,false);
    }
    
    // update a Row
    public void updateRow( int key, Row sr, boolean crypt ) throws DBException, Exception
    {
        
        // delete the record first
        delete(key);
        
        // call addObj
        addRow( key, sr, crypt );
    }
    
    // the following methods are currently just invoking the corresponding MDB method.
    //public int first() throws MDBException { return( db_.first() ); }
    //public int next() throws MDBException { return( db_.next() ); }
    public Collection getKeys() { return( db_.keys()); }
    public void close(){ db_.close(); }
    //public int getFlags() throws MDBException { return( db_.getFlags() ); }
    public int getFlags(int key) throws DBException { return( db_.getFlags(key) ); }
    public int nextkey(){ return db_.nextkey(); }
    public void delete( int key ) throws Exception { db_.delete(key); }

    public boolean isMDBDirty() throws DBException
    {
    	return db_.isDirty();
    }
    
    public void syncMDB() throws DBException
    {
        db_.sync();
    }
}


