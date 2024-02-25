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
package net.sf.borg.ui.calendar;

import net.sf.borg.common.*;
import net.sf.borg.model.*;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.CalendarEntity;
import net.sf.borg.model.sync.SubscribedCalendars;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.CalendarModule;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.NavPanel;
import net.sf.borg.ui.DorkTrayIconProxy;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.DateFormat;
import java.util.List;
import java.util.*;

/**
 * DayPanel is the UI for a single day. It consists of a Navigator attached to a
 * DaySubPanel
 */
public class DayPanel extends DockableView implements Printable, CalendarModule {

	private static final long serialVersionUID = 1L;

	/**
	 * DaySubPanel is the Panel that shows the items for a day with a section
	 * for untimed items and a time-grid for timed items
	 */
	private class DaySubPanel extends ApptBoxPanel implements
			NavPanel.Navigator, Prefs.Listener, Printable, Model.Listener,
			MouseWheelListener {

		private static final long serialVersionUID = 1L;
		// set up dash line stroke for time-grid divisions
		private final float[] dash1 = { 1.0f, 3.0f };
		private final BasicStroke dashed = new BasicStroke(0.02f,
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3.0f, dash1, 0.0f);

		// date being shown
		private int date_;
		private int month_;
		private int year_;

		// portion of the screen taken up by the non-timed section
		private double nonTimedPortion = 1.0 / 6.0;

		// flag to indicate if we need to reload from the model. If false, we
		// can just redraw
		// from cached data
		private boolean needLoad = true;

		// records the last date on which a draw took place - for handling the
		// first redraw after midnight
		private int lastDrawDate = -1;

		// background color
		private Color backgroundColor = null;

		// zoom factor
		private int zoom = 0;

		/**
		 * Instantiates a new day sub panel.
		 * 
		 */
		public DaySubPanel() {

			// refresh if prefs change
			Prefs.addListener(this);

			// react to mouse wheel activity
			addMouseWheelListener(this);

			// refresh if the appt or task models change
			AppointmentModel.getReference().addListener(this);
			TaskModel.getReference().addListener(this);
			AddressModel.getReference().addListener(this);
			SubscribedCalendars.getReference().addListener(this);


			goTo(new GregorianCalendar());

		}

		/**
		 * Clear all data. forces next draw to reload from the model
		 */
		public void clearData() {
			clearBoxes();
			needLoad = true;
			setToolTipText(null);
		}

		/**
		 * Draw the day
		 * 
		 * @param g
		 *            the Graphics to draw into
		 * @param width
		 *            the width
		 * @param height
		 *            the height
		 * @param pageWidth
		 *            the page width
		 * @param pageHeight
		 *            the page height
		 * @param pagex
		 *            the pagex
		 * @param pagey
		 *            the pagey
		 * @param sm_font
		 *            the font for appointment text
		 * 
		 * @return the page exists flag for printing
		 */
		private int drawIt(Graphics g, double width, double height,
				double pageWidth, double pageHeight, double pagex,
				double pagey, Font sm_font) {

			Theme t = Theme.getCurrentTheme();

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			// draw a white background
			g2.setColor(new Color(t.getDefaultBg()));
			g2.fillRect(0, 0, (int) width, (int) height);
			g2.setColor(new Color(t.getDefaultFg()));

			// translate coordinates to page margins
			g2.translate(pagex, pagey);

			// save original clip
			Shape s = g2.getClip();

			// get current time
			GregorianCalendar now = new GregorianCalendar();
			int tdate = now.get(Calendar.DATE);

			// force reload if the date has changed since the last draw
			if (lastDrawDate != tdate) {
				needLoad = true;
			}
			lastDrawDate = tdate;

			GregorianCalendar cal = new GregorianCalendar(year_, month_, date_,
					23, 59);

			// top of drawn day - used to be non-zero to fit a title
			int caltop = 0;

			// height of box containing day's appts
			double rowheight = pageHeight - caltop;

			// width of column with time scale (Y axis)
			double timecolwidth = pageWidth / 15;

			// top of timed appts (the time-grid). untimed appts appear above
			double aptop = caltop + rowheight * nonTimedPortion;

			// width of each day column - related to timecolwidth
			double colwidth = (pageWidth - timecolwidth);

			// calculate the bottom and right edge of the grid
			int calbot = (int) rowheight + caltop;

			// limit resizing to the time-grid
			setResizeBounds((int) aptop, calbot);

			// allow dragging around the entire day - across timed and untimed
			// areas
			setDragBounds(caltop, calbot, (int) timecolwidth, (int) (pageWidth));

			// start and end hour = range of Y axis
			String shr = Prefs.getPref(PrefName.WKSTARTHOUR);
			String ehr = Prefs.getPref(PrefName.WKENDHOUR);
			int starthr = 7;
			int endhr = 22;
			try {
				starthr = Integer.parseInt(shr);
				endhr = Integer.parseInt(ehr);
			} catch (Exception e) {
				Errmsg.getErrorHandler().errmsg(e);
			}

			// calculate size of Y-axis ticks (each half-hour)
			int numhalfhours = (endhr - starthr) * 2;
			double tickheight = (calbot - aptop) / numhalfhours;

			// draw background of time column (where the y-axis times are shown)
			// using the default background color
			g2.setColor(new Color(t.getDefaultBg()));
			g2.fillRect(0, caltop, (int) timecolwidth, calbot - caltop);

			// set small font for appt text
			g2.setFont(sm_font);
			int smfontHeight = g2.getFontMetrics().getHeight();
			if( smfontHeight == 0) smfontHeight = 7;

			int colleft = (int) (timecolwidth);

			// reload from model if needed
			if (needLoad) {

				// add the date zone for the day
				addDateZone(cal.getTime(), new Rectangle(colleft, 0,
						(int) colwidth, calbot));

				try {

					startmin = starthr * 60;
					endmin = endhr * 60;

					// get the day's items from the model
					Day dayInfo = Day.getDay(cal.get(Calendar.YEAR),
							cal.get(Calendar.MONTH), cal.get(Calendar.DATE));

					// set a different background color based on various
					// circumstances
					backgroundColor = new Color(t.getDefaultBg());
					int dow = cal.get(Calendar.DAY_OF_WEEK);
					Calendar today = new GregorianCalendar();
					if (today.get(Calendar.MONTH) == month_
							&& today.get(Calendar.YEAR) == year_
							&& today.get(Calendar.DATE) == cal
									.get(Calendar.DATE)) {
						// day is today
						backgroundColor = new Color(t.getTodayBg());
					} else if (dayInfo.getHoliday() != 0) {
						// holiday
						backgroundColor = new Color(t.getHolidayBg());
					} else if (dayInfo.getVacation() == 1) {
						// full day vacation
						backgroundColor = new Color(Theme.getCurrentTheme()
								.getVacationBg());
					} else if (dayInfo.getVacation() == 2) {
						// half-day vacation
						backgroundColor = new Color(t.getHalfdayBg());
					} else if (dow == Calendar.SUNDAY
							|| dow == Calendar.SATURDAY) {
						// weekend
						backgroundColor = new Color(t.getWeekendBg());
					} else {
						// weekday
						backgroundColor = new Color(t.getWeekdayBg());
					}

					// determine initial Y coord for non-scheduled appts (notes)
					// they will be above the timed appt area
					int notey = caltop;

					// loop through entities
					for (CalendarEntity entity : dayInfo.getItems()) {

						Date d = entity.getDate();

						// sanity check - shouldn't happen
						if (d == null)
							continue;

						// determine appt start and end minutes
						GregorianCalendar acal = new GregorianCalendar();
						acal.setTime(d);
						double apstartmin = 60 * acal.get(Calendar.HOUR_OF_DAY)
								+ acal.get(Calendar.MINUTE);
						int dur = 0;
						Integer duri = entity.getDuration();
						if (duri != null) {
							dur = duri.intValue();
						}
						double apendmin = apstartmin + dur;

						// check if the entity is a note (untime)
						// an entity can only be timed if it is an appointment
						// that fits in the time-grid, has a duration, and is
						// timed
						if (!(entity instanceof Appointment)
								|| AppointmentModel
										.isNote((Appointment) entity)
								|| apendmin < startmin
								|| apstartmin >= endmin - 4
								|| entity.getDuration() == null
								|| entity.getDuration().intValue() == 0) {

							if (addNoteBox(
									cal.getTime(),
									entity,
									new Rectangle(colleft + 2, notey,
											(int) (colwidth - 4), smfontHeight),
									new Rectangle(colleft, caltop,
											(int) colwidth,
											(int) (aptop - caltop))) != null) {

								// increment Y coord for next note text
								notey += smfontHeight;
							}
						} else {

							// appt box bounds and clip are set to the whole
							// grid.
							// will be laid out later
							addApptBox(cal.getTime(), (Appointment) entity,
									new Rectangle(colleft + 4, (int) aptop,
											(int) colwidth - 8,
											(int) (calbot - aptop)),
									new Rectangle(colleft, (int) aptop,
											(int) colwidth,
											(int) (calbot - aptop)));
						}

					}

				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}

				// layout all of the ApptBoxes
				List<ApptBox> layoutlist = new ArrayList<ApptBox>();
				for (Box b : boxes) {
					if (!(b instanceof ApptBox))
						continue;
					layoutlist.add((ApptBox) b);
				}
				ApptBox.layoutBoxes(layoutlist, starthr, endhr);

			}

			// draw background for day area with the user color
			g2.setColor(backgroundColor);
			g2.fillRect((int) timecolwidth, caltop,
					(int) (pageWidth - timecolwidth), (int) pageHeight - caltop);
			g2.setColor(new Color(t.getDefaultFg()));

			// draw dashed lines for 1/2 hour intervals
			Stroke defstroke = g2.getStroke();
			g2.setStroke(dashed);
			for (int row = 0; row < numhalfhours; row++) {
				int rowtop = (int) ((row * tickheight) + aptop);
				g2.drawLine((int) timecolwidth, rowtop, (int) pageWidth, rowtop);
			}
			g2.setStroke(defstroke);

			// add the zoom buttons
			if (zoom < 4)
				boxes.add(new ButtonBox(cal.getTime(), "", new ImageIcon(
						getClass().getResource("/resource/ZoomIn16.gif")),
						new Rectangle(0, caltop, 20, smfontHeight), null) {

					@Override
					public void onClick() {
						zoom++;
						refresh();
					}

				});

			if (zoom > 0)
				boxes.add(new ButtonBox(cal.getTime(), "", new ImageIcon(
						getClass().getResource("/resource/ZoomOut16.gif")),
						new Rectangle(colleft - 20, caltop, 20, smfontHeight),
						null) {

					@Override
					public void onClick() {
						zoom--;
						refresh();
					}

				});

			// add the scroll buttons
			if (nonTimedPortion < 0.8) {
				boxes.add(new ButtonBox(cal.getTime(), "", new ImageIcon(
						getClass().getResource("/resource/Down16.gif")),
						new Rectangle(0, (int) aptop, colleft, smfontHeight),
						null) {

					@Override
					public void onClick() {
						nonTimedPortion += 1.0 / 6.0;
						refresh();
					}

				});
			}
			if (nonTimedPortion > 0.2) {
				boxes.add(new ButtonBox(cal.getTime(), "", new ImageIcon(
						getClass().getResource("/resource/Up16.gif")),
						new Rectangle(0, (int) aptop - smfontHeight, colleft,
								smfontHeight), null) {

					@Override
					public void onClick() {
						nonTimedPortion -= 1.0 / 6.0;
						refresh();
					}

				});
			}

			// draw all boxes
			g2.setClip(s);
			drawBoxes(g2);
			g2.setColor(new Color(t.getDefaultFg()));

			// draw the day's lines last so they show on top of other stuff
			// first - the horizontal lines
			g2.drawLine(0, caltop, (int) pageWidth, caltop);
			g2.drawLine(0, (int) aptop, (int) pageWidth, (int) aptop);
			g2.drawLine(0, calbot, (int) pageWidth, calbot);
			g2.drawLine(0, caltop, 0, calbot);

			// draw the Y axis time labels
			g2.setFont(sm_font);
			boolean mt = false;
			String mil = Prefs.getPref(PrefName.MILTIME);
			if (mil.equals("true"))
				mt = true;
			for (int row = 1; row < endhr - starthr; row++) {
				int y = (int) ((row * tickheight * 2) + aptop);
				int hr = row + starthr;

				if (!mt && hr > 12)
					hr = hr - 12;

				String tmlabel = hr + ":00";
				g2.drawString(tmlabel, 2, y + smfontHeight / 2);
			}

			// vertical lines
			g2.drawLine(colleft, caltop, colleft, calbot);
			g2.drawLine((int) pageWidth, caltop, (int) pageWidth, calbot);

			needLoad = false;

			// for printing - in case we are drawing this for a printer
			return Printable.PAGE_EXISTS;
		}

		/**
		 * return the date for the mouse coord
		 */
		@Override
		public Date getDateForCoord(double x, double y) {
			// always return the current day as this UI only shows 1 day
			GregorianCalendar cal = new GregorianCalendar(year_, month_, date_,
					23, 59);
			return cal.getTime();
		}

		/**
		 * return the navigator label
		 */
		@Override
		public String getNavLabel() {
			GregorianCalendar cal = new GregorianCalendar(year_, month_, date_,
					23, 59);
			Date dt = cal.getTime();
			DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
			return df.format(dt);
		}

		/**
		 * show the given date
		 */
		@Override
		public void goTo(Calendar cal) {
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			date_ = cal.get(Calendar.DATE);
			clearData();
			repaint();
		}

		/**
		 * react to a mouse wheel event (navigate forward or back)
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
		 * advance 1 day
		 */
		@Override
		public void next() {
			GregorianCalendar cal = new GregorianCalendar(year_, month_, date_,
					23, 59);
			cal.add(Calendar.DATE, 1);
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			date_ = cal.get(Calendar.DATE);
			clearData();
			repaint();
			MultiView.getMainView().goTo(cal);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			try {
				Font sm_font = Font.decode(Prefs.getPref(PrefName.DAYVIEWFONT));
				drawIt(g, getWidth(), getHeight(), getWidth() - 20,
						getHeight() - 20, 10, 10, sm_font);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * reload and redraw if prefs change. some prefs change the grid size
		 * and/or what items get shown
		 */
		@Override
		public void prefsChanged() {
			clearData();
			repaint();

		}

		/**
		 * navigate backwards 1 day
		 */
		@Override
		public void prev() {
			GregorianCalendar cal = new GregorianCalendar(year_, month_, date_,
					23, 59);
			cal.add(Calendar.DATE, -1);
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			date_ = cal.get(Calendar.DATE);
			clearData();
			repaint();
			MultiView.getMainView().goTo(cal);

		}

		/**
		 * draw the UI into a Graphics for printing
		 */
		@Override
		public int print(Graphics g, PageFormat pageFormat, int pageIndex)
				throws PrinterException {
			// only print 1 page
			if (pageIndex > 0)
				return Printable.NO_SUCH_PAGE;
			Font sm_font = Font.decode(Prefs.getPref(PrefName.PRINTFONT));
			clearData();
			int ret = drawIt(g, pageFormat.getWidth(), pageFormat.getHeight(),
					pageFormat.getImageableWidth(),
					pageFormat.getImageableHeight(),
					pageFormat.getImageableX(), pageFormat.getImageableY(),
					sm_font);
			refresh();
			return ret;
		}

		/**
		 * reload data and redraw
		 */
		@Override
		public void refresh() {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Dimension dim = toolkit.getScreenSize();
			int h = ((zoom + 2) * (int) dim.getHeight()) / 2;
			if (zoom == 0)
				h = 0;
			this.setPreferredSize(new Dimension(0, h));
			clearData();
			repaint();
			this.getParent().doLayout();
		}

		@Override
		public void update(ChangeEvent event) {
			refresh();
		}

		/**
		 * navigate to the current date
		 */
		@Override
		public void today() {
			GregorianCalendar cal = new GregorianCalendar();
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			date_ = cal.get(Calendar.DATE);
			clearData();
			repaint();
			MultiView.getMainView().goTo(cal);

		}

	}

	// container panel for the day boxes
	private DaySubPanel dp_ = null;

	// navigation panel
	private NavPanel nav = null;
	
	private boolean isInitialized = false;


	/**
	 * Instantiates a new day panel.
	 * 
	 */
	public DayPanel() {

		
	}

	/**
	 * Go to a particular date
	 * 
	 * @param cal
	 *            the date
	 */
	@Override
	public void goTo(Calendar cal) {
		if( isInitialized ){
			dp_.goTo(cal);
			nav.setLabel(dp_.getNavLabel());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.Printable#print(java.awt.Graphics,
	 * java.awt.print.PageFormat, int)
	 */
	@Override
	public int print(Graphics arg0, PageFormat arg1, int arg2)
			throws PrinterException {
		return dp_.print(arg0, arg1, arg2);
	}

	@Override
	public String getModuleName() {
		return Resource.getResourceString("Day_View");
	}

	@Override
	public JPanel getComponent() {
		
		if( !isInitialized )
		{
			// create the day ui and attached navigator
			dp_ = new DaySubPanel();
			nav = new NavPanel(dp_);

			JScrollPane sp = new JScrollPane();
			sp.setViewportView(dp_);

			setLayout(new java.awt.GridBagLayout());
			add(nav,
					GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));
			add(sp, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH,
					1.0, 1.0));
			isInitialized = true;

		}
		return this;
	}

	@Override
	public void initialize(MultiView parent) {
		final MultiView par = parent;
		parent.addToolBarItem(
				new ImageIcon(getClass().getResource("/resource/day.jpg")),
				getModuleName(), new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						par.setView(ViewType.DAY);
					}
				});
		DorkTrayIconProxy.addAction(getModuleName(), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				par.setView(ViewType.DAY);
			}
		});
	}

	@Override
	public void print() {
		try {
			PrintHelper.printPrintable(this);
		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}

	@Override
	public ViewType getViewType() {
		return ViewType.DAY;
	}

	@Override
	public String getFrameTitle() {
		return this.getModuleName();
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
