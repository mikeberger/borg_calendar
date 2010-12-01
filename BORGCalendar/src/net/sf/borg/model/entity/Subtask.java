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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;

/**
 * Subtask entity
 */
@XmlRootElement(name="Subtask")
@XmlAccessorType(XmlAccessType.NONE)
public class Subtask extends KeyedEntity<Subtask> implements CalendarEntity {

	private static final long serialVersionUID = -5794908342032518360L;
	
	/** The Create date. */
	@XmlElement
	private Date StartDate;

	/**
	 * Gets the start date.
	 * 
	 * @return the start date
	 */
	public Date getStartDate() {
		return (StartDate);
	}

	/**
	 * Sets the start date.
	 * 
	 * @param xx the new start date
	 */
	public void setStartDate(Date xx) {
		StartDate = xx;
	}

	/** The Close date. */
	@XmlElement
	private Date CloseDate;

	/**
	 * Gets the close date.
	 * 
	 * @return the close date
	 */
	public Date getCloseDate() {
		return (CloseDate);
	}

	/**
	 * Sets the close date.
	 * 
	 * @param xx the new close date
	 */
	public void setCloseDate(Date xx) {
		CloseDate = xx;
	}

	/** The Due date. */
	@XmlElement
	private Date DueDate;

	/**
	 * Gets the due date.
	 * 
	 * @return the due date
	 */
	public Date getDueDate() {
		return (DueDate);
	}

	/**
	 * Sets the due date.
	 * 
	 * @param xx the new due date
	 */
	public void setDueDate(Date xx) {
		DueDate = xx;
	}

	/** The Description. */
	@XmlElement
	private String Description;

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return (Description);
	}

	/**
	 * Sets the description.
	 * 
	 * @param xx the new description
	 */
	public void setDescription(String xx) {
		Description = xx;
	}

	/** The Task. */
	@XmlElement
	private Integer Task;

	/**
	 * Gets the parent task.
	 * 
	 * @return the parent task id
	 */
	public Integer getTask() {
		return (Task);
	}

	/**
	 * Sets the parent task.
	 * 
	 * @param xx the parent task id
	 */
	public void setTask(Integer xx) {
		Task = xx;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
	@Override
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
	@Override
	public String getColor() {
		// logical color - maps to a user-defined color - legacy code
		return "navy";
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDate()
	 */
	@Override
	public Date getDate() {
		return getDueDate();
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDuration()
	 */
	@Override
	public Integer getDuration() {
		return new Integer(0);
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getNextTodo()
	 */
	@Override
	public Date getNextTodo() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getText()
	 */
	@Override
	public String getText() {
		// return the text as it should appear on the calendar
		String showabb = Prefs.getPref(PrefName.TASK_SHOW_ABBREV);
		String abb = "";
		if (showabb.equals("true"))
			abb = "BT" + getTask() + "/ST" + getKey() + " ";
		String de = abb + getDescription();
		String tx = de.replace('\n', ' ');
		return tx;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getTodo()
	 */
	@Override
	public boolean getTodo() {
		return true;
	}

	@Override
	public Integer getPriority() {
		return null;
	}
}
