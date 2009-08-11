/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
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