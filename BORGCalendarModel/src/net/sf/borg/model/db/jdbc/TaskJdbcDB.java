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

import net.sf.borg.model.Task;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.KeyedBean;


/**
 *
 * this is the JDBC layer for access to the task table
 */
class TaskJdbcDB extends JdbcDB
{

             
    /** Creates a new instance of AppJdbcDB */
    TaskJdbcDB(String url, String username)  throws Exception
    {
        super( url, username );
    }
    
    TaskJdbcDB(Connection conn) 
    {
        super( conn );
    }
    
    public void addObj(KeyedBean bean, boolean crypt) throws DBException, Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "INSERT INTO tasks ( tasknum, username, start_date, due_date, person_assigned," + 
        " priority, state, type, description, resolution, todo_list, user_task1," +
        " user_task2, user_task3, user_task4, user_task5, category) VALUES " +
        "( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        
        Task task = (Task) bean;
        
        stmt.setInt( 1, task.getKey() );
        stmt.setString( 2, username_ );

        java.util.Date sd = task.getStartDate();        
        if( sd != null )
            stmt.setDate( 3, new java.sql.Date( sd.getTime()) );
        else
            stmt.setDate(3, null );
        
        java.util.Date dd = task.getDueDate();
        if( dd != null )
            stmt.setDate( 4, new java.sql.Date( dd.getTime()) );
        else
            stmt.setDate(4, null ); 
        
        stmt.setString( 5, task.getPersonAssigned() );
        stmt.setString( 6, task.getPriority());
        stmt.setString( 7, task.getState() );
        stmt.setString( 8, task.getType() );
        stmt.setString( 9, task.getDescription() );
        stmt.setString( 10, task.getResolution() );
        stmt.setString( 11, task.getTodoList() );
        stmt.setString( 12, task.getUserTask1() );
        stmt.setString( 13, task.getUserTask2() );
        stmt.setString( 14, task.getUserTask3() );
        stmt.setString( 15, task.getUserTask4() );
        stmt.setString( 16, task.getUserTask5() );
        stmt.setString( 17, task.getCategory() );

        
        stmt.executeUpdate();
        
        writeCache( task );

    }
    
    public void delete(int key) throws DBException, Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "DELETE FROM tasks WHERE tasknum = ? AND username = ?" );
        stmt.setInt( 1, key );
        stmt.setString( 2, username_ );
        stmt.executeUpdate();
        
        delCache( key );
    }
    
    public Collection getKeys() throws Exception
    {
        ArrayList keys = new ArrayList();
        PreparedStatement stmt = connection_.prepareStatement("SELECT tasknum FROM tasks WHERE username = ?" );
        stmt.setString( 1, username_ );
        ResultSet rs = stmt.executeQuery();
        while( rs.next() )
        {
            keys.add( new Integer(rs.getInt("tasknum")) );
        }
        
        return( keys );
        
    }
    
    public int nextkey() throws Exception
    {
        PreparedStatement stmt = connection_.prepareStatement("SELECT MAX(tasknum) FROM tasks WHERE  username = ?" );
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
        return( new Task() );
    }
    
	PreparedStatement getPSOne(int key) throws SQLException
	{
		PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM tasks WHERE tasknum = ? AND username = ?" );
		stmt.setInt( 1, key );
		stmt.setString( 2, username_ );
		return stmt;
	}
	
	PreparedStatement getPSAll() throws SQLException
	{
		PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM tasks WHERE username = ?" );
		stmt.setString( 1, username_ );
		return stmt;
	}
	
	KeyedBean createFrom(ResultSet r) throws SQLException
	{
		Task task = new Task();
		task.setKey(r.getInt("tasknum"));
		task.setTaskNumber(new Integer(r.getInt("tasknum")));
		task.setStartDate( r.getDate("start_date"));
		if( r.getDate("due_date") != null )
			task.setDueDate( new java.util.Date( r.getDate("due_date").getTime()));
		task.setPersonAssigned( r.getString("person_assigned"));
		task.setPriority( r.getString("priority"));
		task.setState( r.getString("state"));
		task.setType( r.getString("type"));
		task.setDescription( r.getString("description"));
		task.setResolution( r.getString("resolution"));
		task.setTodoList( r.getString("todo_list"));
		task.setUserTask1( r.getString("user_task1"));
		task.setUserTask2( r.getString("user_task2"));
		task.setUserTask3( r.getString("user_task3"));
		task.setUserTask4( r.getString("user_task4"));
		task.setUserTask5( r.getString("user_task5"));
		task.setCategory( r.getString("category"));
		return task;
	}
	
    public void updateObj(KeyedBean bean, boolean crypt) throws DBException, Exception
    {
        PreparedStatement stmt = connection_.prepareStatement( "UPDATE tasks SET  start_date = ?, due_date = ?, person_assigned = ?," + 
        " priority = ?, state = ?, type = ?, description = ?, resolution = ?, todo_list = ?, user_task1 = ?," +
        " user_task2 = ?, user_task3 = ?, user_task4 = ?, user_task5 = ?, category = ? WHERE tasknum = ? AND username = ?" );        

        Task task = (Task) bean;
        

        java.util.Date sd = task.getStartDate();        
        if( sd != null )
            stmt.setDate( 1, new java.sql.Date( sd.getTime()) );
        else
            stmt.setDate(1, null );
        
        java.util.Date dd = task.getDueDate();
        if( dd != null )
            stmt.setDate( 2, new java.sql.Date( dd.getTime()) );
        else
            stmt.setDate(2, null ); 
        
        stmt.setString( 3, task.getPersonAssigned() );
        stmt.setString( 4, task.getPriority());
        stmt.setString( 5, task.getState() );
        stmt.setString( 6, task.getType() );
        stmt.setString( 7, task.getDescription() );
        stmt.setString( 8, task.getResolution() );
        stmt.setString( 9, task.getTodoList() );
        stmt.setString( 10, task.getUserTask1() );
        stmt.setString( 11, task.getUserTask2() );
        stmt.setString( 12, task.getUserTask3() );
        stmt.setString( 13, task.getUserTask4() );
        stmt.setString( 14, task.getUserTask5() );
        stmt.setString( 15, task.getCategory() );

        stmt.setInt( 16, task.getKey() );
        stmt.setString( 17, username_ );

        
        stmt.executeUpdate();
        
        delCache( task.getKey() );
        writeCache( task );
    }
    
}
