/*
This file is part of BORG.
 
	BORG is free software; you can redistribute it and/or modify
	it under the terms of the GNU General public final synchronized License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.
 
	BORG is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General public final synchronized License for more details.
 
	You should have received a copy of the GNU General public final synchronized License
	along with BORG; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
Copyright 2003 by ==Quiet==
 */

package net.sf.borg.model.db.remote;

import java.util.Collection;

import net.sf.borg.common.util.XTree;
import net.sf.borg.model.BorgOption;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.KeyedBean;

/**
 * @author Mohan Embar
 */
class RemoteBeanDB implements BeanDB
{
	// BeanDB overrides
	public final synchronized Collection readAll() throws DBException, Exception
	{
		return (Collection) call("readAll", null);
	}

	public final synchronized KeyedBean readObj(int key) throws DBException, Exception
	{
		return (KeyedBean) call("readObj", new Integer(key));
	}

	public final synchronized KeyedBean newObj()
	{
		try
		{
			return (KeyedBean) cls.newInstance();
		}
		catch (Exception e)
		{
			throw new AssertionError(e.getClass().getName()+": "+e.getMessage());
		}
	}

	public final synchronized void addObj(KeyedBean bean, boolean crypt) throws DBException, Exception
	{
		checkReadOnly();
		call(
			"addObj",
			new IRemoteProxy.ComposedObject(bean, Boolean.valueOf(crypt)));
	}

	public final synchronized void updateObj(KeyedBean bean, boolean crypt) throws DBException, Exception
	{
		checkReadOnly();
		call(
			"updateObj",
			new IRemoteProxy.ComposedObject(bean, Boolean.valueOf(crypt)));
	}

	public final synchronized void delete(int key) throws Exception
	{
		checkReadOnly();
		call("delete", new Integer(key));
	}

	public final synchronized String getOption(String oname) throws Exception
	{
		return (String) call("getOption", oname);
	}

	public final synchronized void setOption(BorgOption option) throws Exception
	{
		checkReadOnly();
		call("setOption", option);
	}

	public final synchronized Collection getOptions() throws Exception
	{
		return (Collection) call("getOptions", null);
	}

	public final synchronized Collection getKeys() throws Exception
	{
		return (Collection) call("getKeys", null);
	}

	public final synchronized void close() throws Exception
	{
		// ignore this - we're sharing
	}

	public final synchronized int nextkey() throws Exception
	{
		return ((Integer) call("nextkey", null)).intValue();
	}

	public final synchronized void setLogFile(String lf) throws DBException
	{
		// ignore this - we're sharing
	}

	public final synchronized String getLogFile() throws DBException
	{
		// not implemented - we're sharing
		return null;
	}

	public final synchronized void sync() throws DBException
	{
		// ignore this - we're sharing
	}
	
	// protected //
	protected Object call(String command, Object args) throws Exception
	{
		IRemoteProxy.Parms parms =
			new IRemoteProxy.Parms(clsstr, command, args);
		XTree xmlParms = XmlObjectHelper.toXml(parms);
		String xmlstr = xmlParms.toString();
		String result = 
			RemoteProxyHome
				.getInstance()
				.getProxy(impl)
				.execute(xmlstr);
		XTree xmlResult = XTree.readFromBuffer(result);
		return XmlObjectHelper.fromXml(xmlResult);
	}
	
	protected void checkReadOnly() throws DBException
	{
		if (readonly)
			throw new DBException("Database is read-only", DBException.RET_READ_ONLY);
	}
	
	// package //
	RemoteBeanDB(Class cls, String clsstr, String impl, boolean readonly)
	{
		this.cls = cls;
		this.clsstr = clsstr;
		this.impl = impl;
		this.readonly = readonly;
	}
	
	// private //
	private Class cls;
	private String clsstr;
	private String impl;
	private boolean readonly;
}
