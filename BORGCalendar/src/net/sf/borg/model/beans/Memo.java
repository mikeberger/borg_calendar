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
package net.sf.borg.model.beans;

import java.util.Date;



public class Memo extends KeyedBean<Memo> implements java.io.Serializable {

	
	private static final long serialVersionUID = -6793670294661709573L;
	private String MemoName_;
	public String getMemoName() { return( MemoName_ ); }
	public void setMemoName( String xx ){ MemoName_ = xx; }

	private String MemoText_;
	public String getMemoText() { return( MemoText_ ); }
	public void setMemoText( String xx ){ MemoText_ = xx; }

	private Integer PalmId_;
	public Integer getPalmId() { return( PalmId_ ); }
	public void setPalmId( Integer xx ){ PalmId_ = xx; }

	private boolean New_;
	public boolean getNew() { return( New_ ); }
	public void setNew( boolean xx ){ New_ = xx; }

	private boolean Modified_;
	public boolean getModified() { return( Modified_ ); }
	public void setModified( boolean xx ){ Modified_ = xx; }

	private boolean Deleted_;
	public boolean getDeleted() { return( Deleted_ ); }
	public void setDeleted( boolean xx ){ Deleted_ = xx; }

	private boolean Private_;
	public boolean getPrivate() { return( Private_ ); }
	public void setPrivate( boolean xx ){ Private_ = xx; }
	
	private Date created_;
	private Date updated_;
	
	public Date getCreated() {
	    return created_;
	}
	public void setCreated(Date created_) {
	    this.created_ = created_;
	}
	public Date getUpdated() {
	    return updated_;
	}
	public void setUpdated(Date updated_) {
	    this.updated_ = updated_;
	}

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
