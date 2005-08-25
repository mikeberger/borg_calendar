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
package net.sf.borg.model.db.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.sf.borg.model.BorgOption;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.KeyedBean;
import net.sf.borg.model.db.file.mdb.Row;
import net.sf.borg.model.db.file.mdb.SMDB;


// FileBeanDB is a layer on top of SMDB. It reads Row records
// from the underlying SMDB database. The user passes in a FileBeanAdapter which will
// be used to convert between the Rows and the corresponding KeyedBean object.
//
// An XML schema for the DB is used when creating the DB. The Row objects
// use this schema to translate to and from a String. A code generator
// also uses this schema to generate the FileBeanAdapter and KeyedBean classes.
//
// A FileBeanDB will use a KeyedBean subclass to communicate data to callers.

class FileBeanDB extends SMDB implements BeanDB
{
    private FileBeanAdapter adapter_; // adapter for converting Rows to DataBeans
    private boolean objectCacheOn_;  // is caching on? 
    private HashMap objectCache_;  // the cache

    FileBeanDB(String file, int locktype, FileBeanAdapter a, boolean shared) throws DBException
    {
        super(file, locktype, shared); 
        objectCacheOn_ = true;
        objectCache_ = new HashMap();
        
        adapter_ = a;
    }
    
    // turn on caching
    public void cacheOn(boolean b)
    {
        objectCacheOn_ = b;
    }
    
	// read all beans from the DB
	public Collection readAll() throws DBException, Exception
	{
		List lst = new ArrayList();
		Iterator itr = getKeys().iterator();
		while (itr.hasNext())
		{
			Integer ki = (Integer) itr.next();
			lst.add(readObj(ki.intValue()));
		}
		return lst;
	}

    // read a bean from the DB given the key
    public KeyedBean readObj( int key ) throws DBException, Exception
    {
        
        // if the bean is in the cache - return it
        if( objectCacheOn_ )
        {
            Object o = objectCache_.get( new Integer(key) );
            
            if( o != null )
            {
                KeyedBean r = (KeyedBean) o;
                return r.copy();
            }
        }
        
        Row sr = readRow( key );

        KeyedBean bean = adapter_.fromRow(sr);
        
        // put the bean in the cache
        if( objectCacheOn_ )
        {
            objectCache_.put( new Integer(key), bean );
        }
        
        // return a copy of the bean - so that changes to the bean
        // do not update the cached bean
        return bean.copy();
    }
    
    public KeyedBean newObj()
    {
        return (adapter_.newBean());
    }
    
    // add a bean unencrypted
    public void addObj( KeyedBean bean ) throws DBException, Exception
    {
        addObj(bean,false);
    }
    
    // add a bean that has been filled in by the caller
    public void addObj( KeyedBean bean, boolean crypt ) throws DBException, Exception
    {
        
        Row sr = adapter_.toRow( schema_, bean, normalize_ );

        addRow( sr.getKey(), sr, crypt );

        // put a copy of the bean in the cache
        if( objectCacheOn_ )
        {
            objectCache_.put( new Integer(bean.getKey()), bean.copy() );
        }
    }
    
    // update unencrypted
    public void updateObj( KeyedBean bean ) throws DBException, Exception
    {
        updateObj(bean,false);
    }
    
    // update a bean
    public void updateObj( KeyedBean bean, boolean crypt ) throws DBException, Exception
    {
        
        // delete the record first
        delete(bean.getKey());
        
        // call addObj
        addObj( bean, crypt );
    }
    
    public void delete( int key ) throws Exception
    {
        
        // remove the bean from the cache
        if( objectCacheOn_ )
        {
            objectCache_.remove( new Integer(key) );
        }
        
        // call MDB.delete()
        super.delete(key);
    }

    public final synchronized boolean isDirty() throws DBException
    {
    	return isMDBDirty();
    }
    
    public void sync() throws DBException {
        syncMDB();
        objectCache_ = new HashMap();
    }
    
    public Collection getOptions() throws Exception {
    	List lst = new ArrayList();
    	Iterator itr = optionKeys().iterator();
    	while (itr.hasNext())
    	{
    		String oname = (String) itr.next();
    		lst.add(new BorgOption(oname, getOption(oname)));
    	}
        return lst;
    }
    
	public void setOption( BorgOption option ) throws Exception
	{
		setOption(option.getKey(), option.getValue());
	}
}


