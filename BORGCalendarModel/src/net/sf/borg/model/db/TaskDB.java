package net.sf.borg.model.db;

import java.sql.SQLException;
import java.util.Collection;

import net.sf.borg.model.Project;
import net.sf.borg.model.Subtask;
import net.sf.borg.model.Tasklog;


public interface TaskDB {

    public Collection getSubTasks(int taskid) throws SQLException;
    
    public Collection getSubTasks() throws SQLException;

    public void deleteSubTask(int id) throws SQLException;

    public void addSubTask(Subtask s) throws SQLException;
    
    public void updateSubTask(Subtask s) throws SQLException;
    
    public int nextSubTaskKey() throws Exception;
    
    public Collection getLogs( int taskid ) throws SQLException;
    public Collection getLogs( ) throws SQLException;
    
    public void addLog(int taskid, String desc) throws SQLException;
    
    public void saveLog( Tasklog tlog ) throws SQLException;
    
    public Collection getProjects() throws SQLException;
    
    public Collection getTasks(int projectid) throws SQLException;
    
    public Project getProject(int projectid) throws SQLException;
    
    public void deleteProject(int id) throws SQLException;

    public void addProject(Project p) throws SQLException;
    
    public void updateProject(Project p) throws SQLException;
    
    public int nextProjectKey() throws Exception;

}