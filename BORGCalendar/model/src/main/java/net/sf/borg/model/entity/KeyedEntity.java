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
/*
 * KeyedEntity.java
 *
 * Created on January 1, 2004, 2:43 PM
 */

package net.sf.borg.model.entity;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

import java.io.Serializable;

/**
 * Abstract base class for all Entities that are keyed by a simple integer key
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@Data
public abstract class KeyedEntity<T> implements Serializable
{
    
    private static final long serialVersionUID = 1L;
    
	@XmlElement(name="KEY")
    private int key = -1;
        
    
    /**
     * Copy the entity
     * 
     * @return a copy
     */
    public T copy()
    {
    	return clone();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    protected T clone(){
    	throw new UnsupportedOperationException();
    }
    
}
