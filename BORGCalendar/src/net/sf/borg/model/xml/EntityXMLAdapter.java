/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.model.xml;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sf.borg.common.XTree;
import net.sf.borg.model.entity.KeyedEntity;


abstract public class EntityXMLAdapter<T extends KeyedEntity<T>> {

	abstract public XTree toXml(T bean);

	abstract public T fromXml(XTree xt);

	private static SimpleDateFormat normalDateFormat_ = new SimpleDateFormat(
			"MM/dd/yy hh:mm aa");

	protected static String toString(Date d) {
		return (normalDateFormat_.format(d));
	}

	protected static String toString(boolean b) {
		if (b)
			return ("true");
		return ("false");
	}

	protected static Date toDate(String s) {

		try {
			Date d = normalDateFormat_.parse(s);
			return (d);
		} catch (Exception e) {
			return (null);
		}
	}

	protected static boolean toBoolean(String s) {
		if (s.equals("true"))
			return (true);
		return (false);
	}

	protected static String toString(Integer i) {
		return (i.toString());
	}

	protected static Integer toInteger(String s) {
		Integer i = null;
		try {
			i = Integer.decode(s);
		} catch (Exception e) {
			return (null);
		}
		return (i);
	}

	protected static int toInt(String s) {
		int i = 0;
		try {
			i = Integer.parseInt(s);
		} catch (Exception e) {
			return (0);
		}
		return (i);
	}

	// yuck
	protected static String toString(Vector<String> v) {

		String val = "";
		if (v == null)
			return ("");
		try {
			while (true) {
				String s = v.remove(0);
				val += s;
				val += ",";
			}
		} catch (Exception e) {
		}
		return (val);

	}

	protected static Vector<String> toVector(String s) {
		if (s == null || s.equals(""))
			return (null);

		StringTokenizer stk = new StringTokenizer(s, ",");
		Vector<String> vect = new Vector<String>();
		while (stk.hasMoreTokens()) {
			String stt = stk.nextToken();
			if (!stt.equals(""))
				vect.add(stt);
		}
		return (vect);
	}
}
