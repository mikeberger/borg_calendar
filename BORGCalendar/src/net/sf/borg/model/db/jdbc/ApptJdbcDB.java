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
 * AppJdbcDB.java
 *
 * Created on February 1, 2004, 3:59 PM
 */

package net.sf.borg.model.db.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.model.db.AppointmentDB;
import net.sf.borg.model.entity.Appointment;


/**
 * this is the JDBC layer for access to the appointment table.
 */
public class ApptJdbcDB extends JdbcBeanDB<Appointment> implements AppointmentDB
{

       
    /**
     * Creates a new instance of AppJdbcDB.
     */
    public ApptJdbcDB() 
    {
    	try {
			JdbcDB.execSQL("select username from appointments");
			Errmsg.notice(Resource.getResourceString("db_username_check"));
		} catch (Exception e) {
			// empty
		}
		
		new JdbcDBUpgrader("select encrypted from appointments",
		"alter table appointments add column encrypted char(1) default null").upgrade();
		new JdbcDBUpgrader("select repeat_until from appointments",
		"alter table appointments add column repeat_until date default null").upgrade();
		new JdbcDBUpgrader("select priority from appointments",
		"alter table appointments add column priority integer default '5' NOT NULL").upgrade();
		
    }
    
    /* (non-Javadoc)
     * @see net.sf.borg.model.db.EntityDB#addObj(net.sf.borg.model.entity.KeyedEntity)
     */
    @Override
    public void addObj(Appointment appt) throws Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "INSERT INTO appointments (appt_date, appt_num, duration, text, skip_list," +
        " next_todo, vacation, holiday, private, times, frequency, todo, color, rpt, category, reminders, untimed, encrypted, repeat_until, priority ) VALUES " +
        "( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        
        
        stmt.setTimestamp( 1, new java.sql.Timestamp( appt.getDate().getTime()), Calendar.getInstance() );
        stmt.setInt( 2, appt.getKey() );
        
        stmt.setInt( 3, toInt( appt.getDuration() ) );
        stmt.setString( 4, appt.getText() );
        stmt.setString( 5, toStr( appt.getSkipList() ) );
        java.util.Date nt = appt.getNextTodo();
        if( nt != null )
            stmt.setDate( 6, new java.sql.Date( appt.getNextTodo().getTime()) );
        else
            stmt.setDate(6, null );
        stmt.setInt( 7, toInt( appt.getVacation() ));
        stmt.setInt( 8, toInt( appt.getHoliday() ));
        stmt.setInt( 9, toInt( appt.getPrivate()) );
        stmt.setInt( 10, toInt( appt.getTimes() ));
        stmt.setString( 11, appt.getFrequency());
        stmt.setInt( 12, toInt( appt.getTodo()) );
        stmt.setString( 13, appt.getColor());
        stmt.setInt( 14, toInt( appt.getRepeatFlag()) );
        stmt.setString( 15, appt.getCategory());
      
        stmt.setString( 16, appt.getReminderTimes());
        stmt.setString( 17, appt.getUntimed());
        if( appt.isEncrypted())
			stmt.setString(18, "Y");
		else
			stmt.setString(18, "N");
        java.util.Date until = appt.getRepeatUntil();
        if( until != null )
            stmt.setDate( 19, new java.sql.Date( until.getTime()) );
        else
            stmt.setDate(19, null );
        if( appt.getPriority() != null )
        	stmt.setInt(20, appt.getPriority().intValue());
        else
        	stmt.setInt(20, 5);
        stmt.executeUpdate();
        
        writeCache( appt );
        
