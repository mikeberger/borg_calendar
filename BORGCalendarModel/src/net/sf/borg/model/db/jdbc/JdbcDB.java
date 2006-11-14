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
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.model.BorgOption;
import net.sf.borg.model.db.BeanDB;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.KeyedBean;

/**
 *
 * @author  mberger
 */
// JdbcDB is a base class for JDBC based DBs for BORG
// it contains caching logic and a common connection
// for all db tables. Each subclass of JdbcDB manages
// a single DB table - mainly for historical reasons
// as each separate BORG file has always been called a "DB".
// JdbcDB will also manage a common options table int he DB
abstract public class JdbcDB implements BeanDB
{

    
    // common db connection shared by sub-classes. in BORG, all sub-classes
    // will manage a table in the same DB
    static protected Connection globalConnection_ = null;
    protected Connection connection_ = null;
    
    // BORG needs its own caching. BORG rebuilds the map of DB data often
    // and going to the DB is too expensive. If BORG is changed to support
    // multi-user access, then the cache algorithm will have to be smarter.
    // currently, it is assumed that a single BORG client is the only DB updater.
    // in the future, if there are multiple writers, the DB will have to contain
    // some info to indicate when another process has written the DB to force
    // a flush of the cache
    private boolean objectCacheOn_;  // is caching on?
    private HashMap objectCache_;  // the cache
    private String url_;
    
    protected String username_;
    
    // For multiuser operation, we need to reserve key values even before a
    // record is created in the database. Any value here overrides the current
    // maximum key in a particular table.
    protected int curMaxKey_ = Integer.MIN_VALUE;
    
    static public void startTransaction() throws Exception {
		globalConnection_.setAutoCommit(false);
	}

	static public void commitTransaction() throws Exception {
		PreparedStatement stmt = globalConnection_.prepareStatement("COMMIT");
		stmt.execute();
		globalConnection_.setAutoCommit(true);
	}

	static public void rollbackTransaction() throws Exception {
		PreparedStatement stmt = globalConnection_.prepareStatement("ROLLBACK");
		stmt.execute();
		globalConnection_.setAutoCommit(true);
	}
	
