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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import net.sf.borg.model.db.EntityDB;
import net.sf.borg.model.entity.Address;


/**
 * this is the JDBC layer for access to the addresses table.
 */
public class AddrJdbcDB extends JdbcBeanDB<Address> implements EntityDB<Address>
{         
    
    /* (non-Javadoc)
     * @see net.sf.borg.model.db.EntityDB#addObj(net.sf.borg.model.entity.KeyedEntity)
     */
    @Override
    public void addObj(Address addr) throws Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "INSERT INTO addresses ( address_num, " +
        "first_name, last_name, nickname, email, screen_name, work_phone," + 
        "home_phone, fax, pager, street, city, state, zip, country, company," +
        "work_street, work_city, work_state, work_zip, work_country, webpage, notes, birthday) " +
        " VALUES " +
        "( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        
        
        stmt.setInt( 1, addr.getKey() );
        stmt.setString( 2, addr.getFirstName() );
        stmt.setString( 3, addr.getLastName());
        stmt.setString( 4, addr.getNickname());
        stmt.setString( 5, addr.getEmail());
        stmt.setString( 6, addr.getScreenName() );
        stmt.setString( 7, addr.getWorkPhone() );
        stmt.setString( 8, addr.getHomePhone() );
        stmt.setString( 9, addr.getFax() );
        stmt.setString( 10, addr.getPager() );
        stmt.setString( 11, addr.getStreetAddress() );
        stmt.setString( 12, addr.getCity() );
        stmt.setString( 13, addr.getState() );
        stmt.setString( 14, addr.getZip() );
        stmt.setString( 15, addr.getCountry() );
        stmt.setString( 16, addr.getCompany() );
        stmt.setString( 17, addr.getWorkStreetAddress() );
        stmt.setString( 18, addr.getWorkCity() );
        stmt.setString( 19, addr.getWorkState() );
        stmt.setString( 20, addr.getWorkZip() );
        stmt.setString( 21, addr.getWorkCountry() );
        stmt.setString( 22, addr.getWebPage() );
        stmt.setString( 23, addr.getNotes() );
        java.util.Date bd = addr.getBirthday();        
        if( bd != null )
            stmt.setDate( 24, new java.sql.Date( bd.getTime()) );
        else
            stmt.setDate(24, null );
        stmt.executeUpdate();
        stmt.close();

        writeCache( addr );

    }
    
    /* (non-Javadoc)
     * @see net.sf.borg.model.db.EntityDB#delete(int)
     */
    @Override
    public void delete(int key) throws Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "DELETE FROM addresses WHERE address_num = ?" );
        stmt.setInt( 1, key );
        stmt.executeUpdate();
        stmt.close();

        delCache( key );
    }
    
    /**
     * Gets the keys.
     * 
     * @return the keys
     * 
     * @throws Exception the exception
     */
    public Collection<Integer> getKeys() throws Exception
    {
        ArrayList<Integer> keys = new ArrayList<Integer>();
        PreparedStatement stmt = connection_.prepareStatement("SELECT address_num FROM addresses ORDER BY last_name, first_name" );
        ResultSet rs = stmt.executeQuery();
        while( rs.next() )
        {
            keys.add( new Integer(rs.getInt("address_num")) );
        }
        rs.close();
        
        stmt.close();

        return( keys );
        
    }
    
    /* (non-Javadoc)
     * @see net.sf.borg.model.db.EntityDB#nextkey()
     */
    @Override
    public int nextkey() throws Exception
    {
     PreparedStatement stmt = connection_.prepareStatement("SELECT MAX(address_num) FROM addresses" );
        ResultSet r = stmt.executeQuery();
        int maxKey = 0;
        if( r.next() )
            maxKey = r.getInt(1);
        r.close();
        stmt.close();

        return ++maxKey;
    }
    
    /* (non-Javadoc)
     * @see net.sf.borg.model.db.EntityDB#newObj()
     */
    @Override
    public Address newObj()
    {
        return( new Address() );
    }
    
	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.jdbc.JdbcBeanDB#getPSOne(int)
	 */
	@Override
	PreparedStatement getPSOne(int key) throws SQLException
	{
		PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM addresses WHERE address_num = ?" );
		stmt.setInt( 1, key );
		return stmt;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.jdbc.JdbcBeanDB#getPSAll()
	 */
	@Override
	PreparedStatement getPSAll() throws SQLException
	{
		PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM addresses" );
		return stmt;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.jdbc.JdbcBeanDB#createFrom(java.sql.ResultSet)
	 */
	@Override
	Address createFrom(ResultSet r) throws SQLException
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
		if( r.getDate("birthday") != null )
			addr.setBirthday( new java.util.Date( r.getDate("birthday").getTime()));
		return addr;
	}
	
    /* (non-Javadoc)
     * @see net.sf.borg.model.db.EntityDB#updateObj(net.sf.borg.model.entity.KeyedEntity)
     */
    @Override
    public void updateObj(Address addr) throws Exception
    {
   
        PreparedStatement stmt = connection_.prepareStatement( "UPDATE addresses SET " +
        "first_name = ?, last_name = ?, nickname = ?, email = ?, screen_name = ?, work_phone = ?," + 
        "home_phone = ?, fax = ?, pager = ?, street = ?, city = ?, state = ?, zip = ?, country = ?, company = ?," +
        "work_street = ?, work_city = ?, work_state = ?, work_zip = ?, work_country = ?, webpage = ?, notes = ?, birthday = ? " +
        " WHERE address_num = ?" );
        
 
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
        
        stmt.setInt( 24, addr.getKey() );

        stmt.executeUpdate();
        stmt.close();

        delCache( addr.getKey() );
        writeCache( addr );
    }
    
}
