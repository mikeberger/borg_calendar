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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.XTree;
import net.sf.borg.model.AppointmentKeyFilter;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.file.mdb.MDB;
import net.sf.borg.model.db.file.mdb.SMDB;
import net.sf.borg.model.db.file.mdb.Schema;




class ApptFileDB extends FileDBCreator implements AppointmentKeyFilter
{
	ApptFileDB()
	{}
	
	// AppointmentKeyFilter overrides
	public final Collection getTodoKeys() throws Exception
	{
		return getKeys(F_TODO);
	}
	
	public final Collection getRepeatKeys() throws Exception
	{
		return getKeys(F_RPT);
	}
	
	// FileDBCreator overrides
	final void init(String file, boolean readonly, boolean shared)
		throws Exception
	{
		file = file + "/borg.jdb";
		
		// here is the schema for an appt with the field names and data types
		// SMDB will use this to manage each DB Appointment.
		// the boolean appt flags are shown near the top of the class
		Schema sch = new Schema();
		java.net.URL schurl = getClass().getResource("/resource/borg_schema.xml");
		XTree sch_xml = XTree.readFromURL(schurl);
		sch.setFromXML( sch_xml );
            
		// create the database if it does not exist
		boolean newdb = false;
		File fp = new File(file);
		if( !fp.exists() && !readonly)
		{
			Errmsg.notice(Resource.getResourceString("Creating_DB_file:_") + file );
			SMDB.create( "Borg Database", file, 60 , sch );
			newdb = true;
		}
            
		AppointmentAdapter adapter = new AppointmentAdapter();
            
		// open the database with correct locking mode
		// probably will never see readonly
		if( readonly )
			db_ = new FileBeanDB(file, MDB.READ_DIRTY, adapter, shared );
		else
		{
			try
			{
				db_ = new FileBeanDB(file, MDB.READ_WRITE, adapter, shared );
			}
			catch( DBException e )
			{
                    
				// oops - DB has no schema - not sure why
				// maybe very old DB - so update the schema
				if( e.getRetCode() == SMDB.RET_NO_SCHEMA )
				{
					SMDB.update_schema(file, sch, shared );
					db_ = new FileBeanDB(file, MDB.READ_WRITE, adapter, shared );
				}
				else
				{
					throw e;
				}
			}
		}
            
		FileBeanDB fdb = (FileBeanDB) db_;
		if( newdb )
		{
			fdb.setNormalize(true);
		}
		Schema oldsch = fdb.getSchema();
		try
		{
			oldsch.getType("REM");
		}
		catch( Exception e )
		{
			// DB does not have new category field - update the schema
			fdb.setSchema(sch);
		}
	}
	
	// private //
	private static final int F_RPT = 0x01;
	private static final int F_TODO = 0x02;

	private Collection getKeys(int mask) throws Exception
	{
		ArrayList l = new ArrayList();
		FileBeanDB fdb = (FileBeanDB) db_;
		Collection keycol = fdb.getKeys();
		Iterator keyiter = keycol.iterator();
            
		while( keyiter.hasNext() )
		{
			Integer ki = (Integer) keyiter.next();
			int key = ki.intValue();
			int flags = fdb.getFlags(key);
			if( ( flags & mask ) == 0 )
				continue;
			l.add( ki );
		}
		
		return l;
	}
}
