// This code was generated by GenerateDataObjects
package net.sf.borg.model;

import net.sf.borg.model.db.*;
import net.sf.borg.model.Appointment;
import net.sf.borg.common.util.XTree;
public class AppointmentXMLAdapter extends BeanXMLAdapter {

	public XTree toXml( KeyedBean b )
	{
		Appointment o = (Appointment) b;
		XTree xt = new XTree();
		xt.name("Appointment");
		xt.appendChild("KEY", Integer.toString(o.getKey()));
		if( o.getDate() != null )
			xt.appendChild("Date", BeanXMLAdapter.toString(o.getDate()));
		if( o.getDuration() != null )
			xt.appendChild("Duration", BeanXMLAdapter.toString(o.getDuration()));
		if( o.getText() != null && !o.getText().equals(""))
			xt.appendChild("Text", o.getText());
		if( o.getSkipList() != null )
			xt.appendChild("SkipList", BeanXMLAdapter.toString(o.getSkipList()));
		if( o.getNextTodo() != null )
			xt.appendChild("NextTodo", BeanXMLAdapter.toString(o.getNextTodo()));
		if( o.getSN() != null )
			xt.appendChild("SN", BeanXMLAdapter.toString(o.getSN()));
		if( o.getVacation() != null )
			xt.appendChild("Vacation", BeanXMLAdapter.toString(o.getVacation()));
		if( o.getHoliday() != null )
			xt.appendChild("Holiday", BeanXMLAdapter.toString(o.getHoliday()));
		if( o.getPrivate() == true )
			xt.appendChild("Private" ,  BeanXMLAdapter.toString(o.getPrivate()));
		if( o.getTimes() != null )
			xt.appendChild("Times", BeanXMLAdapter.toString(o.getTimes()));
		if( o.getFrequency() != null && !o.getFrequency().equals(""))
			xt.appendChild("Frequency", o.getFrequency());
		if( o.getTodo() == true )
			xt.appendChild("Todo" ,  BeanXMLAdapter.toString(o.getTodo()));
		if( o.getColor() != null && !o.getColor().equals(""))
			xt.appendChild("Color", o.getColor());
		if( o.getRepeatFlag() == true )
			xt.appendChild("RepeatFlag" ,  BeanXMLAdapter.toString(o.getRepeatFlag()));
		if( o.getCategory() != null && !o.getCategory().equals(""))
			xt.appendChild("Category", o.getCategory());
		if( o.getNew() == true )
			xt.appendChild("New" ,  BeanXMLAdapter.toString(o.getNew()));
		if( o.getModified() == true )
			xt.appendChild("Modified" ,  BeanXMLAdapter.toString(o.getModified()));
		if( o.getDeleted() == true )
			xt.appendChild("Deleted" ,  BeanXMLAdapter.toString(o.getDeleted()));
		if( o.getAlarm() != null && !o.getAlarm().equals(""))
			xt.appendChild("Alarm", o.getAlarm());
		return( xt );
	}

	public KeyedBean fromXml( XTree xt )
	{
		Appointment ret = new Appointment();
		String ks = xt.child("KEY").value();
		ret.setKey( BeanXMLAdapter.toInt(ks) );
		String val = "";
		val = xt.child("Date").value();
		ret.setDate( BeanXMLAdapter.toDate(val) );
		val = xt.child("Duration").value();
		ret.setDuration( BeanXMLAdapter.toInteger(val) );
		val = xt.child("Text").value();
		if( !val.equals("") )
			ret.setText( val );
		val = xt.child("SkipList").value();
		ret.setSkipList( BeanXMLAdapter.toVector(val) );
		val = xt.child("NextTodo").value();
		ret.setNextTodo( BeanXMLAdapter.toDate(val) );
		val = xt.child("SN").value();
		ret.setSN( BeanXMLAdapter.toInteger(val) );
		val = xt.child("Vacation").value();
		ret.setVacation( BeanXMLAdapter.toInteger(val) );
		val = xt.child("Holiday").value();
		ret.setHoliday( BeanXMLAdapter.toInteger(val) );
		val = xt.child("Private").value();
		ret.setPrivate( BeanXMLAdapter.toBoolean(val) );
		val = xt.child("Times").value();
		ret.setTimes( BeanXMLAdapter.toInteger(val) );
		val = xt.child("Frequency").value();
		if( !val.equals("") )
			ret.setFrequency( val );
		val = xt.child("Todo").value();
		ret.setTodo( BeanXMLAdapter.toBoolean(val) );
		val = xt.child("Color").value();
		if( !val.equals("") )
			ret.setColor( val );
		val = xt.child("RepeatFlag").value();
		ret.setRepeatFlag( BeanXMLAdapter.toBoolean(val) );
		val = xt.child("Category").value();
		if( !val.equals("") )
			ret.setCategory( val );
		val = xt.child("New").value();
		ret.setNew( BeanXMLAdapter.toBoolean(val) );
		val = xt.child("Modified").value();
		ret.setModified( BeanXMLAdapter.toBoolean(val) );
		val = xt.child("Deleted").value();
		ret.setDeleted( BeanXMLAdapter.toBoolean(val) );
		val = xt.child("Alarm").value();
		if( !val.equals("") )
			ret.setAlarm( val );
		return( ret );
	}
}
