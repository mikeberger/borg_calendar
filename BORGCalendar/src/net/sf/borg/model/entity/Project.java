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



public class Project extends KeyedEntity<Project> implements CalendarEntity,java.io.Serializable {

	
	private static final long serialVersionUID = -3250115693306817331L;
	private Integer Id_;
	public Integer getId() { return( Id_ ); }
	public void setId( Integer xx ){ Id_ = xx; }

	private java.util.Date StartDate_;
	public java.util.Date getStartDate() { return( StartDate_ ); }
	public void setStartDate( java.util.Date xx ){ StartDate_ = xx; }

	private java.util.Date DueDate_;
	public java.util.Date getDueDate() { return( DueDate_ ); }
	public void setDueDate( java.util.Date xx ){ DueDate_ = xx; }

	private String Description_;
	public String getDescription() { return( Description_ ); }
	public void setDescription( String xx ){ Description_ = xx; }

	private String Category_;
	public String getCategory() { return( Category_ ); }
	public void setCategory( String xx ){ Category_ = xx; }

	private String Status_;
	public String getStatus() { return( Status_ ); }
	public void setStatus( String xx ){ Status_ = xx; }
	
	private Integer parent_;
	public Integer getParent() {
	    return parent_;
	}
	public void setParent(Integer parent) {
	    this.parent_ = parent;
	}

	protected Project clone() {
		Project dst = new Project();
		dst.setKey( getKey());
		dst.setId( getId() );
		dst.setStartDate( getStartDate() );
		dst.setDueDate( getDueDate() );
		dst.setDescription( getDescription() );
		dst.setCategory( getCategory() );
		dst.setStatus( getStatus() );
		dst.setParent(getParent());
		return(dst);
	}
	
	public String getColor()
	{
		return "navy";
	}
	
	public Integer getDuration()
	{
		return new Integer(0);
	}
	
	public Date getDate(){ return getDueDate(); }
	
	public boolean getTodo(){ return true; }
	
	public Date getNextTodo(){ return null; }
	
	public String getText(){
		 String show_abb = Prefs.getPref(PrefName.TASK_SHOW_ABBREV);
		 String abb = "";
         if (show_abb.equals("true"))
             abb = "PR" + getId().toString() + " ";
         String de = abb + getDescription();
         String tx = de.replace('\n', ' ');
         return tx;
	}

}
