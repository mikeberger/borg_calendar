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

import java.lang.reflect.Method;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;

/**
 * A singleton instance which creates an
 * {@link IBeanDataFactory IBeanDataFactory} based on the file type.
 * 
 * @author Mohan Embar
 */
public class BeanDataFactoryFactory {
    /**
         * Singleton.
         */
    public static BeanDataFactoryFactory getInstance() {
	return instance;
    }

    public final IBeanDataFactory getFactory(StringBuffer dbdir,
	    boolean shared) throws Exception {
	String db = dbdir.toString();
	String factoryClass = null;
	if (db.startsWith("jdbc:")) {
	    factoryClass = "net.sf.borg.model.db.jdbc.JdbcBeanDataFactory";
	} else if (db.startsWith("remote:")) {
	    factoryClass = "net.sf.borg.model.db.remote.RemoteBeanDataFactory";
	} else {
	    // Use default File DB; append parms to url
	    factoryClass = "net.sf.borg.model.db.file.FileBeanDataFactory";
	    dbdir.append("::").append(shared);
	}

	Method getInst = Class.forName(factoryClass).getMethod("getInstance",
		null);
	return (IBeanDataFactory) getInst.invoke(null, null);
    }

    public static String buildDbDir() {
	// get dir for DB
	String dbdir = "";
	String dbtype = Prefs.getPref(PrefName.DBTYPE);
	if (dbtype.equals("local")) {
	    dbdir = Prefs.getPref(PrefName.DBDIR);
	} else if (dbtype.equals("remote")) {
	    dbdir = "remote:" + Prefs.getPref(PrefName.DBURL);
	} else if (dbtype.equals("hsqldb")) {
	    String hdir = Prefs.getPref(PrefName.HSQLDBDIR);
	    if (hdir.equals("not-set"))
		return hdir;
	    dbdir = "jdbc:hsqldb:file:" + Prefs.getPref(PrefName.HSQLDBDIR)
		    + "/borg_";
	} else if( dbtype.equals("jdbc")) {
		dbdir = Prefs.getPref(PrefName.JDBCURL);
	} else {
	    // build a mysql URL
	    dbdir = "jdbc:mysql://" + Prefs.getPref(PrefName.DBHOST) + ":"
		    + Prefs.getPref(PrefName.DBPORT) + "/"
		    + Prefs.getPref(PrefName.DBNAME) + "?user="
		    + Prefs.getPref(PrefName.DBUSER) + "&password="
		    + Prefs.getPref(PrefName.DBPASS) + "&autoReconnect=true";
	}

	System.out.println("DB URL is: " + dbdir);
	return (dbdir);
    }

    // private //
    private static final BeanDataFactoryFactory instance = new BeanDataFactoryFactory();

    private BeanDataFactoryFactory() {
    }
}
