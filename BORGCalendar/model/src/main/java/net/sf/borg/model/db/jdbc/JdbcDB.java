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

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    static private String getDriverType() {
        if (url_ != null && url_.startsWith("jdbc:"))
            return url_.substring(5, url_.indexOf(':', 6));
        return null;
    }

    /**
     * begin a JDBC transaction on the shared connection
     *
     * @throws Exception
     */
    static synchronized public void beginTransaction() throws Exception {
        if ("sqlite".equals(getDriverType())) return;
        connection_.setAutoCommit(false);
    }

    /**
     * commit a JDBC transaction on the shared connection
     *
     * @throws Exception
     */
    static synchronized public final void commitTransaction() throws Exception {
        if ("sqlite".equals(getDriverType())) return;
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
        if ("sqlite".equals(getDriverType())) return;
        PreparedStatement stmt = connection_.prepareStatement("ROLLBACK");
        stmt.execute();
        stmt.close();
        connection_.setAutoCommit(true);
    }


    /**
     * Connect to the database. The logic varies based on the URL.
     *
     * @param urlIn the JDBC url
     * @throws Exception the exception
     */
    static synchronized public void connect(String urlIn) throws Exception {
        String url = urlIn;
        if (url == null)
            url = buildDbDir();
        url_ = url;
       if (url.startsWith("jdbc:h2")) {
            if (connection_ == null) {
                Properties props = new Properties();
                props.setProperty("user", "sa");
                props.setProperty("password", "");
                props.setProperty("shutdown", "true");
                props.setProperty("ifexists", "true");

                try {
                    connection_ = DriverManager.getConnection(url, props);
                } catch (SQLException se) {
                    if (se.getSQLState().equals("90146")) {
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
        } else if (url.startsWith("jdbc:sqlite")) {
            if (connection_ == null) {

                try {
                    Properties props = new Properties();
                    props.setProperty("allowMultiQueries", "true");
                    connection_ = DriverManager.getConnection(url, props);
                    
                    // turn on foreign key validation
                    String s = "PRAGMA foreign_keys = true";
                    log.fine("SQL: " + s);
                    execSQL(s);
                    
                    InputStream is = JdbcDB.class
                            .getResourceAsStream("/borg_sqlite.sql");
                    StringBuffer sb = new StringBuffer();
                    InputStreamReader r = new InputStreamReader(is);
                    boolean more = true;
                    while (more) {
                        while (true) {
                            int ch = r.read();
                            if (ch == ';') {
                                sb.append(";\n");
                                log.fine("SQL: " + sb);
                                execSQL(sb.toString());
                                sb.setLength(0);
                                continue;
                            }
                            if (ch == -1) {
                                more = false;
                                break;
                            }
                            sb.append((char) ch);
                        }
                    }
                    r.close();
                } catch (SQLException se) {
                    log.severe(se.getMessage());
                    se.printStackTrace();
                    throw se;
                }
            }

        } else {
            if (connection_ == null) {
                connection_ = DriverManager.getConnection(url);
            }
        }


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
     * @param sql the sql
     * @return the result set
     * @throws Exception the exception
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
     * @throws Exception the exception
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
     * Builds the db url from the user's settings. Supports HSQL, SQLITE, generic
     * JDBC
     *
     * @return the jdbc url
     */
    public static String buildDbDir() {
        // get dir for DB
        String dbdir = "";
        String dbtype = Prefs.getPref(PrefName.DBTYPE);
       if (dbtype.equals("h2")) {
            String hdir = Prefs.getPref(PrefName.H2DIR);
            if (hdir.equals("not-set"))
                return hdir;
            dbdir = "jdbc:h2:file:" + Prefs.getPref(PrefName.H2DIR)
                    + "/borgdb;USER=sa;NON_KEYWORDS=value";
        } else if (dbtype.equals("sqlite")) {
            String hdir = Prefs.getPref(PrefName.SQLITEDIR);
            if (hdir.equals("not-set"))
                return hdir;
            
            // make sure dir exists
            File borgdir = new File(hdir);
            if (!borgdir.exists()) {
				borgdir.mkdir();
			}
            dbdir = "jdbc:sqlite:" + Prefs.getPref(PrefName.SQLITEDIR)
                    + "/borg_sqlite.db";
        } else if (dbtype.equals("jdbc")) {
            dbdir = Prefs.getPref(PrefName.JDBCURL);
        } 
        
        log.info("DB URL is: " + dbdir);
        return (dbdir);
    }


}
