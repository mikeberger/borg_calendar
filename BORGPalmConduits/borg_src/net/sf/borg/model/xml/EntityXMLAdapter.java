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

import net.sf.borg.common.XTree;
import net.sf.borg.model.entity.KeyedEntity;


/**
 * abstract base class for XML adapters. Contains some code to convert
 * various types to Strings in a standard way
 */
abstract public class EntityXMLAdapter {

	/**
	 * convert entity To xml.
	 * 
	 * @param entity the entity
	 * 
	 * @return the xml tree
	 */
	abstract public XTree toXml(KeyedEntity entity);

	/**
	 * convert xml to an entity.
	 * 
	 * @param xt the xml tree
	 * 
	 * @return the entity
	 */
	abstract public KeyedEntity fromXml(XTree xt);

	/** The normalized, standard date format_. */
	private static SimpleDateFormat normalDateFormat_ = new SimpleDateFormat(
			"MM/dd/yy hh:mm aa");

	/**
	 * standard date to string conversion.
	 * 
	 * @param d the date
	 * 
	 * @return the string
	 */
	protected static String toString(Date d) {
		return (normalDateFormat_.format(d));
	}

	/**
	 * standard boolean to string conversion.
	 * 
	 * @param b the boolean
	 * 
	 * @return the string
	 */
	protected static String toString(boolean b) {
		if (b)
			return ("true");
		return ("false");
	}

	/**
	 * standard string to date conversion.
	 * 
	 * @param s the string
	 * 
	 * @return the date
	 */
	protected static Date toDate(String s) {

		try {
			Date d = normalDateFormat_.parse(s);
			return (d);
		} catch (Exception e) {
			return (null);
		}
	}

	/**
	 * standard boolean to string conversion.
	 * 
	 * @param s the string
	 * 
	 * @return the boolean
	 */
	protected static boolean toBoolean(String s) {
		if (s.equals("true"))
			return (true);
		return (false);
	}

	/**
	 * integer to string conversion.
	 * 
	 * @param i the Integer
	 * 
	 * @return the string
	 */
	protected static String toString(Integer i) {
		return (i.toString());
	}

	/**
	 * String to Integer conversion.
	 * 
	 * @param s the string
	 * 
	 * @return the Integer
	 */
	protected static Integer toInteger(String s) {
		Integer i = null;
		try {
			i = Integer.decode(s);
		} catch (Exception e) {
			return (null);
		}
		return (i);
	}

	/**
	 * String to int conversion.
	 * 
	 * @param s the string
	 * 
	 * @return the int
	 */
	protected static int toInt(String s) {
		int i = 0;
		try {
			i = Integer.parseInt(s);
		} catch (Exception e) {
			return (0);
		}
		return (i);
	}

	
}
