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
 * Copyright 2003 by ==Quiet==
 */
package net.sf.borg.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TreeSet;

import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;

// This class determines the logical layout of appointment boxes within a
// day given that appointments can overlap
public class ApptDayBoxLayout {

	// ApptDayBox holds the logical information needs to determine
	// how an appointment box should be drawn in a day grid
	public class ApptDayBox {

		private boolean placed = false; // whether or not this box has been

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

		private int numOverLap = 0; // number of appts overlapping this one

		private Appointment appt = null;

		public double getLeft() {
			return left;
		}

		public void setLeft(double left) {
			this.left = left;
		}

		public double getTop() {
			return top;
		}

		public void setTop(double top) {
			this.top = top;
		}

		public double getBottom() {
			return bottom;
		}

		public void setBottom(double bottom) {
			this.bottom = bottom;
		}

		public double getRight() {
			return right;
		}

		public void setRight(double right) {
			this.right = right;
		}

		public boolean isPlaced() {
			return placed;
		}

		public void setPlaced(boolean placed) {
			this.placed = placed;
		}

		public boolean isOutsideGrid() {
			return outsideGrid;
		}

		public void setOutsideGrid(boolean outsideGrid) {
			this.outsideGrid = outsideGrid;
		}

		public Appointment getAppt() {
			return appt;
		}

		public void setAppt(Appointment appt) {
			this.appt = appt;
		}

		public void setNumOverLap(int numOverLap) {
			this.numOverLap = numOverLap;
		}

		public int getNumOverLap() {
			return numOverLap;
		}
	}

	private TreeSet boxes = new TreeSet(new boxcompare()); // ApptDayBox objects sorted by number of overlaps

	// compare boxes by number of overlaps
    private static class boxcompare implements Comparator
    {
        
        public int compare(java.lang.Object obj, java.lang.Object obj1)
        {
            ApptDayBox so1 = (ApptDayBox)obj;
            ApptDayBox so2 = (ApptDayBox)obj1;
            int diff = so1.getNumOverLap() - so2.getNumOverLap();
            if( diff != 0 ) return( diff );
            
            
            return( 1 );
        }
        
    }
	// create an ApptDatBoxLayout and layout the appts
	public ApptDayBoxLayout(Collection appts, int starthr, int endhr) {

		double startmin = starthr * 60;
		double endmin = endhr * 60;

		
		ArrayList list = new ArrayList();
		// initialize the boxes
		Iterator it = appts.iterator();
		while (it.hasNext()) {

			Appointment ap = (Appointment) it.next();
			Date d = ap.getDate();
			if (d == null)
				continue;

			// determine appt start and end minutes
			GregorianCalendar acal = new GregorianCalendar();
			acal.setTime(d);
			double apstartmin = 60 * acal.get(Calendar.HOUR_OF_DAY)
					+ acal.get(Calendar.MINUTE);
			int dur = 14;
			Integer duri = ap.getDuration();
			if (duri != null && duri.intValue() > 15) {
				dur = duri.intValue() - 1;
			}
			double apendmin = apstartmin + dur;

			// initialize the box
			ApptDayBox box = new ApptDayBox();
			box.setAppt(ap);

			// check if appt will fall in the grid
			if (AppointmentModel.isNote(ap) || apendmin < startmin
					|| apstartmin >= endmin - 4) {
				box.setOutsideGrid(true);
			} else {
				if (apstartmin < startmin)
					apstartmin = startmin;
				if (apendmin > endmin)
					apendmin = endmin;
				box.setTop((apstartmin - startmin) / (endmin - startmin));
				box.setBottom((apendmin - startmin) / (endmin - startmin));
			}

			list.add(box);

		}

		// determine the overlaps for each appt
		Iterator it1 = list.iterator();
		while (it1.hasNext()) {
			ApptDayBox curBox = (ApptDayBox) it1.next();
			if (curBox.isOutsideGrid())
				continue;

			Iterator it2 = list.iterator();
			while (it2.hasNext()) {
				ApptDayBox otherBox = (ApptDayBox) it2.next();
				if (otherBox == curBox)
					continue;
				if (otherBox.isOutsideGrid())
					continue;

				// detect overlap
				if (otherBox.getTop() > curBox.getBottom()
						|| otherBox.getBottom() < curBox.getTop()) {
					// no overlap
					continue;
				}

				// otherBox overlaps curBox
				curBox.setNumOverLap(curBox.getNumOverLap() + 1);
			}
		}
		
		// sort the list
		boxes.addAll(list);

		// determine left and right for each box by placing boxes on the grid
		// one at a time
		it1 = boxes.iterator();
		while (it1.hasNext()) {
			// curBox is the one we are trying to place in the grid
			ApptDayBox curBox = (ApptDayBox) it1.next();
			if (curBox.isOutsideGrid())
				continue;

			Iterator it2 = boxes.iterator();
			double maxRightOfPlaced = 0; // farthest right edge of any placed
										 // appts that overlap the current
			while (it2.hasNext()) {
				ApptDayBox otherBox = (ApptDayBox) it2.next();
				if (otherBox == curBox)
					continue;
				if (otherBox.isOutsideGrid())
					continue;

				// detect overlap
				if (otherBox.getTop() > curBox.getBottom()
						|| otherBox.getBottom() < curBox.getTop()) {
					// no overlap
					continue;
				}

				if (otherBox.isPlaced()
						&& otherBox.getRight() > maxRightOfPlaced) {
					maxRightOfPlaced = otherBox.getRight();
				}
			}

			curBox.setLeft(maxRightOfPlaced);
			curBox.setRight(curBox.getLeft() + (1 / (1+(double)curBox.getNumOverLap())));
			curBox.setPlaced(true);
		}

	}

	// get the boxes to be used to draw the appt grid
	Collection getBoxes() {
		return (boxes);
	}

}