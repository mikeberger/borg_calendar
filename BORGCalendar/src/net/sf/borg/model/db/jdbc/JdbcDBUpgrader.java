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
package net.sf.borg.model.db.jdbc;

import java.sql.SQLException;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;

/**
 * Class JdbcDBUpgrader is used to upgrade HSQL database in-place when the schema changes for a 
 * release. This class will check an update condition, and if true, execute SQL to upgrade the DB.
 * It is meant to be called during model initialization - preferably from the constructors
 * of the JdbcDB classes
 */
public class JdbcDBUpgrader {

	/** The check sql. */
	private String checkSql;

	/** The upd sql. */
	private String updSql[];

	/**
	 * Instantiates a new jdbc db upgrader.
	 * 
	 * @param checkSql the sql that checks if an upgrade is needed
	 * @param usql the sql to upgrade the db if needed
	 */
	public JdbcDBUpgrader(String checkSql, String usql) {
		updSql = new String[1];
		this.updSql[0] = usql;
		this.checkSql = checkSql;
	}
	
	/**
	 * Instantiates a new jdbc db upgrader.
	 * 
	 * @param checkSql the sql that checks if an upgrade is needed
	 * @param usql an array of SQL statements to execute to perform the upgrade
	 */
	public JdbcDBUpgrader(String checkSql, String usql[]) {
		this.updSql = usql;
		this.checkSql = checkSql;
	}

	/**
	 * check if db Needs upgrade.
	 * 
	 * @return true, if upgrade needed
	 * 
	 * @throws Exception the exception
	 */
	private boolean needsUpgrade() throws Exception {
		try {
			JdbcDB.execSQL(checkSql);
		} catch (Exception e) {
			if (e instanceof SQLException)
				return true;
		}

		return false;
	}

	/**
	 * Execute the upgrade SQL.
	 * If MYSQL - just show the SQL to the user - do not upgrade
	 * 
	 * @throws Exception the exception
	 */
	private void performUpgrade() throws Exception {
		String dbtype = Prefs.getPref(PrefName.DBTYPE);
		for( int i = 0; i < updSql.length; i++ )
		{
			
			if (dbtype.equals("mysql")) {
			    Errmsg.notice(Resource.getResourceString("update_error") + updSql[i]);
				continue;
			} 
			System.out.println("Running Upgrade SQL:" + updSql[i]);
			JdbcDB.execSQL(updSql[i]);
		}
	}

	/**
	 * run the upgrade check and then upgrade if needed
	 */
	public void upgrade() {
		try {
			if (needsUpgrade()) {
				performUpgrade();
			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}
}
