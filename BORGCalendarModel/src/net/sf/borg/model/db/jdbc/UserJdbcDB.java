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
 
Copyright 2003 by ==Quiet==
 */

package net.sf.borg.model.db.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import net.sf.borg.common.util.Version;
import net.sf.borg.model.User;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.KeyedBean;


/**
 *
 * this is the JDBC layer for access to the addresses table
 */
class UserJdbcDB extends JdbcDB
{
    static
    {
        Version.addVersion("$Id$");
    }
             
    UserJdbcDB(String url) throws Exception
    {
        super( url, 0 );
    }
    
    UserJdbcDB(Connection conn) 
    {
        super( conn );
    }
    
    public void addObj(KeyedBean bean, boolean crypt) throws DBException, Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "INSERT INTO users ( username, userid, password) " +
        " VALUES " +
        "( ?, ?, ?)");
        
        User u = (User) bean;
        
        stmt.setString( 1, u.getUserName() );
        stmt.setInt( 2, u.getUserId().intValue() );
        stmt.setString( 3, u.getPassword() );
        
        stmt.executeUpdate();
        
        writeCache( u );

    }
    
    public void delete(int key) throws DBException, Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "DELETE FROM users WHERE userid = ?" );
        stmt.setInt( 1, key );
        stmt.executeUpdate();        
        delCache( key );
    }
    
    public Collection getKeys() throws Exception
    {
        ArrayList keys = new ArrayList();
        PreparedStatement stmt = connection_.prepareStatement("SELECT userid FROM users" );
        ResultSet rs = stmt.executeQuery();
        while( rs.next() )
        {
            keys.add( new Integer(rs.getInt("userid")) );
        }
        
        return( keys );
        
    }
    
    public int maxkey() throws Exception
    {
        PreparedStatement stmt = connection_.prepareStatement("SELECT MAX(userid) FROM users" );
        ResultSet r = stmt.executeQuery();
        if( r.next() )
            return( r.getInt(1) );
        return(0);
    }
    
    public KeyedBean newObj()
    {
        return( new User() );
    }
    
	PreparedStatement getPSOne(int key) throws SQLException
	{
		PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM users WHERE userid = ?" );
		stmt.setInt( 1, key );
		return stmt;
	}
	
	PreparedStatement getPSAll() throws SQLException
	{
		PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM users" );
		return stmt;
	}
	
	KeyedBean createFrom(ResultSet r) throws SQLException
	{
		User u = new User();
		u.setKey(r.getInt("userid"));
		u.setUserName( r.getString("username"));
		u.setUserId( new Integer( r.getInt("userid")));
		u.setPassword( r.getString("password"));
		return u;
	}
	
    public void updateObj(KeyedBean bean, boolean crypt) throws DBException, Exception
    {
   
        PreparedStatement stmt = connection_.prepareStatement( "UPDATE users SET " +
        "username = ?, password = ? WHERE userid = ?" );
        
        User u = (User) bean;
        
        stmt.setString( 1, u.getUserName() );
        stmt.setString( 2, u.getPassword());
        stmt.setInt( 3, u.getUserId().intValue());

        stmt.executeUpdate();
              
        delCache( u.getKey() );
        writeCache( u );
    }
    
}
