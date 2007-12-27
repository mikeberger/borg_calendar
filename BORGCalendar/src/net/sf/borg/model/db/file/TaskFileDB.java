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

package net.sf.borg.model.db.file;

import java.io.File;
import java.net.URL;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.common.XTree;
import net.sf.borg.model.db.file.mdb.MDB;
import net.sf.borg.model.db.file.mdb.SMDB;
import net.sf.borg.model.db.file.mdb.Schema;

class TaskFileDB extends FileDBCreator {
    TaskFileDB() {
    }

    final void init(String file, boolean shared) throws Exception {
	file = file + "/mrdb.jdb";

	TaskAdapter ta = new TaskAdapter();
	// set the schema for the task DB
	Schema schema = new Schema();
	URL schurl = getClass().getResource("/resource/task_schema.xml");
	XTree sch_xml = XTree.readFromURL(schurl);
	schema.setFromXML(sch_xml);

	// create the database if it does not exisit
	File fp = new File(file);
	boolean newdb = false;
	if (!fp.exists()) {
	    // System.out.println( "Task DB does not exist...creating DB " );
	    Errmsg.notice(Resource.getResourceString("Creating_DB_file:_") + file);
	    SMDB.create(Resource.getResourceString("BorgTrac_Database"), file, 100, schema);
	    newdb = true;
	}

	// open the DB with the proper mode


	    db_ = new FileBeanDB(file, MDB.READ_WRITE, ta, shared);
	

	// check schema for update - transition
	FileBeanDB fdb = (FileBeanDB) db_;
	if (newdb) {
	    fdb.setNormalize(true);
	}
	Schema oldsch = fdb.getSchema();
	try {
	    oldsch.getType("UT1");
	    oldsch.getType("CAT");
	} catch (Exception e) {
	    // DB does not have new fields - update the schema
	    fdb.setSchema(schema);
	}
    }
}
