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

package net.sf.borg.model.db;

import net.sf.borg.model.db.file.FileBeanDataFactory;
import net.sf.borg.model.db.jdbc.JdbcBeanDataFactory;
//import net.sf.borg.model.db.remote.RemoteBeanDataFactory;
import net.sf.borg.model.db.serial.SerialBeanDataFactory;

/**
 * A singleton instance which creates an
 * {@link IBeanDataFactory IBeanDataFactory} based on the file type.
 * @author Mohan Embar
 */
public class BeanDataFactoryFactory
{
	/**
	 * Singleton.
	 */
	public static BeanDataFactoryFactory getInstance()
	{
		return instance;
	}

	public final IBeanDataFactory getFactory(String file)
	{
		if (file.startsWith("jdbc:"))
			return JdbcBeanDataFactory.getInstance();
		//else if (file.startsWith("remote:"))
			//return RemoteBeanDataFactory.getInstance();
		else if (file.startsWith("serialize:") || file.startsWith("mem:"))
			return SerialBeanDataFactory.getInstance();
		else // assume file
			return FileBeanDataFactory.getInstance();
	}

	// private //
	private static final BeanDataFactoryFactory instance = new BeanDataFactoryFactory();

	private BeanDataFactoryFactory()
	{
	}
}
