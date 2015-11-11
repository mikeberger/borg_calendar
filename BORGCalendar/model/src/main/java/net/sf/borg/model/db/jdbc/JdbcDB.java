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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.entity.Option;

/**
 * class providing basic common JDBC services 
 */
final class JdbcDB {

	static private final Logger log = Logger.getLogger("net.sf.borg");

	// common db connection shared by sub-classes. in BORG, all sub-classes
	// will manage a table in the same DB
	static protected Connection connection_ = null;

	// JDBC URL
	static private String url_;

	/**
	 * begin a JDBC transaction on the shared connection
	 * 
	 * @throws Exception
	 */
	static synchronized public void beginTransaction() throws Exception {
		connection_.setAutoCommit(false);
	}

	/**
	 * commit a JDBC transaction on the shared connection
	 * 
	 * @throws Exception
	 */
	static synchronized public final void commitTransaction() throws Exception {
		PreparedStatement stmt = connection_.prepareStatement("COMMIT");
		stmt.execute();
		stmt.close();
		connection_.setAutoCommit(true);
	}

	/**
	 * rollback a JDBC transaction on the shared connection
	 * 
	 * @throws Exception
	 */
	static synchronized public final void rollbackTransaction() throws Exception {
		PreparedStatement stmt = connection_.prepareStatement("ROLLBACK");
		stmt.execute();
		stmt.close();
		connection_.setAutoCommit(true);
	}
	
	/**
	 * close and reopen the db. For H2 - this will force a flush to disk which otherwise doesn't happen while the db is open
	 * @throws Exception
	 */
	static synchronized public void reopen() throws Exception {
		close();
		connect(url_);
		
	}

