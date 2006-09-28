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
package net.sf.borg.ui;

import java.util.*;

import net.sf.borg.common.util.*;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.AppointmentXMLAdapter;

// This class determines the logical layout of appointment boxes within a
// day given that appointments can overlap
public class ApptDayBoxLayout
{

    // ApptDayBox holds the logical information needs to determine
    // how an appointment box should be drawn in a day grid
    static public class ApptDayBox implements ApptBoxPanel.BoxModel
    {

    	private Date date; // date being displayed - not necessarily date of appt
        private double startmin;
        private double endmin;
        private boolean isPlaced = false; // whether or not this box has been

        // "placed" in the grid yet
        private boolean outsideGrid = false; // flag to indicate an appt will
        // not fall in the grid at all

        // during the layout process
        private double left; // fraction of the available grid width at which

        // the right side of the box

        // should be drawn
        private double right; // fraction of the available grid width at which

        // the right side of the box should be drawn

        private double top; // fraction of the available grid height at which

        // the top side of the box should be drawn

        private double bottom; // fraction of the available grid height at which

        // the bottom side of the box should be drawn
 
        private int maxAcrossAtOneTime = 0; // max number of appts overlapping this one at a particular time
        

        private Appointment appt = null;

        private boolean isSelected = false;

        public ApptDayBox(Date d, double sm, double em)
        {
            startmin = sm;
            endmin = em;
            date = d;
        }

        public double getLeftAdjustment()
        {
            return left;
        }

        public void setLeftAdjustment(double left)
        {
            this.left = left;
        }

        public double getTopAdjustment()
        {
            return top;
        }

        public void setTopAdjustment(double top)
        {
            this.top = top;
        }

        public double getBottomAdjustment()
        {
            return bottom;
        }

        public void setBottomAdjustment(double bottom)
        {
            this.bottom = bottom;
        }

        public double getRightAdjustment()
        {
            return right;
        }

        public void setRightAdjustment(double right)
        {
            this.right = right;
        }

        public boolean isOutsideGrid()
        {
            return outsideGrid;
        }

        public void setOutsideGrid(boolean outsideGrid)
        {
            this.outsideGrid = outsideGrid;
        }

        public Appointment getAppt()
        {
            return appt;
        }

        public void setAppt(Appointment appt)
        {
            this.appt = appt;
        }

        public void setSelected(boolean isSelected)
        {
            this.isSelected = isSelected;
        }

        public boolean isSelected()
        {
            return isSelected;
        }

        public void resize(boolean isTop, double y_fraction) throws Exception
        {
            // calculate new start hour or duration and update appt
            double realtime = startmin + (endmin - startmin) * y_fraction;
            int hour = (int) (realtime / 60);
            int min = (int) (realtime % 60);
            // System.out.println(y_fraction);
            //System.out.println( "Resize to =" + hour +":" + min);

            if (isTop)
            {
                // get appt from DB - one cached here has time prepended to text by Day.getDayInfo()
                Appointment ap = AppointmentModel.getReference().getAppt(appt.getKey());
                Date oldTime = ap.getDate();
                GregorianCalendar newCal = new GregorianCalendar();
                newCal.setTime(oldTime);
                newCal.set(Calendar.HOUR_OF_DAY, hour);
                int roundMin = (min / 5) * 5;
                newCal.set(Calendar.MINUTE, roundMin);
                Date newTime = newCal.getTime();
                int newDur = ap.getDuration().intValue() + ((int) (oldTime.getTime() - newTime.getTime()) / (1000 * 60));
                //System.out.println( newTime.toString() + " " + newDur);
                if (newDur < 5)
                    return;
                ap.setDate(newTime);
                ap.setDuration(new Integer(newDur));
                AppointmentModel.getReference().saveAppt(ap, false);
            }
            else
            {
                //              get appt from DB - one cached here has time prepended to text by Day.getDayInfo()
                Appointment ap = AppointmentModel.getReference().getAppt(appt.getKey());
                Date start = ap.getDate();
                long endtime = start.getTime() + (60 * 1000) * ap.getDuration().intValue();
                Date oldEnd = new Date(endtime);
                //System.out.println("oldend=" + oldEnd);
                Calendar newEnd = new GregorianCalendar();
                newEnd.setTime(oldEnd);
                newEnd.set(Calendar.HOUR_OF_DAY, hour);
                int roundMin = (min / 5) * 5;
                newEnd.set(Calendar.MINUTE, roundMin);
                //System.out.println("newEnd=" + newEnd.getTime());
                //System.out.println("start=" + start.getTime());
                int newDur = (int) (newEnd.getTime().getTime() - start.getTime()) / (1000 * 60);
                //System.out.println( "newDur=" + newDur);
                if (newDur < 5)
                    return;
                ap.setDuration(new Integer(newDur));
                AppointmentModel.getReference().saveAppt(ap, false);
            }
        }

