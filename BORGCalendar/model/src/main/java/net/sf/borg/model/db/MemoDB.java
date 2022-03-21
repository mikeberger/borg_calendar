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
package net.sf.borg.model.db;

import net.sf.borg.model.entity.Memo;

import java.util.Collection;

/**
 * The Interface for a Memo DB.
 */
public interface MemoDB {

    /**
     * Adds a memo to the database.
     * 
     * @param m the Memo
     * 
     * @throws Exception
     */
    void addMemo(Memo m) throws Exception;

    /**
     * Delete a memo by name.
     * 
     * @param name the name
     * 
     * @throws Exception
     */
    void delete(String name) throws Exception;

    /**
     * Gets all memo names form the db
     * 
     * @return a collection of memo names
     * 
     * @throws Exception
     */
    Collection<String> getNames() throws Exception;

    /**
     * Read all memos from the db
     * 
     * @return a collection of all memos
     * 
     * @throws Exception
     */
    Collection<Memo> readAll() throws Exception;

    /**
     * Read a memo by name
     * 
     * @param name the name
     * 
     * @return the memo
     * 
     * @throws Exception
     */
    Memo readMemo(String name) throws Exception;

    /**
     * Update a memo in the db.
     * 
     * @param m the memo
     * 
     * @throws Exception
     */
    void updateMemo(Memo m) throws Exception;
        
    
}