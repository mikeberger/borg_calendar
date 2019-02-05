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
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.TaskModel;

/**
 * Subtask entity
 */
@XmlRootElement(name = "Subtask")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"lastMod","createTime"})
public class Subtask extends KeyedEntity<Subtask> implements CalendarEntity, SyncableEntity {

	private static final long serialVersionUID = -5794908342032518360L;

	private Date StartDate;
	private Date CloseDate;
	private Date DueDate;
	private String Description;
	private Integer Task;
	private Date createTime;
	private Date lastMod;
	private String uid;
	private String url;

	// cached task description
	private String taskDesc = null;

	/*
	 * (non-Javadoc)
	 * 
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
		dst.setCreateTime(getCreateTime());
		dst.setLastMod(getLastMod());
		dst.setUid(getUid());
		dst.setUrl(getUrl());
		return (dst);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.entity.CalendarEntity#getColor()
	 */
	@Override
	public String getColor() {
		// logical color - maps to a user-defined color - legacy code
		return "navy";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.entity.CalendarEntity#getDate()
	 */
	@Override
	public Date getDate() {
		return getDueDate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.entity.CalendarEntity#getDuration()
	 */
	@Override
	public Integer getDuration() {
		return Integer.valueOf(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.entity.CalendarEntity#getNextTodo()
	 */
	@Override
	public Date getNextTodo() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.entity.CalendarEntity#getText()
	 */
	@Override
	public String getText() {
		// return the text as it should appear on the calendar
		String showabb = Prefs.getPref(PrefName.TASK_SHOW_ABBREV);
		String abb = "";
		if (showabb.equals("true"))
			abb = "BT" + getTask() + "/ST" + getKey() + " ";
		else {
			if (taskDesc == null) {
				Task t;
				try {
					t = TaskModel.getReference().getTask(Task);
					if (t != null) {
						taskDesc = t.getSummary();
					}
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}

			}
			abb = "[" + taskDesc + "] ";
		}
		String de = abb + getDescription();
		String tx = de.replace('\n', ' ');
		return tx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.entity.CalendarEntity#getTodo()
	 */
	@Override
	public boolean isTodo() {
		return true;
	}

	@Override
	public Integer getPriority() {
		return null;
	}

	@Override
	public boolean isPrivate() {
		return false;
	}
	
	@Override
	public ObjectType getObjectType() {
		return ObjectType.SUBTASK;
	}
}