	/**
	 * Connect to the database. The logic varies based on the URL. Supports
	 * MYSQL, HSQL For HSQL - if the DB doesn't exist, it will be created
	 * 
	 * @param urlIn
	 *            the JDBC url
	 * 
	 * @throws Exception
	 *             the exception
	 */
	static synchronized public void connect(String urlIn) throws Exception {
		String url = urlIn;
		if (url == null)
			url = buildDbDir();
		url_ = url;
		if (url.startsWith("jdbc:mysql")) {
			Class.forName("com.mysql.jdbc.Driver");
			if (connection_ == null) {
				connection_ = DriverManager.getConnection(url);
			}

		} else if (url.startsWith("jdbc:hsqldb")) {
			Class.forName("org.hsqldb.jdbcDriver");
			if (connection_ == null) {
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
					connection_ = DriverManager.getConnection(url, props);
				} catch (SQLException se) {
					if (se.getSQLState().equals("08003")) {
						// need to create the db
						try {
							log.info("Creating Database");
							InputStream is = JdbcDB.class
									.getResourceAsStream("/borg_hsqldb.sql");
							StringBuffer sb = new StringBuffer();
							InputStreamReader r = new InputStreamReader(is);
							while (true) {
								int ch = r.read();
								if (ch == -1)
									break;
								sb.append((char) ch);
							}
							r.close();
							props.setProperty("ifexists", "false");
							connection_ = DriverManager.getConnection(url,
									props);
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
				// if running with a memory only db (for testing)
				// always need to run the db creation scripts
				if (url.startsWith("jdbc:hsqldb:mem")) {
					// need to create the db
					try {
						log.info("Creating Database");
						InputStream is = JdbcDB.class
								.getResourceAsStream("/borg_hsqldb.sql");
						StringBuffer sb = new StringBuffer();
						InputStreamReader r = new InputStreamReader(is);
						while (true) {
							int ch = r.read();
							if (ch == -1)
								break;
							sb.append((char) ch);
						}
						r.close();
						props.setProperty("ifexists", "false");
						connection_ = DriverManager.getConnection(url, props);
						execSQL(sb.toString());
					} catch (Exception e2) {
						throw e2;
					}
				}
			}
		} else if (url.startsWith("jdbc:h2")) {
			if (connection_ == null) {
				Properties props = new Properties();
				props.setProperty("user", "sa");
				props.setProperty("password", "");
				props.setProperty("shutdown", "true");
				props.setProperty("ifexists", "true");

				try {
					connection_ = DriverManager.getConnection(url, props);
				} catch (SQLException se) {
					if (se.getSQLState().equals("90013")) {
						// need to create the db
						try {
							log.info("Creating Database");
							InputStream is = JdbcDB.class
									.getResourceAsStream("/borg_hsqldb.sql");
							StringBuffer sb = new StringBuffer();
							InputStreamReader r = new InputStreamReader(is);
							while (true) {
								int ch = r.read();
								if (ch == -1)
									break;
								sb.append((char) ch);
							}
							r.close();
							props.setProperty("ifexists", "false");
							connection_ = DriverManager.getConnection(url,
									props);
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

		} else {
			if (connection_ == null) {
				connection_ = DriverManager.getConnection(url);
			}
		}
		
		JdbcDB.doDBUpgrades();

	}

	/**
	 * perform a clean HSQL shutdown if we are using HSQL
	 */
	static synchronized private final void cleanup() {
		try {
			if (connection_ != null && !connection_.isClosed()
					&& url_.startsWith("jdbc:hsqldb:file")) {

				execSQL("SHUTDOWN");

			}
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}

	//
	// various data conversion methods are below to provide a standard
	// db representation of String Lists, Integer, and boolean
	//
	public final static String toStr(Vector<String> v) {
		StringBuffer val = new StringBuffer();
		if (v == null)
			return ("");
		try {
			while (true) {
				String s = v.remove(0);
				val.append(s);
				val.append(",");
				
			}
		} catch (Exception e) {
			// empty
		}
		return (val.toString());
	}

	public final static int toInt(Integer in) {
		if (in == null)
			return (0);
		return (in.intValue());
	}

	public final static int toInt(boolean in) {
		if (in == false)
			return (0);
		return (1);
	}

	public final static Vector<String> toVect(String s) {
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

	/**
	 * Execute arbitrary SQL against the open JDBC connection
	 * 
	 * @param sql
	 *            the sql
	 * 
	 * @return the result set
	 * 
	 * @throws Exception
	 *             the exception
	 */
	static synchronized final public void execSQL(String sql) throws Exception {
		PreparedStatement stmt = connection_.prepareStatement(sql);
		stmt.execute();
		stmt.close();
	}

	static synchronized final public ResultSet execQuery(String sql) throws Exception {
		PreparedStatement stmt = connection_.prepareStatement(sql);
		stmt.execute();
		return stmt.getResultSet();
	}
	
	/**
	 * Gets the connection.
	 * 
	 * @return the connection
	 */
	static synchronized public Connection getConnection() {
		return connection_;
	}

	/**
	 * Close the open connection and shutdown the db (if HSQL)
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public synchronized static void close() throws Exception {
		writeTimestamp();
		cleanup();
		if (connection_ != null)
			connection_.close();
		connection_ = null;
	}

	/**
	 * write a timestamp to the DB and prefs to detect shutdown problem on next
	 * start
	 * 
	 * @throws Exception
	 */
	private static void writeTimestamp() throws Exception {
		Date now = new Date();
		Prefs.putPref(PrefName.SHUTDOWNTIME, Long.toString(now.getTime()));
		Option option = new Option(PrefName.SHUTDOWNTIME.getName(),
				Long.toString(now.getTime()));
		new OptionJdbcDB().setOption(option);
	}

	/**
	 * Builds the db url from the user's settings. Supports HSQL, MYSQL, generic
	 * JDBC
	 * 
	 * @return the jdbc url
	 */
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
		} else if (dbtype.equals("h2")) {
			String hdir = Prefs.getPref(PrefName.H2DIR);
			if (hdir.equals("not-set"))
				return hdir;
			dbdir = "jdbc:h2:file:" + Prefs.getPref(PrefName.H2DIR)
					+ "/borgdb;USER=sa";
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

		log.info("DB URL is: " + dbdir);
		return (dbdir);
	}

	/**
	 * do any db upgrades that are too complex for JdbcDBUpgrader or not portable via SQL
	 */
	private static void doDBUpgrades() throws Exception {
		
		// increase category size
		alterVarcharColumnSize("appointments", "category", 255);
		alterVarcharColumnSize("tasks", "category", 255);
		alterVarcharColumnSize("projects", "category", 255);
	}
	
	static private void alterVarcharColumnSize(String table, String column, int size) throws Exception
	{
		Statement st = null;
		ResultSet rs = null;
		try {
			st = connection_.createStatement();
			rs = st.executeQuery("select " + column + " from " + table);
			ResultSetMetaData md = rs.getMetaData();
			if( md.getColumnDisplaySize(1) != size) {
				new JdbcDBUpgrader(null, "alter table " + table + " alter " + column + " varchar(" + size + ")").upgrade();
			}
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
	}
}
