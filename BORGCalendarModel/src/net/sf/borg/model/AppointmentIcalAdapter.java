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
 
Copyright 2003 by ==Quiet==
 */
package net.sf.borg.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.CategoryList;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.sf.borg.common.io.IOHelper;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Warning;


public class AppointmentIcalAdapter {
	static public void exportIcal(String filename) throws Exception {
		
		ComponentList clist = new ComponentList();	
		boolean showpriv = false;
		if( Prefs.getPref(PrefName.SHOWPRIVATE).equals("true") )
			showpriv = true;
		boolean todo_as_ev = false;
		if( Prefs.getPref(PrefName.ICALTODOEV ).equals("true") )
			todo_as_ev = true;		
		Iterator it = AppointmentModel.getReference().getAllAppts().iterator();
		while( it.hasNext() )
		{
			CategoryList catlist = new CategoryList();
			Appointment ap = (Appointment) it.next();
			Component ve = null;
			if( ap.getTodo() && !todo_as_ev)
			{
				ve = new VToDo();
			}
			else if( ap.getTodo())
			{
				ve = new VEvent();
				catlist.add("ToDo");
			}
			else
			{
				ve = new VEvent();
			}
			
			// unique-id
			String hostname = "";
			try {
		        InetAddress addr = InetAddress.getLocalHost();
		    
		        // Get hostname
		        hostname = addr.getHostName();
		    } catch (UnknownHostException e) {
		    }
			String uidval = String.valueOf(ap.getKey()) + "@" + hostname;
			Uid uid = new Uid(uidval);
			ve.getProperties().add(uid);
			
			// add text
			String appttext = ap.getText();
			Summary sum = null;
			Description desc = null;

            int ii = appttext.indexOf('\n');
            if( ii != -1 )
            {
                sum = new Summary(appttext.substring(0,ii));
                desc = new Description( appttext.substring(ii+1));
            }
            else
            {
                sum = new Summary(appttext);
            }
            
			ve.getProperties().add(sum);
			if( desc != null )
			{
			    ve.getProperties().add(desc);
			}
			
			// date
			if( AppointmentModel.isNote(ap))
			{
				ParameterList pl = new ParameterList();
				pl.add(new Value(Value.DATE));
				DtStart dts = new DtStart(pl, ap.getDate());
				ve.getProperties().add(dts);
			}
			else
			{
				DtStart dts = new DtStart(ap.getDate());
				ve.getProperties().add(dts);
			}
			
			// duration
			if( ap.getDuration() != null )
			{
				ve.getProperties().add( new Duration( ap.getDuration().intValue()*60*1000));
			}
			
			// vacation is a category
			if( ap.getVacation() != null && ap.getVacation().intValue() != 0)
			{
				catlist.add( "Vacation");
			}
			
			// holiday is a category
			if( ap.getHoliday() != null && ap.getHoliday().intValue() != 0)
			{
				catlist.add( "Holidays");
			}
			
			// private
			if( ap.getPrivate() && !showpriv )
			{
				ve.getProperties().add( new Clazz(Clazz.PRIVATE));
			}
			
			// add color as a cetegory
			if( ap.getColor() != null && 
					( ap.getColor().equals("black") || ap.getColor().equals("blue") || ap.getColor().equals("green")
							|| ap.getColor().equals("red") || ap.getColor().equals("white") )
					)
			{
				catlist.add( ap.getColor() );
			}
			
			if( ap.getCategory() != null && !ap.getCategory().equals(""))
			{
				catlist.add( ap.getCategory() );
			}
			
			if( !catlist.isEmpty() )
			{
				ve.getProperties().add( new Categories(catlist) );
			}
			
			// repeat stuff
			if(ap.getRepeatFlag())
			{
				// build recur string
				String rec = "FREQ=";
				String freq = ap.getFrequency();
				if( freq == null )
				{
					continue;
				}
				if( freq.equals("daily"))
				{
					rec += "DAILY";
				}
				else if( freq.equals("weekly"))
				{
					rec += "WEEKLY";
				}
				else if( freq.equals("biweekly"))
				{
					rec += "WEEKLY;INTERVAL=2";
				}
				else if( freq.equals("monthly"))
				{
					Date dd = ap.getDate();
					GregorianCalendar gc = new GregorianCalendar();
					gc.setTime(dd);
					rec += "MONTHLY;BYMONTHDAY=" + gc.get(GregorianCalendar.DATE);
				}
				else if( freq.equals("yearly"))
				{
					rec += "YEARLY";
				}
				else
				{
					// bad default - need to fix
					rec += "DAILY";
				}
				
				if( ap.getTimes().intValue() != 9999 )
				{
					rec += ";COUNT=" + ap.getTimes();
				}
				//System.out.println(rec);
				
				ve.getProperties().add( new RRule( new Recur(rec)));

			}
			clist.add(ve);
			
		}
		
		PropertyList pl = new PropertyList();
		pl.add(new ProdId("BORG Calendar"));
		pl.add(new net.fortuna.ical4j.model.property.Version("1","4"));
		net.fortuna.ical4j.model.Calendar cal = new net.fortuna.ical4j.model.Calendar( pl, clist );
		

		cal.validate();

		OutputStream oostr = IOHelper.createOutputStream(filename);
		CalendarOutputter op = new CalendarOutputter();
		op.output( cal, oostr );
		oostr.close();
	}
	
