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



/**
 * The Tasklog Entity
 */
public class Tasklog extends KeyedEntity<Tasklog> implements java.io.Serializable {


	private static final long serialVersionUID = -7296390517941361874L;

	/** The log time_. */
	private java.util.Date logTime_;
	
	/**
	 * Gets the log time.
	 * 
	 * @return the log time
	 */
	public java.util.Date getlogTime() { return( logTime_ ); }
	
	/**
	 * Sets the log time.
	 * 
	 * @param xx the new log time
	 */
	public void setlogTime( java.util.Date xx ){ logTime_ = xx; }

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

	/** The Task_. */
	private Integer Task_;
	
	/**
	 * Gets the task.
	 * 
	 * @return the task
	 */
	public Integer getTask() { return( Task_ ); }
	
	/**
	 * Sets the task.
	 * 
	 * @param xx the new task
	 */
	public void setTask( Integer xx ){ Task_ = xx; }

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
	protected Tasklog clone() {
		Tasklog dst = new Tasklog();
		dst.setKey( getKey());
		dst.setlogTime( getlogTime() );
		dst.setDescription( getDescription() );
		dst.setTask( getTask() );
		return(dst);
	}
}
