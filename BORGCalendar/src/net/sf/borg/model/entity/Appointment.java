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

import java.util.Date;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.borg.common.EncryptionHelper;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;



/**
 * The Appointment Entity
 */
@XmlRootElement(name="Appointment")
@XmlAccessorType(XmlAccessType.NONE)
public class Appointment extends EncryptableEntity<Appointment> implements CalendarEntity {

	
	private static final long serialVersionUID = 7225675837209156249L;
	
	/** The Date. */
	@XmlElement
	private Date Date;
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDate()
	 */
	@Override
	public Date getDate() { return( Date ); }
	
	/**
	 * Sets the date.
	 * 
	 * @param xx the new date
	 */
	public void setDate( Date xx ){ Date = xx; }

	/** The Duration in minutes */
	@XmlElement
	private Integer Duration;
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDuration()
	 */
	@Override
	public Integer getDuration() { return( Duration ); }
	
	/**
	 * Sets the appointment duration in minutes.
	 * 
	 * @param xx the new duration
	 */
	public void setDuration( Integer xx ){ Duration = xx; }

	/** The appointment Text. */
	@XmlElement
	private String Text;
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getText()
	 */
	@Override
	public String getText() { return( Text ); }
	
	/**
	 * Sets the text.
	 * 
	 * @param xx the new text
	 */
	public void setText( String xx ){ Text = xx; }

	/** The Skip list. - a list of repeat occurrences that are marked as skipped. */
	@XmlElement
	private Vector<String> SkipList;
	
	/**
	 * Gets the skip list - a list of repeat occurrences that are marked as skipped.
	 * 
	 * @return the skip list
	 */
	public Vector<String> getSkipList() { return( SkipList ); }
	
	/**
	 * Sets the skip list - a list of repeat occurrences that are marked as skipped..
	 * 
	 * @param xx the new skip list
	 */
	public void setSkipList( Vector<String> xx ){ SkipList = xx; }

	/** The Next todo - the date of the next todo for repeating todos */
	@XmlElement
	private Date NextTodo;
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getNextTodo()
	 */
	@Override
	public Date getNextTodo() { return( NextTodo ); }
	
	/**
	 * Sets the next todo  - the date of the next todo for repeating todos 
	 * 
	 * @param xx the new next todo
	 */
	public void setNextTodo( Date xx ){ NextTodo = xx; }

	
	/** The Vacation flag - 0 = not vacation 1 = vacation day, 2 = half day */
	@XmlElement
	private Integer Vacation;
	
	/**
	 * Gets the vacation - 0 = not vacation 1 = vacation day, 2 = half day
	 * 
	 * @return the vacation flag
	 */
	public Integer getVacation() { return( Vacation ); }
	
	/**
	 * Sets the vacation - 0 = not vacation 1 = vacation day, 2 = half day
	 * 
	 * @param xx the new vacation flag
	 */
	public void setVacation( Integer xx ){ Vacation = xx; }

	/** The Holiday flag (1 = holiday) */
	@XmlElement
	private Integer Holiday;
	
	/**
	 * Gets the holiday flag (1 = holiday).
	 * 
	 * @return the holiday flag
	 */
	public Integer getHoliday() { return( Holiday ); }
	
	/**
	 * Sets the holiday flag (1 = holiday).
	 * 
	 * @param xx the new holiday flag
	 */
	public void setHoliday( Integer xx ){ Holiday = xx; }

	/** The Private flag (1 = private) */
	@XmlElement
	private boolean Private;
	
	/**
	 * Gets the private flag (1 = private).
	 * 
	 * @return the private flag (1 = private)
	 */
	public boolean getPrivate() { return( Private ); }
	
	/**
	 * Sets the private flag (1 = private).
	 * 
	 * @param xx the new private flag
	 */
	public void setPrivate( boolean xx ){ Private = xx; }

	/** The number of Repeat Times. */
	@XmlElement
	private Integer Times;
	
	/**
	 * Gets the number of repeat times.
	 * 
	 * @return the times
	 */
	public Integer getTimes() { return( Times ); }
	
	/**
	 * Sets the number of repeat times.
	 * 
	 * @param xx the new times
	 */
	public void setTimes( Integer xx ){ Times = xx; }

	/** The repeat Frequency. */
	@XmlElement
	private String Frequency;
	
	/**
	 * Gets the repeat frequency. See Repeat.java
	 * 
	 * @return the repeat frequency
	 */
	public String getFrequency() { return( Frequency ); }
	
