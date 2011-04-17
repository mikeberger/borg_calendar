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
package net.sf.borg.ui.calendar;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Day;
import net.sf.borg.model.Model;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.CalendarEntity;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.CalendarModule;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.NavPanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * MonthPanel is the UI for a calendar month. It is Printable, but is NOT used
 * for printing a monthly calendar as borg has a more specialized month printing
 * class.
 */
public class MonthPanel extends JPanel implements Printable, CalendarModule {

	private static final long serialVersionUID = 1L;

	/**
	 * MonthViewSubPanel draws a month and provides the UI container for the
	 * month items
	 */
	private class MonthViewSubPanel extends ApptBoxPanel implements Printable,
			NavPanel.Navigator, Model.Listener, Prefs.Listener,
			MouseWheelListener {

		private static final long serialVersionUID = 1L;

		// number of boxes on the calendar - 6 rows of 7 days
		final private int numBoxes = 42;

		// width of a column of days
		private int colwidth;

		// colors of the days
		Color colors[] = new Color[numBoxes];

		// top of a day - including the day label
		private int daytop;

		// records the last date on which a draw took place - for handling the
		// first redraw after midnight
		private int lastDrawDate = -1;

		// the month being displayed
		private int month_;
		private int year_;

		// flag to indicate if we need to reload from the model. If false, we
		// can just redraw
		// from cached data
		boolean needLoad = true;

		// height of a row of days
		private int rowheight;

		/**
		 * constructor
		 */
		public MonthViewSubPanel() {

			// react to pref changes
			Prefs.addListener(this);

			// react to mouse wheel events
			addMouseWheelListener(this);

			// react to task or appt changes
			AppointmentModel.getReference().addListener(this);
			TaskModel.getReference().addListener(this);
			
			goTo(new GregorianCalendar());

		}

		/**
		 * clear cached data and force reload on next draw
		 */
		public void clearData() {
			clearBoxes();
			needLoad = true;
			setToolTipText(null);
		}

		/**
		 * draw month into a Graphics
		 */
		private int drawIt(Graphics g, double width, double height,
				double pageWidth, double pageHeight, double pagex,
				double pagey) {

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);  

			// appt text font
			Font sm_font = Font.decode(Prefs.getPref(PrefName.APPTFONT));

			// draw a white background
			g2.setColor(Color.white);
			g2.fillRect(0, 0, (int) width, (int) height);
			g2.setColor(Color.black);

			// get font sizes
			int fontHeight = g2.getFontMetrics().getHeight();
			int fontDesent = g2.getFontMetrics().getDescent();

			// translate coordinates based on the page margins
			g2.translate(pagex, pagey);

			Shape s = g2.getClip();

			// get current time
			GregorianCalendar now = new GregorianCalendar();
			int tmon = now.get(Calendar.MONTH);
			int tyear = now.get(Calendar.YEAR);
			int tdate = now.get(Calendar.DATE);

			// force reload if the date has changed since the last draw
			if (lastDrawDate != tdate) {
				needLoad = true;
			}
			lastDrawDate = tdate;

			// get first day of the month and set first day of week based on
			// user pref
			GregorianCalendar cal = new GregorianCalendar(year_, month_, 1);
			cal.setFirstDayOfWeek(Prefs.getIntPref(PrefName.FIRSTDOW));

			// top of drawn month - used to be non-zero to fit a title
			int caltop = 0;

			// set top of the day - which is under the weekday name
			daytop = caltop + fontHeight + fontDesent;

			// set width of the week button on the right edge of each week
			int weekbutwidth = fontHeight + fontDesent;

			// calculate width and height of day boxes (6x7 grid)
			rowheight = ((int) pageHeight - daytop) / 6;
			colwidth = (int) (pageWidth - weekbutwidth) / 7;

			// calculate the bottom and right edge of the grid
			int calbot = 6 * rowheight + daytop;
			int calright = 7 * colwidth;

			// allow items to be dragged all over the calendar (but not over the
			// weekday labels or week buttons
			setDragBounds(daytop, calbot, 0, (int) pageWidth - weekbutwidth);

			// do not allow any resizing or dragging out of new appointments
			// since
			// the month UI does not have a time-grid
			setResizeBounds(0, 0);

			// draw background for weekday labels default color
			g2.setColor(this.getBackground());
			g2.fillRect(0, caltop, calright, daytop - caltop);
			g2.setColor(Color.black);

			// draw the weekday names centered in each column - no boxes drawn
			// yet
			SimpleDateFormat dfw = new SimpleDateFormat("EEE");
			cal.add(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek()
					- cal.get(Calendar.DAY_OF_WEEK));
			for (int col = 0; col < 7; col++) {
				int colleft = (col * colwidth);
				String dayofweek = dfw.format(cal.getTime());
				int swidth = g2.getFontMetrics().stringWidth(dayofweek);
				g2.drawString(dayofweek, colleft + (colwidth - swidth) / 2,
						caltop + fontHeight);
				cal.add(Calendar.DAY_OF_WEEK, 1);
			}

			// reset time to first day on calendar
			// *** this will likely be a day from the prior month
			// we do show prior month and next month days in any of the 42 boxes
			// not used for the current month
			cal.set(year_, month_, 1);
			int fdow = cal.get(Calendar.DAY_OF_WEEK) - cal.getFirstDayOfWeek();
			if (fdow == -1)
				fdow = 6;
			cal.add(Calendar.DATE, -1 * fdow);

			// draw the days - all 42!!
			for (int box = 0; box < numBoxes; box++) {

				// calculate column and row
				int boxcol = box % 7;
				int boxrow = box / 7;

				// calculate top left of the day area
				int rowtop = (boxrow * rowheight) + daytop;
				int colleft = boxcol * colwidth;

				// get day of week
				int dow = cal.getFirstDayOfWeek() + boxcol;
				if (dow == 8)
					dow = 1;

				// set small font for appt text
				g2.setFont(sm_font);
				int smfontHeight = g2.getFontMetrics().getHeight();

				// set clip to the day box to truncate long appointment text
				g2.clipRect(colleft, rowtop, colwidth, rowheight);

				// check if we need to reload form the model
				if (needLoad) {
					try {

						// add adate zone to this box for the date
						addDateZone(cal.getTime(), new Rectangle(colleft,
								rowtop, colwidth, rowheight));

						// get the appointment info for the given day
						Day dayInfo = Day.getDay(cal.get(Calendar.YEAR), cal
								.get(Calendar.MONTH), cal.get(Calendar.DATE));

						// set a different background color based on various
						// circumstances
						Color c = null;
						if (tmon == month_ && tyear == year_
								&& tdate == cal.get(Calendar.DATE)) {
							// day is today
							c = new Color(Prefs.getIntPref(PrefName.UCS_TODAY));
						} else if (dayInfo.getHoliday() != 0) {
							// holiday
							c = new Color(Prefs
									.getIntPref(PrefName.UCS_HOLIDAY));
						} else if (dayInfo.getVacation() == 1) {
							// full day vacation
							c = new Color(Prefs
									.getIntPref(PrefName.UCS_VACATION));
						} else if (dayInfo.getVacation() == 2) {
							// half-day vacation
							c = new Color(Prefs
									.getIntPref(PrefName.UCS_HALFDAY));
						} else if (dow == Calendar.SUNDAY
								|| dow == Calendar.SATURDAY) {
							// weekend
							c = new Color(Prefs
									.getIntPref(PrefName.UCS_WEEKEND));
						} else {
							// weekday
							c = new Color(Prefs
									.getIntPref(PrefName.UCS_WEEKDAY));
						}

						// if a day is not in the current month, then always use
						// the default
						// panel background
						if (cal.get(Calendar.MONTH) != month_)
							c = this.getBackground();

						// remember the color for when we redraw from cache
						colors[box] = c;

						// set initial Y coord for item text
						int notey = rowtop + smfontHeight;

						// loop through entities for the day
						for (CalendarEntity entity : dayInfo.getItems()) {

							// add the item NoteBox to the container
							if (addNoteBox(cal.getTime(), entity,
									new Rectangle(colleft + 2, notey,
											colwidth - 4, smfontHeight),
									new Rectangle(colleft, rowtop, colwidth,
											rowheight)) != null)

							{
								// increment Y coord for next note text
								notey += smfontHeight;
							}

						}

						// check if we clipped some appts - meaning that they do
						// not all fit vertically
						// in the day's box due to lack of room
						Icon clipIcon = null;
						if (notey > rowtop + rowheight) {
							// set clipping indication icon so that we dray it
							// next to the date label
							clipIcon = new ImageIcon(getClass().getResource(
									"/resource/Import16.gif"));
						}

						// add a label for the date. this is actually a button
						// box and the user
						// can press it to go to that date's day view
						String datetext = Integer.toString(cal
								.get(Calendar.DATE));
						if (Prefs.getPref(PrefName.DAYOFYEAR).equals("true"))
							datetext += "   [" + cal.get(Calendar.DAY_OF_YEAR)
									+ "]";
						boxes.add(new ButtonBox(cal.getTime(), datetext,
								clipIcon, new Rectangle(colleft + 2, rowtop,
										colwidth - 4, smfontHeight),
								new Rectangle(colleft, rowtop, colwidth,
										rowheight)) {
							@Override
							public void onClick() {
								MultiView.getMainView().setView(ViewType.DAY);
								GregorianCalendar gc = new GregorianCalendar();
								gc.setTime(getDate());
								MultiView.getMainView().goTo(gc);
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				// reset the clip or bad things happen
				g2.setClip(s);

				// fill box with background color
				g2.setColor(colors[box]);
				g2.fillRect(colleft, rowtop, colwidth, rowheight);

				// increment the day
				cal.add(Calendar.DATE, 1);
			}

			// add week buttons along the right side. they display the week
			// number
			// and will bring up the week view if pressed
			if (needLoad) {
				// use iso week numbering if option set
				if (Prefs.getPref(PrefName.ISOWKNUMBER).equals("true"))
					cal.setMinimalDaysInFirstWeek(4);
				else
					cal.setMinimalDaysInFirstWeek(1);
				for (int row = 0; row < 6; row++) {

					cal.set(year_, month_, 1 + 7 * row);
					int wk = cal.get(Calendar.WEEK_OF_YEAR);
					int rowtop = (row * rowheight) + daytop;
					boxes.add(new ButtonBox(cal.getTime(),
							Integer.toString(wk), null, new Rectangle(
									(int) pageWidth - weekbutwidth, rowtop,
									weekbutwidth, rowheight), new Rectangle(
									(int) pageWidth - weekbutwidth, rowtop,
									weekbutwidth, rowheight)) {
						@Override
						public void onClick() {
							MultiView.getMainView().setView(ViewType.WEEK);
							GregorianCalendar gc = new GregorianCalendar();
							gc.setTime(getDate());
							MultiView.getMainView().goTo(gc);
						}
					});
				}
			}

			needLoad = false;

			// draw all items
			drawBoxes(g2);
			g2.setClip(s);

			// draw the lines last

			// horizontal line at top of calendar - above day names
			g2.drawLine(0, caltop, calright, caltop);

			// horizontal lines for each row from below day names to bottom
			for (int row = 0; row < 7; row++) {
				int rowtop = (row * rowheight) + daytop;
				g2.drawLine(0, rowtop, calright, rowtop);
			}

			// vertcal lines
			for (int col = 0; col < 8; col++) {
				int colleft = (col * colwidth);
				g2.drawLine(colleft, caltop, colleft, calbot);
			}

			return Printable.PAGE_EXISTS;
		}

		/**
		 * return the date corresponding to the box that the given x/y
		 * coordinate is in
		 */
		@Override
		public Date getDateForCoord(double x, double y) {

			int col = (int) x / colwidth;
			int row = ((int) y - daytop) / rowheight;
			GregorianCalendar cal = new GregorianCalendar(year_, month_, 1);
			cal.setFirstDayOfWeek(Prefs.getIntPref(PrefName.FIRSTDOW));
			int fdow = cal.get(Calendar.DAY_OF_WEEK) - cal.getFirstDayOfWeek();
			if( fdow < 0 ) fdow += 7; // adjustment for fdow = MON and month starts on SUN
			cal.add(Calendar.DATE, -1 * fdow);
			cal.add(Calendar.DATE, row * 7 + col);
			return cal.getTime();
		}

		/**
		 * get the navigator label
		 */
		@Override
		public String getNavLabel() {
			SimpleDateFormat df = new SimpleDateFormat("MMMM yyyy");
			Calendar cal = new GregorianCalendar(year_, month_, 1);
			return df.format(cal.getTime());
		}

		/**
		 * go to a particular month
		 */
		@Override
		public void goTo(Calendar cal) {
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			clearData();
			repaint();
		}

		/**
		 * navigate forward or backward 1 month when mouse wheel moved
		 */
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getWheelRotation() > 0) {
				next();
				nav.setLabel(getNavLabel());
			} else if (e.getWheelRotation() < 0) {
				prev();
				nav.setLabel(getNavLabel());
			}

		}

		/**
		 * navigate forward 1 month
		 */
		@Override
		public void next() {
			GregorianCalendar cal = new GregorianCalendar(year_, month_, 1, 23,
					59);
			cal.add(Calendar.MONTH, 1);
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			clearData();
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			try {

				drawIt(g, getWidth(), getHeight(), getWidth() - 20,
						getHeight() - 20, 10, 10);

			} catch (Exception e) {
				// Errmsg.errmsg(e);
				e.printStackTrace();
			}
		}

		/**
		 * reload and redraw if prefs changed
		 */
		@Override
		public void prefsChanged() {
			clearData();
			repaint();

		}

		/**
		 * navigate backwards 1 month
		 */
		@Override
		public void prev() {
			GregorianCalendar cal = new GregorianCalendar(year_, month_, 1, 23,
					59);
			cal.add(Calendar.MONTH, -1);
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			clearData();
			repaint();
		}

		/**
		 * draw to Graphics for printing
		 */
		@Override
		public int print(Graphics g, PageFormat pageFormat, int pageIndex)
				throws PrinterException {

			if (pageIndex > 1)
				return Printable.NO_SUCH_PAGE;

			return (drawIt(g, pageFormat.getWidth(), pageFormat.getHeight(),
					pageFormat.getImageableWidth(), pageFormat
							.getImageableHeight(), pageFormat.getImageableX(),
					pageFormat.getImageableY()));
		}

		/**
		 * reload and redraw
		 */
		@Override
		public void refresh() {
			clearData();
			repaint();
		}
		
		@Override
		public void update(ChangeEvent event) {
			refresh();
		}

		/**
		 * go to current month
		 */
		@Override
		public void today() {
			GregorianCalendar cal = new GregorianCalendar();
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			clearData();
			repaint();
		}

	}

	// navigator panel
	private NavPanel nav = null;

	// month UI panel
	private MonthViewSubPanel monthSubPanel = null;

	/**
	 * constructor
	 */
	public MonthPanel() {

		// create the month UI panel
		monthSubPanel = new MonthViewSubPanel();

		// create the navigator panel
		nav = new NavPanel(monthSubPanel);

		setLayout(new java.awt.GridBagLayout());
		add(nav, GridBagConstraintsFactory
				.create(0, 0, GridBagConstraints.BOTH));
		add(monthSubPanel, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH, 1.0, 1.0));

	}

	/**
	 * go to a particular month
	 */
	@Override
	public void goTo(Calendar cal) {
		monthSubPanel.goTo(cal);
		nav.setLabel(monthSubPanel.getNavLabel());
	}

	/**
	 * print month to a Graphics
	 */
	@Override
	public int print(Graphics arg0, PageFormat arg1, int arg2)
			throws PrinterException {
		return monthSubPanel.print(arg0, arg1, arg2);
	}

	/**
	 * prints 1 or more months when the user requests a month print. This method
	 * currently redirects to the MonthPrintPanel object to do the actual
	 * drawing as the borg month printout is specialized for a paper printout
	 * and does not use the MonthPanel view for printing
	 */
	public void printMonths() {
		try {
			MonthPrintPanel.printMonths(monthSubPanel.month_,
					monthSubPanel.year_);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	@Override
	public void initialize(MultiView parent) {
		final MultiView par = parent;
		parent.addToolBarItem(new ImageIcon(getClass().getResource(
		"/resource/month.jpg")), getModuleName(), 
		new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				par.setView(ViewType.MONTH);
			}
		});
	}

	@Override
	public JPanel getComponent() {
		return this;
	}

	@Override
	public String getModuleName() {
		return Resource.getResourceString("Month_View");
	}
	
	@Override
	public void print() {
		printMonths();
	}
	
	@Override
	public ViewType getViewType() {
		return ViewType.MONTH;
	}

	
}
