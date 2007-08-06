package net.sf.borg.model.db.jdbc;

import java.sql.SQLException;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;

public class JdbcDBUpgrader {

    private String checkSql;

    private String updSql;

    public JdbcDBUpgrader(String checkSql, String usql) {
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
	System.out.println("Running Upgrade SQL:" + updSql);
	JdbcDB.execSQL(updSql);
    }

    public void upgrade() {
	try {
	    if (needsUpgrade()) {
		performUpgrade();
		if (needsUpgrade()) {
		    Errmsg.notice(Resource
			    .getPlainResourceString("update_error")
			    + updSql);
		}
	    }
	} catch (Exception e) {
	    Errmsg.notice(Resource.getPlainResourceString("update_error")
		    + updSql);
	}
    }
}
