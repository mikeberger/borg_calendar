package net.sf.borg.model.db;

import java.util.Collection;

import net.sf.borg.model.beans.Project;
import net.sf.borg.model.beans.Subtask;
import net.sf.borg.model.beans.Task;
import net.sf.borg.model.beans.Tasklog;


public interface TaskDB {

    public Collection<Subtask> getSubTasks(int taskid) throws Exception;
    
    public Collection<Subtask> getSubTasks() throws Exception;
    
    public Subtask getSubTask(int id) throws Exception;

    public void deleteSubTask(int id) throws Exception;

    public void addSubTask(Subtask s) throws Exception;
    
    public void updateSubTask(Subtask s) throws Exception;
    
    public int nextSubTaskKey() throws Exception;
    
    public Collection<Tasklog> getLogs( int taskid ) throws Exception;
    public Collection<Tasklog> getLogs( ) throws Exception;
    
    public void addLog(int taskid, String desc) throws Exception;
    
    public void saveLog( Tasklog tlog ) throws Exception;
    
    public Collection<Project> getProjects() throws Exception;
    
    public Collection<Task> getTasks(int projectid) throws Exception;
    
    public Project getProject(int projectid) throws Exception;
    
    public void deleteProject(int id) throws Exception;

    public void addProject(Project p) throws Exception;
    
    public void updateProject(Project p) throws Exception;
    
    public int nextProjectKey() throws Exception;
    
    public Collection<Project> getSubProjects(int projid) throws Exception;

}