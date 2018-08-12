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
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;



/**
 * The Task Entity
 */
@XmlRootElement(name="Task")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@EqualsAndHashCode(callSuper=true)
public class Task extends KeyedEntity<Task> implements CalendarEntity, SyncableEntity {

	
	private static final long serialVersionUID = -8980203293028263282L;
	
	private Date StartDate;
	private Date CompletionDate;
	private Date DueDate;
	private String PersonAssigned;
	private Integer Priority;
	private String State;
	private String Type;
	private String Description;
	private String Resolution;
	private String Category;
	private Integer Project;
	private String summary;
	private Date createTime;
	private Date lastMod;
	private String uid;
	private String url;

	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getColor()
	 */
	@Override
	public String getColor()
	{
		return "navy";
	}
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDuration()
	 */
	@Override
	public Integer getDuration()
	{
		return Integer.valueOf(0);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDate()
	 */
	@Override
	public Date getDate(){ return getDueDate(); }
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getTodo()
	 */
	@Override
	public boolean isTodo(){ return true; }
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getNextTodo()
	 */
	@Override
	public Date getNextTodo(){ return null; }
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getText()
	 */
	@Override
	public String getText(){
		// return the text as it should appear on the calendar
		 String showabb = Prefs.getPref(PrefName.TASK_SHOW_ABBREV);
		 String abb = "";
         if (showabb.equals("true"))
             abb = "BT" + getKey() + " ";
         return abb + getSummary();
	}
	
	// for backwards compatibility
	public String getSummary(){
		if( summary != null && !summary.isEmpty())
			return summary;
		
		if( Description != null && !Description.isEmpty())
		{
			String de = Description;
			if( de.indexOf('\n') != -1)
				de = de.substring(0,de.indexOf('\n'));
			return de;
		}
		
		return "";
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
	@Override
	protected Task clone() {
		Task dst = new Task();
		dst.setKey( getKey());
		dst.setStartDate( getStartDate() );
		dst.setCompletionDate( getCompletionDate() );
		dst.setDueDate( getDueDate() );
		dst.setPersonAssigned( getPersonAssigned() );
		dst.setPriority( getPriority() );
		dst.setState( getState() );
		dst.setType( getType() );
		dst.setDescription( getDescription() );
		dst.setResolution( getResolution() );
		dst.setCategory( getCategory() );
		dst.setProject( getProject() );
		dst.setSummary( getSummary() );
		dst.setCreateTime(getCreateTime());
		dst.setLastMod(getLastMod());
		dst.setUid(getUid());
		dst.setUrl(getUrl());
		return(dst);
	}

	@Override
	public boolean isPrivate() {
		return false;
	}

	@Override
	public ObjectType getObjectType() {
		return ObjectType.TASK;
	}
}
