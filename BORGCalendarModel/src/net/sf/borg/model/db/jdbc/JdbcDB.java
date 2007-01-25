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

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.model.BorgOption;
import net.sf.borg.model.Transactional;


/**
 * 
 * @author mberger
 */
// JdbcDB is a base class for JDBC based DBs for BORG
abstract public class JdbcDB implements /*BeanDB,*/ Transactional {

    // common db connection shared by sub-classes. in BORG, all sub-classes
    // will manage a table in the same DB
    static protected Connection globalConnection_ = null;

    protected Connection connection_ = null;

    private String url_;

    protected String username_;

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
    JdbcDB(String url, String username) throws Exception {
	username_ = username;

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
		
		// only want to apply these properties to an embedded stand-alone db
		if( url.startsWith("jdbc:hsqldb:file"))
		{
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
			    InputStream is = getClass().getResourceAsStream(
				    "/resource/borg_hsqldb.sql");
			    StringBuffer sb = new StringBuffer();
			    InputStreamReader r = new InputStreamReader(is);
			    while (true) {
				int ch = r.read();
				if (ch == -1)
				    break;
				sb.append((char) ch);
			    }
			    props.setProperty("ifexists", "false");
			    globalConnection_ = DriverManager.getConnection(
				    url, props);
			    execSQL(sb.toString());
			} catch (Exception e2) {
			    throw e2;
			    // Errmsg.errmsg(e2);
			    // Errmsg.notice("Cannot create
                                // database...exiting");
			    // System.exit(1);
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
	    if (globalConnection_ != null && !globalConnection_.isClosed()
		    && url_.startsWith("jdbc:hsqldb:file")) {

		execSQL("SHUTDOWN");

	    }
	} catch (Exception e) {
	    Errmsg.errmsg(e);
	}
    }


    public boolean isDirty()  {
	// implement a way to check for external DB
	// modification???
	return false;
    }

    protected final static String toStr(Vector v) {
	String val = "";
	if (v == null)
	    return ("");
	try {
	    while (true) {
		String s = (String) v.remove(0);
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

    protected final static Vector toVect(String s) {
	if (s == null || s.equals(""))
	    return (null);

	StringTokenizer stk = new StringTokenizer(s, ",");
	Vector vect = new Vector();
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
	PreparedStatement stmt = connection_
		.prepareStatement("SELECT value FROM options WHERE name = ? AND username = ?");
	stmt.setString(1, oname);
	stmt.setString(2, username_);
	ResultSet rs = stmt.executeQuery();
	if (rs.next()) {
	    ret = rs.getString("value");
	}

	return (ret);
    }

    public final Collection getOptions() throws Exception {
	ArrayList keys = new ArrayList();
	PreparedStatement stmt = connection_
		.prepareStatement("SELECT name, value FROM options WHERE username = ?");
	stmt.setString(1, username_);
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    keys
		    .add(new BorgOption(rs.getString("name"), rs
			    .getString("value")));
	}

	return (keys);

    }

    public final void setOption(BorgOption option) throws Exception {
	String oname = option.getKey();
	String value = option.getValue();

	try {
	    PreparedStatement stmt = connection_
		    .prepareStatement("DELETE FROM options WHERE name = ? AND username = ?");
	    stmt.setString(1, oname);
	    stmt.setString(2, username_);
	    stmt.executeUpdate();
	} catch (Exception e) {
	}

	if (value == null || value.equals(""))
	    return;

	PreparedStatement stmt = connection_
		.prepareStatement("INSERT INTO options ( name, username, value ) "
			+ "VALUES ( ?, ?, ?)");

	stmt.setString(1, oname);
	stmt.setString(2, username_);
	stmt.setString(3, value);

	stmt.executeUpdate();
    }


}
