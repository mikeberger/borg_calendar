package net.sf.borgweb.util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.TaskModel;

public class GetModel {

	static public AddressModel getAddressModel( HttpSession sess, String user ) throws Exception
	{
		AddressModel am = (AddressModel) sess.getAttribute("address_model");
		if (am == null) {
			am = AddressModel.create();
			sess.setAttribute("address_model", am);
			String url = sess.getServletContext().getInitParameter("dburl");
			am.open_db(url,user,false,false);
		}
		return am;
	}
	
	static public AppointmentModel getAppointmentModel( HttpSession sess, String user ) throws Exception
	{
		AppointmentModel am = (AppointmentModel) sess.getAttribute("appointment_model");
		if (am == null) {
			am = AppointmentModel.create();
			sess.setAttribute("appointment_model", am);
			String url = sess.getServletContext().getInitParameter("dburl");
			am.open_db(url,user,false,false);
		}
		return am;
	}
	
	static public TaskModel getTaskModel( HttpSession sess, String user ) throws Exception
	{
		TaskModel tm = (TaskModel) sess.getAttribute("task_model");
		if (tm == null) {
			tm = TaskModel.create();
			sess.setAttribute("task_model", tm);
			String url = sess.getServletContext().getInitParameter("dburl");
			if( url == null )
				throw new ServletException( "URL is null ");
			tm.open_db(url,user,false,false);
		}
		return tm;
	}
}