        public void move(double y_fraction) throws Exception
        {
            // calculate new start hour or duration and update appt
            double realtime = startmin + (endmin - startmin) * y_fraction;
            int hour = (int) (realtime / 60);
            int min = (int) (realtime % 60);

            // get appt from DB - one cached here has time prepended to text by Day.getDayInfo()
            Appointment ap = AppointmentModel.getReference().getAppt(appt.getKey());
            Date oldTime = ap.getDate();
            GregorianCalendar newCal = new GregorianCalendar();
            newCal.setTime(oldTime);
            newCal.set(Calendar.HOUR_OF_DAY, hour);
            int roundMin = (min / 5) * 5;
            newCal.set(Calendar.MINUTE, roundMin);
            Date newTime = newCal.getTime();
            ap.setDate(newTime);
            AppointmentModel.getReference().saveAppt(ap, false);

        }

        public void setText(String s)
        {
            // TODO Auto-generated method stub

        }

        public String getText()
        {
            return appt.getText();
        }

        public String getTextColor()
        {
            if (appt == null)
                return null;
            
            if ((appt.getColor() != null && appt.getColor().equals("strike"))
                    || (appt.getTodo() && !(appt.getNextTodo() == null || !appt.getNextTodo().after(date))))
            {
                return ("strike");
            }
            return appt.getColor();
        }

        public void edit()
        {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(appt.getDate());
            AppointmentListView ag = new AppointmentListView(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                    .get(Calendar.DATE));
            ag.showApp(appt.getKey());
            ag.setVisible(true);
        }

        public void create(double top, double bottom, String text)
        {
            // TODO Auto-generated method stub

        }

        public void delete()
        {
            AppointmentModel.getReference().delAppt(appt.getKey());
        }
    }

    static public class DateZone implements ApptBoxPanel.BoxModel
    {

        private Date date;
        private double startmin;
        private double endmin;

        public DateZone(Date d, double sm, double em)
        {
            this.date = d;
            startmin = sm;
            endmin = em;
        }

        public void resize(boolean b, double d)
        {
        }

        public void move(double d)
        {
        }

        public void setText(String s)
        {
        }

        public String getText()
        {
            return "";
        }

        public String getTextColor()
        {
            return "black";
        }

        public void edit()
        {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(date);
            AppointmentListView ag = new AppointmentListView(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                    .get(Calendar.DATE));
            ag.setVisible(true);
        }

        public double getBottomAdjustment()
        {
            return 0;
        }

        public double getRightAdjustment()
        {
            return 0;
        }

        public double getTopAdjustment()
        {
            return 0;
        }

        public double getLeftAdjustment()
        {
            return 0;
        }

        public boolean isOutsideGrid()
        {
            return false;
        }

        public boolean isSelected()
        {
            return false;
        }

        public void setSelected(boolean b)
        {
        }

        public void delete()
        {
        };

