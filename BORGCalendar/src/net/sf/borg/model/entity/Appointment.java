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

import java.util.Vector;



/**
 * The Appointment Entity
 */
public class Appointment extends KeyedEntity<Appointment> implements CalendarEntity, java.io.Serializable {

	
	private static final long serialVersionUID = 7225675837209156249L;
	
	/** The Date_. */
	private java.util.Date Date_;
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDate()
	 */
	public java.util.Date getDate() { return( Date_ ); }
	
	/**
	 * Sets the date.
	 * 
	 * @param xx the new date
	 */
	public void setDate( java.util.Date xx ){ Date_ = xx; }

	/** The Duration in minutes */
	private Integer Duration_;
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDuration()
	 */
	public Integer getDuration() { return( Duration_ ); }
	
	/**
	 * Sets the appointment duration in minutes.
	 * 
	 * @param xx the new duration
	 */
	public void setDuration( Integer xx ){ Duration_ = xx; }

	/** The appointment Text_. */
	private String Text_;
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getText()
	 */
	public String getText() { return( Text_ ); }
	
	/**
	 * Sets the text.
	 * 
	 * @param xx the new text
	 */
	public void setText( String xx ){ Text_ = xx; }

	/** The Skip list_. - a list of repeat occurrences that are marked as skipped. */
	private Vector<String> SkipList_;
	
	/**
	 * Gets the skip list - a list of repeat occurrences that are marked as skipped.
	 * 
	 * @return the skip list
	 */
	public Vector<String> getSkipList() { return( SkipList_ ); }
	
	/**
	 * Sets the skip list - a list of repeat occurrences that are marked as skipped..
	 * 
	 * @param xx the new skip list
	 */
	public void setSkipList( Vector<String> xx ){ SkipList_ = xx; }

	/** The Next todo_ - the date of the next todo for repeating todos */
	private java.util.Date NextTodo_;
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getNextTodo()
	 */
	public java.util.Date getNextTodo() { return( NextTodo_ ); }
	
	/**
	 * Sets the next todo  - the date of the next todo for repeating todos 
	 * 
	 * @param xx the new next todo
	 */
	public void setNextTodo( java.util.Date xx ){ NextTodo_ = xx; }

	
	/** The Vacation flag - 0 = not vacation 1 = vacation day, 2 = half day */
	private Integer Vacation_;
	
	/**
	 * Gets the vacation - 0 = not vacation 1 = vacation day, 2 = half day
	 * 
	 * @return the vacation flag
	 */
	public Integer getVacation() { return( Vacation_ ); }
	
	/**
	 * Sets the vacation - 0 = not vacation 1 = vacation day, 2 = half day
	 * 
	 * @param xx the new vacation flag
	 */
	public void setVacation( Integer xx ){ Vacation_ = xx; }

	/** The Holiday_ flag (1 = holiday) */
	private Integer Holiday_;
	
	/**
	 * Gets the holiday flag (1 = holiday).
	 * 
	 * @return the holiday flag
	 */
	public Integer getHoliday() { return( Holiday_ ); }
	
	/**
	 * Sets the holiday flag (1 = holiday).
	 * 
	 * @param xx the new holiday flag
	 */
	public void setHoliday( Integer xx ){ Holiday_ = xx; }

	/** The Private_ flag (1 = private) */
	private boolean Private_;
	
	/**
	 * Gets the private flag (1 = private).
	 * 
	 * @return the private flag (1 = private)
	 */
	public boolean getPrivate() { return( Private_ ); }
	
	/**
	 * Sets the private flag (1 = private).
	 * 
	 * @param xx the new private flag
	 */
	public void setPrivate( boolean xx ){ Private_ = xx; }

	/** The Times_. */
	private Integer Times_;
	
	/**
	 * Gets the number of repeat times.
	 * 
	 * @return the times
	 */
	public Integer getTimes() { return( Times_ ); }
	
	/**
	 * Sets the number of repeat times.
	 * 
	 * @param xx the new times
	 */
	public void setTimes( Integer xx ){ Times_ = xx; }

	/** The repeat Frequency_. */
	private String Frequency_;
	
