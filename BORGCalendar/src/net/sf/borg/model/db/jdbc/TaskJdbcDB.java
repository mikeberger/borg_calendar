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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.sf.borg.model.db.TaskDB;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.Task;
import net.sf.borg.model.entity.Tasklog;

/**
 * this is the JDBC layer for access to the task table.
 */
public class TaskJdbcDB extends JdbcBeanDB<Task> implements TaskDB {


    /* (non-Javadoc)
     * @see net.sf.borg.model.db.EntityDB#addObj(net.sf.borg.model.entity.KeyedEntity)
     */
    @Override
    public void addObj(Task task) throws Exception {
        PreparedStatement stmt = connection_
                .prepareStatement("INSERT INTO tasks ( tasknum, start_date, due_date, person_assigned,"
                        + " priority, state, type, description, resolution, category, close_date, project) VALUES "
                        + "( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        stmt.setInt(1, task.getKey());
       
        java.util.Date sd = task.getStartDate();
        if (sd != null)
            stmt.setDate(2, new java.sql.Date(sd.getTime()));
        else
            stmt.setDate(2, null);

        java.util.Date dd = task.getDueDate();
        if (dd != null)
            stmt.setDate(3, new java.sql.Date(dd.getTime()));
        else
            stmt.setDate(3, null);

        stmt.setString(4, task.getPersonAssigned());
        if( task.getPriority() != null )
        	stmt.setInt(5, task.getPriority().intValue());
        else
        	stmt.setInt(5, 1);
        stmt.setString(6, task.getState());
        stmt.setString(7, task.getType());
        stmt.setString(8, task.getDescription());
        stmt.setString(9, task.getResolution());
        stmt.setString(10, task.getCategory());
        java.util.Date cd = task.getCompletionDate();
        if (cd != null)
            stmt.setDate(11, new java.sql.Date(cd.getTime()));
        else
            stmt.setDate(11, null);
        if (task.getProject() != null)
            stmt.setInt(12, task.getProject().intValue());
        else
            stmt.setNull(12, java.sql.Types.INTEGER);
        stmt.executeUpdate();

        writeCache(task);
        stmt.close();

    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.EntityDB#delete(int)
     */
    @Override
    public void delete(int key) throws Exception {
        PreparedStatement stmt = connection_.prepareStatement("DELETE FROM tasks WHERE tasknum = ?");
        stmt.setInt(1, key);
        stmt.executeUpdate();
        stmt.close();

        delCache(key);
    }

    /**
     * Gets the keys.
     * 
     * @return the keys
     * 
     * @throws Exception the exception
     */
    public Collection<Integer> getKeys() throws Exception {
        ArrayList<Integer> keys = new ArrayList<Integer>();
        PreparedStatement stmt = connection_.prepareStatement("SELECT tasknum FROM tasks");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            keys.add(new Integer(rs.getInt("tasknum")));
        }
        rs.close();
        stmt.close();

        return (keys);

    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.EntityDB#nextkey()
     */
    @Override
    public int nextkey() throws Exception {
        PreparedStatement stmt = connection_.prepareStatement("SELECT MAX(tasknum) FROM tasks");
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
    public Task newObj() {
        return (new Task());
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.jdbc.JdbcBeanDB#getPSOne(int)
     */
    @Override
    PreparedStatement getPSOne(int key) throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM tasks WHERE tasknum = ?");
        stmt.setInt(1, key);
        return stmt;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.jdbc.JdbcBeanDB#getPSAll()
     */
    @Override
    PreparedStatement getPSAll() throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM tasks");
        return stmt;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.jdbc.JdbcBeanDB#createFrom(java.sql.ResultSet)
     */
    @Override
    Task createFrom(ResultSet r) throws SQLException {
        Task task = new Task();
        task.setKey(r.getInt("tasknum"));
        task.setStartDate(r.getDate("start_date"));
        if (r.getDate("due_date") != null)
            task.setDueDate(new java.util.Date(r.getDate("due_date").getTime()));
        task.setPersonAssigned(r.getString("person_assigned"));
        task.setPriority(new Integer(r.getInt("priority")));
        task.setState(r.getString("state"));
        task.setType(r.getString("type"));
        task.setDescription(r.getString("description"));
        task.setResolution(r.getString("resolution"));
        task.setCategory(r.getString("category"));
        if (r.getDate("close_date") != null)
            task.setCompletionDate(new java.util.Date(r.getDate("close_date").getTime()));
        task.setProject((Integer) r.getObject("project"));
        return task;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.EntityDB#updateObj(net.sf.borg.model.entity.KeyedEntity)
     */
    @Override
    public void updateObj(Task task) throws Exception {
        PreparedStatement stmt = connection_
                .prepareStatement("UPDATE tasks SET  start_date = ?, due_date = ?, person_assigned = ?,"
                        + " priority = ?, state = ?, type = ?, description = ?, resolution = ?,"
                        + " category = ?, close_date = ?, project = ? WHERE tasknum = ?");

        
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
        stmt.setInt(4, task.getPriority().intValue());
        stmt.setString(5, task.getState());
        stmt.setString(6, task.getType());
        stmt.setString(7, task.getDescription());
        stmt.setString(8, task.getResolution());
        stmt.setString(9, task.getCategory());
        java.util.Date cd = task.getCompletionDate();
        if (cd != null)
            stmt.setDate(10, new java.sql.Date(cd.getTime()));
        else
            stmt.setDate(10, null);
        if (task.getProject() != null)
            stmt.setInt(11, task.getProject().intValue());
        else
            stmt.setNull(11, java.sql.Types.INTEGER);
        stmt.setInt(12, task.getKey());

        stmt.executeUpdate();

        delCache(task.getKey());
        writeCache(task);
        stmt.close();

    }

    /**
     * Create a subtask object from a result set.
     * 
     * @param r the result set
     * 
     * @return the subtask
     * 
     * @throws SQLException the SQL exception
     */
    private Subtask createSubtask(ResultSet r) throws SQLException {
        Subtask s = new Subtask();
        s.setKey(r.getInt("id"));
        s.setTask(new Integer(r.getInt("task")));
        if (r.getTimestamp("due_date") != null)
            s.setDueDate(new java.util.Date(r.getTimestamp("due_date").getTime()));
        if (r.getTimestamp("create_date") != null)
            s.setStartDate(new java.util.Date(r.getTimestamp("create_date").getTime()));
        if (r.getTimestamp("close_date") != null)
            s.setCloseDate(new java.util.Date(r.getTimestamp("close_date").getTime()));
        s.setDescription(r.getString("description"));

        return s;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#getSubTasks(int)
     */
    @Override
    public Collection<Subtask> getSubTasks(int taskid) throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("SELECT * from subtasks where task = ?");
        ResultSet r = null;
        try {
            stmt.setInt(1, taskid);
            r = stmt.executeQuery();
            List<Subtask> lst = new ArrayList<Subtask>();
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

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#getSubTasks()
     */
    @Override
    public Collection<Subtask> getSubTasks() throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("SELECT * from subtasks");
        ResultSet r = null;
        try {

            r = stmt.executeQuery();
            List<Subtask> lst = new ArrayList<Subtask>();
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
    
    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#getSubTask(int)
     */
    @Override
    public Subtask getSubTask(int id) throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("SELECT * from subtasks WHERE id = ?");
        stmt.setInt(1, id);
        Subtask p = null;
        ResultSet r = null;
        try {
            r = stmt.executeQuery();
            if (r.next()) {
                p = createSubtask(r);
            }

        } finally {
            if (r != null)
                r.close();
                stmt.close();
        }
        return p;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#deleteSubTask(int)
     */
    @Override
    public void deleteSubTask(int id) throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("DELETE FROM subtasks WHERE id = ?");
        stmt.setInt(1, id);
        stmt.executeUpdate();
        stmt.close();

    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#addSubTask(net.sf.borg.model.entity.Subtask)
     */
    @Override
    public void addSubTask(Subtask s) throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("INSERT INTO subtasks ( id, create_date, due_date,"
                + " close_date, description, task ) VALUES " + "( ?, ?, ?, ?, ?, ?)");

        stmt.setInt(1, s.getKey());

        java.util.Date sd = s.getStartDate();
        if (sd != null)
            stmt.setDate(2, new java.sql.Date(sd.getTime()));
        else
            stmt.setDate(2, null);

        java.util.Date dd = s.getDueDate();
        if (dd != null)
            stmt.setDate(3, new java.sql.Date(dd.getTime()));
        else
            stmt.setDate(3, null);

        java.util.Date cd = s.getCloseDate();
        if (cd != null)
            stmt.setDate(4, new java.sql.Date(cd.getTime()));
        else
            stmt.setDate(4, null);

        stmt.setString(5, s.getDescription());
        stmt.setInt(6, s.getTask().intValue());

        stmt.executeUpdate();
        stmt.close();

    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#updateSubTask(net.sf.borg.model.entity.Subtask)
     */
    @Override
    public void updateSubTask(Subtask s) throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("UPDATE subtasks SET create_date = ?, due_date = ?,"
                + " close_date = ?, description = ?, task = ?  WHERE id = ?");

        stmt.setInt(6, s.getKey());

        java.util.Date sd = s.getStartDate();
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
        stmt.close();

    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#nextSubTaskKey()
     */
    @Override
    public int nextSubTaskKey() throws Exception {
        PreparedStatement stmt = connection_.prepareStatement("SELECT MAX(id) FROM subtasks");
        ResultSet r = stmt.executeQuery();
        int maxKey = 0;
        if (r.next())
            maxKey = r.getInt(1);
        r.close();
        stmt.close();

        return ++maxKey;
    }

    /**
     * Get the next log key
     * 
     * @return the next log key
     * 
     * @throws SQLException the SQL exception
     */
    private int nextLogKey() throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("SELECT MAX(id) FROM tasklog");
        ResultSet r = stmt.executeQuery();
        int maxKey = 0;
        if (r.next())
            maxKey = r.getInt(1);
        r.close();
        stmt.close();

        return ++maxKey;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#addLog(int, java.lang.String)
     */
    @Override
    public void addLog(int taskid, String desc) throws SQLException {
        PreparedStatement stmt = connection_
                .prepareStatement("INSERT INTO tasklog ( id, logtime, description, task ) VALUES " + "( ?, ?, ?, ?)");

        stmt.setInt(1, nextLogKey());
        Date now = new Date();
        stmt.setTimestamp(2, new java.sql.Timestamp(now.getTime()), Calendar.getInstance());
        stmt.setString(3, desc);
        stmt.setInt(4, taskid);

        stmt.executeUpdate();
        stmt.close();

    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#saveLog(net.sf.borg.model.entity.Tasklog)
     */
    @Override
    public void saveLog(Tasklog tlog) throws SQLException {
        PreparedStatement stmt = connection_
                .prepareStatement("INSERT INTO tasklog ( id, logtime, description, task ) VALUES " + "( ?, ?, ?, ?)");

        stmt.setInt(1, nextLogKey());
        Date d = tlog.getlogTime();
        stmt.setTimestamp(2, new java.sql.Timestamp(d.getTime()), Calendar.getInstance());
        stmt.setString(3, tlog.getDescription());
        stmt.setInt(4, tlog.getTask().intValue());

        stmt.executeUpdate();
        stmt.close();

    }

    /**
     * Creates a tasklog object from a result set
     * 
     * @param r the result set
     * 
     * @return the tasklog object
     * 
     * @throws SQLException the SQL exception
     */
    private Tasklog createTasklog(ResultSet r) throws SQLException {
        Tasklog s = new Tasklog();
        s.setKey(r.getInt("id"));
        s.setTask(new Integer(r.getInt("task")));
        if (r.getTimestamp("logtime") != null)
            s.setlogTime(new java.util.Date(r.getTimestamp("logtime").getTime()));
        s.setDescription(r.getString("description"));

        return s;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#getLogs(int)
     */
    @Override
    public Collection<Tasklog> getLogs(int taskid) throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("SELECT * from tasklog where task = ?");
        ResultSet r = null;
        List<Tasklog> lst = new ArrayList<Tasklog>();
        try {

            stmt.setInt(1, taskid);
            r = stmt.executeQuery();
            while (r.next()) {
                Tasklog s = createTasklog(r);
                lst.add(s);
            }

        } finally {
            if (r != null)
                r.close();
            if (stmt != null)
                stmt.close();
        }
        return lst;

    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#getLogs()
     */
    @Override
    public Collection<Tasklog> getLogs() throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("SELECT * from tasklog");
        ResultSet r = null;
        try {

            r = stmt.executeQuery();
            List<Tasklog> lst = new ArrayList<Tasklog>();
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

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#addProject(net.sf.borg.model.entity.Project)
     */
    @Override
    public void addProject(Project p) throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("INSERT INTO projects ( id,start_date, due_date,"
                + " description, category, status, parent ) VALUES " + "( ?, ?, ?, ?, ?, ?, ?)");

        stmt.setInt(1, p.getKey());

        java.util.Date sd = p.getStartDate();
        if (sd != null)
            stmt.setDate(2, new java.sql.Date(sd.getTime()));
        else
            stmt.setDate(2, null);

        java.util.Date dd = p.getDueDate();
        if (dd != null)
            stmt.setDate(3, new java.sql.Date(dd.getTime()));
        else
            stmt.setDate(3, null);

        stmt.setString(4, p.getDescription());
        stmt.setString(5, p.getCategory());
        stmt.setString(6, p.getStatus());
        if (p.getParent() != null)
            stmt.setInt(7, p.getParent().intValue());
        else
            stmt.setNull(7, java.sql.Types.INTEGER);

        stmt.executeUpdate();
        stmt.close();

    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#deleteProject(int)
     */
    @Override
    public void deleteProject(int id) throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("DELETE FROM projects WHERE id = ?");
        stmt.setInt(1, id);
        stmt.executeUpdate();
        stmt.close();

    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#getProject(int)
     */
    @Override
    public Project getProject(int projectid) throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("SELECT * FROM projects WHERE id = ?");
        stmt.setInt(1, projectid);
        Project p = null;
        ResultSet r = null;
        try {
            r = stmt.executeQuery();
            if (r.next()) {
                p = createProject(r);
            }

        } finally {
            if (r != null)
                r.close();
           
                stmt.close();
        }
        return p;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#getProjects()
     */
    @Override
    public Collection<Project> getProjects() throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("SELECT * from projects");
        ResultSet r = null;
        List<Project> lst = new ArrayList<Project>();
        try {

            r = stmt.executeQuery();
            while (r.next()) {
                Project s = createProject(r);
                lst.add(s);
            }

        } finally {
            if (r != null)
                r.close();
            if (stmt != null)
                stmt.close();
        }
        return lst;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#getTasks(int)
     */
    @Override
    public Collection<Task> getTasks(int projectid) throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("SELECT * from tasks where project = ?");
        ResultSet r = null;
        List<Task> lst = new ArrayList<Task>();
        try {

            stmt.setInt(1, projectid);
            r = stmt.executeQuery();
            while (r.next()) {
                Task s = createFrom(r);
                lst.add(s);
            }

        } finally {
            if (r != null)
                r.close();
            if (stmt != null)
                stmt.close();
        }
        return lst;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#getSubProjects(int)
     */
    @Override
    public Collection<Project> getSubProjects(int projectid) throws SQLException {
        PreparedStatement stmt = connection_
                .prepareStatement("SELECT * from projects where parent = ? ORDER BY start_date");
        ResultSet r = null;
        List<Project> lst = new ArrayList<Project>();
        try {

            stmt.setInt(1, projectid);
            r = stmt.executeQuery();
            while (r.next()) {
                Project s = createProject(r);
                lst.add(s);
            }

        } finally {
            if (r != null)
                r.close();
            if (stmt != null)
                stmt.close();
        }
        return lst;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#nextProjectKey()
     */
    @Override
    public int nextProjectKey() throws Exception {
        PreparedStatement stmt = connection_.prepareStatement("SELECT MAX(id) FROM projects");
        ResultSet r = stmt.executeQuery();
        int maxKey = 0;
        if (r.next())
            maxKey = r.getInt(1);
        r.close();
        stmt.close();

        return ++maxKey;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.model.db.TaskDB#updateProject(net.sf.borg.model.entity.Project)
     */
    @Override
    public void updateProject(Project s) throws SQLException {
        PreparedStatement stmt = connection_.prepareStatement("UPDATE projects SET start_date = ?, due_date = ?,"
                + " description = ?, category = ?, status = ?, parent = ?  WHERE id = ?");

        stmt.setInt(7, s.getKey());

        java.util.Date sd = s.getStartDate();
        if (sd != null)
            stmt.setDate(1, new java.sql.Date(sd.getTime()));
        else
            stmt.setDate(1, null);

        java.util.Date dd = s.getDueDate();
        if (dd != null)
            stmt.setDate(2, new java.sql.Date(dd.getTime()));
        else
            stmt.setDate(2, null);

        stmt.setString(3, s.getDescription());
        stmt.setString(4, s.getCategory());
        stmt.setString(5, s.getStatus());
        if (s.getParent() != null)
            stmt.setInt(6, s.getParent().intValue());
        else
            stmt.setNull(6, java.sql.Types.INTEGER);
        stmt.executeUpdate();
        stmt.close();

    }

    /**
     * Creates a project from a result set
     * 
     * @param r the result set
     * 
     * @return the project
     * 
     * @throws SQLException the SQL exception
     */
    private Project createProject(ResultSet r) throws SQLException {
        Project s = new Project();
        s.setKey(r.getInt("id"));
        if (r.getTimestamp("due_date") != null)
            s.setDueDate(new java.util.Date(r.getTimestamp("due_date").getTime()));
        if (r.getTimestamp("start_date") != null)
            s.setStartDate(new java.util.Date(r.getTimestamp("start_date").getTime()));
        s.setDescription(r.getString("description"));
        s.setCategory(r.getString("category"));
        s.setStatus(r.getString("status"));
        int parent = r.getInt("parent");
        if (r.wasNull())
            s.setParent(null);
        else
            s.setParent(new Integer(parent));

        return s;
    }
}
