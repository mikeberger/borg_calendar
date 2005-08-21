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

package net.sf.borg.model.db.jdbc;

import net.sf.borg.model.Address;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.Task;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.IBeanDataFactory;

/**
 * A singleton instance which creates class-specific {@link BeanDB BeanDB}
 * instances.
 */
public class JdbcBeanDataFactory implements IBeanDataFactory
{
	/**
	 * Singleton.
	 */
	public static JdbcBeanDataFactory getInstance()
	{
		return instance;
	}

	// IBeanDataFactory overrides
	public final BeanDB create(
			Class cls,
			String url,
			String username)
			throws Exception
		{
			if (cls == Address.class)
				return new AddrJdbcDB( url, username );
			else if (cls == Task.class)
				return new TaskJdbcDB( url, username );
			else if (cls == Appointment.class)
				return new ApptJdbcDB( url, username );

			throw new IllegalArgumentException(cls.getName());
		}

	// private //
	private static final JdbcBeanDataFactory instance = new JdbcBeanDataFactory();
	private JdbcBeanDataFactory()
	{
	}
}
