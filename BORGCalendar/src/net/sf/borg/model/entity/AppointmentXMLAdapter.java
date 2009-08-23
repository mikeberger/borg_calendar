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
package net.sf.borg.model.entity;

import net.sf.borg.common.XTree;
public class AppointmentXMLAdapter extends EntityXMLAdapter<Appointment> {

	public XTree toXml( Appointment o )
	{
		
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
			xt.appendChild("SkipList", EntityXMLAdapter.toString(o.getSkipList()));
		if( o.getNextTodo() != null )
			xt.appendChild("NextTodo", EntityXMLAdapter.toString(o.getNextTodo()));
		if( o.getSN() != null )
			xt.appendChild("SN", EntityXMLAdapter.toString(o.getSN()));
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
		if( o.getNew() == true )
			xt.appendChild("New" ,  EntityXMLAdapter.toString(o.getNew()));
		if( o.getModified() == true )
			xt.appendChild("Modified" ,  EntityXMLAdapter.toString(o.getModified()));
		if( o.getDeleted() == true )
			xt.appendChild("Deleted" ,  EntityXMLAdapter.toString(o.getDeleted()));
		if( o.getAlarm() != null && !o.getAlarm().equals(""))
			xt.appendChild("Alarm", o.getAlarm());
		if( o.getReminderTimes() != null && !o.getReminderTimes().equals(""))
			xt.appendChild("ReminderTimes", o.getReminderTimes());
		if( o.getUntimed() != null && !o.getUntimed().equals(""))
			xt.appendChild("Untimed", o.getUntimed());
		return( xt );
	}

	public Appointment fromXml( XTree xt )
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
		ret.setSkipList( EntityXMLAdapter.toVector(val) );
		val = xt.child("NextTodo").value();
		ret.setNextTodo( EntityXMLAdapter.toDate(val) );
		val = xt.child("SN").value();
		ret.setSN( EntityXMLAdapter.toInteger(val) );
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
		val = xt.child("New").value();
		ret.setNew( EntityXMLAdapter.toBoolean(val) );
		val = xt.child("Modified").value();
		ret.setModified( EntityXMLAdapter.toBoolean(val) );
		val = xt.child("Deleted").value();
		ret.setDeleted( EntityXMLAdapter.toBoolean(val) );
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
}
