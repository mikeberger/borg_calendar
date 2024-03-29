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

import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.Task;
import net.sf.borg.model.entity.Tasklog;

import java.util.Collection;


/**
 * The Interface for a Task database.
 */
public interface TaskDB extends EntityDB<Task>{

    /**
     * Gets all sub tasks for a given task.
     * 
     * @param taskid the taskid
     * 
     * @return the sub tasks
     * 
     * @throws Exception the exception
     */
    Collection<Subtask> getSubTasks(int taskid) throws Exception;
    
    /**
     * Gets all sub tasks in the database.
     * 
     * @return the sub tasks
     * 
     * @throws Exception the exception
     */
    Collection<Subtask> getSubTasks() throws Exception;
    
    /**
     * Gets a sub task by subtask id.
     * 
     * @param id the id
     * 
     * @return the sub task
     * 
     * @throws Exception the exception
     */
    Subtask getSubTask(int id) throws Exception;

    /**
     * Delete a sub task by id.
     * 
     * @param id the id
     * 
     * @throws Exception the exception
     */
    void deleteSubTask(int id) throws Exception;

    /**
     * Adds a sub task.
     * 
     * @param s the subtask
     * 
     * @throws Exception the exception
     */
    void addSubTask(Subtask s) throws Exception;
    
    /**
     * Update a sub task.
     * 
     * @param s the subtask
     * 
     * @throws Exception the exception
     */
    void updateSubTask(Subtask s) throws Exception;
    
    /**
     * get the Next available sub task key.
     * 
     * @return the next available sub task key
     * 
     * @throws Exception the exception
     */
    int nextSubTaskKey() throws Exception;
    
    /**
     * Gets all task logs for a given task.
     * 
     * @param taskid the task id
     * 
     * @return the logs
     * 
     * @throws Exception the exception
     */
    Collection<Tasklog> getLogs(int taskid) throws Exception;
    
    /**
     * Gets all task logs in the db.
     * 
     * @return the logs
     * 
     * @throws Exception the exception
     */
    Collection<Tasklog> getLogs() throws Exception;
    
    /**
     * Adds a task log for a task.
     * 
     * @param taskid the task id
     * @param desc the log text
     * 
     * @throws Exception the exception
     */
    void addLog(int taskid, String desc) throws Exception;
    
    /**
     * Save a task log in the db.
     * 
     * @param tlog the task log object
     * 
     * @throws Exception the exception
     */
    void saveLog(Tasklog tlog) throws Exception;
    
    /**
     * Gets all projects in the database.
     * 
     * @return the projects
     * 
     * @throws Exception the exception
     */
    Collection<Project> getProjects() throws Exception;
    
    /**
     * Gets all tasks for a given project.
     * 
     * @param projectid the project id
     * 
     * @return the tasks
     * 
     * @throws Exception the exception
     */
    Collection<Task> getTasks(int projectid) throws Exception;
    
    /**
     * Gets a project by id.
     * 
     * @param projectid the project id
     * 
     * @return the project
     * 
     * @throws Exception the exception
     */
    Project getProject(int projectid) throws Exception;
    
    /**
     * Delete a project by id 
     * 
     * @param id the project id
     * 
     * @throws Exception the exception
     */
    void deleteProject(int id) throws Exception;

    /**
     * Adds a project to the db.
     * 
     * @param p the project
     * 
     * @throws Exception the exception
     */
    void addProject(Project p) throws Exception;
    
    /**
     * Updates a project in the db.
     * 
     * @param p the project
     * 
     * @throws Exception the exception
     */
    void updateProject(Project p) throws Exception;
    
    /**
     * get the Next available project key.
     * 
     * @return the next available project key
     * 
     * @throws Exception the exception
     */
    int nextProjectKey() throws Exception;
    
    /**
     * Gets all sub projects (child projects) for a given project.
     * 
     * @param projid the project id
     * 
     * @return the sub projects
     * 
     * @throws Exception the exception
     */
    Collection<Project> getSubProjects(int projid) throws Exception;
    
    /**
     * get all tasks with a given type
     * @param type the type
     * @return the tasks
     * @throws Exception
     */
    Collection<Task> getTasksByType(String type) throws Exception;
    
    /**
     * rename a task type in existing tasks
     * @param oldtype
     * @param newtype
     * @throws Exception
     */
    void renameTaskType(String oldtype, String newtype) throws Exception;

	Subtask getSubTaskByUid(String uid) throws Exception;

	Task getTaskByUid(String uid) throws Exception;

}