	/**
	 * Sets the repeat frequency. See Repeat.java
	 * 
	 * @param xx the new repeat frequency
	 */
	public void setFrequency( String xx ){ Frequency = xx; }

	/** The Todo flag. */
	@XmlElement
	private boolean Todo;
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getTodo()
	 */
	@Override
	public boolean getTodo() { return( Todo ); }
	
	/**
	 * Sets the todo flag
	 * 
	 * @param xx the new todo
	 */
	public void setTodo( boolean xx ){ Todo = xx; }

	/** The Color. */
	@XmlElement
	private String Color;
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getColor()
	 */
	@Override
	public String getColor() { return( Color ); }
	
	/**
	 * Sets the color.
	 * 
	 * @param xx the new color
	 */
	public void setColor( String xx ){ Color = xx; }

	/** The Repeat flag. */
	@XmlElement
	private boolean RepeatFlag;
	
	/**
	 * Gets the repeat flag.
	 * 
	 * @return the repeat flag
	 */
	public boolean getRepeatFlag() { return( RepeatFlag ); }
	
	/**
	 * Sets the repeat flag.
	 * 
	 * @param xx the new repeat flag
	 */
	public void setRepeatFlag( boolean xx ){ RepeatFlag = xx; }

	/** The Category. */
	@XmlElement
	private String Category;
	
	/**
	 * Gets the category.
	 * 
	 * @return the category
	 */
	public String getCategory() { return( Category ); }
	
	/**
	 * Sets the category.
	 * 
	 * @param xx the new category
	 */
	public void setCategory( String xx ){ Category = xx; }

	/** The Reminder times.  See ReminderTimes.java*/
	@XmlElement
	private String ReminderTimes;
	
	/**
	 * Gets the reminder times. See ReminderTimes.java
	 * 
	 * @return the reminder times
	 */
	public String getReminderTimes() { return( ReminderTimes ); }
	
	/**
	 * Sets the reminder times.  See ReminderTimes.java
	 * 
	 * @param xx the new reminder times
	 */
	public void setReminderTimes( String xx ){ ReminderTimes = xx; }
	
	/** The Untimed flag.Y = untimed
	 * provides a positive indication that an appointment has no specific time of day */
	@XmlElement
	private String Untimed;
	
	/**
	 * Gets the untimed flag. Y = untimed
	 * 
	 * @return the untimed
	 */
	public String getUntimed() { return( Untimed ); }
	
	/**
	 * Sets the untimed flag. Y = untimed
	 * 
	 * @param xx the new untimed
	 */
	public void setUntimed( String xx ){ Untimed = xx; }
	
	@XmlElement
	private Date repeatUntil;

	/**
	 * get the Repeat Until date recurrence setting
	 * @return the repeat until date
	 */
	public Date getRepeatUntil() {
		return repeatUntil;
	}

	/**
	 * set the repeat until date recurrence setting
	 * @param repeatUntil the repeat until date
	 */
	public void setRepeatUntil(Date repeatUntil) {
		this.repeatUntil = repeatUntil;
	}
	
	@XmlElement
	private Integer priority;
	
	/**
	 * get the priority value
	 * @return the priority value
	 */
	public Integer getPriority() { return priority; }
	
	/**
	 * set the priority value
	 * @param priority the priority value
	 */
	public void setPriority(Integer priority) { this.priority = priority; }

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
	@Override
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
		dst.setReminderTimes( getReminderTimes() );
		dst.setUntimed( getUntimed() );
		dst.setEncrypted(isEncrypted());
		dst.setRepeatUntil( getRepeatUntil());
		dst.setPriority( getPriority() );

		return(dst);
	}

	@Override
	public void decrypt(String password) throws Exception {
		if( !isEncrypted() )
			return;
		
		/* decrypt the memo text field */
		EncryptionHelper helper = new EncryptionHelper(Prefs.getPref(PrefName.KEYSTORE), password);
		String clearText = helper.decrypt(this.getText(), Prefs.getPref(PrefName.KEYALIAS));
		this.setText(clearText);
		this.setEncrypted(false);
	}

	@Override
	public void encrypt(String password) throws Exception {
		if( isEncrypted() )
			return;
		
		/* encrypt the memo text field */
		EncryptionHelper helper = new EncryptionHelper(Prefs.getPref(PrefName.KEYSTORE), password);
		String cipherText = helper.encrypt(this.getText(), Prefs.getPref(PrefName.KEYALIAS));
		this.setText(cipherText);
		this.setEncrypted(true);
		
	}

	
}
