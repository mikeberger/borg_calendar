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
