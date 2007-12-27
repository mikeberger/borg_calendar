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

import java.util.Collection;

import net.sf.borg.model.BorgOption;
import net.sf.borg.model.beans.KeyedBean;
import net.sf.borg.model.db.BeanDB;

/**
 * Helps create a <code>FileBeanDB</code>.
 */
abstract class FileDBCreator implements BeanDB
{
	abstract void init(String file, boolean shared)
		throws Exception;

	// BeanDB delegate methods
	public final void addObj(KeyedBean bean, boolean crypt)
		throws Exception
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

	public final String getOption(String oname) throws Exception
	{
		return db_.getOption(oname);
	}

	public final Collection getOptions() throws Exception
	{
		return db_.getOptions();
	}

	public final int nextkey() throws Exception
	{
		return db_.nextkey();
	}

	public final KeyedBean newObj()
	{
		return db_.newObj();
	}

	public final Collection readAll() throws Exception
	{
		return db_.readAll();
	}

	public final KeyedBean readObj(int key) throws Exception
	{
		return db_.readObj(key);
	}

	public final void setOption(BorgOption option) throws Exception
	{
		db_.setOption(option);
	}

    public final boolean isDirty() throws Exception
    {
    	return db_.isDirty();
    }
    
	public final void sync()
	{
		db_.sync();
	}

	public final void updateObj(KeyedBean bean, boolean crypt)
		throws Exception
	{
		db_.updateObj(bean, crypt);
	}

	// protected //
	protected BeanDB db_;
}
