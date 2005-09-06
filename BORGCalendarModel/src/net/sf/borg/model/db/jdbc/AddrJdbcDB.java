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

package net.sf.borg.model.db.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import net.sf.borg.model.Address;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.KeyedBean;


/**
 *
 * this is the JDBC layer for access to the addresses table
 */
class AddrJdbcDB extends JdbcDB
{

             
    AddrJdbcDB(String url, String username) throws Exception
    {
        super( url, username );
    }
    
    AddrJdbcDB(Connection conn) 
    {
        super( conn );
    }
    
    public void addObj(KeyedBean bean, boolean crypt) throws DBException, Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "INSERT INTO addresses ( address_num, username, " +
        "first_name, last_name, nickname, email, screen_name, work_phone," + 
        "home_phone, fax, pager, street, city, state, zip, country, company," +
        "work_street, work_city, work_state, work_zip, work_country, webpage, notes, birthday, new, modified, deleted) " +
        " VALUES " +
        "( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        
        Address addr = (Address) bean;
        
        stmt.setInt( 1, addr.getKey() );
        stmt.setString( 2, username_ );

        stmt.setString( 3, addr.getFirstName() );
        stmt.setString( 4, addr.getLastName());
        stmt.setString( 5, addr.getNickname());
        stmt.setString( 6, addr.getEmail());
        stmt.setString( 7, addr.getScreenName() );
        stmt.setString( 8, addr.getWorkPhone() );
        stmt.setString( 9, addr.getHomePhone() );
        stmt.setString( 10, addr.getFax() );
        stmt.setString( 11, addr.getPager() );
        stmt.setString( 12, addr.getStreetAddress() );
        stmt.setString( 13, addr.getCity() );
        stmt.setString( 14, addr.getState() );
        stmt.setString( 15, addr.getZip() );
        stmt.setString( 16, addr.getCountry() );
        stmt.setString( 17, addr.getCompany() );
        stmt.setString( 18, addr.getWorkStreetAddress() );
        stmt.setString( 19, addr.getWorkCity() );
        stmt.setString( 20, addr.getWorkState() );
        stmt.setString( 21, addr.getWorkZip() );
        stmt.setString( 22, addr.getWorkCountry() );
        stmt.setString( 23, addr.getWebPage() );
        stmt.setString( 24, addr.getNotes() );
        java.util.Date bd = addr.getBirthday();        
        if( bd != null )
            stmt.setDate( 25, new java.sql.Date( bd.getTime()) );
        else
            stmt.setDate(25, null );
        stmt.setInt( 26, toInt( addr.getNew()));
        stmt.setInt( 27, toInt( addr.getModified()));
        stmt.setInt( 28, toInt( addr.getDeleted()));
        stmt.executeUpdate();
        
