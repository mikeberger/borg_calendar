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
 
 Copyright 2006 by Michael Berger
 */

package net.sf.borg.model.db.remote.server;

import net.sf.borg.common.util.J13Helper;
import net.sf.borg.common.util.XTree;
import net.sf.borg.model.Address;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.BorgOption;
import net.sf.borg.model.Memo;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.Task;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.AppointmentDB;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.KeyedBean;
import net.sf.borg.model.db.MemoDB;
import net.sf.borg.model.db.MultiUserDB;
import net.sf.borg.model.db.remote.IRemoteProxy;
import net.sf.borg.model.db.remote.XmlObjectHelper;

/**
 * The server-side component of the BORG remote invocation framework. This is
 * designed to be run out of a running BORG instance - not a web server The
 * databases already open by the application will be used
 */
public class SingleInstanceHandler {

    public static String execute(String msg) {
	Object result = null;

	// System.out.println("[INPUT] "+msg);
	String strXml = J13Helper.replace(msg, "%NEWLINE%", "\n");
	try {
	    XTree xmlParms = XTree.readFromBuffer(strXml);
	    IRemoteProxy.Parms parms = (IRemoteProxy.Parms) XmlObjectHelper
		    .fromXml(xmlParms);

	    // Figure out what we need to do
	    // String uid = parms.getUser();
	    String cmd = parms.getCommand();
	    if (parms.getMyClass() == Memo.class) {
		MemoDB db = MemoModel.getReference().getDB();
		if (cmd.equals("getNames")) {
		    result = db.getNames();
		} else if (cmd.equals("readAllMemos")) {
		    result = db.readAll();
		} else if (cmd.equals("readMemo")) {
		    result = db.readMemo((String) parms.getArgs());
		} else if (cmd.equals("getMemoByPalmId")) {
		    result = db.getMemoByPalmId(((Integer) parms.getArgs())
			    .intValue());
		} else if (cmd.equals("addMemo") || cmd.equals("updateMemo")) {
		    IRemoteProxy.ComposedObject agg = (IRemoteProxy.ComposedObject) parms
			    .getArgs();
		    Memo m = (Memo) agg.getO1();

		    if (cmd.equals("addMemo")) {
			db.addMemo(m);
		    } else {
			db.updateMemo(m);
		    }
		} else if (cmd.equals("deleteMemo")) {
		    db.delete((String) parms.getArgs());
		}

	    } else {
		BeanDB beanDB = getBeanDB(parms);

		if (cmd.equals("readAll")) {
		    result = beanDB.readAll();
		} else if (cmd.equals("readObj")) {
		    int key = ((Integer) parms.getArgs()).intValue();
		    result = beanDB.readObj(key);
		} else if (cmd.equals("delete")) {
		    int key = ((Integer) parms.getArgs()).intValue();
		    beanDB.delete(key);
		} else if (cmd.equals("getOption")) {
		    String key = (String) parms.getArgs();
		    result = beanDB.getOption(key);
		} else if (cmd.equals("getOptions")) {
		    result = beanDB.getOptions();
		} else if (cmd.equals("getTodoKeys")) {
		    result = ((AppointmentDB) beanDB).getTodoKeys();
		} else if (cmd.equals("getRepeatKeys")) {
		    result = ((AppointmentDB) beanDB).getRepeatKeys();
		} else if (cmd.equals("nextkey")) {
		    result = new Integer(beanDB.nextkey());
		} else if (cmd.equals("isDirty")) {
		    result = new Boolean(beanDB.isDirty());
		} else if (cmd.equals("setOption")) {
		    BorgOption option = (BorgOption) parms.getArgs();
		    beanDB.setOption(option);
		} else if (cmd.equals("addObj") || cmd.equals("updateObj")) {
		    IRemoteProxy.ComposedObject agg = (IRemoteProxy.ComposedObject) parms
			    .getArgs();
		    KeyedBean bean = (KeyedBean) agg.getO1();
		    boolean crypt = ((Boolean) agg.getO2()).booleanValue();

		    if (cmd.equals("addObj"))
			beanDB.addObj(bean, crypt);
		    else
			beanDB.updateObj(bean, crypt);

		} else if (cmd.equals("getAllUsers")
			&& beanDB instanceof MultiUserDB) {
		    result = ((MultiUserDB) beanDB).getAllUsers();
		} else
		    throw new UnsupportedOperationException(cmd);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    if (e instanceof DBException)
		result = e;
	    else
		result = new DBException(e.getClass().getName() + ": "
			+ e.getLocalizedMessage());
	}

	String resultString = XmlObjectHelper.toXml(result).toString();
	// System.out.println("[OUTPUT] "+resultString);
	resultString = J13Helper.replace(resultString, "\n", "%NEWLINE%");
	return resultString;
    }

    private static BeanDB getBeanDB(IRemoteProxy.Parms parms) throws Exception {

	if (parms.getMyClass() == Address.class) {
	    return AddressModel.getReference().getDB();
	} else if (parms.getMyClass() == Appointment.class) {
	    return AppointmentModel.getReference().getDB();
	} else if (parms.getMyClass() == Task.class) {
	    return TaskModel.getReference().getDB();
	} else
	    throw new Exception("Invalid Class: " + parms.getClassString());
    }

}
