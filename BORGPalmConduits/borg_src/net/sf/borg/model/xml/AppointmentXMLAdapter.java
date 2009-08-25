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

import java.util.StringTokenizer;
import java.util.Vector;

import net.sf.borg.common.XTree;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.KeyedEntity;

/**
 * Appointment XML Adapter.
 */
public class AppointmentXMLAdapter extends EntityXMLAdapter {

	/* (non-Javadoc)
	 * @see net.sf.borg.model.xml.EntityXMLAdapter#toXml(net.sf.borg.model.entity.KeyedEntity)
	 */
	public XTree toXml( KeyedEntity e )
	{
		Appointment o = (Appointment)e;
		XTree xt = new XTree();
		xt.name("Appointment");
		xt.appendChild("KEY", Integer.toString(o.getKey()));
		if( o.getDate() != null )
			xt.appendChild("Date", EntityXMLAdapter.toString(o.getDate()));
		if( o.getDuration() != null )
			xt.appendChild("Duration", EntityXMLAdapter.toString(o.getDuration()));
		if( o.getText() != null && !o.getText().equals(""))
			xt.appendChild("Text", o.getText());
		if( o.getSkipList() != null )
			xt.appendChild("SkipList", toString(o.getSkipList()));
		if( o.getNextTodo() != null )
			xt.appendChild("NextTodo", EntityXMLAdapter.toString(o.getNextTodo()));
		if( o.getVacation() != null )
			xt.appendChild("Vacation", EntityXMLAdapter.toString(o.getVacation()));
		if( o.getHoliday() != null )
			xt.appendChild("Holiday", EntityXMLAdapter.toString(o.getHoliday()));
		if( o.getPrivate() == true )
			xt.appendChild("Private" ,  EntityXMLAdapter.toString(o.getPrivate()));
		if( o.getTimes() != null )
			xt.appendChild("Times", EntityXMLAdapter.toString(o.getTimes()));
		if( o.getFrequency() != null && !o.getFrequency().equals(""))
			xt.appendChild("Frequency", o.getFrequency());
		if( o.getTodo() == true )
			xt.appendChild("Todo" ,  EntityXMLAdapter.toString(o.getTodo()));
		if( o.getColor() != null && !o.getColor().equals(""))
			xt.appendChild("Color", o.getColor());
		if( o.getRepeatFlag() == true )
			xt.appendChild("RepeatFlag" ,  EntityXMLAdapter.toString(o.getRepeatFlag()));
		if( o.getCategory() != null && !o.getCategory().equals(""))
			xt.appendChild("Category", o.getCategory());
		if( o.getModified() == true )
			xt.appendChild("Modified" ,  EntityXMLAdapter.toString(o.getModified()));
		if( o.getAlarm() != null && !o.getAlarm().equals(""))
			xt.appendChild("Alarm", o.getAlarm());
		if( o.getReminderTimes() != null && !o.getReminderTimes().equals(""))
			xt.appendChild("ReminderTimes", o.getReminderTimes());
		if( o.getUntimed() != null && !o.getUntimed().equals(""))
			xt.appendChild("Untimed", o.getUntimed());
		return( xt );
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.xml.EntityXMLAdapter#fromXml(net.sf.borg.common.XTree)
	 */
	public KeyedEntity fromXml( XTree xt )
	{
		Appointment ret = new Appointment();
		String ks = xt.child("KEY").value();
		ret.setKey( EntityXMLAdapter.toInt(ks) );
		String val = "";
		val = xt.child("Date").value();
		ret.setDate( EntityXMLAdapter.toDate(val) );
		val = xt.child("Duration").value();
		ret.setDuration( EntityXMLAdapter.toInteger(val) );
		val = xt.child("Text").value();
		if( !val.equals("") )
			ret.setText( val );
		val = xt.child("SkipList").value();
		ret.setSkipList( toVector(val) );
		val = xt.child("NextTodo").value();
		ret.setNextTodo( EntityXMLAdapter.toDate(val) );
		val = xt.child("Vacation").value();
		ret.setVacation( EntityXMLAdapter.toInteger(val) );
		val = xt.child("Holiday").value();
		ret.setHoliday( EntityXMLAdapter.toInteger(val) );
		val = xt.child("Private").value();
		ret.setPrivate( EntityXMLAdapter.toBoolean(val) );
		val = xt.child("Times").value();
		ret.setTimes( EntityXMLAdapter.toInteger(val) );
		val = xt.child("Frequency").value();
		if( !val.equals("") )
			ret.setFrequency( val );
		val = xt.child("Todo").value();
		ret.setTodo( EntityXMLAdapter.toBoolean(val) );
		val = xt.child("Color").value();
		if( !val.equals("") )
			ret.setColor( val );
		val = xt.child("RepeatFlag").value();
		ret.setRepeatFlag( EntityXMLAdapter.toBoolean(val) );
		val = xt.child("Category").value();
		if( !val.equals("") )
			ret.setCategory( val );
		val = xt.child("Modified").value();
		ret.setModified( EntityXMLAdapter.toBoolean(val) );
		val = xt.child("Alarm").value();
		if( !val.equals("") )
			ret.setAlarm( val );
		val = xt.child("ReminderTimes").value();
		if( !val.equals("") )
			ret.setReminderTimes( val );
		val = xt.child("Untimed").value();
		if( !val.equals("") )
			ret.setUntimed( val );
		return( ret );
	}
	
	/**
	 * Legacy code - converts a Vector of Strings to a single String. used only for specific
	 * appointment fields
	 * 
	 * @param v the vector of strings
	 * 
	 * @return the string
	 */
	private static String toString(Vector v) {

		String val = "";
		if (v == null)
			return ("");
		try {
			while (true) {
				String s = (String)v.remove(0);
				val += s;
				val += ",";
			}
		} catch (Exception e) {
		}
		return (val);

	}

	/**
	 * legacy code - converts a string holding a list of items to a vector of strings. used
	 * only for specfic appointment fields
	 * 
	 * @param s the string
	 * 
	 * @return the vector
	 */
	private static Vector toVector(String s) {
		if (s == null || s.equals(""))
			return (null);

		StringTokenizer stk = new StringTokenizer(s, ",");
		Vector vect = new Vector();
		while (stk.hasMoreTokens()) {
			String stt = stk.nextToken();
			if (!stt.equals(""))
				vect.add(stt);
		}
		return (vect);
	}
}
