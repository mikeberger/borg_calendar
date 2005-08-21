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
 
Copyright 2003 by Mike Berger
 */
package net.sf.borg.model;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;




public class AppointmentVcalAdapter {
	
	
	static public void exportVcal(Writer w) throws Exception {
		
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		SimpleDateFormat untilformat = new SimpleDateFormat("yyyyMMdd");
		dateformat.setTimeZone(TimeZone.getTimeZone("UTC"));
		w.write("BEGIN:VCALENDAR\nVERSION:1.0\r\n");

		Iterator it = AppointmentModel.getReference().getAllAppts().iterator();
		while( it.hasNext() )
		{
			w.write("BEGIN:VEVENT\r\n");
			Appointment ap = (Appointment) it.next();
			
			
			// add text
			String appttext = ap.getText();
			
			// escape data
			appttext = appttext.replaceAll(";", "");

            int ii = appttext.indexOf('\n');
            if( ii != -1 )
            {
            	w.write("SUMMARY:"+appttext.substring(0,ii)+"\r\n");
            	String desc = appttext.substring(ii+1);
            	desc = desc.replaceAll("\n", "\r\n ");
            	w.write("DESCRIPTION:"+desc+"\r\n");
            }
            else
            {
            	w.write("SUMMARY:"+appttext+"\r\n"); 
            }
            

			// date
            w.write("DTSTART:"+dateformat.format(ap.getDate())+"\r\n");
            if( ap.getDuration() != null && ap.getDuration().intValue() != 0 )
            {	
            	long t = ap.getDate().getTime();
            	t += ap.getDuration().intValue()*60*1000;
            	w.write("DTEND:"+dateformat.format(new Date(t))+"\r\n");
            }
            else if( AppointmentModel.isNote(ap))
            {
                Date d = new Date();
                d.setTime( ap.getDate().getTime()+ 1000*60*60*24);
            	w.write("DTEND:"+dateformat.format(d)+"\r\n");
            }           
            else
            {
            	w.write("DTEND:"+dateformat.format(ap.getDate())+"\r\n");
            }
			
			
			// repeat stuff
			if(ap.getRepeatFlag())
			{
				// build recur string
				String rec = "RRULE:";
				String freq = Repeat.getFreq(ap.getFrequency());
				if( freq == null )
				{
				    w.write("END:VEVENT\r\n");
				    continue;
				}
				
				
				Date dd = ap.getDate();
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTime(dd);
				Calendar untilcal = Repeat.until(gc,freq,ap.getTimes().intValue());
				String until = untilformat.format(untilcal.getTime());
				

				if( freq.equals("daily"))
				{
					rec += "D1 " + until;
				}
				else if( freq.equals("weekly"))
				{
					rec += "D7 " + until;
				}
				else if( freq.equals("weekdays"))
				{
					rec += "W1 MO TU WE TH FR " + until;
				}
				else if( freq.equals("mwf"))
				{
					rec += "W1 MO WE FR " + until;
				}
				else if( freq.equals("tth"))
				{
					rec += "W1 TU TH " + until;
				}
				else if( freq.equals("weekends"))
				{
					rec += "W1 SA SU " + until;
				}
				else if( freq.equals("biweekly"))
				{
					rec += "D14 " + until;
				}
				else if( freq.equals("monthly"))
				{

					rec += "MD1 " + gc.get(java.util.Calendar.DATE) + " " + until;
				}
				else if( freq.equals("yearly"))
				{
					rec += "MD12 " + gc.get(java.util.Calendar.DATE) + " " + until;
				}
				else
				{
					// bad default - need to fix
					rec += "D1 " + until;
				}
				
				w.write(rec + "\r\n");

			}
			

			w.write("END:VEVENT\r\n");
		}
		
		w.write("END:VCALENDAR\r\n");
	}
	
	
}