        public void create(double top, double bottom, String text)
        {

            //          get default appt values, if any
            Appointment appt = null;
            String defApptXml = Prefs.getPref(PrefName.DEFAULT_APPT);
            if (!defApptXml.equals(""))
            {
                try
                {
                    XTree xt = XTree.readFromBuffer(defApptXml);
                    AppointmentXMLAdapter axa = new AppointmentXMLAdapter();
                    appt = (Appointment) axa.fromXml(xt);

                }
                catch (Exception e)
                {
                    Errmsg.errmsg(e);
                    appt = null;
                }
            }

            if (appt == null)
            {
                appt = AppointmentModel.getReference().newAppt();
            }

            //System.out.println(top + " " + bottom);
            double realtime = startmin + (endmin - startmin) * top;
            int hour = (int) (realtime / 60);
            int min = (int) (realtime % 60);
            min = (min / 5) * 5;
            Calendar startCal = new GregorianCalendar();
            startCal.setTime(date);
            startCal.set(Calendar.HOUR_OF_DAY, hour);
            startCal.set(Calendar.MINUTE, min);
            appt.setDate(startCal.getTime());

            int dur = (int) ((bottom - top) * (endmin - startmin));
            dur = (dur / 5) * 5;
            appt.setDuration(new Integer(dur));
            appt.setText(text);
            AppointmentModel.getReference().saveAppt(appt, true);
        }

    }

    private TreeSet boxes = new TreeSet(new boxcompare()); // ApptDayBox objects sorted by number of overlaps

    // compare boxes by number of overlaps
    private static class boxcompare implements Comparator
    {

        public int compare(java.lang.Object obj, java.lang.Object obj1)
        {
            ApptDayBox so1 = (ApptDayBox) obj;
            ApptDayBox so2 = (ApptDayBox) obj1;
            int diff = so2.maxAcrossAtOneTime - so1.maxAcrossAtOneTime;
            if (diff != 0)
                return (diff);

            return (1);
        }

    }

