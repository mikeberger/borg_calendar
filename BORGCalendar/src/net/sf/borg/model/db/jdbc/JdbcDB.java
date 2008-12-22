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
 * JdbcDB.java
 *
 * Created on February 2, 2004, 12:57 PM
 */

package net.sf.borg.model.db.jdbc;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.BorgOption;
import net.sf.borg.model.Transactional;

/**
 * 
 * @author mberger
 */
// JdbcDB is a base class for JDBC based DBs for BORG
abstract public class JdbcDB implements /* BeanDB, */Transactional {

	// common db connection shared by sub-classes. in BORG, all sub-classes
	// will manage a table in the same DB
	static protected Connection globalConnection_ = null;

	protected Connection connection_ = null;

	private String url_;

	public void beginTransaction() throws Exception {
		globalConnection_.setAutoCommit(false);
	}

	public final void commitTransaction() throws Exception {
		PreparedStatement stmt = globalConnection_.prepareStatement("COMMIT");
		stmt.execute();
		globalConnection_.setAutoCommit(true);
	}

	public final void rollbackTransaction() throws Exception {
		PreparedStatement stmt = globalConnection_.prepareStatement("ROLLBACK");
		stmt.execute();
		globalConnection_.setAutoCommit(true);
	}

	/** Creates a new instance of JdbcDB */
	JdbcDB(String url) throws Exception {
	
		url_ = url;
		if (url.startsWith("jdbc:mysql")) {
			Class.forName("com.mysql.jdbc.Driver");
			if (globalConnection_ == null) {
				globalConnection_ = DriverManager.getConnection(url);
			}

		} else if (url.startsWith("jdbc:hsqldb")) {
			Class.forName("org.hsqldb.jdbcDriver");
			if (globalConnection_ == null) {
				Properties props = new Properties();
				props.setProperty("user", "sa");
				props.setProperty("password", "");

				// only want to apply these properties to an embedded
				// stand-alone db
				if (url.startsWith("jdbc:hsqldb:file")) {
					props.setProperty("shutdown", "true");
					props.setProperty("ifexists", "true");
				}
				try {
					globalConnection_ = DriverManager.getConnection(url, props);
				} catch (SQLException se) {
					if (se.getSQLState().equals("08003")) {
						// need to create the db
						try {
							System.out.println("Creating Database");
							InputStream is = getClass().getResourceAsStream("/resource/borg_hsqldb.sql");
							StringBuffer sb = new StringBuffer();
							InputStreamReader r = new InputStreamReader(is);
							while (true) {
								int ch = r.read();
								if (ch == -1)
									break;
								sb.append((char) ch);
							}
							props.setProperty("ifexists", "false");
							globalConnection_ = DriverManager.getConnection(url, props);
							execSQL(sb.toString());
						} catch (Exception e2) {
							throw e2;

						}
					} else if (se.getMessage().indexOf("locked") != -1) {
						throw se;
					} else {
						throw se;
					}
				}
			}

		}

		connection_ = globalConnection_;
	}

	JdbcDB(Connection conn) {

		connection_ = conn;
	}

	private final void cleanup() {
		try {
			if (globalConnection_ != null && !globalConnection_.isClosed() && url_.startsWith("jdbc:hsqldb:file")) {

				execSQL("SHUTDOWN");

			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	
	protected final static String toStr(Vector<String> v) {
		String val = "";
		if (v == null)
			return ("");
		try {
			while (true) {
				String s = v.remove(0);
				val += s;
				val += ",";
			}
		} catch (Exception e) {
		}
		return (val);
	}

	protected final static int toInt(Integer in) {
		if (in == null)
			return (0);
		return (in.intValue());
	}

	protected final static int toInt(boolean in) {
		if (in == false)
			return (0);
		return (1);
	}

	protected final static Vector<String> toVect(String s) {
		if (s == null || s.equals(""))
			return (null);

		StringTokenizer stk = new StringTokenizer(s, ",");
		Vector<String> vect = new Vector<String>();
		while (stk.hasMoreTokens()) {
			String stt = stk.nextToken();
			if (!stt.equals(""))
				vect.add(stt);
		}
		return (vect);
	}

	static final public ResultSet execSQL(String sql) throws Exception {
		PreparedStatement stmt = globalConnection_.prepareStatement(sql);
		stmt.execute();
		return stmt.getResultSet();
	}

	static public Connection getConnection() {
		return globalConnection_;
	}

	public final void close() throws Exception {
		cleanup();
		if (connection_ != null && connection_ != globalConnection_)
			connection_.close();
		else if (globalConnection_ != null)
			globalConnection_.close();
		globalConnection_ = null;
		connection_ = null;
	}


	public final String getOption(String oname) throws Exception {
		String ret = null;
		PreparedStatement stmt = connection_.prepareStatement("SELECT value FROM options WHERE name = ?");
		stmt.setString(1, oname);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			ret = rs.getString("value");
		}

		return (ret);
	}
	public final Collection<BorgOption> getOptions() throws Exception {
		ArrayList<BorgOption> keys = new ArrayList<BorgOption>();
		PreparedStatement stmt = connection_.prepareStatement("SELECT name, value FROM options");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			keys.add(new BorgOption(rs.getString("name"), rs.getString("value")));
		}

		return (keys);

	}

	public final void setOption(BorgOption option) throws Exception {
		String oname = option.getKey();
		String value = option.getValue();

		try {
			PreparedStatement stmt = connection_.prepareStatement("DELETE FROM options WHERE name = ?");
			stmt.setString(1, oname);
			stmt.executeUpdate();
		} catch (Exception e) {
		}

		if (value == null || value.equals(""))
			return;

		PreparedStatement stmt = connection_.prepareStatement("INSERT INTO options ( name, value ) "
				+ "VALUES ( ?, ?)");

		stmt.setString(1, oname);
		stmt.setString(2, value);

		stmt.executeUpdate();
	}

    public static String buildDbDir() {
    	// get dir for DB
    	String dbdir = "";
    	String dbtype = Prefs.getPref(PrefName.DBTYPE);
    	if (dbtype.equals("hsqldb")) {
    		String hdir = Prefs.getPref(PrefName.HSQLDBDIR);
    		if (hdir.equals("not-set"))
    			return hdir;
    		dbdir = "jdbc:hsqldb:file:" + Prefs.getPref(PrefName.HSQLDBDIR)
    				+ "/borg_";
    	} else if (dbtype.equals("jdbc")) {
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

}
