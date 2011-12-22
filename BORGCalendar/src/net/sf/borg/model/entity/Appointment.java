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
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.borg.common.EncryptionHelper;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;



/**
 * The Appointment Entity
 */
@XmlRootElement(name="Appointment")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@EqualsAndHashCode(callSuper=true)
public class Appointment extends EncryptableEntity<Appointment> implements CalendarEntity {

	
	private static final long serialVersionUID = 7225675837209156249L;
		
	private Date Date;
	/** The Duration in minutes */
	private Integer Duration;
	private String Text;
	/** The Skip list. - a list of repeat occurrences that are marked as skipped. */
	private Vector<String> SkipList;
	/** The Next todo - the date of the next todo for repeating todos */
	private Date NextTodo;
	/** The Vacation flag - 0 = not vacation 1 = vacation day, 2 = half day */
	private Integer Vacation;
	/** The Holiday flag (1 = holiday) */
	private Integer Holiday;
	/** The Private flag (1 = private) */
	private boolean Private;
	/** The number of Repeat Times. */
	private Integer Times;
	/** The repeat Frequency. */
	private String Frequency;
	/** The Todo flag. */
	private boolean Todo;
	private String Color;
	private boolean RepeatFlag;
	private String Category;
	/** The Reminder times.  See ReminderTimes.java*/
	private String ReminderTimes;
	/** The Untimed flag.Y = untimed
	 * provides a positive indication that an appointment has no specific time of day */
	private String Untimed;
	private Date repeatUntil;
	private Integer priority;
	
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
		dst.setPrivate( isPrivate() );
		dst.setTimes( getTimes() );
		dst.setFrequency( getFrequency() );
		dst.setTodo( isTodo() );
		dst.setColor( getColor() );
		dst.setRepeatFlag( isRepeatFlag() );
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
		
		EncryptionHelper helper = new EncryptionHelper(Prefs.getPref(PrefName.KEYSTORE), password);
		String clearText = helper.decrypt(this.getText(), Prefs.getPref(PrefName.KEYALIAS));
		this.setText(clearText);
		this.setEncrypted(false);
	}

	@Override
	public void encrypt(String password) throws Exception {
		if( isEncrypted() )
			return;
		
		EncryptionHelper helper = new EncryptionHelper(Prefs.getPref(PrefName.KEYSTORE), password);
		String cipherText = helper.encrypt(this.getText(), Prefs.getPref(PrefName.KEYALIAS));
		this.setText(cipherText);
		this.setEncrypted(true);
		
	}

	
}
