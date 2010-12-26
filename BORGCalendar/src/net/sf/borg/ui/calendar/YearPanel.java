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
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.PrintHelper;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Day;
import net.sf.borg.model.Model;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.CalendarEntity;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.NavPanel;
import net.sf.borg.ui.MultiView.CalendarModule;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * YearPanel is the Year UI. It shows all days of the year and allows navigation to months, days, and weeks.
 * It gives indications of the presence of appointments, but not individual appt details
 */
public class YearPanel extends DockableView implements Printable, CalendarModule {

	private static final long serialVersionUID = 1L;

	/**
	 * YearViewSubPanel is the panel that draws the year UI
	 */
	private class YearViewSubPanel extends ApptBoxPanel implements Printable, NavPanel.Navigator, Model.Listener, Prefs.Listener,
			MouseWheelListener {
		
		private static final long serialVersionUID = 1L;

		// number of day boxes - 37 per month. not all are used. 
		final private int numBoxes = 37 * 12;
		
		// cached day box colors
		Color colors[] = new Color[numBoxes];

		// width of a day column
		private int colwidth;

		// top of year UI - under the day of week labels
		private int gridtop;

		// records the last date on which a draw took place - for handling the first redraw after midnight
		private int lastDrawDate = -1;

		// flag to indicate if we need to reload from the model. If false, we can just redraw
		// from cached data
		boolean needLoad = true;

		// height of a month row
		private int rowheight;

		// year being shown
		private int year_;

		/**
		 * constructor
		 */
		public YearViewSubPanel() {
			
			// react to pref changes
			Prefs.addListener(this);
			
			// react to mouse wheel actions
			addMouseWheelListener(this);
			
			// react to appointment mode changes
			AppointmentModel.getReference().addListener(this);
			
			goTo(new GregorianCalendar());
		}

		/**
		 * clear cached data - will reload on next draw
		 */
		public void clearData() {
			clearBoxes();
			needLoad = true;
			setToolTipText(null);
		}

		/**
		 * draw the year UI
		 */
		private int drawIt(Graphics g, double width, double height, double pageWidth, double pageHeight, double pagex,
				double pagey, Font sm_font) {

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);  
			g2.setFont(sm_font);
			
			// draw a white background
			g2.setColor(Color.white);
			g2.fillRect(0, 0, (int) width, (int) height);
			g2.setColor(Color.black);

			// get font sizes
			int fontHeight = g2.getFontMetrics().getHeight();
			int fontDesent = g2.getFontMetrics().getDescent();

			// translate coordinates based on page margins
			g2.translate(pagex, pagey);
			
			Shape s = g2.getClip();

			// set current date
			GregorianCalendar now = new GregorianCalendar();
			int tmon = now.get(Calendar.MONTH);
			int tyear = now.get(Calendar.YEAR);
			int tdate = now.get(Calendar.DATE);

			// force reload if the date has changed since the last draw
			if (lastDrawDate != tdate) {
				needLoad = true;
			}
			lastDrawDate = tdate;

			// set date to first day of year and set first day of week according to prefs
			GregorianCalendar cal = new GregorianCalendar(year_, Calendar.JANUARY, 1);
			cal.setFirstDayOfWeek(Prefs.getIntPref(PrefName.FIRSTDOW));

			// top of drawn year - used to be non-zero to fit a title
			int caltop = 0;
			
			// grid starts under the day of week labels
			gridtop = caltop + fontHeight + fontDesent;
			
			// width of month column
			int monthwidth = (int) pageWidth / 10;

			// calculate width and height of day boxes 
			rowheight = ((int) pageHeight - gridtop) / 12;
			colwidth = (int) (pageWidth - monthwidth) / 37;

			// ui right and bottom
			int calright = monthwidth + colwidth * 37;
			int calbot = gridtop + 12 * rowheight;

			// do not allow anything to be dragged
			setDragBounds(0, 0, 0, 0);
			
			// do not allow anything to be resized
			setResizeBounds(0, 0);

			// background for day of week labels
			g2.setColor(this.getBackground());
			g2.fillRect(0, caltop, calright, gridtop - caltop);
			g2.setColor(Color.black);

			// month name format
			SimpleDateFormat dfm = new SimpleDateFormat("MMMM");

			// draw the day of week names centered in each column - no boxes drawn yet
			SimpleDateFormat dfw = new SimpleDateFormat("E");
			cal.add(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() - cal.get(Calendar.DAY_OF_WEEK));
			for (int col = 0; col < 37; col++) {
				int colleft = monthwidth + (col * colwidth);
				String dayofweek = dfw.format(cal.getTime());
				dayofweek = dayofweek.substring(0, 1);
				int swidth = g2.getFontMetrics().stringWidth(dayofweek);
				g2.drawString(dayofweek, colleft + (colwidth - swidth) / 2, caltop + fontHeight);
				cal.add(Calendar.DAY_OF_WEEK, 1);
			}

			// draw the days of the year. loop by month
			int mon = Calendar.JANUARY;
			for (int row = 0; row < 12; row++) {
				
				// start with first of month
				cal.set(year_, mon, 1);

				// back up calendar to first day of week
				int fdow = cal.get(Calendar.DAY_OF_WEEK) - cal.getFirstDayOfWeek();
				if (fdow == -1)
					fdow = 6;
				cal.add(Calendar.DATE, -1 * fdow);

				// print the days
				for (int col = 0; col < 37; col++) {

					
					int numbox = row * 37 + col;
					int rowtop = (row * rowheight) + gridtop;
					int colleft = monthwidth + (col * colwidth);
					int dow = cal.get(Calendar.DAY_OF_WEEK);

					// set clip to the day box to truncate long appointment text
					g2.clipRect(colleft, rowtop, colwidth, rowheight);
					
					// load from model if needed
					if (needLoad) {
						try {

							// if day is not in the month for this row - then use the default background
							// and don't add any ui components - will be an empty box
							if (cal.get(Calendar.MONTH) != mon) {
								colors[numbox] = this.getBackground();
							} else {

								// add the month button with the month name. pressing it will navigate to the month view
								if (cal.get(Calendar.DATE) == 1) {
									boxes.add(new ButtonBox(cal.getTime(), dfm.format(cal.getTime()), null, new Rectangle(2,
											rowtop, monthwidth - 4, fontHeight), new Rectangle(0, rowtop, monthwidth, rowheight)) {
										@Override
										public void onClick() {
											MultiView.getMainView().setView(ViewType.MONTH);
											GregorianCalendar gc = new GregorianCalendar();
											gc.setTime(getDate());
											MultiView.getMainView().goTo(gc);
										}
									});
								}
								
								// get the appointment info for the given day
								Day dayInfo = Day.getDay(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)
										);

								// default background for day button - none
								Color bbg = null;

								// default box color
								Color c = new Color(Prefs.getIntPref(PrefName.UCS_WEEKDAY));

								// if there are entities for the day...
								if (dayInfo != null) {
									
									Collection<CalendarEntity> appts = dayInfo.getItems();
									
									// set the day button background to some different color to indicate the presence
									// of items on this day
									if (appts != null && !appts.isEmpty()) {
										bbg = Color.pink;
									}
									
									// set the day box background color based on various items
									if (tmon == cal.get(Calendar.MONTH) && tyear == year_ && tdate == cal.get(Calendar.DATE)) {
										c = new Color(Prefs.getIntPref(PrefName.UCS_TODAY));
									} else if (dayInfo.getHoliday() != 0) {
										c = new Color(Prefs.getIntPref(PrefName.UCS_HOLIDAY));
									} else if (dayInfo.getVacation() == 1) {
										c = new Color(Prefs.getIntPref(PrefName.UCS_VACATION));
									} else if (dayInfo.getVacation() == 2) {
										c = new Color(Prefs.getIntPref(PrefName.UCS_HALFDAY));
									} else if (dow == Calendar.SUNDAY || dow == Calendar.SATURDAY) {
										c = new Color(Prefs.getIntPref(PrefName.UCS_WEEKEND));
									} else {
										c = new Color(Prefs.getIntPref(PrefName.UCS_WEEKDAY));
									}
								}

								// save the box color
								colors[numbox] = c;

								// get the date String
								String datetext = Integer.toString(cal.get(Calendar.DATE));

								// add the date button - clicking it goes to the day view
								boxes.add(new ButtonBox(cal.getTime(), datetext, null, new Rectangle(colleft + 2, rowtop,
										colwidth - 4, fontHeight), new Rectangle(colleft, rowtop, colwidth, rowheight), bbg) {
									@Override
									public void onClick() {
										MultiView.getMainView().setView(ViewType.DAY);
										GregorianCalendar gc = new GregorianCalendar();
										gc.setTime(getDate());
										MultiView.getMainView().goTo(gc);
									}
								});

							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}

					// reset the clip or bad things happen
					g2.setClip(s);

					// draw the current box's background
					g2.setColor(colors[numbox]);
					g2.fillRect(colleft, rowtop, colwidth, rowheight);

					// increment the day
					cal.add(Calendar.DATE, 1);
				}

				mon++;

			}
			
			needLoad = false;

			// draw all of the box items (i.e. buttons)
			drawBoxes(g2);
			g2.setClip(s);

			// draw the lines last
			// top of calendar - above day names
			g2.drawLine(0, caltop, calright, caltop);

			// left border
			g2.drawLine(0, caltop, 0, calbot);
			
			// horizontal
			for (int row = 0; row <= 12; row++) {
				int rowtop = (row * rowheight) + gridtop;
				g2.drawLine(0, rowtop, calright, rowtop);
			}

			// vertical
			for (int col = 0; col <= 37; col++) {
				int colleft = monthwidth + (col * colwidth);
				g2.drawLine(colleft, caltop, colleft, calbot);
			}

			return Printable.PAGE_EXISTS;
		}

		/**
		 * get date for given x/y coord
		 */
		@Override
		public Date getDateForCoord(double x, double y) {

			int col = (int) x / colwidth;
			int row = ((int) y - gridtop) / rowheight;
			GregorianCalendar cal = new GregorianCalendar(year_, row + 1, 1);
			cal.setFirstDayOfWeek(Prefs.getIntPref(PrefName.FIRSTDOW));
			int fdow = cal.get(Calendar.DAY_OF_WEEK) - cal.getFirstDayOfWeek();
			cal.add(Calendar.DATE, -1 * fdow);
			cal.add(Calendar.DATE, row * 7 + col);
			return cal.getTime();
		}

		/**
		 * get navigator label
		 */
		@Override
		public String getNavLabel() {
			SimpleDateFormat df = new SimpleDateFormat("yyyy");
			Calendar cal = new GregorianCalendar(year_, Calendar.JANUARY, 1);
			return df.format(cal.getTime());
		}

		/**
		 * goto a particular year
		 */
		@Override
		public void goTo(Calendar cal) {
			year_ = cal.get(Calendar.YEAR);
			clearData();
			repaint();
		}

		/**
		 * go forward or back 1 year based on mouse wheel
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
		 * navigate forward 1 year
		 */
		@Override
		public void next() {
			year_++;
			clearData();
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			try {
				Font sm_font = Font
						.decode(Prefs.getPref(PrefName.WEEKVIEWFONT));
				drawIt(g, getWidth(), getHeight(), getWidth() - 20,
						getHeight() - 20, 10, 10, sm_font);

			} catch (Exception e) {
				// Errmsg.errmsg(e);
				e.printStackTrace();
			}
		}

		/**
		 * react to prefs changed - reload and redraw
		 */
		@Override
		public void prefsChanged() {
			clearData();
			repaint();

		}

		/**
		 * navigate back 1 year
		 */
		@Override
		public void prev() {
			year_--;
			clearData();
			repaint();
		}

		/**
		 * draw the UI for printing
		 */
		@Override
		public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {

			if (pageIndex > 0)
				return Printable.NO_SUCH_PAGE;
			Font sm_font = Font.decode(Prefs.getPref(PrefName.PRINTFONT));
			clearData();
			int ret = drawIt(g, pageFormat.getWidth(), pageFormat.getHeight(), pageFormat.getImageableWidth(), pageFormat
					.getImageableHeight(), pageFormat.getImageableX(), pageFormat.getImageableY(),  sm_font);
			refresh();
			return ret;
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
		 * navigate to the current year
		 */
		@Override
		public void today() {
			GregorianCalendar cal = new GregorianCalendar();
			year_ = cal.get(Calendar.YEAR);
			clearData();
			repaint();
		}

	}

	// navigator panel
	private NavPanel nav = null;

	// year UI panel
	private YearViewSubPanel yearPanel = null;

	/**
	 * constructor
	 */
	public YearPanel() {

		// create the year ui
		yearPanel = new YearViewSubPanel();
		// create the nav panel
		nav = new NavPanel(yearPanel);

		setLayout(new java.awt.GridBagLayout());
		add(nav, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));
		add(yearPanel, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH, 1.0, 1.0));

	}

	/**
	 * goto a particular year
	 */
	@Override
	public void goTo(Calendar cal) {
		yearPanel.goTo(cal);
		nav.setLabel(yearPanel.getNavLabel());
	}

	/**
	 * draw for printing
	 */
	@Override
	public int print(Graphics arg0, PageFormat arg1, int arg2) throws PrinterException {
		return yearPanel.print(arg0, arg1, arg2);
	}
	
	@Override
	public String getModuleName() {
		return Resource.getResourceString("Year_View");
	}

	@Override
	public JPanel getComponent() {
		return this;
	}

	@Override
	public void initialize(MultiView parent) {
		final MultiView par = parent;
		parent.addToolBarItem(new ImageIcon(getClass().getResource(
		"/resource/year.jpg")), getModuleName(), 
		new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				par.setView(ViewType.YEAR);
			}
		});
	}
	
	@Override
	public void print() {
		try {
			PrintHelper.printPrintable(this);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}
	
	@Override
	public ViewType getViewType() {
		return ViewType.YEAR;
	}

	@Override
	public String getFrameTitle() {
		return this.getModuleName();
	}

	@Override
	public JMenuBar getMenuForFrame() {
		return null;
	}

	@Override
	public void refresh() {
		// do nothing - children do their own refresh
		
	}

	@Override
	public void update(ChangeEvent event) {
		// do nothing - children do their own refresh
	}
	
}
