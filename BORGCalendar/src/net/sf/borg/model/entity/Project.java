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
 * Project Entity - a project contains tasks and can have child projects
 */
public class Project extends KeyedEntity<Project> implements CalendarEntity,java.io.Serializable {

	
	private static final long serialVersionUID = -3250115693306817331L;
	
	/** The Start date_. */
	private java.util.Date StartDate_;
	
	/**
	 * Gets the start date.
	 * 
	 * @return the start date
	 */
	public java.util.Date getStartDate() { return( StartDate_ ); }
	
	/**
	 * Sets the start date.
	 * 
	 * @param xx the new start date
	 */
	public void setStartDate( java.util.Date xx ){ StartDate_ = xx; }

	/** The Due date_. */
	private java.util.Date DueDate_;
	
	/**
	 * Gets the due date.
	 * 
	 * @return the due date
	 */
	public java.util.Date getDueDate() { return( DueDate_ ); }
	
	/**
	 * Sets the due date.
	 * 
	 * @param xx the new due date
	 */
	public void setDueDate( java.util.Date xx ){ DueDate_ = xx; }

	/** The Description_. */
	private String Description_;
	
	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() { return( Description_ ); }
	
	/**
	 * Sets the description.
	 * 
	 * @param xx the new description
	 */
	public void setDescription( String xx ){ Description_ = xx; }

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

	/** The Status_. */
	private String Status_;
	
	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public String getStatus() { return( Status_ ); }
	
	/**
	 * Sets the status.
	 * 
	 * @param xx the new status
	 */
	public void setStatus( String xx ){ Status_ = xx; }
	
	/** The parent_. */
	private Integer parent_;
	
	/**
	 * Gets the parent.
	 * 
	 * @return the parent
	 */
	public Integer getParent() {
	    return parent_;
	}
	
	/**
	 * Sets the parent.
	 * 
	 * @param parent the new parent
	 */
	public void setParent(Integer parent) {
	    this.parent_ = parent;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
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
	public String getColor()
	{
		return "navy";
	}
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDuration()
	 */
	public Integer getDuration()
	{
		return new Integer(0);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getDate()
	 */
	public Date getDate(){ return getDueDate(); }
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getTodo()
	 */
	public boolean getTodo(){ return true; }
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getNextTodo()
	 */
	public Date getNextTodo(){ return null; }
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.CalendarEntity#getText()
	 */
	public String getText(){
		 String show_abb = Prefs.getPref(PrefName.TASK_SHOW_ABBREV);
		 String abb = "";
         if (show_abb.equals("true"))
             abb = "PR" + getKey() + " ";
         String de = abb + getDescription();
         String tx = de.replace('\n', ' ');
         return tx;
	}

}
