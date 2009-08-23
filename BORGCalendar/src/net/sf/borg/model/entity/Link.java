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



public class Link extends KeyedEntity<Link> implements java.io.Serializable {

	
	private static final long serialVersionUID = 1476303921088473573L;
	private Integer ownerKey;
	private String ownerType;
	private String path;
	private String linkType;
	
	protected Link clone() {
		Link dst = new Link();
		dst.setKey( getKey());
		dst.setLinkType(getLinkType());
		dst.setOwnerKey( getOwnerKey() );
		dst.setOwnerType( getOwnerType() );
		dst.setPath( getPath() );
		return(dst);
	}
	public Integer getOwnerKey() {
		return ownerKey;
	}
	public void setOwnerKey(Integer ownerKey) {
		this.ownerKey = ownerKey;
	}
	public String getOwnerType() {
		return ownerType;
	}
	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getLinkType() {
		return linkType;
	}
	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}
	
}
