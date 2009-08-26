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

package net.sf.borg.control.socketServer;

import net.sf.borg.common.XTree;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.db.AppointmentDB;
import net.sf.borg.model.db.EntityDB;
import net.sf.borg.model.db.MemoDB;
import net.sf.borg.model.entity.Address;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.KeyedEntity;
import net.sf.borg.model.entity.Memo;

/**
 * The server-side component of the BORG remote invocation framework. This is
 * designed to be run out of a running BORG instance - not a web server The
 * databases already open by the application will be used.
 * 
 * It is used only for Palm Sync - although it used to be for much more.
 * 
 * Trivia: Palm's hot-sync support requires Java 1.3. There is no way to have the hot-sync talk directly
 * to a jdbc database because java 1.3 is too old for jdbc/hsqldb. Therefore, the borg remote db code was
 * salvaged. A Java 1.3 compatible fragment of BORG is run by the hot-sync. This fragment sends db commands to
 * a running BORG instance (running java 1.5 or better). The commands are send as XML over sockets.
 * 
 * This stuff is deprecated and not worth commenting. It will disappear when my last 2 palms die.
 */
@Deprecated public class SingleInstanceHandler {

	@SuppressWarnings("unchecked")
	public static String execute(String msg) {
		Object result = null;

		//System.out.println("[INPUT] "+msg);
		String strXml = msg.replace("%NEWLINE%", "\n");
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
				EntityDB beanDB = getBeanDB(parms);

				if (cmd.equals("readAll")) {
					result = beanDB.readAll();
				} else if (cmd.equals("readObj")) {
					int key = ((Integer) parms.getArgs()).intValue();
					result = beanDB.readObj(key);
				} else if (cmd.equals("delete")) {
					int key = ((Integer) parms.getArgs()).intValue();
					beanDB.delete(key);
				} else if (cmd.equals("getOption")) {
					//String key = (String) parms.getArgs();
					result = null;//beanDB.getOption(key);
				} else if (cmd.equals("getOptions")) {
					result = null;
				} else if (cmd.equals("getTodoKeys")) {
					result = ((AppointmentDB) beanDB).getTodoKeys();
				} else if (cmd.equals("getRepeatKeys")) {
					result = ((AppointmentDB) beanDB).getRepeatKeys();
				} else if (cmd.equals("nextkey")) {
					result = new Integer(beanDB.nextkey());
				} else if (cmd.equals("setOption")) {
					//BorgOption option = (BorgOption) parms.getArgs();
					//beanDB.setOption(option);
				} else if (cmd.equals("addObj") || cmd.equals("updateObj")) {
					IRemoteProxy.ComposedObject agg = (IRemoteProxy.ComposedObject) parms
							.getArgs();
					KeyedEntity bean = (KeyedEntity) agg.getO1();
					
					if (cmd.equals("addObj"))
						beanDB.addObj(bean);
					else
					{				
						beanDB.updateObj(bean);
					}

				} else
					throw new UnsupportedOperationException(cmd);
			}
		} catch (Exception e) {
			e.printStackTrace();

		}

		String resultString = XmlObjectHelper.toXml(result).toString();
		// System.out.println("[OUTPUT] "+resultString);
		resultString = resultString.replace("\n", "%NEWLINE%");
		return resultString;
	}

	@SuppressWarnings("unchecked")
	private static EntityDB getBeanDB(IRemoteProxy.Parms parms) throws Exception {

		if (parms.getMyClass() == Address.class) {
			return AddressModel.getReference().getDB();
		} else if (parms.getMyClass() == Appointment.class) {
			return AppointmentModel.getReference().getDB();
		} else
			throw new Exception("Invalid Class: " + parms.getClassString());
	}

}