	/**
	 * Gets the repeat frequency. See Repeat.java
	 * 
	 * @return the repeat frequency
	 */
	public String getFrequency() { return( Frequency_ ); }
	
	/**
	 * Sets the repeat frequency. See Repeat.java
	 * 
	 * @param xx the new repeat frequency
	 */
	public void setFrequency( String xx ){ Frequency_ = xx; }

	/** The Todo flag. */
	private boolean Todo_;
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getTodo()
	 */
	public boolean getTodo() { return( Todo_ ); }
	
	/**
	 * Sets the todo flag
	 * 
	 * @param xx the new todo
	 */
	public void setTodo( boolean xx ){ Todo_ = xx; }

	/** The Color_. */
	private String Color_;
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getColor()
	 */
	public String getColor() { return( Color_ ); }
	
	/**
	 * Sets the color.
	 * 
	 * @param xx the new color
	 */
	public void setColor( String xx ){ Color_ = xx; }

	/** The Repeat flag_. */
	private boolean RepeatFlag_;
	
	/**
	 * Gets the repeat flag.
	 * 
	 * @return the repeat flag
	 */
	public boolean getRepeatFlag() { return( RepeatFlag_ ); }
	
	/**
	 * Sets the repeat flag.
	 * 
	 * @param xx the new repeat flag
	 */
	public void setRepeatFlag( boolean xx ){ RepeatFlag_ = xx; }

	/** The Category_. */
	private String Category_;
	
	/**
	 * Gets the category.
	 * 
	 * @return the category
	 */
	public String getCategory() { return( Category_ ); }
	
	/**
	 * Sets the category.
	 * 
	 * @param xx the new category
	 */
	public void setCategory( String xx ){ Category_ = xx; }

	// palm sync stuff
	@Deprecated private boolean Modified_;
	@Deprecated public boolean getModified() { return( Modified_ ); }
	@Deprecated public void setModified( boolean xx ){ Modified_ = xx; }
	@Deprecated private String Alarm_;
	@Deprecated public String getAlarm() { return( Alarm_ ); }
	@Deprecated public void setAlarm( String xx ){ Alarm_ = xx; }

	/** The Reminder times_.  See ReminderTimes.java*/
	private String ReminderTimes_;
	
	/**
	 * Gets the reminder times. See ReminderTimes.java
	 * 
	 * @return the reminder times
	 */
	public String getReminderTimes() { return( ReminderTimes_ ); }
	
	/**
	 * Sets the reminder times.  See ReminderTimes.java
	 * 
	 * @param xx the new reminder times
	 */
	public void setReminderTimes( String xx ){ ReminderTimes_ = xx; }
	
	/** The Untimed flag.Y = untimed
	 * provides a positive indication that an appointment has no specific time of day */
	private String Untimed_;
	
	/**
	 * Gets the untimed flag. Y = untimed
	 * 
	 * @return the untimed
	 */
	public String getUntimed() { return( Untimed_ ); }
	
	/**
	 * Sets the untimed flag. Y = untimed
	 * 
	 * @param xx the new untimed
	 */
	public void setUntimed( String xx ){ Untimed_ = xx; }

	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
	@SuppressWarnings("unchecked")
	protected Appointment clone() {
		Appointment dst = new Appointment();
		dst.setKey( getKey());
		dst.setDate( getDate() );
		dst.setDuration( getDuration() );
		dst.setText( getText() );
		Vector<String> v = getSkipList();
		if( v != null )
		{		
			dst.setSkipList((Vector<String>)v.clone());
		}
				
		dst.setNextTodo( getNextTodo() );
		dst.setVacation( getVacation() );
		dst.setHoliday( getHoliday() );
		dst.setPrivate( getPrivate() );
		dst.setTimes( getTimes() );
		dst.setFrequency( getFrequency() );
		dst.setTodo( getTodo() );
		dst.setColor( getColor() );
		dst.setRepeatFlag( getRepeatFlag() );
		dst.setCategory( getCategory() );
		dst.setModified( getModified() );
		dst.setAlarm( getAlarm() );
		dst.setReminderTimes( getReminderTimes() );
		dst.setUntimed( getUntimed() );
		return(dst);
	}
}
