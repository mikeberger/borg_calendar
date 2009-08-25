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
 
 Copyright 2003 by Mike Berger
 */

package net.sf.borg.model.db.remote;

import java.util.Collection;

import net.sf.borg.common.XTree;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.entity.KeyedEntity;

/**
 * @author Mohan Embar
 */
public class RemoteBeanDB implements BeanDB {
	// BeanDB overrides
	public final synchronized Collection readAll() throws 
			Exception {
		Collection col = (Collection) call("readAll", null);
		return col;
	}

	public final synchronized KeyedEntity readObj(int key) throws 
			Exception {
		return (KeyedEntity) call("readObj", new Integer(key));
	}

	public final synchronized KeyedEntity newObj() {
		try {
			return (KeyedEntity) cls.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e.getClass().getName() + ": "
					+ e.getMessage());
		}
	}

	public final synchronized void addObj(KeyedEntity bean)
			throws  Exception {
		call(
				"addObj",
				new IRemoteProxy.ComposedObject(bean, new Boolean(false) /* Boolean.valueOf(crypt) */));
	}

	public final synchronized void updateObj(KeyedEntity bean)
			throws  Exception {
		call("updateObj", new IRemoteProxy.ComposedObject(bean, new Boolean(
				false) /* Boolean.valueOf(crypt) */));
	}

	public final synchronized void delete(int key) throws Exception {
		call("delete", new Integer(key));
	}

	

	public final synchronized Collection getOptions() throws Exception {
		return (Collection) call("getOptions", null);
	}

	public final synchronized void close() throws Exception {
		// ignore this - we're sharing
	}

	public final synchronized int nextkey() throws Exception {
		return ((Integer) call("nextkey", null)).intValue();
	}



	// protected //
	protected Object call(String command, Object args) throws Exception {
		IRemoteProxy.Parms parms = new IRemoteProxy.Parms(clsstr, command,
				args);
		XTree xmlParms = XmlObjectHelper.toXml(parms);
		String xmlstr = xmlParms.toString();
		// System.out.println(xmlstr);
		
		String result = SocketProxy.execute(xmlstr);
		//System.out.println("OutTrace - " + result);
		//System.err.println("ErrTrace - " + result);
		XTree xmlResult = XTree.readFromBuffer(result);
		Object retval = XmlObjectHelper.fromXml(xmlResult);
		if (retval instanceof Exception)
			throw (Exception) retval;

		return retval;
	}

	// package //
	public RemoteBeanDB(Class cls, String clsstr) {
		this.cls = cls;
		this.clsstr = clsstr;
		
	}

	// private //
	private Class cls;

	private String clsstr;

}