        writeCache( addr );

    }
    
    public void delete(int key) throws DBException, Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "DELETE FROM addresses WHERE address_num = ? AND username = ?" );
        stmt.setInt( 1, key );
        stmt.setString( 2, username_ );
        stmt.executeUpdate();
        
        delCache( key );
    }
    
    public Collection getKeys() throws Exception
    {
        ArrayList keys = new ArrayList();
        PreparedStatement stmt = connection_.prepareStatement("SELECT address_num FROM addresses WHERE username = ? ORDER BY last_name, first_name" );
        stmt.setString( 1, username_ );
        ResultSet rs = stmt.executeQuery();
        while( rs.next() )
        {
            keys.add( new Integer(rs.getInt("address_num")) );
        }
        
        return( keys );
        
    }
    
    public int nextkey() throws Exception
    {
     PreparedStatement stmt = connection_.prepareStatement("SELECT MAX(address_num) FROM addresses WHERE  username = ?" );
        stmt.setString( 1, username_ );
        ResultSet r = stmt.executeQuery();
        int maxKey = 0;
        if( r.next() )
            maxKey = r.getInt(1);
        curMaxKey_ = Math.max(curMaxKey_, maxKey);
        return ++curMaxKey_;
    }
    
    public KeyedBean newObj()
    {
        return( new Address() );
    }
    
	PreparedStatement getPSOne(int key) throws SQLException
	{
		PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM addresses WHERE address_num = ? AND username = ?" );
		stmt.setInt( 1, key );
		stmt.setString( 2, username_ );
		return stmt;
	}
	
	PreparedStatement getPSAll() throws SQLException
	{
		PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM addresses WHERE username = ?" );
		stmt.setString( 1, username_ );
		return stmt;
	}
	
	KeyedBean createFrom(ResultSet r) throws SQLException
	{
		Address addr = new Address();
		addr.setKey(r.getInt("address_num"));
		addr.setFirstName( r.getString("first_name"));
		addr.setLastName( r.getString("last_name"));
		addr.setNickname( r.getString("nickname"));
		addr.setEmail( r.getString("email"));
		addr.setScreenName( r.getString("screen_name"));
		addr.setWorkPhone( r.getString("work_phone"));
		addr.setHomePhone( r.getString("home_phone"));
		addr.setFax( r.getString("fax"));
		addr.setPager( r.getString("pager"));
		addr.setStreetAddress( r.getString("street"));
		addr.setCity( r.getString("city"));
		addr.setState( r.getString("state"));
		addr.setZip( r.getString("zip"));
		addr.setCountry( r.getString("country"));
		addr.setCompany( r.getString("company"));
		addr.setWorkStreetAddress( r.getString("work_street"));
		addr.setWorkCity( r.getString("work_city"));
		addr.setWorkState( r.getString("work_state"));
		addr.setWorkZip( r.getString("work_zip"));
		addr.setWorkCountry( r.getString("work_country"));
		addr.setWebPage( r.getString("webpage"));
		addr.setNotes( r.getString("notes"));
		addr.setNew( r.getInt("new" ) != 0 );
		addr.setModified( r.getInt("modified" ) != 0 );
		addr.setDeleted( r.getInt("deleted" ) != 0 );
		if( r.getDate("birthday") != null )
			addr.setBirthday( new java.util.Date( r.getDate("birthday").getTime()));
		return addr;
	}
	
    public void updateObj(KeyedBean bean, boolean crypt) throws DBException, Exception
    {
   
        PreparedStatement stmt = connection_.prepareStatement( "UPDATE addresses SET " +
        "first_name = ?, last_name = ?, nickname = ?, email = ?, screen_name = ?, work_phone = ?," + 
        "home_phone = ?, fax = ?, pager = ?, street = ?, city = ?, state = ?, zip = ?, country = ?, company = ?," +
        "work_street = ?, work_city = ?, work_state = ?, work_zip = ?, work_country = ?, webpage = ?, notes = ?, birthday = ?, new = ?, modified = ?, deleted = ? " +
        " WHERE address_num = ? AND username = ?" );
        
        Address addr = (Address) bean;
        
 
        stmt.setString( 1, addr.getFirstName() );
        stmt.setString( 2, addr.getLastName());
        stmt.setString( 3, addr.getNickname());
        stmt.setString( 4, addr.getEmail());
        stmt.setString( 5, addr.getScreenName() );
        stmt.setString( 6, addr.getWorkPhone() );
        stmt.setString( 7, addr.getHomePhone() );
        stmt.setString( 8, addr.getFax() );
        stmt.setString( 9, addr.getPager() );
        stmt.setString( 10, addr.getStreetAddress() );
        stmt.setString( 11, addr.getCity() );
        stmt.setString( 12, addr.getState() );
        stmt.setString( 13, addr.getZip() );
        stmt.setString( 14, addr.getCountry() );
        stmt.setString( 15, addr.getCompany() );
        stmt.setString( 16, addr.getWorkStreetAddress() );
        stmt.setString( 17, addr.getWorkCity() );
        stmt.setString( 18, addr.getWorkState() );
        stmt.setString( 19, addr.getWorkZip() );
        stmt.setString( 20, addr.getWorkCountry() );
        stmt.setString( 21, addr.getWebPage() );
        stmt.setString( 22, addr.getNotes() );
        java.util.Date bd = addr.getBirthday();        
        if( bd != null )
            stmt.setDate( 23, new java.sql.Date( bd.getTime()) );
        else
            stmt.setDate(23, null );
        stmt.setInt( 24, toInt( addr.getNew()));
        stmt.setInt( 25, toInt( addr.getModified()));
        stmt.setInt( 26, toInt( addr.getDeleted()));
        
        
        stmt.setInt( 27, addr.getKey() );
        stmt.setString( 28, username_ );

        stmt.executeUpdate();
               
        delCache( addr.getKey() );
        writeCache( addr );
    }
    
}
