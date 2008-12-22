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
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JPanel;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Day;
import net.sf.borg.model.Model;
import net.sf.borg.model.beans.CalendarBean;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.NavPanel;
import net.sf.borg.ui.Navigator;

public class YearPanel extends JPanel implements Printable {

	// monthPanel handles the printing of a single month
	private class YearViewSubPanel extends ApptBoxPanel implements Printable, Navigator, Model.Listener, Prefs.Listener,
			MouseWheelListener {

		private int year_;

		private int colwidth;

		private int rowheight;

		private int daytop;

		final private int numBoxes = 37 * 12;

		Color colors[] = new Color[numBoxes];

		boolean needLoad = true;

		public YearViewSubPanel(int year) {
			year_ = year;
			clearData();
			Prefs.addListener(this);
			addMouseWheelListener(this);
			AppointmentModel.getReference().addListener(this);
		}

		public void clearData() {
			clearBoxes();
			needLoad = true;
			setToolTipText(null);
		}

		public String getNavLabel() {
			SimpleDateFormat df = new SimpleDateFormat("yyyy");
			Calendar cal = new GregorianCalendar(year_, Calendar.JANUARY, 1);
			return df.format(cal.getTime());
		}

		public void goTo(Calendar cal) {
			year_ = cal.get(Calendar.YEAR);
			clearData();
			repaint();
		}

		public void next() {
			year_++;
			clearData();
			repaint();
		}

		public void prev() {
			year_--;
			clearData();
			repaint();
		}

		// print does the actual formatting of the printout
		public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {

			if (pageIndex > 0)
				return Printable.NO_SUCH_PAGE;
			Font sm_font = Font.decode(Prefs.getPref(PrefName.MONTHVIEWFONT));
			clearData();
			int ret = drawIt(g, pageFormat.getWidth(), pageFormat.getHeight(), pageFormat.getImageableWidth(), pageFormat
					.getImageableHeight(), pageFormat.getImageableX(), pageFormat.getImageableY(), pageIndex, sm_font);
			refresh();
			return ret;
		}

		public void today() {
			GregorianCalendar cal = new GregorianCalendar();
			year_ = cal.get(Calendar.YEAR);
			clearData();
			repaint();
		}

		private int lastDrawDate = -1;

		private int drawIt(Graphics g, double width, double height, double pageWidth, double pageHeight, double pagex,
				double pagey, int pageIndex, Font sm_font) {

			// set up default and small fonts
			Graphics2D g2 = (Graphics2D) g;

			// Font sm_font = def_font.deriveFont(6f);

			g2.setFont(sm_font);

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

			GregorianCalendar cal = new GregorianCalendar(year_, Calendar.JANUARY, 1);
			cal.setFirstDayOfWeek(Prefs.getIntPref(PrefName.FIRSTDOW));

			int caltop = fontHeight;
			daytop = caltop + fontHeight + fontDesent;
			int monthwidth = (int) pageWidth / 10;

			// calculate width and height of day boxes (6x7 grid)
			rowheight = ((int) pageHeight - daytop) / 12;
			colwidth = (int) (pageWidth - monthwidth) / 37;

			int calright = monthwidth + colwidth * 37;
			int calbot = daytop + 12 * rowheight;

			setDragBounds(0, 0, 0, 0);
			setResizeBounds(0, 0, 0, 0);

			g2.setColor(this.getBackground());
			g2.fillRect(0, caltop, calright, daytop - caltop);
			g2.setColor(Color.black);

			SimpleDateFormat dfm = new SimpleDateFormat("MMMM");

			// draw the day names centered in each column - no boxes drawn yet
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

			int mon = Calendar.JANUARY;
			for (int row = 0; row < 12; row++) {
				cal.set(year_, mon, 1);

				int fdow = cal.get(Calendar.DAY_OF_WEEK) - cal.getFirstDayOfWeek();
				if (fdow == -1)
					fdow = 6;
				cal.add(Calendar.DATE, -1 * fdow);

				// print the days - either grayed out or containing a number and
				// appts
				for (int col = 0; col < 37; col++) {

					int numbox = row * 37 + col;
					int rowtop = (row * rowheight) + daytop;
					int colleft = monthwidth + (col * colwidth);
					int dow = cal.get(Calendar.DAY_OF_WEEK);

					// set clip to the day box to truncate long appointment text
					g2.clipRect(colleft, rowtop, colwidth, rowheight);
					if (needLoad) {
						try {

							if (cal.get(Calendar.MONTH) != mon) {
								colors[numbox] = this.getBackground();
							} else {

								if (cal.get(Calendar.DATE) == 1) {
									boxes.add(new ButtonBox(cal.getTime(), dfm.format(cal.getTime()), null, new Rectangle(2,
											rowtop, monthwidth - 4, fontHeight), new Rectangle(0, rowtop, monthwidth, rowheight)) {
										public void edit() {
											MultiView.getMainView().setView(MultiView.MONTH);
											GregorianCalendar gc = new GregorianCalendar();
											gc.setTime(getDate());
											MultiView.getMainView().goTo(gc);
										}
									});
								}
								// addDateZone(cal.getTime(), 0 * 60, 23 * 60,
								// new Rectangle(colleft, rowtop,
								// colwidth, rowheight));

								// get the appointment info for the given day
								Day di = Day.getDay(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), true,
										true, true);

								Color bbg = null;

								Color c = new Color(Prefs.getIntPref(PrefName.UCS_DEFAULT));

								if (di != null) {
									Collection<CalendarBean> appts = di.getItems();
									if (appts != null && !appts.isEmpty()) {
										bbg = Color.pink;
									}
									if (tmon == cal.get(Calendar.MONTH) && tyear == year_ && tdate == cal.get(Calendar.DATE)) {
										c = new Color(Prefs.getIntPref(PrefName.UCS_TODAY));
									} else if (di.getHoliday() != 0) {
										c = new Color(Prefs.getIntPref(PrefName.UCS_HOLIDAY));
									} else if (di.getVacation() == 1) {
										c = new Color(Prefs.getIntPref(PrefName.UCS_VACATION));
									} else if (di.getVacation() == 2) {
										c = new Color(Prefs.getIntPref(PrefName.UCS_HALFDAY));
									} else if (dow == Calendar.SUNDAY || dow == Calendar.SATURDAY) {
										c = new Color(Prefs.getIntPref(PrefName.UCS_WEEKEND));
									} else {
										c = new Color(Prefs.getIntPref(PrefName.UCS_WEEKDAY));
									}
								}

								colors[numbox] = c;

								String datetext = Integer.toString(cal.get(Calendar.DATE));

								boxes.add(new ButtonBox(cal.getTime(), datetext, null, new Rectangle(colleft + 2, rowtop,
										colwidth - 4, fontHeight), new Rectangle(colleft, rowtop, colwidth, rowheight), bbg) {
									public void edit() {
										MultiView.getMainView().setView(MultiView.DAY);
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

					// fill box
					g2.setColor(colors[numbox]);
					g2.fillRect(colleft, rowtop, colwidth, rowheight);

					// increment the day
					cal.add(Calendar.DATE, 1);
				}

				mon++;

			}
			needLoad = false;

			drawBoxes(g2);
			g2.setClip(s);

			// draw the lines last
			// top of calendar - above day names
			g2.drawLine(0, caltop, calright, caltop);
			g2.drawLine(0, caltop, 0, calbot);
			for (int row = 0; row <= 12; row++) {
				int rowtop = (row * rowheight) + daytop;
				g2.drawLine(0, rowtop, calright, rowtop);
			}

			for (int col = 0; col <= 37; col++) {
				int colleft = monthwidth + (col * colwidth);
				g2.drawLine(colleft, caltop, colleft, calbot);
			}

			return Printable.PAGE_EXISTS;
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			try {
				Font sm_font = Font.decode(Prefs.getPref(PrefName.WEEKVIEWFONT));
				drawIt(g, getWidth(), getHeight(), getWidth() - 20, getHeight() - 20, 10, 10, 0, sm_font);

			} catch (Exception e) {
				// Errmsg.errmsg(e);
				e.printStackTrace();
			}
		}

		public Date getDateForCoord(double x, double y) {

			int col = (int) x / colwidth;
			int row = ((int) y - daytop) / rowheight;
			GregorianCalendar cal = new GregorianCalendar(year_, row + 1, 1);
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

	private YearViewSubPanel wp_ = null;

	public YearPanel(int year) {

		wp_ = new YearViewSubPanel(year);
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

	public int print(Graphics arg0, PageFormat arg1, int arg2) throws PrinterException {
		return wp_.print(arg0, arg1, arg2);
	}
}
