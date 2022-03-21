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

package net.sf.borg.model.db;

import net.sf.borg.model.entity.KeyedEntity;

import java.util.Collection;


/**
 * Interface for a class that provides basic ORM mapping for a KeyedEntity
 */
public interface EntityDB<T extends KeyedEntity<T>>
{
	
	/**
	 * Read all KeyedEntities of a particular type from the database.
	 * 
	 * @return the collection of KeyedEntities
	 * 
	 * @throws Exception
	 */
    Collection<T> readAll() throws  Exception;
    
    /**
     * Read a single KeyedEntity from the database by key
     * 
     * @param key the key
     * 
     * @return the KeyedEntity
     * 
     * @throws Exception 
     */
    T readObj(int key) throws  Exception;
    
    /**
     * Return a new instance of the KeyedEntity
     * 
     * @return the new KeyedEntity
     */
    T newObj();
    
    /**
     * Adds a KeyedEntity to the database
     * 
     * @param entity the KeyedEntity
     * 
     * @throws Exception 
     */
    void addObj(T entity) throws  Exception;
    
    /**
     * Update a KeyedEntity in the database
     * 
     * @param entity the KeyedEntity
     * 
     * @throws Exception 
     */
    void updateObj(T entity) throws  Exception;
    
    /**
     * Delete a KeyedEntity from the database
     * 
     * @param key the key of the entity
     * 
     * @throws Exception
     */
    void delete(int key) throws Exception;
    
    /**
     * get the next available key value for this entity type
     * 
     * @return the next available key
     * 
     * @throws Exception
     */
    int nextkey() throws Exception;
    
    /**
     * Sync with the database (likely to just be a cache flush)
     */
    void sync();
}
