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



/**
 * Memo Entity. A Memo is a simple text entry keyed by a memo name. It remains simple
 * since it corresponds to the simple memo objects that can be synced to a palm pilot.
 */
public class Memo extends KeyedEntity<Memo> implements java.io.Serializable {

	
	private static final long serialVersionUID = -6793670294661709573L;
	
	/** The Memo name_. */
	private String MemoName_;
	
	/**
	 * Gets the memo name.
	 * 
	 * @return the memo name
	 */
	public String getMemoName() { return( MemoName_ ); }
	
	/**
	 * Sets the memo name.
	 * 
	 * @param xx the new memo name
	 */
	public void setMemoName( String xx ){ MemoName_ = xx; }

	/** The Memo text_. */
	private String MemoText_;
	
	/**
	 * Gets the memo text.
	 * 
	 * @return the memo text
	 */
	public String getMemoText() { return( MemoText_ ); }
	
	/**
	 * Sets the memo text.
	 * 
	 * @param xx the new memo text
	 */
	public void setMemoText( String xx ){ MemoText_ = xx; }
	
	// deprecating the palm stuff - it's almost dead

	/** The Palm id_. */
	@Deprecated private Integer PalmId_;
	
	/**
	 * Gets the palm id.
	 * 
	 * @return the palm id
	 */
	@Deprecated public Integer getPalmId() { return( PalmId_ ); }
	
	/**
	 * Sets the palm id.
	 * 
	 * @param xx the new palm id
	 */
	@Deprecated public void setPalmId( Integer xx ){ PalmId_ = xx; }

	@Deprecated private boolean New_;
	@Deprecated public boolean getNew() { return( New_ ); }
	@Deprecated public void setNew( boolean xx ){ New_ = xx; }
	@Deprecated private boolean Modified_;
	@Deprecated public boolean getModified() { return( Modified_ ); }
	@Deprecated public void setModified( boolean xx ){ Modified_ = xx; }
	@Deprecated private boolean Deleted_;
	@Deprecated public boolean getDeleted() { return( Deleted_ ); }
	@Deprecated public void setDeleted( boolean xx ){ Deleted_ = xx; }

	/** The Private flag - used by the palm. */
	@Deprecated private boolean Private_;
	
	/**
	 * Gets the private flag.
	 * 
	 * @return the private
	 */
	@Deprecated public boolean getPrivate() { return( Private_ ); }
	
	/**
	 * Sets the private flag.
	 * 
	 * @param xx the new private
	 */
	@Deprecated public void setPrivate( boolean xx ){ Private_ = xx; }
	
	/** The creation date. */
	private Date created_;
	
	/** The last update date. */
	private Date updated_;
	
	/**
	 * Gets the creation date.
	 * 
	 * @return the creation date
	 */
	public Date getCreated() {
	    return created_;
	}
	
	/**
	 * Sets the creation date.
	 * 
	 * @param created_ the creation date
	 */
	public void setCreated(Date created_) {
	    this.created_ = created_;
	}
	
	/**
	 * Gets the  last update date.
	 * 
	 * @return the  last update date
	 */
	public Date getUpdated() {
	    return updated_;
	}
	
	/**
	 * Sets the  last update date.
	 * 
	 * @param updated_ the  last update date
	 */
	public void setUpdated(Date updated_) {
	    this.updated_ = updated_;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
	protected Memo clone() {
		Memo dst = new Memo();
		dst.setKey( getKey());
		dst.setMemoName( getMemoName() );
		dst.setMemoText( getMemoText() );
		dst.setPalmId( getPalmId() );
		dst.setNew( getNew() );
		dst.setModified( getModified() );
		dst.setDeleted( getDeleted() );
		dst.setPrivate( getPrivate() );
		dst.setCreated(getCreated());
		dst.setUpdated(getUpdated());
		return(dst);
	}
	
}
