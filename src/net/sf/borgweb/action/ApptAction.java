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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
import net.sf.borgweb.form.AppointmentForm;
import net.sf.borgweb.util.EH;
import net.sf.borgweb.util.GetModel;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ApptAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		
		HttpSession sess = request.getSession();
		AppointmentForm af = (AppointmentForm) form;
		// System.out.println(mapping.getPath());
		String cmd = mapping.getPath();
		try {
			
			if (cmd.endsWith("/editAppt")) {
				// determine if this is add, edit, delete
				String key = request.getParameter("key");
				if (key != null) {
					
					// TODO load data from db into form
					int k = Integer.parseInt(key);
					
					af.setKey(key);
					AppointmentModel model = GetModel.getAppointmentModel(sess,request.getUserPrincipal().getName());
					Appointment appt = model.getAppt(k);

					af.setCategory(appt.getCategory());


					return (mapping.findForward("success"));
					
				}
			} else if (cmd.endsWith("/saveAppt") ) {
				
				AppointmentModel model = GetModel.getAppointmentModel(sess,request.getUserPrincipal().getName());
				int k = Integer.parseInt(af.getKey());
				
				// add new Appointment
				Appointment appt = model.newAppt();

				
				model.saveAppt(appt, (k == -1) ? true : false);
				return( mapping.findForward("success"));
				
				
			} else if (cmd.endsWith("/delAppt")) {
				// determine if this is add, edit, delete
				String key = request.getParameter("key");
				if (key != null) {
					
					AppointmentModel model = GetModel.getAppointmentModel(sess,request.getUserPrincipal().getName());
					int k = Integer.parseInt(key);
					model.delAppt(k);
					
					return (mapping.findForward("success"));
					
				}
			} else if (cmd.endsWith("/newAppt")) {				
				
				GetModel.getAppointmentModel(sess,request.getUserPrincipal().getName());
				
				af = new AppointmentForm();
				af.setKey("-1");
				return (mapping.findForward("success"));
				
			}
			
		} catch (Exception e) {
			throw new ServletException(EH.stackTrace(e));
		}
		
		return (mapping.findForward("failure"));
		



}
}

