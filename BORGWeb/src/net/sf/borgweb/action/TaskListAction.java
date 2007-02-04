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
 * Copyright 2004 by ==Quiet==
 */
package net.sf.borgweb.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.model.Task;
import net.sf.borg.model.TaskModel;
import net.sf.borgweb.util.EH;
import net.sf.borgweb.util.GetModel;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class TaskListAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		try {

			// get model from session or create
			HttpSession sess = request.getSession();
			Errmsg.console(true);
			TaskModel model = GetModel.getTaskModel(sess,request.getUserPrincipal().getName());

			Collection tasks = model.getTasks();

			// check filtering - default is OPEN only
			String filter = request.getParameter("filter");
			if (filter == null || !filter.equals("all")) {
				ArrayList filteredTasks = new ArrayList();

				Iterator it = tasks.iterator();
				while (it.hasNext()) {
					Task task = (Task) it.next();
					
					if (task.getState() != null && !task.getState().equals("CLOSED")) {
						filteredTasks.add(task);
					}
				}

				tasks = filteredTasks;
			}

			sess.setAttribute("tasks", tasks);
		} catch (Exception e) {
			throw new ServletException(EH.stackTrace(e));
		}

		return (mapping.findForward("success"));

	}
}

