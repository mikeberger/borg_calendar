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
 
 Copyright 2004 by ==Quiet==
 */
package net.sf.borgweb.action;

import java.io.IOException;
import java.text.DateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.borg.model.Task;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.TaskTypes;
import net.sf.borgweb.form.TaskForm;
import net.sf.borgweb.util.EH;
import net.sf.borgweb.util.GetModel;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class TaskAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		
		HttpSession sess = request.getSession();
		TaskForm tf = (TaskForm) form;
		// System.out.println(mapping.getPath());
		String cmd = mapping.getPath();
		try {
			
			if (cmd.endsWith("/editTask")) {
				// determine if this is add, edit, delete
				String key = request.getParameter("key");
				if (key != null) {
					
					// load data from db into form
					int k = Integer.parseInt(key);
					
					tf.setKey(key);
					TaskModel model = GetModel.getTaskModel(sess,request.getUserPrincipal().getName());
					Task task = model.getMR(k);
					tf.setTaskNumber(task.getTaskNumber().toString());
					tf.setState(task.getState());
					tf.setType(task.getType());
					//tf.setPriority(task.getPriority());
					tf.setPersonAssigned(task.getPersonAssigned());
					tf.setCategory(task.getCategory());
					tf.setDescription(task.getDescription());
					tf.setResolution(task.getResolution());
					java.util.Date bd = task.getStartDate();
					if (bd != null) {
						DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
						tf.setStartDate(df.format(bd));
					}
					bd = task.getDueDate();
					if (bd != null) {
						DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
						tf.setDueDate(df.format(bd));
					}
					
					java.util.Collection states = model.getTaskTypes().nextStates(task.getState(),task.getType());
					sess.setAttribute("task_states", states );
					// tf.setStates(states);
					return (mapping.findForward("success"));
					
				}
			} else if (cmd.endsWith("/saveTask") || cmd.endsWith("/saveNewTask")) {
				
				TaskModel model = GetModel.getTaskModel(sess,request.getUserPrincipal().getName());
				int k = Integer.parseInt(tf.getKey());
				
				// add new task
				Task task = model.newMR();
				task.setType( tf.getType());
				//task.setPriority( tf.getPriority());
				task.setPersonAssigned( tf.getPersonAssigned());
				task.setDescription( tf.getDescription());
				task.setCategory( tf.getCategory());
				if( k == -1 )
					task.setState("OPEN");
				else
					task.setState(tf.getState());
				task.setTaskNumber(new Integer(k));
				String sd = tf.getStartDate();
				if( sd != null && !sd.equals(""))
				{
					DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
					task.setStartDate(df.parse(sd));
				}
				else
				{
					task.setStartDate( new java.util.Date());
				}
				String dd = tf.getDueDate();
				if( dd != null && !dd.equals(""))
				{
					DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
					task.setDueDate(df.parse(dd));
				}
				
				model.savetask(task);
				return( mapping.findForward("success"));
				
				
			} else if (cmd.endsWith("/delTask")) {
				// determine if this is add, edit, delete
				String key = request.getParameter("key");
				if (key != null) {
					
					TaskModel model = GetModel.getTaskModel(sess,request.getUserPrincipal().getName());
					int k = Integer.parseInt(key);
					model.delete(k);
					
					return (mapping.findForward("success"));
					
				}
			} else if (cmd.endsWith("/newTask")) {				
				
				TaskModel model = GetModel.getTaskModel(sess,request.getUserPrincipal().getName());
				
				TaskTypes types = model.getTaskTypes();
				sess.setAttribute("task_types", types.getTaskTypes() );
				
				tf.setState("OPEN");
				tf.setKey("-1");
				return (mapping.findForward("success"));
				
			}
			
		} catch (Exception e) {
			throw new ServletException(EH.stackTrace(e));
		}
		
		return (mapping.findForward("failure"));
		



}
}

