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

package net.sf.borg.model.db.file;

import java.util.Collection;

import net.sf.borg.model.BorgOption;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.KeyedBean;

/**
 * Helps create a <code>FileBeanDB</code>.
 */
abstract class FileDBCreator implements BeanDB
{
	abstract void init(String file, boolean readonly, boolean shared)
		throws Exception;

	// BeanDB delegate methods
	public final void addObj(KeyedBean bean, boolean crypt)
		throws DBException, Exception
	{
		db_.addObj(bean, crypt);
	}

	public final void close() throws Exception
	{
		db_.close();
	}

	public final void delete(int key) throws Exception
	{
		db_.delete(key);
	}

	public final String getLogFile() throws DBException
	{
		return db_.getLogFile();
	}

	public final String getOption(String oname) throws Exception
	{
		return db_.getOption(oname);
	}

	public final Collection getOptions() throws Exception
	{
		return db_.getOptions();
	}

	public final int maxkey() throws Exception
	{
		return db_.maxkey();
	}

	public final KeyedBean newObj()
	{
		return db_.newObj();
	}

	public final Collection readAll() throws Exception
	{
		return db_.readAll();
	}

	public final KeyedBean readObj(int key) throws DBException, Exception
	{
		return db_.readObj(key);
	}

	public final void setLogFile(String lf) throws DBException
	{
		db_.setLogFile(lf);
	}

	public final void setOption(BorgOption option) throws Exception
	{
		db_.setOption(option);
	}

	public final void sync() throws DBException
	{
		db_.sync();
	}

	public final void updateObj(KeyedBean bean, boolean crypt)
		throws DBException, Exception
	{
		db_.updateObj(bean, crypt);
	}

	// protected //
	protected BeanDB db_;
}
