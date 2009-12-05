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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



/**
 * Link entity. A Link represents an association between an Entity (Appointment, Project, Task, Address) and 
 * another Entity, a URL, or a File
 */
@XmlRootElement(name="Link")
@XmlAccessorType(XmlAccessType.NONE)
public class Link extends KeyedEntity<Link> {

	
	private static final long serialVersionUID = 1476303921088473573L;
	
	/** The key of the owning KeyedEntity. */
	@XmlElement(name="OwnerKey")
	private Integer ownerKey;
	
	/** The type of the KeyedEntity - mapped in LinkModel.java  */
	@XmlElement(name="OwnerType")
	private String ownerType;
	
	/** The identifier of the link target - i.e. an entity id, file path, or url */
	@XmlElement(name="Path")
	private String path;
	
	/** The link type - see LinkModel.LinkType. */
	@XmlElement(name="LinkType")
	private String linkType;
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.entity.KeyedEntity#clone()
	 */
	@Override
	protected Link clone() {
		Link dst = new Link();
		dst.setKey( getKey());
		dst.setLinkType(getLinkType());
		dst.setOwnerKey( getOwnerKey() );
		dst.setOwnerType( getOwnerType() );
		dst.setPath( getPath() );
		return(dst);
	}
	
	/**
	 * Gets the key of the owning KeyedEntity.
	 * 
	 * @return the owner key
	 */
	public Integer getOwnerKey() {
		return ownerKey;
	}
	
	/**
	 * Sets the key of the owning KeyedEntity.
	 * 
	 * @param ownerKey the new owner key
	 */
	public void setOwnerKey(Integer ownerKey) {
		this.ownerKey = ownerKey;
	}
	
	/**
	 * Gets the type of the KeyedEntity - mapped in LinkModel.java
	 * 
	 * @return the owner type
	 */
	public String getOwnerType() {
		return ownerType;
	}
	
	/**
	 * Sets the type of the KeyedEntity - mapped in LinkModel.java.
	 * 
	 * @param ownerType the new owner type
	 */
	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}
	
	/**
	 * Gets the identifier of the link target - i.e. an entity id, file path, or url
	 * 
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Sets the identifier of the link target - i.e. an entity id, file path, or url
	 * 
	 * @param path the new path
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * Gets the link type  - see LinkModel.LinkType.
	 * 
	 * @return the link type
	 */
	public String getLinkType() {
		return linkType;
	}
	
	/**
	 * Sets the link type - see LinkModel.LinkType.
	 * 
	 * @param linkType the new link type
	 */
	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}
	
}
