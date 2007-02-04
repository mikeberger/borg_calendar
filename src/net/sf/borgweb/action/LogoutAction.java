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
import javax.servlet.*;
import javax.servlet.http.*;

import net.sf.borg.model.AddressModel;
import net.sf.borg.model.TaskModel;
import net.sf.borgweb.util.EH;

import org.apache.struts.action.*;

public class LogoutAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		try {

			HttpSession sess = request.getSession();
			AddressModel amodel = (AddressModel) sess.getAttribute("addr_model");
			if (amodel != null) {
				amodel.close_db();
				sess.setAttribute("addr_model", null);
			}
			TaskModel tmodel = (TaskModel) sess.getAttribute("task_model");
			if (tmodel != null) {
				tmodel.close_db();
				sess.setAttribute("task_model", null);
			}

			sess.invalidate();
			return (mapping.findForward("success"));
		} catch (Exception e) {
			throw new ServletException(EH.stackTrace(e));
		}
	}
}

