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

package net.sf.borg.model.db.remote;

import net.sf.borg.model.beans.Address;
import net.sf.borg.model.beans.Appointment;
import net.sf.borg.model.beans.Task;
import net.sf.borg.model.db.ApptCachingBeanDB;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.CachingBeanDB;
import net.sf.borg.model.db.IBeanDataFactory;
import net.sf.borg.model.db.MemoDB;

/**
 * A singleton instance which creates class-specific {@link BeanDB BeanDB}
 * instances.
 * @author Mohan Embar
 */
public class RemoteBeanDataFactory implements IBeanDataFactory
{
	/**
	 * Singleton.
	 */
	public static RemoteBeanDataFactory getInstance()
	{
		return instance;
	}


	public final BeanDB create(Class cls, String url, String username)
			throws Exception
	{
		String clsstr;
		if (cls == Address.class)
			clsstr = "Address";
		else if (cls == Task.class)
			clsstr = "Task";
		else if (cls == Appointment.class)
			clsstr = "Appointment";
		else
			throw new IllegalArgumentException(cls.getName());
			
		// Hack off the leading "remote:" to get the implementation.
		int nColon = url.indexOf(':');
		String file = url.substring(nColon+1);
		
		BeanDB db = null;
		
		if (cls == Appointment.class)
			db = new ApptCachingBeanDB(new ApptRemoteBeanDB(clsstr, file,
					username));
		else if (cls == Task.class)
			db = new TaskRemoteBeanDB(cls, clsstr, file, username);
		else
			db = new CachingBeanDB(new RemoteBeanDB(cls, clsstr, file,
					username));
		

		
		return db;
	}

	// private //
	private static final RemoteBeanDataFactory instance = new RemoteBeanDataFactory();

	private RemoteBeanDataFactory()
	{
	}

	public MemoDB createMemoDB(String url, String username) throws Exception {
	    // memo db not yet supported for remote HTTP server
	    if( url.indexOf("http:") != -1)
		return null;
	    return new RemoteMemoDB(url,username);
	}
}
