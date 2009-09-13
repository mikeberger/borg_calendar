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



/**
 * Memo Entity. A Memo is a simple text entry keyed by a memo name. It remains simple
 * since it corresponds to the simple memo objects that can be synced to a palm pilot.
 */
@XmlRootElement(name="Memo")
@XmlAccessorType(XmlAccessType.FIELD)
public class Memo extends KeyedEntity<Memo> implements java.io.Serializable {

	
	private static final long serialVersionUID = -6793670294661709573L;
	
	/** The Memo name. */
	private String MemoName;
	
	/**
	 * Gets the memo name.
	 * 
	 * @return the memo name
	 */
	public String getMemoName() { return( MemoName ); }
	
	/**
	 * Sets the memo name.
	 * 
	 * @param xx the new memo name
	 */
	public void setMemoName( String xx ){ MemoName = xx; }

	/** The Memo text. */
	private String MemoText;
	
	/**
	 * Gets the memo text.
	 * 
	 * @return the memo text
	 */
	public String getMemoText() { return( MemoText ); }
	
	/**
	 * Sets the memo text.
	 * 
	 * @param xx the new memo text
	 */
	public void setMemoText( String xx ){ MemoText = xx; }
	
	/** The creation date. */
	private Date Created;
	
	/** The last update date. */
	private Date Updated;
	
	/**
	 * Gets the creation date.
	 * 
	 * @return the creation date
	 */
	public Date getCreated() {
	    return Created;
	}
	
	/**
	 * Sets the creation date.
	 * 
	 * @param Created the creation date
	 */
	public void setCreated(Date created) {
	    this.Created = created;
	}
	
	/**
	 * Gets the  last update date.
	 * 
	 * @return the  last update date
	 */
	public Date getUpdated() {
	    return Updated;
	}
	
	/**
	 * Sets the  last update date.
	 * 
	 * @param Updated the  last update date
	 */
	public void setUpdated(Date updated) {
	    this.Updated = updated;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
	protected Memo clone() {
		Memo dst = new Memo();
		dst.setKey( getKey());
		dst.setMemoName( getMemoName() );
		dst.setMemoText( getMemoText() );
		dst.setCreated(getCreated());
		dst.setUpdated(getUpdated());
		return(dst);
	}
	
}