    /** Creates a new instance of JdbcDB */
    JdbcDB(String url, String username) throws Exception
    {
        username_ = username;
        
        objectCacheOn_ = true;
        objectCache_ = new HashMap();
        
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
				props.setProperty("shutdown", "true");
				props.setProperty("ifexists", "true");
				try{
					globalConnection_ = DriverManager.getConnection(url, props);
				}
				catch( SQLException se)
				{
					if( se.getSQLState().equals("08003"))
					{
						// need to create the db
						try {
							System.out.println("Creating Database");
							InputStream is = 
								getClass().getResourceAsStream("/resource/borg_hsqldb.sql");
							StringBuffer sb = new StringBuffer();
							InputStreamReader r = new InputStreamReader(is);
							while (true) {
								int ch = r.read();
								if (ch == -1)
									break;
								sb.append((char)ch);
							}
							props.setProperty("ifexists", "false");
							globalConnection_ = DriverManager.getConnection(url, props);
							execSQL(sb.toString());
						} catch (Exception e2) {
							throw e2;
							//Errmsg.errmsg(e2);
							//Errmsg.notice("Cannot create database...exiting");
							//System.exit(1);
						}
					}else if( se.getMessage().indexOf("locked") != -1)
					{
						throw se;
					}
					else
					{
						throw se;
					}
				}
			}

		}
 
        connection_ = globalConnection_;
    }
    
    JdbcDB(Connection conn) 
    {
        objectCacheOn_ = true;
        objectCache_ = new HashMap();
        
        connection_ = conn;
    }
    
    private void cleanup() {
		try {
			if (globalConnection_ != null && !globalConnection_.isClosed()
					&& url_.startsWith("jdbc:hsqldb")) {

				execSQL("SHUTDOWN");

			}
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}
    
    // turn on caching
    public void cacheOn(boolean b)
    {
        objectCacheOn_ = b;
    }
    
    public boolean isDirty() throws DBException
    {
    	// implement a way to check for external DB
    	// modification???
    	return false;
    }
    
    public void sync()
    {
        emptyCache();
    }
    
    protected static String toStr( Vector v )
    {
        String val = "";
        if( v == null ) return( "" );
        try
        {
            while(true)
            {
                String s = (String)v.remove(0);
                val += s;
                val += ",";
            }
        }
        catch( Exception e )
        {}
        return(val);
    }
    
    protected static int toInt( Integer in )
    {
        if( in == null )
            return(0);
        return( in.intValue() );
    }
    
    protected static int toInt( boolean in )
    {
        if( in == false )
            return(0 );
        return( 1 );
    }
    
    protected static Vector toVect( String s )
    {
        if( s == null || s.equals("") )
            return(null);
        
        StringTokenizer stk = new StringTokenizer(s,",");
        Vector vect = new Vector();
        while (stk.hasMoreTokens())
        {
            String stt = stk.nextToken();
            if( !stt.equals("") )
                vect.add(stt);
        }
        return(vect);
    }

	static public ResultSet execSQL(String sql) throws Exception {
		PreparedStatement stmt = globalConnection_.prepareStatement(sql);
		stmt.execute();
		return stmt.getResultSet();
	}
	
    protected void writeCache( KeyedBean bean )
    {
        // put a copy of the bean in the cache
        if( objectCacheOn_ )
        {
            objectCache_.put( new Integer(bean.getKey()), bean.copy() );
        }
    }
    
    protected void emptyCache()
    {
        if( objectCacheOn_ )
            objectCache_.clear();
    }
    
    protected void delCache( int key )
    {
        // remove the bean from the cache
        if( objectCacheOn_ )
        {
            objectCache_.remove( new Integer(key) );
        }
    }
    
    protected KeyedBean readCache( int key )
    {
        // need to remove cache here if DB has been updated
        // by any other process besides this one
        // TBD
        
        // if the bean is in the cache - return it
        if( objectCacheOn_ )
        {
            Object o = objectCache_.get( new Integer(key) );
            
            if( o != null )
            {
                KeyedBean r = (KeyedBean) o;
                return r.copy();
            }
        }
        
        return( null );
    }
    
    public void close() throws Exception
    {
    	cleanup();
        if( connection_ != null && connection_ != globalConnection_ )
            connection_.close();
        else if( globalConnection_ != null )
        	globalConnection_.close();
        globalConnection_ = null;
        connection_ = null;
    }
    
    public String getOption(String oname) throws Exception
    {
        String ret = null;
        PreparedStatement stmt = connection_.prepareStatement("SELECT value FROM options WHERE name = ? AND username = ?" );
        stmt.setString( 1, oname );
        stmt.setString( 2, username_ );
        ResultSet rs = stmt.executeQuery();
        if( rs.next() )
        {
            ret = rs.getString("value");
        }
        
        return(ret);
    }
    
    public Collection getOptions() throws Exception
    {
        ArrayList keys = new ArrayList();
        PreparedStatement stmt = connection_.prepareStatement("SELECT name, value FROM options WHERE username = ?" );
        stmt.setString( 1, username_ );
        ResultSet rs = stmt.executeQuery();
        while( rs.next() )
        {
            keys.add( new BorgOption(rs.getString("name"), rs.getString("value")) );
        }
        
        return( keys );
        
    }
   
    
    public void setOption(BorgOption option) throws Exception
    {
		String oname = option.getKey();
		String value = option.getValue();

        try{
            PreparedStatement stmt = connection_.prepareStatement( "DELETE FROM options WHERE name = ? AND username = ?" );
            stmt.setString( 1, oname );
            stmt.setString( 2, username_ );
            stmt.executeUpdate();
        }
        catch( Exception e )
        {}
        
        if( value == null || value.equals("") )
            return;
        
        PreparedStatement stmt = connection_.prepareStatement( "INSERT INTO options ( name, username, value ) " + 
                    "VALUES ( ?, ?, ?)");
        
        
        stmt.setString( 1, oname );
        stmt.setString( 2, username_ );
        stmt.setString( 3, value );
 
        stmt.executeUpdate();
    }
    
	public Collection readAll() throws DBException, Exception
	{
		PreparedStatement stmt = null;
		ResultSet r = null;
		try
		{
			stmt = getPSAll();
			r = stmt.executeQuery();
			List lst = new ArrayList();
			while ( r.next() )
			{
				KeyedBean bean = createFrom(r);
				lst.add(bean);
				writeCache(bean);
			}
			return lst;
		}
		finally
		{
			if (r!=null) r.close();
			if (stmt!=null) stmt.close();
		}
	}
    
	public KeyedBean readObj(int key) throws DBException, Exception
	{
		KeyedBean bean = readCache(key);

		if( bean != null )
			return bean;

		PreparedStatement stmt = null;
		ResultSet r = null;
		try
		{
			stmt = getPSOne(key);
			r = stmt.executeQuery();
			if( r.next() )
			{
				bean = createFrom(r);
				writeCache(bean);
			}
			return bean;
		}
		finally
		{
			if (r!=null) r.close();
			if (stmt!=null) stmt.close();
		}
	}
	
	
    
    // package //
    abstract PreparedStatement getPSOne(int key) throws SQLException;
    abstract PreparedStatement getPSAll() throws SQLException;
    abstract KeyedBean createFrom(ResultSet rs) throws SQLException;
}
