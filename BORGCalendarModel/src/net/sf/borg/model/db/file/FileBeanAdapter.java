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
/*
 * FileBeanAdapter.java
 *
 * Created on January 1, 2004, 2:50 PM
 */

package net.sf.borg.model.db.file;
import net.sf.borg.model.db.KeyedBean;
import net.sf.borg.model.db.file.mdb.Row;
import net.sf.borg.model.db.file.mdb.Schema;

/**
 *
 * @author  mbb
 */
// DataAdapters are used by BeanDB to convert DataBeans to and from Row objects
// or XML
interface FileBeanAdapter
{
    public KeyedBean fromRow( Row r ) throws Exception;
    public Row toRow( Schema sch, KeyedBean bean, boolean normalize ) throws Exception;
    public KeyedBean newBean();
}

