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
import java.util.Date;

import net.sf.borg.common.util.Version;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentKeyFilter;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.KeyedBean;


/**
 *
 * this is the JDBC layer for access to the appointment table
 */
class ApptJdbcDB extends JdbcDB implements AppointmentKeyFilter
{
    static
    {
        Version.addVersion("$Id$");
    }
       
    /** Creates a new instance of AppJdbcDB */
    ApptJdbcDB(String url, int userid) throws Exception
    {
        super( url, userid );
    }
    
    public void addObj(KeyedBean bean, boolean crypt) throws DBException, Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "INSERT INTO appointments ( appt_date, appt_num, userid, duration, text, skip_list," +
        " next_todo, vacation, holiday, private, times, frequency, todo, color, repeat, category, new, modified, deleted, alarm, reminders ) VALUES " +
        "( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        
        Appointment appt = (Appointment) bean;
        
        stmt.setTimestamp( 1, new java.sql.Timestamp( appt.getDate().getTime()), Calendar.getInstance() );
        stmt.setInt( 2, appt.getKey() );
        stmt.setInt( 3, userid_ );
        
        stmt.setInt( 4, toInt( appt.getDuration() ) );
        stmt.setString( 5, appt.getText() );
        stmt.setString( 6, toStr( appt.getSkipList() ) );
        java.util.Date nt = appt.getNextTodo();
        if( nt != null )
            stmt.setDate( 7, new java.sql.Date( appt.getNextTodo().getTime()) );
        else
            stmt.setDate(7, null );
        stmt.setInt( 8, toInt( appt.getVacation() ));
        stmt.setInt( 9, toInt( appt.getHoliday() ));
        stmt.setInt( 10, toInt( appt.getPrivate()) );
        stmt.setInt( 11, toInt( appt.getTimes() ));
        stmt.setString( 12, appt.getFrequency());
        stmt.setInt( 13, toInt( appt.getTodo()) );
        stmt.setString( 14, appt.getColor());
        stmt.setInt( 15, toInt( appt.getRepeatFlag()) );
        stmt.setString( 16, appt.getCategory());
               
        stmt.setInt( 17, toInt( appt.getNew()));
        stmt.setInt( 18, toInt( appt.getModified()));
        stmt.setInt( 19, toInt( appt.getDeleted()));
        stmt.setString( 20, appt.getAlarm());
        stmt.setString( 21, appt.getReminderTimes());
        stmt.executeUpdate();
        
        writeCache( appt );
    }
    
    public void delete(int key) throws DBException, Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "DELETE FROM appointments WHERE appt_num = ? AND userid = ?" );
        stmt.setInt( 1, key );
        stmt.setInt( 2, userid_ );
        stmt.executeUpdate();
        
        delCache( key );
    }
    
    public Collection getKeys() throws Exception
    {
        ArrayList keys = new ArrayList();
        PreparedStatement stmt = connection_.prepareStatement("SELECT appt_num FROM appointments WHERE userid = ?" );
        stmt.setInt( 1, userid_ );
        ResultSet rs = stmt.executeQuery();
        while( rs.next() )
        {
            keys.add( new Integer(rs.getInt("appt_num")) );
        }
        
        return( keys );
        
    }
    
    public Collection getTodoKeys() throws Exception
    {
        ArrayList keys = new ArrayList();
        PreparedStatement stmt = connection_.prepareStatement("SELECT appt_num FROM appointments WHERE userid = ? AND todo = '1'" );
        stmt.setInt( 1, userid_ );
        ResultSet rs = stmt.executeQuery();
        while( rs.next() )
        {
            keys.add( new Integer(rs.getInt("appt_num")) );
        }
        
        return( keys );
        
    }
    
    public Collection getRepeatKeys() throws Exception
    {
        ArrayList keys = new ArrayList();
        PreparedStatement stmt = connection_.prepareStatement("SELECT appt_num FROM appointments WHERE userid = ? AND repeat = '1'" );
        stmt.setInt( 1, userid_ );
        ResultSet rs = stmt.executeQuery();
        while( rs.next() )
        {
            keys.add( new Integer(rs.getInt("appt_num")) );
        }
        
        return( keys );
        
    }

    public int nextkey()
    {
     // TODO: Does this need to be implemented?!
        int maxKey = 0;
        curMaxKey_ = Math.max(curMaxKey_, maxKey);
        return ++curMaxKey_;
    }
    
    public KeyedBean newObj()
    {
        return( new Appointment() );
    }
    
	PreparedStatement getPSOne(int key) throws SQLException
	{
		PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM appointments WHERE appt_num = ? AND userid = ?" );
		stmt.setInt( 1, key );
		stmt.setInt( 2, userid_ );
		return stmt;
	}
	
	PreparedStatement getPSAll() throws SQLException
	{
		PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM appointments WHERE userid = ?" );
		stmt.setInt( 1, userid_ );
		return stmt;
	}
	
	KeyedBean createFrom(ResultSet r) throws SQLException
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
		appt.setRepeatFlag( r.getInt("repeat" ) != 0 );
		appt.setCategory( r.getString("category"));
		appt.setNew( r.getInt("new" ) != 0 );
		appt.setModified( r.getInt("modified" ) != 0 );
		appt.setDeleted( r.getInt("deleted" ) != 0 );
		appt.setAlarm( r.getString("alarm"));
		appt.setReminderTimes( r.getString("reminders"));
		
		return appt;
	}
	
    public void updateObj(KeyedBean bean, boolean crypt) throws DBException, Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "UPDATE appointments SET  appt_date = ?, " +
        "duration = ?, text = ?, skip_list = ?," +
        " next_todo = ?, vacation = ?, holiday = ?, private = ?, times = ?, frequency = ?, todo = ?, color = ?, repeat = ?, category = ?," +
		" new = ?, modified = ?, deleted = ?, alarm = ?, reminders = ?" +
        " WHERE appt_num = ? AND userid = ?");
        
        Appointment appt = (Appointment) bean;
        
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
        
 
        stmt.setInt( 15, toInt( appt.getNew()));
        stmt.setInt( 16, toInt( appt.getModified()));
        stmt.setInt( 17, toInt( appt.getDeleted()));
        stmt.setString( 18, appt.getAlarm());
        stmt.setString( 19, appt.getReminderTimes());
        
        stmt.setInt( 20, appt.getKey() );
        stmt.setInt( 21, userid_ );

        stmt.executeUpdate();
        
        delCache( appt.getKey() );
        writeCache( appt );
    }
    
}
