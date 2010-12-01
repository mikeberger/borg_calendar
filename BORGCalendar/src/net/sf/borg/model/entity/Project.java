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
 * Project Entity - a project contains tasks and can have child projects
 */
@XmlRootElement(name="Project")
@XmlAccessorType(XmlAccessType.NONE)
public class Project extends KeyedEntity<Project> implements CalendarEntity {

	
	private static final long serialVersionUID = -3250115693306817331L;
	
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

	/** The Status. */
	@XmlElement
	private String Status;
	
	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public String getStatus() { return( Status ); }
	
	/**
	 * Sets the status.
	 * 
	 * @param xx the new status
	 */
	public void setStatus( String xx ){ Status = xx; }
	
	/** The Parent. */
	@XmlElement
	private Integer Parent;
	
	/**
	 * Gets the parent.
	 * 
	 * @return the parent
	 */
	public Integer getParent() {
	    return Parent;
	}
	
	/**
	 * Sets the parent.
	 * 
	 * @param parent the new parent
	 */
	public void setParent(Integer parent) {
	    this.Parent = parent;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
	@Override
	protected Project clone() {
		Project dst = new Project();
		dst.setKey( getKey());
		dst.setStartDate( getStartDate() );
		dst.setDueDate( getDueDate() );
		dst.setDescription( getDescription() );
		dst.setCategory( getCategory() );
		dst.setStatus( getStatus() );
		dst.setParent(getParent());
		return(dst);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getColor()
	 */
	@Override
	public String getColor()
	{
		// for showing on calendar
		// legacy color name - maps to a user-defined color
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
             abb = "PR" + getKey() + " ";
         String de = abb + getDescription();
         String tx = de.replace('\n', ' ');
         return tx;
	}

	@Override
	public Integer getPriority() {
		return null;
	}

}
