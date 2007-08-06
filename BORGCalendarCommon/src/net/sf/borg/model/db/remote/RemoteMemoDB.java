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
import net.sf.borg.model.beans.Memo;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.MemoDB;


class RemoteMemoDB implements MemoDB {
    // BeanDB overrides
    public final synchronized Collection getNames() throws DBException,
	    Exception {
	Collection col = (Collection) call("getNames", null);
	return col;
    }

    public final synchronized Collection readAll() throws DBException,
	    Exception {
	Collection col = (Collection) call("readAllMemos", null);
	return col;
    }

    public final synchronized Memo readMemo(String name) throws DBException,
	    Exception {
	return (Memo) call("readMemo", name);
    }

    public final synchronized Memo getMemoByPalmId(int id) throws DBException,
	    Exception {
	return (Memo) call("getMemoByPalmId", new Integer(id));
    }

    public final synchronized void addMemo(Memo m) throws DBException,
	    Exception {
	call("addMemo", new IRemoteProxy.ComposedObject(m, null));
    }

    public final synchronized void updateMemo(Memo m) throws DBException,
	    Exception {
	call("updateMemo", new IRemoteProxy.ComposedObject(m, null));
    }

    public final synchronized void delete(String name) throws Exception {
	call("deleteMemo", name);
    }

    public final synchronized void close() throws Exception {
	// ignore this - we're sharing
    }

    // protected //
    protected Object call(String command, Object args) throws Exception {
	IRemoteProxy.Parms parms = new IRemoteProxy.Parms(clsstr, command,
		args, user);
	XTree xmlParms = XmlObjectHelper.toXml(parms);
	String xmlstr = xmlParms.toString();
	// System.out.println(xmlstr);
	RemoteProxyHome home = RemoteProxyHome.getInstance();
	String result = home.getProxy(url).execute(xmlstr,
		home.getProxyProvider());
	// System.out.println("OutTrace - " + result);
	// System.err.println("ErrTrace - " + result);
	XTree xmlResult = XTree.readFromBuffer(result);
	Object retval = XmlObjectHelper.fromXml(xmlResult);
	if (retval instanceof Exception)
	    throw (Exception) retval;

	return retval;
    }

    // package //
    RemoteMemoDB(String url, String user) {
	this.clsstr = "Memo";
	this.url = url;
	this.user = user;
    }

    // private //

    private String clsstr;

    private String url;

    private String user;
}