	static public String importIcal( String file ) throws Exception, Warning
	{
		StringBuffer warning = new StringBuffer();
		CalendarBuilder builder = new CalendarBuilder();
		InputStream is = IOHelper.openStream(file);
		Calendar cal = builder.build(is);
		is.close();
		
		cal.validate();

		ArrayList aplist = new ArrayList();
		
		AppointmentModel amodel = AppointmentModel.getReference();
		ComponentList clist = cal.getComponents();
		Iterator it = clist.iterator();
		while( it.hasNext())
		{
			Component comp = (Component) it.next();
			if( comp instanceof VEvent || comp instanceof VToDo)
			{
				Appointment ap = amodel.newAppt();
				PropertyList pl = comp.getProperties();
				String appttext = "";
				String summary = "";
				Property prop = pl.getProperty(Property.SUMMARY);
				if( prop != null )
				{
					summary = prop.getValue();
					appttext += prop.getValue();
				}
				
				prop = pl.getProperty(Property.DESCRIPTION);
				if( prop != null )
				{
					appttext += "\n" + prop.getValue();
				}
				
				ap.setText(appttext);
				prop = pl.getProperty(Property.DTSTART);
				if( prop != null)
				{
					DtStart dts = (DtStart) prop;
					Date d = dts.getTime();
					ap.setDate(d);
		            
				}
				
				if( comp instanceof VToDo )
				{
					ap.setTodo(true);
				}
				
				prop = pl.getProperty( Property.DURATION );
				if( prop != null )
				{
					Duration dur = (Duration ) prop;
					int durmin = (int)dur.getDuration() / 60000;
					// skip the the duration if >= 1 day
					// not much else we can do about it right now without getting
					// really complicated
					if( durmin < (24*60))
					{						
						ap.setDuration( new Integer( durmin)  );
					}
					else if( durmin > (24*60))
					{
						warning.append("WARNING: Cannot handle duration of [" + durmin + "] minutes for appt [" + summary + "], using 0\n" );
					}
					
				}
				
				prop = pl.getProperty( Property.CATEGORIES );
				if( prop != null )
				{
					Categories cats = (Categories) prop;
					CategoryList catlist = cats.getCategories();
					Iterator cit = catlist.iterator();
					while( cit.hasNext() )
					{
						String cat = (String) cit.next();
						if( cat.equals("Holidays"))
						{
							ap.setHoliday(new Integer(1));
						}
						else if( cat.equals("Vacation"))
						{
							ap.setVacation(new Integer(1));
						}
						else if( cat.equals("ToDo"))
						{
							ap.setTodo(true);
						}
						else if( cat.equals("black") | cat.equals("red") || cat.equals("green")
									|| cat.equals("blue") || cat.equals("white"))
						{
							ap.setColor(cat);
						}
						else
						{
							ap.setCategory(cat);					
						}
					}
				}
				
				prop = pl.getProperty( Property.CLASS );
				if( prop != null )
				{
					Clazz clazz = (Clazz) prop;
					if( clazz.getValue().equals(Clazz.PRIVATE))
					{
						ap.setPrivate(true);
					}
				}
				
				prop = pl.getProperty( Property.RRULE );
				if( prop != null )
				{
					RRule rr = (RRule) prop;
					Recur recur = rr.getRecur();

					Date until = recur.getUntil();
					if( until != null )
					{
						warning.append("ERROR: BORG cannot yet handle UNTIL clause for appt [" + summary + "], skipping\n" );
						continue;
					}
					int times = recur.getCount();
					if( times < 1 )
						times = 9999;
					ap.setTimes( new Integer(times));
	
					ap.setRepeatFlag(true);
					String freq = recur.getFrequency();
					int interval = recur.getInterval();
					if( freq.equals("DAILY"))
					{
						ap.setFrequency("daily");
					}
					else if( freq.equals("WEEKLY"))
					{
						if( interval == 2)
						{
							ap.setFrequency("biweekly");
						}
						else
						{
							ap.setFrequency("weekly");
						}
					}
					else if( freq.equals("MONTHLY"))
					{
						ap.setFrequency("monthly");
					}
					else if( freq.equals("YEARLY"))
					{
						ap.setFrequency("yearly");
					}
					else
					{
						warning.append("ERROR: Cannot handle frequency of [" + freq + "], for appt [" + summary + "], skipping\n" );
						continue;
					}
					
					if( recur.getMonthList() != null)
					{
						// we can handle a month list of 1 month if we are repeating yearly
						if( recur.getMonthList().size() > 1 || !ap.getFrequency().equals("yearly"))
						{
							warning.append("ERROR: BORG cannot yet handle the BYMONTH clause for appt [" + summary + "], skipping\n" );
							continue;
						}
					}
					
					if( recur.getDayList() != null )
					{
						warning.append("ERROR: BORG cannot yet handle BYDAY clause for appt [" + summary + "], skipping\n" );
						continue;
					}
					
					if( recur.getMonthDayList() != null )
					{
						warning.append("ERROR: BORG cannot yet handle BYMONTHDAY clause for appt [" + summary + "], skipping\n" );
						continue;
					}
					
					if( recur.getYearDayList() != null )
					{
						warning.append("ERROR: BORG cannot yet handle BYYEARDAY clause for appt [" + summary + "], skipping\n" );
						continue;
					}
					
					if( recur.getWeekNoList() != null )
					{
						warning.append("ERROR: BORG cannot yet handle BYWEEKNO clause for appt [" + summary + "], skipping\n" );
						continue;
					}
					
				}

				//amodel.saveAppt(ap, true);
				aplist.add(ap);
			}
		}
		
		 amodel.bulkAdd(aplist);
		 
		 if( warning.length() == 0)
		 	return( null );
		 else
		 	return( warning.toString());
		
	}
}
