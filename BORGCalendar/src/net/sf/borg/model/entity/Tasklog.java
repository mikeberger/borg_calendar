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



public class Tasklog extends KeyedEntity<Tasklog> implements java.io.Serializable {


	private static final long serialVersionUID = -7296390517941361874L;

	private java.util.Date logTime_;
	public java.util.Date getlogTime() { return( logTime_ ); }
	public void setlogTime( java.util.Date xx ){ logTime_ = xx; }

	private String Description_;
	public String getDescription() { return( Description_ ); }
	public void setDescription( String xx ){ Description_ = xx; }

	private Integer Task_;
	public Integer getTask() { return( Task_ ); }
	public void setTask( Integer xx ){ Task_ = xx; }

	protected Tasklog clone() {
		Tasklog dst = new Tasklog();
		dst.setKey( getKey());
		dst.setlogTime( getlogTime() );
		dst.setDescription( getDescription() );
		dst.setTask( getTask() );
		return(dst);
	}
}