        stmt.close();
    }
    
    /* (non-Javadoc)
     * @see net.sf.borg.model.db.EntityDB#delete(int)
     */
    @Override
    public void delete(int key) throws Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "DELETE FROM appointments WHERE appt_num = ?" );
        stmt.setInt( 1, key );
        stmt.executeUpdate();
        
        delCache( key );
        
        stmt.close();

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
        PreparedStatement stmt = connection_.prepareStatement("SELECT appt_num FROM appointments" );
        ResultSet rs = stmt.executeQuery();
        while( rs.next() )
        {
            keys.add( new Integer(rs.getInt("appt_num")) );
        }
        
        return( keys );
        
    }
    
    /* (non-Javadoc)
     * @see net.sf.borg.model.db.AppointmentDB#getTodoKeys()
     */
    @Override
    public Collection<Integer> getTodoKeys() throws Exception
    {
        ArrayList<Integer> keys = new ArrayList<Integer>();
        PreparedStatement stmt = connection_.prepareStatement("SELECT appt_num FROM appointments WHERE todo = '1'" );
        ResultSet rs = stmt.executeQuery();
        while( rs.next() )
        {
            keys.add( new Integer(rs.getInt("appt_num")) );
        }
        rs.close();
        stmt.close();

        return( keys );
        
        
        
    }
    
    /* (non-Javadoc)
     * @see net.sf.borg.model.db.AppointmentDB#getRepeatKeys()
     */
    @Override
    public Collection<Integer> getRepeatKeys() throws Exception
    {
        ArrayList<Integer> keys = new ArrayList<Integer>();
        PreparedStatement stmt = connection_.prepareStatement("SELECT appt_num FROM appointments WHERE rpt = '1'" );
        ResultSet rs = stmt.executeQuery();
        while( rs.next() )
        {
            keys.add( new Integer(rs.getInt("appt_num")) );
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
       PreparedStatement stmt = connection_.prepareStatement("SELECT MAX(appt_num) FROM appointments");
       ResultSet r = stmt.executeQuery();
       int maxKey = 0;
       if (r.next())
           maxKey = r.getInt(1);
       r.close();
       stmt.close();

       return ++maxKey;
    }
    
    /* (non-Javadoc)
     * @see net.sf.borg.model.db.EntityDB#newObj()
     */
    @Override
    public Appointment newObj()
    {
        return( new Appointment() );
    }
    
	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.jdbc.JdbcBeanDB#getPSOne(int)
	 */
	@Override
	PreparedStatement getPSOne(int key) throws SQLException
	{
		PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM appointments WHERE appt_num = ?" );
		stmt.setInt( 1, key );
		return stmt;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.jdbc.JdbcBeanDB#getPSAll()
	 */
	@Override
	PreparedStatement getPSAll() throws SQLException
	{
		PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM appointments" );
		return stmt;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.borg.model.db.jdbc.JdbcBeanDB#createFrom(java.sql.ResultSet)
	 */
	@Override
	Appointment createFrom(ResultSet r) throws SQLException
	{
		Appointment appt = new Appointment();
		appt.setKey(r.getInt("appt_num"));
		if( r.getTimestamp("appt_date") != null)
			appt.setDate( new java.util.Date (r.getTimestamp("appt_date").getTime()));
		appt.setDuration( new Integer(r.getInt("duration")));
		appt.setText( r.getString("text"));
		appt.setSkipList( toVect(r.getString("skip_list")));
		if( r.getDate("next_todo") != null )
			appt.setNextTodo( new java.util.Date(r.getDate("next_todo").getTime()));
		appt.setVacation( new Integer(r.getInt("vacation")));
		appt.setHoliday( new Integer(r.getInt("holiday")));
		appt.setPrivate( r.getInt("private") != 0 );
		appt.setTimes( new Integer(r.getInt("times")));
		appt.setFrequency( r.getString("frequency"));
		appt.setTodo( r.getInt("todo") != 0 );
		appt.setColor( r.getString("color"));
		appt.setRepeatFlag( r.getInt("rpt" ) != 0 );
		appt.setCategory( r.getString("category"));
		appt.setReminderTimes( r.getString("reminders"));
		appt.setUntimed( r.getString("untimed"));
		String enc = r.getString("encrypted");
		if( enc != null && enc.equals("Y"))
			appt.setEncrypted(true);
		if( r.getDate("repeat_until") != null )
			appt.setRepeatUntil( new java.util.Date(r.getDate("repeat_until").getTime()));
		appt.setPriority(new Integer(r.getInt("priority")));
		
		return appt;
	}
	
    /* (non-Javadoc)
     * @see net.sf.borg.model.db.EntityDB#updateObj(net.sf.borg.model.entity.KeyedEntity)
     */
    @Override
    public void updateObj(Appointment appt) throws Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "UPDATE appointments SET  appt_date = ?, " +
        "duration = ?, text = ?, skip_list = ?," +
        " next_todo = ?, vacation = ?, holiday = ?, private = ?, times = ?, frequency = ?, todo = ?, color = ?, rpt = ?, category = ?," +
		" reminders = ?, untimed = ?, encrypted = ?, repeat_until = ?, priority = ?" +
        " WHERE appt_num = ?");
       
        
        stmt.setTimestamp( 1, new java.sql.Timestamp( appt.getDate().getTime()), Calendar.getInstance() );
        
        stmt.setInt( 2, toInt( appt.getDuration() ) );
        stmt.setString( 3, appt.getText() );
        stmt.setString( 4, toStr( appt.getSkipList() ));
        java.util.Date nt = appt.getNextTodo();
        if( nt != null )
            stmt.setDate( 5, new java.sql.Date( appt.getNextTodo().getTime()) );
        else
            stmt.setDate( 5, null );
        stmt.setInt( 6, toInt( appt.getVacation() ));
        stmt.setInt( 7, toInt( appt.getHoliday() ));
        stmt.setInt( 8, toInt( appt.getPrivate()) );
        stmt.setInt( 9, toInt( appt.getTimes() ));
        stmt.setString( 10, appt.getFrequency());
        stmt.setInt( 11, toInt( appt.getTodo()) );
        stmt.setString( 12, appt.getColor());
        stmt.setInt( 13, toInt( appt.getRepeatFlag()) );
        stmt.setString( 14, appt.getCategory());
        stmt.setString( 15, appt.getReminderTimes());
        stmt.setString( 16, appt.getUntimed());
        if( appt.isEncrypted())
			stmt.setString(17, "Y");
		else
			stmt.setString(17, "N");
        java.util.Date until = appt.getRepeatUntil();
        if( until != null )
            stmt.setDate( 18, new java.sql.Date( until.getTime()) );
        else
            stmt.setDate(18, null );
        if( appt.getPriority() != null )
        	stmt.setInt(19, appt.getPriority().intValue());
        else
        	stmt.setInt(19, 5);
        stmt.setInt(20, appt.getKey());

        stmt.executeUpdate();
        stmt.close();

        delCache( appt.getKey() );
        writeCache( appt );
    }

	
	
    
}
