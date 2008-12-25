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
import java.util.Collection;

import net.sf.borg.model.beans.KeyedBean;


/**
 *
 * @author  mbb
 */
/*
 * interface for a database that manages DataBeans, keyed by an integer key 
 */
public interface BeanDB
{
	public Collection readAll() throws  Exception;
    public KeyedBean readObj( int key ) throws  Exception;
    public KeyedBean newObj();
    public void addObj( KeyedBean bean, boolean crypt ) throws  Exception;
    public void updateObj( KeyedBean bean, boolean crypt ) throws  Exception;
    public void delete( int key ) throws Exception;

    public void close() throws Exception;
    public int nextkey() throws Exception;
   
}
