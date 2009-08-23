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

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;

/**
 * Subtask entity
 */
public class Subtask extends KeyedEntity<Subtask> implements CalendarEntity,
		java.io.Serializable {

	private static final long serialVersionUID = -5794908342032518360L;
	
	/** The Create date_. */
	private java.util.Date startDate;

	/**
	 * Gets the start date.
	 * 
	 * @return the start date
	 */
	public java.util.Date getStartDate() {
		return (startDate);
	}

	/**
	 * Sets the start date.
	 * 
	 * @param xx the new start date
	 */
	public void setStartDate(java.util.Date xx) {
		startDate = xx;
	}

	/** The Close date_. */
	private java.util.Date CloseDate_;

	/**
	 * Gets the close date.
	 * 
	 * @return the close date
	 */
	public java.util.Date getCloseDate() {
		return (CloseDate_);
	}

	/**
	 * Sets the close date.
	 * 
	 * @param xx the new close date
	 */
	public void setCloseDate(java.util.Date xx) {
		CloseDate_ = xx;
	}

	/** The Due date_. */
	private java.util.Date DueDate_;

	/**
	 * Gets the due date.
	 * 
	 * @return the due date
	 */
	public java.util.Date getDueDate() {
		return (DueDate_);
	}

	/**
	 * Sets the due date.
	 * 
	 * @param xx the new due date
	 */
	public void setDueDate(java.util.Date xx) {
		DueDate_ = xx;
	}

	/** The Description_. */
	private String Description_;

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return (Description_);
	}

	/**
	 * Sets the description.
	 * 
	 * @param xx the new description
	 */
	public void setDescription(String xx) {
		Description_ = xx;
	}

	/** The Task_. */
	private Integer Task_;

	/**
	 * Gets the parent task.
	 * 
	 * @return the parent task id
	 */
	public Integer getTask() {
		return (Task_);
	}

	/**
	 * Sets the parent task.
	 * 
	 * @param xx the parent task id
	 */
	public void setTask(Integer xx) {
		Task_ = xx;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
	protected Subtask clone() {
		Subtask dst = new Subtask();
		dst.setKey(getKey());
		dst.setStartDate(getStartDate());
		dst.setCloseDate(getCloseDate());
		dst.setDueDate(getDueDate());
		dst.setDescription(getDescription());
		dst.setTask(getTask());
		return (dst);
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getColor()
	 */
	public String getColor() {
		// logical color - maps to a user-defined color - legacy code
		return "navy";
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDate()
	 */
	public Date getDate() {
		return getDueDate();
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDuration()
	 */
	public Integer getDuration() {
		return new Integer(0);
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getNextTodo()
	 */
	public Date getNextTodo() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getText()
	 */
	public String getText() {
		// return the text as it should appear on the calendar
		String show_abb = Prefs.getPref(PrefName.TASK_SHOW_ABBREV);
		String abb = "";
		if (show_abb.equals("true"))
			abb = "BT" + getTask() + "/ST" + getKey() + " ";
		String de = abb + getDescription();
		String tx = de.replace('\n', ' ');
		return tx;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getTodo()
	 */
	public boolean getTodo() {
		return true;
	}
}
