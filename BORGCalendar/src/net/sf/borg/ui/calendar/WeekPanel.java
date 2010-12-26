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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.TextAttribute;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.Model.ChangeEvent;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.CalendarEntity;
import net.sf.borg.ui.DockableView;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.NavPanel;
import net.sf.borg.ui.MultiView.CalendarModule;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * WeekPanel is the UI for a single week. It consists of a Navigator attached to
 * a WeekSubPanel
 */
public class WeekPanel extends DockableView implements Printable, CalendarModule {

	private static final long serialVersionUID = 1L;

	/**
	 * WeekSubPanel is the Panel that shows the items for a week with a section
	 * for untimed items and a time-grid for timed items
	 */
	private class WeekSubPanel extends ApptBoxPanel implements
			NavPanel.Navigator, Prefs.Listener, Model.Listener, Printable,
			MouseWheelListener {

		private static final long serialVersionUID = 1L;

		// week start date
		private Date beg_ = null;

		// width of each day
		private double colwidth = 0;

		// set up dash line stroke
		private float dash1[] = { 1.0f, 3.0f };
		private BasicStroke dashed = new BasicStroke(0.02f,
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3.0f, dash1, 0.0f);

		// date passed in - its week is the week to show
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

		// width of time column (where times are shown for y axis)
		private double timecolwidth = 0;

		// daily background colors
		private Color backgroundColors[] = new Color[7];

		/**
		 * constructor
		 * 
		 */
		public WeekSubPanel() {

			// react to pref changes
			Prefs.addListener(this);

			// react to mouse wheel changes
			addMouseWheelListener(this);

			// react to appt or task model changes
			AppointmentModel.getReference().addListener(this);
			TaskModel.getReference().addListener(this);
			
			goTo(new GregorianCalendar());

		}

		/** clear data - will cause a reload on new redraw */
		public void clearData() {
			clearBoxes();
			needLoad = true;
			setToolTipText(null);
		}

		/**
		 * draw the week into a Graphics
		 */
		private int drawIt(Graphics g, double width, double height,
				double pageWidth, double pageHeight, double pagex,
				double pagey, Font sm_font) {

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  
	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);  
			
			// strike-font
			Map<TextAttribute, Serializable> stmap = new HashMap<TextAttribute, Serializable>();
			stmap.put(TextAttribute.STRIKETHROUGH,
					TextAttribute.STRIKETHROUGH_ON);
			stmap.put(TextAttribute.FONT, sm_font);

			// draw a white background
			g2.setColor(Color.white);
			g2.fillRect(0, 0, (int) width, (int) height);
			g2.setColor(Color.black);

			// get font sizes
			int fontHeight = g2.getFontMetrics().getHeight();
			int fontDesent = g2.getFontMetrics().getDescent();

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

			// set up calendar and determine first day of week from options
			GregorianCalendar cal = new GregorianCalendar(year_, month_, date_,
					23, 59);
			int fdow = Prefs.getIntPref(PrefName.FIRSTDOW);
			cal.setFirstDayOfWeek(fdow);

			// move cal back to first dow for the chosen week
			int offset = cal.get(Calendar.DAY_OF_WEEK) - fdow;
			if (offset == -1)
				offset = 6;
			cal.add(Calendar.DATE, -1 * offset);

			// save begin date
			beg_ = cal.getTime();

			// top of drawn week - used to be non-zero to fit a title
			int caltop = 0;

			// top of untimed region - under day name
			int daytop = caltop + fontHeight + fontDesent;

			// height of box containing day's appts
			double rowheight = pageHeight - daytop;

			// width of column with time labels (Y axis)
			timecolwidth = pageWidth / 21;

			// top of timed appts (the time-grid). untimed appts appear above
			double aptop = caltop + rowheight * nonTimedPortion;

			// width of each day column - related to timecolwidth
			colwidth = (pageWidth - timecolwidth) / 7;

			// calculate the bottom edge of the grid
			int calbot = (int) rowheight + daytop;

			// limit resize (and dragging out of new items) to the time-grid
			// (timed appt area)
			setResizeBounds((int) aptop, calbot);

			// allow dragging of items across the entire week - including both
			// timed and untimed areas)
			setDragBounds(daytop, calbot, (int) timecolwidth, (int) (pageWidth));
			
			// start and end hour = range of Y axis
			String shr = Prefs.getPref(PrefName.WKSTARTHOUR);
			String ehr = Prefs.getPref(PrefName.WKENDHOUR);
			int starthr = 7;
			int endhr = 22;
			try {
				starthr = Integer.parseInt(shr);
				endhr = Integer.parseInt(ehr);
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}

			// calculate size of Y-axis ticks (each half-hour)
			int numhalfhours = (endhr - starthr) * 2;
			double tickheight = (calbot - aptop) / numhalfhours;

			// format for day labels
			SimpleDateFormat dfw = new SimpleDateFormat("EEE dd");

			// set time label column to default background
			g2.setColor(this.getBackground());
			g2.fillRect(0, caltop, (int) timecolwidth, calbot - caltop);
			g2.setColor(Color.BLACK);

			// set small font for appt text
			g2.setFont(sm_font);
			int smfontHeight = g2.getFontMetrics().getHeight();

			// loop through the days of the week
			for (int col = 0; col < 7; col++) {

				// position of the left of the day's column
				int colleft = (int) (timecolwidth + col * colwidth);

				// load data from model if needed
				if (needLoad) {

					Calendar today = new GregorianCalendar();

					// add a zone for this day
					addDateZone(cal.getTime(), new Rectangle(colleft, 0,
							(int) colwidth, calbot));

					try {
						startmin = starthr * 60;
						endmin = endhr * 60;

						// get the day's items
						Day dayInfo = Day.getDay(cal.get(Calendar.YEAR), cal
								.get(Calendar.MONTH), cal.get(Calendar.DATE));

						// set a different background color based on various
						// circumstances
						Color backgroundColor = null;
						int dow = cal.get(Calendar.DAY_OF_WEEK);
						if (today.get(Calendar.MONTH) == month_
								&& today.get(Calendar.YEAR) == year_
								&& today.get(Calendar.DATE) == cal
										.get(Calendar.DATE)) {
							// day is today
							backgroundColor = new Color(Prefs
									.getIntPref(PrefName.UCS_TODAY));
						} else if (dayInfo.getHoliday() != 0) {
							// holiday
							backgroundColor = new Color(Prefs
									.getIntPref(PrefName.UCS_HOLIDAY));
						} else if (dayInfo.getVacation() == 1) {
							// full day vacation
							backgroundColor = new Color(Prefs
									.getIntPref(PrefName.UCS_VACATION));
						} else if (dayInfo.getVacation() == 2) {
							// half-day vacation
							backgroundColor = new Color(Prefs
									.getIntPref(PrefName.UCS_HALFDAY));
						} else if (dow == Calendar.SUNDAY
								|| dow == Calendar.SATURDAY) {
							// weekend
							backgroundColor = new Color(Prefs
									.getIntPref(PrefName.UCS_WEEKEND));
						} else {
							// weekday
							backgroundColor = new Color(Prefs
									.getIntPref(PrefName.UCS_WEEKDAY));
						}
						backgroundColors[col] = backgroundColor;

						// determine Y coord for non-scheduled appts (notes)
						// they will be above the timed appt area
						int notey = daytop;// + smfontHeight;

						// loop through appts
						for (CalendarEntity entity : dayInfo.getItems()) {

							Date d = entity.getDate();

							// sanity check - should not happen
							if (d == null)
								continue;

							// determine appt start and end minutes
							GregorianCalendar acal = new GregorianCalendar();
							acal.setTime(d);
							double apstartmin = 60
									* acal.get(Calendar.HOUR_OF_DAY)
									+ acal.get(Calendar.MINUTE);
							int dur = 0;
							Integer duri = entity.getDuration();
							if (duri != null) {
								dur = duri.intValue();
							}
							double apendmin = apstartmin + dur;

							// check if the entity is a note (untime)
							// an entity can only be timed if it is an
							// appointment
							// that fits in the time-grid, has a duration, and
							// is timed
							if (!(entity instanceof Appointment)
									|| AppointmentModel
											.isNote((Appointment) entity)
									|| apendmin < startmin
									|| apstartmin >= endmin - 4
									|| entity.getDuration() == null
									|| entity.getDuration().intValue() == 0) {

								if (addNoteBox(cal.getTime(), entity,
										new Rectangle(colleft + 2, notey,
												(int) (colwidth - 4),
												smfontHeight), new Rectangle(
												colleft, caltop,
												(int) colwidth,
												(int) (aptop - caltop))) != null) {
									// increment Y coord for next note text
									notey += smfontHeight;
								}
							} else {
								// appt box bounds and clip are set to the day's
								// column in the grid.
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
						Errmsg.errmsg(e);
					}

					// layout the timed items
					List<ApptBox> layoutlist = new ArrayList<ApptBox>();
					for (Box b : boxes) {
						if (!(b instanceof ApptBox))
							continue;
						ApptBox ab = (ApptBox) b;
						Calendar c = new GregorianCalendar();
						c.setTime(ab.getDate());
						if (c.get(Calendar.DAY_OF_YEAR) == cal
								.get(Calendar.DAY_OF_YEAR))
							layoutlist.add(ab);
					}
					ApptBox.layoutBoxes(layoutlist, starthr, endhr);

				}

				// reset the clip or bad things happen
				g2.setClip(s);

				// adjust for rounding errors
				int nextColumnLeft = (int) (timecolwidth + (col+1) * colwidth);
				int fixedWidth = nextColumnLeft - colleft;
				g2.setColor(this.getBackground());
				g2.fillRect(colleft, caltop, fixedWidth, daytop - caltop);

				g2.setColor(backgroundColors[col]);
				g2.fillRect(colleft, caltop, fixedWidth, calbot - caltop);

				g2.setColor(Color.black);

				// increment the day
				cal.add(Calendar.DATE, 1);

			}

			needLoad = false;
			
			// draw dashed lines for 1/2 hour intervals
			Stroke defstroke = g2.getStroke();
			g2.setStroke(dashed);
			for (int row = 0; row < numhalfhours; row++) {
				int rowtop = (int) ((row * tickheight) + aptop);
				g2
						.drawLine((int) timecolwidth, rowtop, (int) pageWidth,
								rowtop);
			}
			g2.setStroke(defstroke);

			// add the scroll buttons
			if (nonTimedPortion < 0.8) {
				boxes.add(new ButtonBox(cal.getTime(), "", new ImageIcon(
						getClass().getResource("/resource/Down16.gif")),
						new Rectangle(0, (int) aptop, (int)timecolwidth, smfontHeight),
						null) {

					@Override
					public void onClick() {
						nonTimedPortion += 1.0 / 6.0;
						refresh();
					}

				});
			}
			if (nonTimedPortion > 0.2 ) {
				boxes.add(new ButtonBox(cal.getTime(), "", new ImageIcon(
						getClass().getResource("/resource/Up16.gif")),
						new Rectangle(0, (int) aptop - smfontHeight, (int)timecolwidth,
								smfontHeight), null) {

					@Override
					public void onClick() {
						nonTimedPortion -= 1.0 / 6.0;
						refresh();
					}

				});
			}

			// draw all boxes for the week
			drawBoxes(g2);

			// reset calendar
			cal.setTime(beg_);

			// add the day labels
			for (int col = 0; col < 7; col++) {

				int colleft = (int) (timecolwidth + (col * colwidth));
				String dayofweek = dfw.format(cal.getTime());
				int swidth = g2.getFontMetrics().stringWidth(dayofweek);

				g2.drawString(dayofweek,
						(int) (colleft + (colwidth - swidth) / 2), caltop
								+ fontHeight);
				cal.add(Calendar.DATE, 1);
			}

			// draw the box lines last so they show on top of other stuff
			// first - the horizontal lines
			g2.drawLine(0, caltop, (int) pageWidth, caltop);
			g2.drawLine(0, daytop, (int) pageWidth, daytop);
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

				String tmlabel = Integer.toString(hr) + ":00";
				g2.drawString(tmlabel, 2, y + smfontHeight / 2);
			}

			// the vertical lines
			for (int col = 0; col < 8; col++) {
				int colleft = (int) (timecolwidth + (col * colwidth));
				g2.drawLine(colleft, caltop, colleft, calbot);
			}

			// to support printing
			return Printable.PAGE_EXISTS;
		}

		/**
		 * return date for a coordinate
		 */
		@Override
		public Date getDateForCoord(double x, double y) {

			// determine the date based on what column we are in
			double col = (x - timecolwidth) / colwidth;
			if (col > 6)
				col = 6;
			Calendar cal = new GregorianCalendar();
			cal.setTime(beg_);
			cal.add(Calendar.DATE, (int) col);
			return cal.getTime();
		}

		/**
		 * get nav label
		 */
		@Override
		public String getNavLabel() {

			// set up calendar and determine first day of week from options
			GregorianCalendar cal = new GregorianCalendar(year_, month_, date_,
					23, 59);
			int fdow = Prefs.getIntPref(PrefName.FIRSTDOW);
			cal.setFirstDayOfWeek(fdow);

			// move cal back to first dow for the chosen week
			int offset = cal.get(Calendar.DAY_OF_WEEK) - fdow;
			if (offset == -1)
				offset = 6;
			cal.add(Calendar.DATE, -1 * offset);

			// save begin/end date and build title
			Date beg = cal.getTime();
			cal.add(Calendar.DATE, 6);
			Date end = cal.getTime();
			DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

			return df.format(beg) + " "
					+ Resource.getResourceString("__through__") + " "
					+ df.format(end);

		}

		/**
		 * go to a particular date
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
		 * navigate forward or back based on mouse wheel action
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
		 * navigate forward 1 week
		 */
		@Override
		public void next() {
			GregorianCalendar cal = new GregorianCalendar(year_, month_, date_,
					23, 59);
			cal.add(Calendar.DATE, 7);
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			date_ = cal.get(Calendar.DATE);
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
				e.printStackTrace();
			}
		}

