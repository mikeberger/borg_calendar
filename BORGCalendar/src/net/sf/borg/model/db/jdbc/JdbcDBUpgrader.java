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

class JdbcDBUpgrader {

	private String checkSql;

	private String updSql[];

	public JdbcDBUpgrader(String checkSql, String usql) {
		updSql = new String[1];
		this.updSql[0] = usql;
		this.checkSql = checkSql;
	}
	
	public JdbcDBUpgrader(String checkSql, String usql[]) {
		this.updSql = usql;
		this.checkSql = checkSql;
	}

	private boolean needsUpgrade() throws Exception {
		try {
			JdbcDB.execSQL(checkSql);
		} catch (Exception e) {
			if (e instanceof SQLException)
				return true;
		}

		return false;
	}

	private void performUpgrade() throws Exception {
		String dbtype = Prefs.getPref(PrefName.DBTYPE);
		for( int i = 0; i < updSql.length; i++ )
		{
			
			if (dbtype.equals("mysql")) {
			    Errmsg.notice(Resource.getPlainResourceString("update_error") + updSql[i]);
				continue;
			} 
			System.out.println("Running Upgrade SQL:" + updSql[i]);
			JdbcDB.execSQL(updSql[i]);
		}
	}

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
