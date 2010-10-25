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
 * The Task Entity
 */
@XmlRootElement(name="Task")
@XmlAccessorType(XmlAccessType.NONE)
public class Task extends KeyedEntity<Task> implements CalendarEntity {

	
	private static final long serialVersionUID = -8980203293028263282L;
	
	/** The Start date. */
	@XmlElement
	private Date StartDate;
	
	/**
	 * Gets the start date.
	 * 
	 * @return the start date
	 */
	public Date getStartDate() { return( StartDate ); }
	
	/**
	 * Sets the start date.
	 * 
	 * @param xx the new start date
	 */
	public void setStartDate( Date xx ){ StartDate = xx; }

	/** The completion date */
	@XmlElement
	private Date CompletionDate;
	
	/**
	 * Gets the completion date.
	 * 
	 * @return the completion date
	 */
	public Date getCompletionDate() { return( CompletionDate ); }
	
	/**
	 * Sets the completion date.
	 * 
	 * @param xx the new completion date
	 */
	public void setCompletionDate( Date xx ){ CompletionDate = xx; }

	/** The Due date. */
	@XmlElement
	private Date DueDate;
	
	/**
	 * Gets the due date.
	 * 
	 * @return the due date
	 */
	public Date getDueDate() { return( DueDate ); }
	
	/**
	 * Sets the due date.
	 * 
	 * @param xx the new due date
	 */
	public void setDueDate( Date xx ){ DueDate = xx; }

	/** The Person assigned. */
	@XmlElement
	private String PersonAssigned;
	
	/**
	 * Gets the person assigned.
	 * 
	 * @return the person assigned
	 */
	public String getPersonAssigned() { return( PersonAssigned ); }
	
	/**
	 * Sets the person assigned.
	 * 
	 * @param xx the new person assigned
	 */
	public void setPersonAssigned( String xx ){ PersonAssigned = xx; }

	/** The Priority. */
	@XmlElement
	private Integer Priority;
	
	/**
	 * Gets the priority.
	 * 
	 * @return the priority
	 */
	public Integer getPriority() { return( Priority ); }
	
	/**
	 * Sets the priority.
	 * 
	 * @param xx the new priority
	 */
	public void setPriority( Integer xx ){ Priority = xx; }

	/** The State. */
	@XmlElement
	private String State;
	
	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public String getState() { return( State ); }
	
	/**
	 * Sets the state.
	 * 
	 * @param xx the new state
	 */
	public void setState( String xx ){ State = xx; }

	/** The Type. */
	@XmlElement
	private String Type;
	
	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public String getType() { return( Type ); }
	
	/**
	 * Sets the type.
	 * 
	 * @param xx the new type
	 */
	public void setType( String xx ){ Type = xx; }

	/** The Description. */
	@XmlElement
	private String Description;
	
	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() { return( Description ); }
	
	/**
	 * Sets the description.
	 * 
	 * @param xx the new description
	 */
	public void setDescription( String xx ){ Description = xx; }

	/** The Resolution. */
	@XmlElement
	private String Resolution;
	
	/**
	 * Gets the resolution.
	 * 
	 * @return the resolution
	 */
	public String getResolution() { return( Resolution ); }
	
	/**
	 * Sets the resolution.
	 * 
	 * @param xx the new resolution
	 */
	public void setResolution( String xx ){ Resolution = xx; }
	
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

	/** The Project. */
	@XmlElement
	private Integer Project;
	
	/**
	 * Gets the project.
	 * 
	 * @return the project
	 */
	public Integer getProject() { return( Project ); }
	
	/**
	 * Sets the project.
	 * 
	 * @param xx the new project
	 */
	public void setProject( Integer xx ){ Project = xx; }
	
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
		return new Integer(0);
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
	public boolean getTodo(){ return true; }
	
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
         String de = abb + getDescription();
         String tx = de.replace('\n', ' ');

         return tx;
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
		return(dst);
	}
}
