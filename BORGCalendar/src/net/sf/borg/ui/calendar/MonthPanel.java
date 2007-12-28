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
import java.awt.Shape;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.TextAttribute;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Day;
import net.sf.borg.model.Model;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.beans.Appointment;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.NavPanel;
import net.sf.borg.ui.Navigator;

public class MonthPanel extends JPanel implements Printable {

	// monthPanel handles the printing of a single month
	private class MonthViewSubPanel extends ApptBoxPanel implements Printable,
			Navigator, Model.Listener, Prefs.Listener, MouseWheelListener {

		private int month_;

		private int year_;

		private int colwidth;

		private int rowheight;

		private int daytop;

		final private int numBoxes = 42;

		Color colors[] = new Color[numBoxes];

		boolean needLoad = true;

		public MonthViewSubPanel(int month, int year) {
			year_ = year;
			month_ = month;
			clearData();
			Prefs.addListener(this);
			addMouseWheelListener(this);
			AppointmentModel.getReference().addListener(this);
			TaskModel.getReference().addListener(this);

		}

		public void clearData() {
			clearBoxes();
			needLoad = true;
			setToolTipText(null);
		}

		public String getNavLabel() {
			SimpleDateFormat df = new SimpleDateFormat("MMMM yyyy");
			Calendar cal = new GregorianCalendar(year_, month_, 1);
			return df.format(cal.getTime());
		}

		public void goTo(Calendar cal) {
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			clearData();
			repaint();
		}

		public void next() {
			GregorianCalendar cal = new GregorianCalendar(year_, month_, 1, 23,
					59);
			cal.add(Calendar.MONTH, 1);
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			clearData();
			repaint();
		}

		public void prev() {
			GregorianCalendar cal = new GregorianCalendar(year_, month_, 1, 23,
					59);
			cal.add(Calendar.MONTH, -1);
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			clearData();
			repaint();
		}

		// print does the actual formatting of the printout
		public int print(Graphics g, PageFormat pageFormat, int pageIndex)
				throws PrinterException {

			if (pageIndex > 1)
				return Printable.NO_SUCH_PAGE;

			return (drawIt(g, pageFormat.getWidth(), pageFormat.getHeight(),
					pageFormat.getImageableWidth(), pageFormat
							.getImageableHeight(), pageFormat.getImageableX(),
					pageFormat.getImageableY(), pageIndex));
		}

		public void today() {
			GregorianCalendar cal = new GregorianCalendar();
			year_ = cal.get(Calendar.YEAR);
			month_ = cal.get(Calendar.MONTH);
			clearData();
			repaint();
		}

		private int lastDrawDate = -1;

		private int drawIt(Graphics g, double width, double height,
				double pageWidth, double pageHeight, double pagex,
				double pagey, int pageIndex) {
			// System.out.println("month " + new
			// GregorianCalendar().toString());
			boolean showpub = false;
			boolean showpriv = false;
			String sp = Prefs.getPref(PrefName.SHOWPUBLIC);
			if (sp.equals("true"))
				showpub = true;
			sp = Prefs.getPref(PrefName.SHOWPRIVATE);
			if (sp.equals("true"))
				showpriv = true;

			// set up default and small fonts
			Graphics2D g2 = (Graphics2D) g;

			// Font sm_font = def_font.deriveFont(6f);
			Font sm_font = Font.decode(Prefs.getPref(PrefName.APPTFONT));
			Map stmap = new HashMap();
			stmap.put(TextAttribute.STRIKETHROUGH,
					TextAttribute.STRIKETHROUGH_ON);
			stmap.put(TextAttribute.FONT, sm_font);
			// Font strike_font = sm_font.deriveFont(stmap);

			g2.setColor(Color.white);
			g2.fillRect(0, 0, (int) width, (int) height);

			// set color to black
			g2.setColor(Color.black);

			// get font sizes
			int fontHeight = g2.getFontMetrics().getHeight();
			int fontDesent = g2.getFontMetrics().getDescent();

			// translate coordinates based on the amount of the page that
			// is going to be printable on - in other words, set upper right
			// to upper right of printable area - not upper right corner of
			// paper
			g2.translate(pagex, pagey);
			Shape s = g2.getClip();

			GregorianCalendar now = new GregorianCalendar();
			int tmon = now.get(Calendar.MONTH);
			int tyear = now.get(Calendar.YEAR);
			int tdate = now.get(Calendar.DATE);

			// force reload if the date has changed since the last draw
			if (lastDrawDate != tdate) {
				needLoad = true;
			}

			lastDrawDate = tdate;

			GregorianCalendar cal = new GregorianCalendar(year_, month_, 1);
			cal.setFirstDayOfWeek(Prefs.getIntPref(PrefName.FIRSTDOW));

			int caltop = fontHeight;
			daytop = caltop + fontHeight + fontDesent;
			int weekbutwidth = fontHeight + fontDesent;

			// calculate width and height of day boxes (6x7 grid)
			rowheight = ((int) pageHeight - daytop) / 6;
			colwidth = (int) (pageWidth - weekbutwidth) / 7;

			// calculate the bottom and right edge of the grid
			int calbot = 6 * rowheight + daytop;
			int calright = 7 * colwidth;

			setDragBounds(daytop, calbot, 0, (int) pageWidth - weekbutwidth);
			setResizeBounds(0, 0, 0, 0);

			g2.setColor(this.getBackground());
			g2.fillRect(0, caltop, calright, daytop - caltop);
			g2.setColor(Color.black);

			// draw the day names centered in each column - no boxes drawn yet
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

			// reset calendar
			cal.set(year_, month_, 1);
			int fdow = cal.get(Calendar.DAY_OF_WEEK) - cal.getFirstDayOfWeek();
			cal.add(Calendar.DATE, -1 * fdow);

			// print the days - either grayed out or containing a number and
			// appts
			for (int box = 0; box < numBoxes; box++) {

				int boxcol = box % 7;
				int boxrow = box / 7;
				int rowtop = (boxrow * rowheight) + daytop;
				int colleft = boxcol * colwidth;
				int dow = cal.getFirstDayOfWeek() + boxcol;
				if (dow == 8)
					dow = 1;

				// set small font for appt text
				g2.setFont(sm_font);
				int smfontHeight = g2.getFontMetrics().getHeight();

				// set clip to the day box to truncate long appointment text
				g2.clipRect(colleft, rowtop, colwidth, rowheight);
				if (needLoad) {
					try {

						addDateZone(cal.getTime(), 0 * 60, 23 * 60,
								new Rectangle(colleft, rowtop, colwidth,
										rowheight));

						// get the appointment info for the given day
						Day di = Day.getDay(cal.get(Calendar.YEAR), cal
								.get(Calendar.MONTH), cal.get(Calendar.DATE),
								showpub, showpriv, true);

						Color c = new Color(Prefs
								.getIntPref(PrefName.UCS_DEFAULT));

						if (tmon == month_ && tyear == year_
								&& tdate == cal.get(Calendar.DATE)) {
							c = new Color(Prefs.getIntPref(PrefName.UCS_TODAY));
						} else if (di.getHoliday() != 0) {
							c = new Color(Prefs
									.getIntPref(PrefName.UCS_HOLIDAY));
						} else if (di.getVacation() == 1) {
							c = new Color(Prefs
									.getIntPref(PrefName.UCS_VACATION));
						} else if (di.getVacation() == 2) {
							c = new Color(Prefs
									.getIntPref(PrefName.UCS_HALFDAY));
						} else if (dow == Calendar.SUNDAY
								|| dow == Calendar.SATURDAY) {
							c = new Color(Prefs
									.getIntPref(PrefName.UCS_WEEKEND));
						} else {
							c = new Color(Prefs
									.getIntPref(PrefName.UCS_WEEKDAY));
						}

						if (cal.get(Calendar.MONTH) != month_)
							c = this.getBackground();

						colors[box] = c;

						// Iterator it = appts.iterator();
						Iterator it = di.getAppts().iterator();

						int notey = rowtop + smfontHeight;

						// loop through appts
						while (it.hasNext()) {

							addNoteBox(cal.getTime(), (Appointment) it.next(),
									new Rectangle(colleft + 2, notey,
											colwidth - 4, smfontHeight),
									new Rectangle(colleft, rowtop, colwidth,
											rowheight));
							// increment Y coord for next note text
							notey += smfontHeight;

						}

						// check if we clipped some appts
						Icon clipIcon = null;
						if (notey > rowtop + rowheight) {
							// draw clipping indication icon
							clipIcon = new ImageIcon(getClass().getResource(
									"/resource/Import16.gif"));
						}

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
							public void edit() {
								MultiView.getMainView().setView(MultiView.DAY);
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

				// fill box
				g2.setColor(colors[box]);
				g2.fillRect(colleft, rowtop, colwidth, rowheight);

				// increment the day
				cal.add(Calendar.DATE, 1);
			}

			// week buttons
			if (needLoad) {
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
						public void edit() {
							MultiView.getMainView().setView(MultiView.WEEK);
							GregorianCalendar gc = new GregorianCalendar();
							gc.setTime(getDate());
							MultiView.getMainView().goTo(gc);
						}
					});
				}
			}

			needLoad = false;

			drawBoxes(g2);
			g2.setClip(s);

			// draw the lines last
			// top of calendar - above day names
			g2.drawLine(0, caltop, calright, caltop);
			for (int row = 0; row < 7; row++) {
				int rowtop = (row * rowheight) + daytop;

				// horizontal lines from below day names to bottom
				g2.drawLine(0, rowtop, calright, rowtop);
			}

			for (int col = 0; col < 8; col++) {
				int colleft = (col * colwidth);

				// vertical lines
				g2.drawLine(colleft, caltop, colleft, calbot);

			}

			return Printable.PAGE_EXISTS;
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			try {

				drawIt(g, getWidth(), getHeight(), getWidth() - 20,
						getHeight() - 20, 10, 10, 0);

			} catch (Exception e) {
				// Errmsg.errmsg(e);
				e.printStackTrace();
			}
		}

		public Date getDateForCoord(double x, double y) {

			int col = (int) x / colwidth;
			int row = ((int) y - daytop) / rowheight;
			GregorianCalendar cal = new GregorianCalendar(year_, month_, 1);
			cal.setFirstDayOfWeek(Prefs.getIntPref(PrefName.FIRSTDOW));
			int fdow = cal.get(Calendar.DAY_OF_WEEK) - cal.getFirstDayOfWeek();
			cal.add(Calendar.DATE, -1 * fdow);
			cal.add(Calendar.DATE, row * 7 + col);
			return cal.getTime();
		}

		public void refresh() {
			clearData();
			repaint();
		}

		public void remove() {

		}

		public void prefsChanged() {
			clearData();
			repaint();

		}

		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getWheelRotation() > 0) {
				next();
				nav.setLabel(getNavLabel());
			} else if (e.getWheelRotation() < 0) {
				prev();
				nav.setLabel(getNavLabel());
			}

		}

	}

	private NavPanel nav = null;

	private MonthViewSubPanel wp_ = null;

	public MonthPanel(int month, int year) {

		wp_ = new MonthViewSubPanel(month, year);
		nav = new NavPanel(wp_);

		setLayout(new java.awt.GridBagLayout());

		GridBagConstraints cons = new GridBagConstraints();

		cons.gridx = 0;
		cons.gridy = 0;
		cons.fill = java.awt.GridBagConstraints.BOTH;
		add(nav, cons);

		cons.gridy = 1;
		cons.weightx = 1.0;
		cons.weighty = 1.0;
		add(wp_, cons);

	}

	public void goTo(Calendar cal) {
		wp_.goTo(cal);
		nav.setLabel(wp_.getNavLabel());
	}

	public void printMonths() {
		try {
			MonthPrintPanel.printMonths(wp_.month_, wp_.year_);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	public int print(Graphics arg0, PageFormat arg1, int arg2)
			throws PrinterException {
		return wp_.print(arg0, arg1, arg2);
	}
}
