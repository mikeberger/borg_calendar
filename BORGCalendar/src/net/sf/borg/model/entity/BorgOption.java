/*
This file is part of BORG.
 
    BORG is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
 
    BORG is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with BORG; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
Copyright 2003 by Mike Berger
 */

package net.sf.borg.model.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Each BorgOption instance holds a single row from the options table in the database
 */
@XmlRootElement(name="Option")
@XmlAccessorType(XmlAccessType.NONE)
public class BorgOption
{
	
	/** for JAXB */
	@SuppressWarnings("unused")
	private BorgOption(){
	  // empty
	}
	
	/**
	 * Instantiates a new borg option.
	 * 
	 * @param key the key
	 * @param value the value
	 */
	public BorgOption(String key, String value)
	{
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Gets the key.
	 * 
	 * @return the key
	 */
	public final String getKey()	{return key;}
	
	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public final String getValue()	{return value;}
	
	/** The key. */
	@XmlElement(name="Key")
	private String key;
	
	/** The value. */
	@XmlElement(name="Value")
	private String value;
}