    // create an ApptDatBoxLayout and layout the appts
    public ApptDayBoxLayout(Date displayDate, Collection appts, int starthr, int endhr)
    {

        double startmin = starthr * 60;
        double endmin = endhr * 60;

        ArrayList list = new ArrayList();
        // initialize the boxes
        Iterator it = appts.iterator();
        while (it.hasNext())
        {

            Appointment ap = (Appointment) it.next();
            Date d = ap.getDate();
            if (d == null)
                continue;

            // determine appt start and end minutes
            GregorianCalendar acal = new GregorianCalendar();
            acal.setTime(d);
            double apstartmin = 60 * acal.get(Calendar.HOUR_OF_DAY) + acal.get(Calendar.MINUTE);
            int dur = 0;
            Integer duri = ap.getDuration();
            if (duri != null )
            {
                dur = duri.intValue() - 1;
            }
            double apendmin = apstartmin + dur;

            // initialize the box
            ApptDayBox box = new ApptDayBox(displayDate, startmin, endmin);
            box.setAppt(ap);

            // check if appt will fall in the grid
            if (AppointmentModel.isNote(ap) || apendmin < startmin || apstartmin >= endmin - 4
                    || ap.getDuration() == null || ap.getDuration().intValue() == 0)
            {
                box.setOutsideGrid(true);
            }
            else
            {
                if (apstartmin < startmin)
                    apstartmin = startmin;
                if (apendmin > endmin)
                    apendmin = endmin;
                box.setTopAdjustment((apstartmin - startmin) / (endmin - startmin));
                box.setBottomAdjustment((apendmin - startmin) / (endmin - startmin));
            }

            list.add(box);

        }


        //       determine the overlaps for each appt
        for (int t = (int)startmin; t <= (int)endmin; t += 5)
        {
            ArrayList lst = new ArrayList();
            Iterator it1 = list.iterator();
            while (it1.hasNext())
            {
                
                ApptDayBox curBox = (ApptDayBox) it1.next();
                if (curBox.isOutsideGrid())
                    continue;
                
                Calendar cal = new GregorianCalendar();
                cal.setTime(curBox.appt.getDate());
                int amin = cal.get(Calendar.HOUR_OF_DAY)*60 + cal.get(Calendar.MINUTE);
                if( amin <= t && (amin + curBox.appt.getDuration().intValue()) > t)
                    lst.add(curBox);

            }
            
            //System.out.println( t + " " + lst.size());
            
            it1 = lst.iterator();
            while(it1.hasNext())
            {
                
                ApptDayBox curBox = (ApptDayBox) it1.next();
                //System.out.println(curBox.appt.getText());
                curBox.maxAcrossAtOneTime = Math.max(curBox.maxAcrossAtOneTime,lst.size());
            }
        }
                
        // sort the list
        boxes.addAll(list);

        // determine left and right for each box by placing boxes on the grid
        // one at a time
        Iterator it1 = boxes.iterator();
        while (it1.hasNext())
        {
            // curBox is the one we are trying to place in the grid
            ApptDayBox curBox = (ApptDayBox) it1.next();
            if (curBox.isOutsideGrid())
                continue;

            Iterator it2 = boxes.iterator();
            double maxRightOfPlaced = 0; // farthest right edge of any placed
            // appts that overlap the current
            while (it2.hasNext())
            {
                ApptDayBox otherBox = (ApptDayBox) it2.next();
                if (otherBox == curBox)
                    continue;
                if (otherBox.isOutsideGrid())
                    continue;

                // detect overlap
                if (otherBox.getTopAdjustment() > curBox.getBottomAdjustment()
                        || otherBox.getBottomAdjustment() < curBox.getTopAdjustment())
                {
                    // no overlap
                    continue;
                }

                if (otherBox.maxAcrossAtOneTime < curBox.maxAcrossAtOneTime)
                    otherBox.maxAcrossAtOneTime = curBox.maxAcrossAtOneTime;

                if (otherBox.isPlaced && otherBox.getRightAdjustment() > maxRightOfPlaced)
                {
                    maxRightOfPlaced = otherBox.getRightAdjustment();
                }
            }
            
            if( maxRightOfPlaced >= 0.999)
            {
                // cannot place here
                // need to fine a "hole" in the grid
                for( int slot = 0; slot < curBox.maxAcrossAtOneTime; slot++)
                {
                    
                    // check if appt is in this slot
                    boolean slotTaken = false;
                    Iterator pi = boxes.iterator();
                    while (pi.hasNext())
                    {
                        ApptDayBox otherBox = (ApptDayBox) pi.next();
                        if (otherBox == curBox)
                            continue;
                        if (otherBox.isOutsideGrid())
                            continue;
                        if( !otherBox.isPlaced)
                            continue;
                        if (otherBox.getTopAdjustment() > curBox.getBottomAdjustment()
                                || otherBox.getBottomAdjustment() < curBox.getTopAdjustment())
                        {
                            // no overlap
                            continue;
                        }
                        //System.out.println(slot + " " + curBox.maxAcrossAtOneTime + " " + curBox.appt.getText());
                        //System.out.println(slot + " " + otherBox.getLeftAdjustment() + " " + (double)slot/(double)otherBox.maxAcrossAtOneTime);
                        // determine if the appt is in this slot
                        if( Math.abs(otherBox.getLeftAdjustment() - ((double)slot)/(double)otherBox.maxAcrossAtOneTime) < 0.001)
                        {
                            slotTaken = true;
                            break;
                        }
                    }
                    
                    if( !slotTaken )
                    {
                        //System.out.println("yay" + slot + " " + curBox.maxAcrossAtOneTime + " " + curBox.appt.getText());
                        curBox.setLeftAdjustment((double)slot/(double)curBox.maxAcrossAtOneTime);
                        curBox.setRightAdjustment(curBox.getLeftAdjustment() + (1 / (double) curBox.maxAcrossAtOneTime));
                        curBox.isPlaced = true;
                        break;
                    }
                        
                }
                
            }

            if( curBox.isPlaced) continue;
            
            curBox.setLeftAdjustment(maxRightOfPlaced);
            curBox.setRightAdjustment(curBox.getLeftAdjustment() + (1 / (double) curBox.maxAcrossAtOneTime));
            curBox.isPlaced = true;
        }

    }

    // get the boxes to be used to draw the appt grid
    Collection getBoxes()
    {
        return (boxes);
    }

}