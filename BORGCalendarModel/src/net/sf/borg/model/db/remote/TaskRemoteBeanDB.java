package net.sf.borg.model.db.remote;

import java.util.Collection;

import net.sf.borg.model.Project;
import net.sf.borg.model.Subtask;
import net.sf.borg.model.Tasklog;
import net.sf.borg.model.db.TaskDB;

public class TaskRemoteBeanDB extends RemoteBeanDB implements TaskDB {

    TaskRemoteBeanDB(Class cls, String clsstr, String impl, boolean readonly, String user) {
	super(cls, clsstr, impl, readonly, user);
    }

    public final synchronized void addLog(int taskid, String desc) throws Exception {
	call("addLog",new IRemoteProxy.ComposedObject(new Integer(taskid), desc));
    }

    public final synchronized void addProject(Project p) throws Exception {
	call("addProject",p);
	
    }

    public final synchronized void addSubTask(Subtask s) throws Exception {
	call("addSubTask",s);
    }

    public final synchronized void deleteProject(int id) throws Exception {
	call("deleteProject",new Integer(id));
	
    }

    public final synchronized void deleteSubTask(int id) throws Exception {
	call("deleteSubTask",new Integer(id));
    }

    public final synchronized Collection getLogs(int taskid) throws Exception {
	return (Collection) call("getLogsI",new Integer(taskid));
    }

    public final synchronized Collection getLogs() throws Exception {
	return (Collection) call("getLogs",null);
    }

    public final synchronized Project getProject(int projectid) throws Exception {
	return (Project) call("getProject",new Integer(projectid));
    }

    public final synchronized Collection getProjects() throws Exception {
	return (Collection) call("getProjects",null);
    }

    public final synchronized Collection getSubTasks(int taskid) throws Exception {
	return (Collection) call("getSubTasksI",new Integer(taskid));
    }

    public final synchronized Collection getSubTasks() throws Exception {
	return (Collection) call("getSubTasks",null);
    }

    public final synchronized Collection getTasks(int projectid) throws Exception {
	return (Collection) call("getTasks",new Integer(projectid));
    }

    public final synchronized int nextProjectKey() throws Exception {
	Integer i = (Integer) call("nextProjectKey", null);
	return i.intValue();
    }

    public final synchronized int nextSubTaskKey() throws Exception {
	Integer i = (Integer) call("nextSubTaskKey", null);
	return i.intValue();
    }

    public final synchronized void saveLog(Tasklog tlog) throws Exception {
	call("saveLog",tlog);
	
    }

    public final synchronized void updateProject(Project p) throws Exception {
	call("updateProject",p);
	
    }

    public final synchronized void updateSubTask(Subtask s) throws Exception {
	call("updateSubTask",s);
	
    }

}
