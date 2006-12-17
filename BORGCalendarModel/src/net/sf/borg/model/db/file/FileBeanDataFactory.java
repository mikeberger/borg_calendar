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

import net.sf.borg.model.Address;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.Task;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.IBeanDataFactory;
import net.sf.borg.model.db.MemoDB;

/**
 * A singleton instance which creates class-specific {@link BeanDB BeanDB}
 * instances.
 */
public class FileBeanDataFactory implements IBeanDataFactory
{
	/**
	 * Singleton.
	 */
	public static FileBeanDataFactory getInstance()
	{
		return instance;
	}

	// IBeanDataFactory overrides
	/**
	 * Replacing the old signature, this new one should receive the url String
	 * using the format <code>filename::isReadonly::isShared</code>.<br>
	 * Example: <code>/home/user/borg/files::false::true</code><br>
	 * @see net.sf.borg.model.db.IBeanDataFactory#create(java.lang.Class, java.lang.String, int)
	 */
	public final BeanDB create(
			Class cls,
			String url,
			String username)
			throws Exception
		{
			boolean readonly = false;
			boolean shared = false;
			String file = url;
			try{
				String[] fileArray = url.split("::");
				file = fileArray[0];
				readonly = Boolean.valueOf(fileArray[1]).booleanValue();
				shared = Boolean.valueOf(fileArray[2]).booleanValue();
			}
			catch( java.lang.NoSuchMethodError e )
			{
				// the Palm conduits run in JRE 1.3
				// so we must ignore that split() does not exist
				int idx = url.indexOf("::");
				file = url.substring(0,idx);
			}
			
	    	FileDBCreator creator = null;
			if (cls == Address.class)
				creator = new AddrFileDB();
			else if (cls == Task.class)
				creator = new TaskFileDB();
			else if (cls == Appointment.class)
				creator = new ApptFileDB();
			else
				throw new IllegalArgumentException(cls.getName());
				
			creator.init(file,readonly,shared);
			return creator;
		}

	// private //
	private static final FileBeanDataFactory instance = new FileBeanDataFactory();

	private FileBeanDataFactory()
	{
	}

	public MemoDB createMemoDB(String url, String username) throws Exception {
	    // TODO Auto-generated method stub
	    return null;
	}
}
