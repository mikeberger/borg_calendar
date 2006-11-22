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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.sf.borg.model.Subtask;
import net.sf.borg.model.Task;
import net.sf.borg.model.Tasklog;
import net.sf.borg.model.db.DBException;
import net.sf.borg.model.db.KeyedBean;
import net.sf.borg.model.db.SubtaskDB;

/**
 * 
 * this is the JDBC layer for access to the task table
 */
class TaskJdbcDB extends JdbcDB implements SubtaskDB {

	/** Creates a new instance of AppJdbcDB */
	TaskJdbcDB(String url, String username) throws Exception {
		super(url, username);
	}

	TaskJdbcDB(Connection conn) {
		super(conn);
	}

	public void addObj(KeyedBean bean, boolean crypt) throws DBException,
			Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("INSERT INTO tasks ( tasknum, username, start_date, due_date, person_assigned,"
						+ " priority, state, type, description, resolution, todo_list, user_task1,"
						+ " user_task2, user_task3, user_task4, user_task5, category, close_date) VALUES "
						+ "( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		Task task = (Task) bean;

		stmt.setInt(1, task.getKey());
		stmt.setString(2, username_);

		java.util.Date sd = task.getStartDate();
		if (sd != null)
			stmt.setDate(3, new java.sql.Date(sd.getTime()));
		else
			stmt.setDate(3, null);

		java.util.Date dd = task.getDueDate();
		if (dd != null)
			stmt.setDate(4, new java.sql.Date(dd.getTime()));
		else
			stmt.setDate(4, null);

		stmt.setString(5, task.getPersonAssigned());
		stmt.setString(6, task.getPriority());
		stmt.setString(7, task.getState());
		stmt.setString(8, task.getType());
		stmt.setString(9, task.getDescription());
		stmt.setString(10, task.getResolution());
		stmt.setString(11, task.getTodoList());
		stmt.setString(12, task.getUserTask1());
		stmt.setString(13, task.getUserTask2());
		stmt.setString(14, task.getUserTask3());
		stmt.setString(15, task.getUserTask4());
		stmt.setString(16, task.getUserTask5());
		stmt.setString(17, task.getCategory());
		java.util.Date cd = task.getCD();
		if (cd != null)
			stmt.setDate(18, new java.sql.Date(cd.getTime()));
		else
			stmt.setDate(18, null);

		stmt.executeUpdate();

		writeCache(task);

	}

	public void delete(int key) throws DBException, Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("DELETE FROM tasks WHERE tasknum = ? AND username = ?");
		stmt.setInt(1, key);
		stmt.setString(2, username_);
		stmt.executeUpdate();

