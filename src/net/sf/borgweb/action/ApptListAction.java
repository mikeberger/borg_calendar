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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.borg.model.Appointment;
import net.sf.borg.model.Day;
import net.sf.borgweb.form.AppointmentForm;
import net.sf.borgweb.form.MonthDTO;
import net.sf.borgweb.util.EH;
import net.sf.borgweb.util.GetModel;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ApptListAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		try {

			// get model from session or create
			HttpSession sess = request.getSession();
			
			// need to force load of models - the Day object assumes they are loaded
			GetModel.getAppointmentModel(sess,request.getUserPrincipal().getName());
			GetModel.getAddressModel(sess,request.getUserPrincipal().getName());
			GetModel.getTaskModel(sess,request.getUserPrincipal().getName());
			GregorianCalendar cal = new GregorianCalendar();
			String m = request.getParameter("month");
			String y = request.getParameter("year");
			int month = 0;
			int year = 0;
			if( m == null || y == null )
			{			    
		        month = cal.get(Calendar.MONTH);
		        year = cal.get(Calendar.YEAR);
			}
			else
			{
				month = Integer.parseInt(m);
				year = Integer.parseInt(y);
			}
			
	        // set cal to day 1 of month
            cal.set( year, month, 1 );
            
            // set month title
            SimpleDateFormat df = new SimpleDateFormat("MMMM yyyy");
            MonthDTO mdto = new MonthDTO();
            mdto.setTitle( df.format(cal.getTime()) );
            
            // get day of week of day 1
            int fd = cal.get( Calendar.DAY_OF_WEEK ) - cal.getFirstDayOfWeek();
            if( fd == -1 ) fd = 6;
            mdto.setFirstDay(fd);
            
            // get last day of month
            int ld = cal.getActualMaximum( Calendar.DAY_OF_MONTH );
			mdto.setLastDay(ld);
			
			Collection days[] = new Collection[31];
					
	        for( int i = 1; i < ld; i++ ) {
                Day di = Day.getDay( year, month, i, true,true,true);
                Collection appts = di.getAppts();
                days[i-1] = new ArrayList();
                if( appts != null ) {
                    Iterator it = appts.iterator();
                    
                    // iterate through the day's appts
                    while( it.hasNext() ) {                       
                        Appointment info = (Appointment) it.next();
                        AppointmentForm af = new AppointmentForm();
                        af.setKey(Integer.toString(info.getKey()));
                        af.setText(info.getText());
                        days[i-1].add(af);
                    }
                }
	        }
	        mdto.setDays(days);
			sess.setAttribute("monthDTO", mdto);
			
		} catch (Exception e) {
			throw new ServletException(EH.stackTrace(e));
		}

		return (mapping.findForward("success"));

	}
}