		/**
		 * reload and redraw if prefs change
		 */
		@Override
		public void prefsChanged() {
			clearData();
			repaint();

		}

		/**
		 * navigate backwards 1 week
		 */
		@Override
		public void prev() {
			GregorianCalendar cal = new GregorianCalendar(year_, month_, date_,
					23, 59);
			cal.add(Calendar.DATE, -7);
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			date_ = cal.get(Calendar.DATE);
			clearData();
			repaint();
		}

		/**
		 * print to a Graphics for a printout
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
					pageFormat.getImageableWidth(), pageFormat
							.getImageableHeight(), pageFormat.getImageableX(),
					pageFormat.getImageableY(), sm_font);
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
		 * navigate to the current week
		 */
		@Override
		public void today() {
			GregorianCalendar cal = new GregorianCalendar();
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			date_ = cal.get(Calendar.DATE);
			clearData();
			repaint();
		}
	}

	// the navigator panel
	private NavPanel nav = null;

	// the panel to draw the week
	private WeekSubPanel wp_ = null;

	/**
	 * constructor
	 * 
	 */
	public WeekPanel() {

		// create the week container panel
		wp_ = new WeekSubPanel();

		// create the navigator
		nav = new NavPanel(wp_);

		setLayout(new java.awt.GridBagLayout());
		add(nav, GridBagConstraintsFactory
				.create(0, 0, GridBagConstraints.BOTH));
		add(wp_, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH, 1.0, 1.0));

	}

	/**
	 * go to a particular week
	 * 
	 * @param cal
	 *            a day in the week
	 */
	@Override
	public void goTo(Calendar cal) {
		wp_.goTo(cal);
		nav.setLabel(wp_.getNavLabel());
	}

	/**
	 * print the UI
	 */
	@Override
	public int print(Graphics arg0, PageFormat arg1, int arg2)
			throws PrinterException {
		return wp_.print(arg0, arg1, arg2);
	}

	@Override
	public String getModuleName() {
		return Resource.getResourceString("Week_View");
	}

	@Override
	public JPanel getComponent() {
		return this;
	}

	@Override
	public void initialize(MultiView parent) {
		final MultiView par = parent;
		parent.addToolBarItem(new ImageIcon(getClass().getResource(
		"/resource/week.jpg")), getModuleName(), 
		new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				par.setView(ViewType.WEEK);
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
		return ViewType.WEEK;
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