		delCache(key);
	}

	public Collection getKeys() throws Exception {
		ArrayList keys = new ArrayList();
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT tasknum FROM tasks WHERE username = ?");
		stmt.setString(1, username_);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			keys.add(new Integer(rs.getInt("tasknum")));
		}

		return (keys);

	}

	public int nextkey() throws Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT MAX(tasknum) FROM tasks WHERE  username = ?");
		stmt.setString(1, username_);
		ResultSet r = stmt.executeQuery();
		int maxKey = 0;
		if (r.next())
			maxKey = r.getInt(1);
		curMaxKey_ = Math.max(curMaxKey_, maxKey);
		return ++curMaxKey_;
	}

	public KeyedBean newObj() {
		return (new Task());
	}

	PreparedStatement getPSOne(int key) throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * FROM tasks WHERE tasknum = ? AND username = ?");
		stmt.setInt(1, key);
		stmt.setString(2, username_);
		return stmt;
	}

	PreparedStatement getPSAll() throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * FROM tasks WHERE username = ?");
		stmt.setString(1, username_);
		return stmt;
	}

	KeyedBean createFrom(ResultSet r) throws SQLException {
		Task task = new Task();
		task.setKey(r.getInt("tasknum"));
		task.setTaskNumber(new Integer(r.getInt("tasknum")));
		task.setStartDate(r.getDate("start_date"));
		if (r.getDate("due_date") != null)
			task
					.setDueDate(new java.util.Date(r.getDate("due_date")
							.getTime()));
		task.setPersonAssigned(r.getString("person_assigned"));
		task.setPriority(r.getString("priority"));
		task.setState(r.getString("state"));
		task.setType(r.getString("type"));
		task.setDescription(r.getString("description"));
		task.setResolution(r.getString("resolution"));
		task.setTodoList(r.getString("todo_list"));
		task.setUserTask1(r.getString("user_task1"));
		task.setUserTask2(r.getString("user_task2"));
		task.setUserTask3(r.getString("user_task3"));
		task.setUserTask4(r.getString("user_task4"));
		task.setUserTask5(r.getString("user_task5"));
		task.setCategory(r.getString("category"));
		if (r.getDate("close_date") != null)
			task.setCD(new java.util.Date(r.getDate("close_date").getTime()));
		return task;
	}

	public void updateObj(KeyedBean bean, boolean crypt) throws DBException,
			Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("UPDATE tasks SET  start_date = ?, due_date = ?, person_assigned = ?,"
						+ " priority = ?, state = ?, type = ?, description = ?, resolution = ?, todo_list = ?, user_task1 = ?,"
						+ " user_task2 = ?, user_task3 = ?, user_task4 = ?, user_task5 = ?, category = ?, close_date = ? WHERE tasknum = ? AND username = ?");

		Task task = (Task) bean;

		java.util.Date sd = task.getStartDate();
		if (sd != null)
			stmt.setDate(1, new java.sql.Date(sd.getTime()));
		else
			stmt.setDate(1, null);

		java.util.Date dd = task.getDueDate();
		if (dd != null)
			stmt.setDate(2, new java.sql.Date(dd.getTime()));
		else
			stmt.setDate(2, null);

		stmt.setString(3, task.getPersonAssigned());
		stmt.setString(4, task.getPriority());
		stmt.setString(5, task.getState());
		stmt.setString(6, task.getType());
		stmt.setString(7, task.getDescription());
		stmt.setString(8, task.getResolution());
		stmt.setString(9, task.getTodoList());
		stmt.setString(10, task.getUserTask1());
		stmt.setString(11, task.getUserTask2());
		stmt.setString(12, task.getUserTask3());
		stmt.setString(13, task.getUserTask4());
		stmt.setString(14, task.getUserTask5());
		stmt.setString(15, task.getCategory());
		java.util.Date cd = task.getCD();
		if (cd != null)
			stmt.setDate(16, new java.sql.Date(cd.getTime()));
		else
			stmt.setDate(16, null);

		stmt.setInt(17, task.getKey());
		stmt.setString(18, username_);

		stmt.executeUpdate();

		delCache(task.getKey());
		writeCache(task);
	}

	private Subtask createSubtask(ResultSet r) throws SQLException {
		Subtask s = new Subtask();
		s.setId(new Integer(r.getInt("id")));
		s.setTask(new Integer(r.getInt("task")));
		if (r.getTimestamp("due_date") != null)
			s.setDueDate(new java.util.Date(r.getTimestamp("due_date")
					.getTime()));
		if (r.getTimestamp("create_date") != null)
			s.setCreateDate(new java.util.Date(r.getTimestamp("create_date")
					.getTime()));
		if (r.getTimestamp("close_date") != null)
			s.setCloseDate(new java.util.Date(r.getTimestamp("close_date")
					.getTime()));
		s.setDescription(r.getString("description"));

		return s;
	}

	public Collection getSubTasks(int taskid) throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * from subtasks where task = ? AND username = ?");
		ResultSet r = null;
		try {
			stmt.setInt(1, taskid);
			stmt.setString(2, username_);
			r = stmt.executeQuery();
			List lst = new ArrayList();
			while (r.next()) {
				Subtask s = createSubtask(r);
				lst.add(s);
			}
			return lst;
		} finally {
			if (r != null)
				r.close();
			if (stmt != null)
				stmt.close();
		}
	}

	public Collection getSubTasks() throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * from subtasks where username = ?");
		ResultSet r = null;
		try {

			stmt.setString(1, username_);
			r = stmt.executeQuery();
			List lst = new ArrayList();
			while (r.next()) {
				Subtask s = createSubtask(r);
				lst.add(s);
			}
			return lst;
		} finally {
			if (r != null)
				r.close();
			if (stmt != null)
				stmt.close();
		}
	}

	public void deleteSubTask(int id) throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("DELETE FROM subtasks WHERE id = ? AND username = ?");
		stmt.setInt(1, id);
		stmt.setString(2, username_);
		stmt.executeUpdate();

	}

	public void addSubTask(Subtask s) throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("INSERT INTO subtasks ( id, username, create_date, due_date,"
						+ " close_date, description, task ) VALUES "
						+ "( ?, ?, ?, ?, ?, ?, ?)");

		stmt.setInt(1, s.getId().intValue());
		stmt.setString(2, username_);

		java.util.Date sd = s.getCreateDate();
		if (sd != null)
			stmt.setDate(3, new java.sql.Date(sd.getTime()));
		else
			stmt.setDate(3, null);

		java.util.Date dd = s.getDueDate();
		if (dd != null)
			stmt.setDate(4, new java.sql.Date(dd.getTime()));
		else
			stmt.setDate(4, null);

		java.util.Date cd = s.getCloseDate();
		if (cd != null)
			stmt.setDate(5, new java.sql.Date(cd.getTime()));
		else
			stmt.setDate(5, null);

		stmt.setString(6, s.getDescription());
		stmt.setInt(7, s.getTask().intValue());

		stmt.executeUpdate();
	}

	public void updateSubTask(Subtask s) throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("UPDATE subtasks SET create_date = ?, due_date = ?,"
						+ " close_date = ?, description = ?, task = ?  WHERE id = ? AND username = ? ");

		stmt.setInt(6, s.getId().intValue());
		stmt.setString(7, username_);

		java.util.Date sd = s.getCreateDate();
		if (sd != null)
			stmt.setDate(1, new java.sql.Date(sd.getTime()));
		else
			stmt.setDate(1, null);

		java.util.Date dd = s.getDueDate();
		if (dd != null)
			stmt.setDate(2, new java.sql.Date(dd.getTime()));
		else
			stmt.setDate(2, null);

		java.util.Date cd = s.getCloseDate();
		if (cd != null)
			stmt.setDate(3, new java.sql.Date(cd.getTime()));
		else
			stmt.setDate(3, null);

		stmt.setString(4, s.getDescription());
		stmt.setInt(5, s.getTask().intValue());

		stmt.executeUpdate();
	}

	public int nextSubTaskKey() throws Exception {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT MAX(id) FROM subtasks WHERE username = ?");
		stmt.setString(1, username_);
		ResultSet r = stmt.executeQuery();
		int maxKey = 0;
		if (r.next())
			maxKey = r.getInt(1);
		return ++maxKey;
	}

	private int nextLogKey() throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT MAX(id) FROM tasklog WHERE username = ?");
		stmt.setString(1, username_);
		ResultSet r = stmt.executeQuery();
		int maxKey = 0;
		if (r.next())
			maxKey = r.getInt(1);
		return ++maxKey;
	}

	public void addLog(int taskid, String desc) throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("INSERT INTO subtasks ( id, username, logtime, description, task ) VALUES "
						+ "( ?, ?, ?, ?, ?)");

		stmt.setInt(1, nextLogKey());
		stmt.setString(2, username_);
		Date now = new Date();
		stmt.setTimestamp(3, new java.sql.Timestamp(now.getTime()), Calendar
				.getInstance());
		stmt.setString(4, desc);
		stmt.setInt(5, taskid);

		stmt.executeUpdate();

	}
	
	private Tasklog createTasklog(ResultSet r) throws SQLException {
		Tasklog s = new Tasklog();
		s.setId(new Integer(r.getInt("id")));
		s.setTask(new Integer(r.getInt("task")));
		if (r.getTimestamp("due_date") != null)
			s.setlogTime(new java.util.Date(r.getTimestamp("logtime")
					.getTime()));
		s.setDescription(r.getString("description"));

		return s;
	}

	public Collection getLogs(int taskid) throws SQLException {
		PreparedStatement stmt = connection_
				.prepareStatement("SELECT * from tasklog where username = ? and task = ?");
		ResultSet r = null;
		try {

			stmt.setString(1, username_);
			stmt.setInt(2,taskid);
			r = stmt.executeQuery();
			List lst = new ArrayList();
			while (r.next()) {
				Tasklog s = createTasklog(r);
				lst.add(s);
			}
			return lst;
		} finally {
			if (r != null)
				r.close();
			if (stmt != null)
				stmt.close();
		}
		
	}

}
