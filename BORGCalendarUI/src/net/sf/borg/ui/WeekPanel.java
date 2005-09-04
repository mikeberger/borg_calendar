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
package net.sf.borg.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JPanel;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Resource;

import net.sf.borg.model.Appointment;
import net.sf.borg.model.Day;
import net.sf.borg.ui.ApptDayBoxLayout.ApptDayBox;

// weekPanel handles the printing of a single week
class WeekPanel extends JPanel implements Printable {

	private int year_;

	private int month_;

	private int date_;

	// set up dash line stroke
	final static private float dash1[] = { 1.0f, 3.0f };

	final static private BasicStroke dashed = new BasicStroke(0.02f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3.0f, dash1, 0.0f);

	// print does the actual formatting of the printout
	public int print(Graphics g, PageFormat pageFormat, int pageIndex)
			throws PrinterException {

		// only print 1 page
		if (pageIndex > 0)
			return Printable.NO_SUCH_PAGE;

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
		Font def_font = g2.getFont();
		Font sm_font = def_font.deriveFont(6f);
		Map stmap = new HashMap();
		stmap.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		stmap.put(TextAttribute.FONT, sm_font);

		g2.setColor(Color.white);
		g2.fillRect(0, 0, (int) pageFormat.getWidth(), (int) pageFormat
				.getHeight());
		g2.setColor(Color.black);

		// get font sizes
		int fontHeight = g2.getFontMetrics().getHeight();
		int fontDesent = g2.getFontMetrics().getDescent();

		// get page size for the given printer
		double pageHeight = pageFormat.getImageableHeight();
		double pageWidth = pageFormat.getImageableWidth();

		// translate coordinates based on the amount of the page that
		// is going to be printable on - in other words, set upper right
		// to upper right of printable area - not upper right corner of
		// paper
		g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

		// save original clip
		Shape s = g2.getClip();

		// set up calendar and determine first day of week from options
		GregorianCalendar cal = new GregorianCalendar(year_, month_, date_,23,59);
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
		SimpleDateFormat sd = new SimpleDateFormat("MMM dd, yyyy");
		String title = sd.format(beg) + " "
				+ Resource.getResourceString("__through__") + " "
				+ sd.format(end);

		// reset cal
		cal.setTime(beg);

		// determine placement of title at correct height and centered
		// horizontally on page
		int titlewidth = g2.getFontMetrics().stringWidth(title);

		g2.drawString(title, ((int) pageWidth - titlewidth) / 2, fontHeight);

		int caltop = fontHeight + fontDesent; // cal starts under title
		int daytop = caltop + fontHeight + fontDesent; // day starts under day
		// labels

		// height of box containing day's appts
		double rowheight = pageHeight - daytop;

		// width of column with time scale (Y axis)
		double timecolwidth = pageWidth / 21;

		// top of schedules appts. non-schedules appts appear above this
		// and get 1/6 of the day box
		double aptop = daytop + rowheight / 6;

		// width of each day column - related to timecolwidth
		double colwidth = (pageWidth - timecolwidth) / 7;

		// calculate the bottom and right edge of the grid
		int calbot = (int) rowheight + daytop;

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
		SimpleDateFormat dfw = new SimpleDateFormat("EEE MMM dd");

		// add day labels
		for (int col = 0; col < 7; col++) {

			int colleft = (int) (timecolwidth + (col * colwidth));
			String dayofweek = dfw.format(cal.getTime());
			int swidth = g2.getFontMetrics().stringWidth(dayofweek);
			g2.drawString(dayofweek, (int) (colleft + (colwidth - swidth) / 2),
					caltop + fontHeight);
			cal.add(Calendar.DATE, 1);
		}

		// draw background for appt area
		g2.setColor(new Color(255, 245, 225));
		g2.fillRect((int) timecolwidth, daytop,
				(int) (pageWidth - timecolwidth), (int) pageHeight - daytop);
		g2.setColor(Color.BLACK);

		// draw dashed lines for 1/2 hour intervals
		Stroke defstroke = g2.getStroke();
		g2.setStroke(dashed);
		for (int row = 0; row < numhalfhours; row++) {
			int rowtop = (int) ((row * tickheight) + aptop);
			g2.drawLine((int) timecolwidth, rowtop, (int) pageWidth, rowtop);
		}
		g2.setStroke(defstroke);

		// reset calendar
		cal.setTime(beg);

		// set small font for appt text
		g2.setFont(sm_font);
		int smfontHeight = g2.getFontMetrics().getHeight();
		// int smfontDesent=g2.getFontMetrics().getDescent();

		// array of colors for appt boxes - each appt gets a different color
		Color acolor[] = new Color[3];
		acolor[0] = new Color(204, 255, 204);
		acolor[1] = new Color(204, 204, 255);
		acolor[2] = new Color(255, 204, 204);

		boolean wrap = false;
		sp = Prefs.getPref(PrefName.WRAP);
		if (sp.equals("true"))
			wrap = true;
		Hashtable atmap = null;
		if (wrap) {
			atmap = new Hashtable();
			atmap.put(TextAttribute.FONT, sm_font);
		}

		// this is the main part of the drawing. The appts are drawn in this
		// loop
		// loop through the 7 days
		for (int col = 0; col < 7; col++) {

			int colleft = (int) (timecolwidth + col * colwidth);
			try {
				// get the appointment info for the given day
				Day di = Day.getDay(cal.get(Calendar.YEAR), cal
						.get(Calendar.MONTH), cal.get(Calendar.DATE), showpub,
						showpriv, true);

				if (di != null) {

					Collection appts = di.getAppts();
					if (appts != null) {
						ApptDayBoxLayout layout = new ApptDayBoxLayout(appts,
								starthr, endhr);
						// Iterator it = appts.iterator();
						Iterator it = layout.getBoxes().iterator();

						// determine x coord for all appt text
						int apptx = colleft + 2 * fontDesent;

						// determine Y coord for non-scheduled appts (notes)
						// they will be above the timed appt area
						int notey = daytop + smfontHeight;

						// count of appts used to vary color
						int apptnum = 0;

						// loop through appts
						while (it.hasNext()) {
							ApptDayBox box = (ApptDayBox) it.next();
							Appointment ai = box.getAppt();

							// change color for a single appointment based on
							// its color - only if color print option set
							g2.setColor(Color.black);
							if (ai.getColor().equals("red"))
								g2.setColor(Color.red);
							else if (ai.getColor().equals("green"))
								g2.setColor(Color.green);
							else if (ai.getColor().equals("blue"))
								g2.setColor(Color.blue);

							// add a single appt text
							// if the appt falls outside the grid - leave it as
							// a note on top
							if (box.isOutsideGrid()) {
								// appt is note or is outside timespan shown
								g2.clipRect(colleft, daytop, (int) colwidth,
										(int) aptop);
								if (wrap) {
									String tx = ai.getText();
									AttributedString as = new AttributedString(
											tx, atmap);
									AttributedCharacterIterator para = as
											.getIterator();
									int start = para.getBeginIndex();
									int endi = para.getEndIndex();
									LineBreakMeasurer lbm = new LineBreakMeasurer(
											para, new FontRenderContext(null,
													false, false));
									lbm.setPosition(start);
									int tt = notey;
									while (lbm.getPosition() < endi) {
										TextLayout tlayout = lbm
												.nextLayout((int) colwidth
														- (2 * fontDesent));
										tt += tlayout.getAscent();
										tlayout.draw(g2, apptx + 2, tt);
										tt += tlayout.getDescent()
												+ tlayout.getLeading();
									}
								} else {
									if ((ai.getColor() != null && ai.getColor()
											.equals("strike"))
											|| (ai.getTodo() && !(ai
													.getNextTodo() == null || !ai
													.getNextTodo().after(
															cal.getTime())))) {
										// g2.setFont(strike_font);
										// System.out.println(ai.getText());
										// need to use AttributedString to work
										// around a bug
										AttributedString as = new AttributedString(
												ai.getText(), stmap);
										g2.drawString(as.getIterator(), apptx,
												notey);
									} else {
										// g2.setFont(sm_font);
										g2.drawString(ai.getText(), apptx,
												notey);
									}
								}
								// System.out.println( ai.getText() + " " +
								// notey );

								// increment Y coord for next note text
								notey += smfontHeight;
							} else {

								int appttop = (int) (aptop + (calbot - aptop)
										* box.getTop());
								int apptbot = (int) (aptop + (calbot - aptop)
										* box.getBottom());
								double realwidth = colwidth - 4;
								int apptleft = (int) (colleft + 2 + realwidth
										* box.getLeft());
								int apptright = (int) (colleft + 2 + realwidth
										* box.getRight());
								// draw box outline
								g2.setColor(Color.BLACK);

								// System.out.println( ai.getText()+ " " +
								// apptleft + " " + apptright + " " + appttop +
								// " " + apptbot );
								g2.drawRect(apptleft, appttop, apptright
										- apptleft, apptbot - appttop);

								// fill the box with color
								g2.setColor(acolor[apptnum % 3]);

								g2.fillRect(apptleft, appttop, apptright
										- apptleft, apptbot - appttop);

								// draw the appt text
								g2.setColor(Color.BLACK);

								g2.clipRect(apptleft, appttop, apptright
										- apptleft, apptbot - appttop);

								// add a single appt text
								if (wrap) {
									String tx = ai.getText();
									AttributedString as = new AttributedString(
											tx, atmap);
									AttributedCharacterIterator para = as
											.getIterator();
									int start = para.getBeginIndex();
									int endi = para.getEndIndex();
									LineBreakMeasurer lbm = new LineBreakMeasurer(
											para, new FontRenderContext(null,
													false, false));
									lbm.setPosition(start);
									while (lbm.getPosition() < endi) {
										TextLayout tlayout = lbm
												.nextLayout(apptright
														- apptleft
														- (2 * fontDesent));
										appttop += tlayout.getAscent();
										tlayout.draw(g2, apptleft + 2, appttop);
										appttop += tlayout.getDescent()
												+ tlayout.getLeading();
									}
								} else {
									if ((ai.getColor() != null && ai.getColor()
											.equals("strike"))
											|| (ai.getTodo() && !(ai
													.getNextTodo() == null || !ai
													.getNextTodo().after(
															cal.getTime())))) {
										// g2.setFont(strike_font);
										// System.out.println(ai.getText());
										// need to use AttributedString to work
										// around a bug
										AttributedString as = new AttributedString(
												ai.getText(), stmap);
										g2.drawString(as.getIterator(),
												apptleft + 2, appttop
														+ smfontHeight);
									} else {
										// g2.setFont(sm_font);
										g2.drawString(ai.getText(),
												apptleft + 2, appttop
														+ smfontHeight);
									}
								}

								apptnum++;
							}

							// reset to black
							g2.setColor(Color.black);

							// reset the clip
							g2.setClip(s);

						}
					}
				}

				// reset the clip or bad things happen
				g2.setClip(s);

				// increment the day
				cal.add(Calendar.DATE, 1);

			} catch (Exception e) {
				Errmsg.errmsg(e);
			}

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
			g2.drawString(tmlabel, smfontHeight, y + smfontHeight / 2);
		}

		// the vertical lines
		for (int col = 0; col < 8; col++) {
			int colleft = (int) (timecolwidth + (col * colwidth));
			g2.drawLine(colleft, caltop, colleft, calbot);
		}

		return Printable.PAGE_EXISTS;
	}

	// scale up for the preview to make it easier to see
	static double prev_scale = 1.5;

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		PageFormat pf = new PageFormat();
		pf.setOrientation(PageFormat.LANDSCAPE);
		try {
			Graphics2D g2 = (Graphics2D) g;
			g2.scale(prev_scale, prev_scale);
			print(g, pf, 0);

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	WeekPanel(int month, int year, int date) {
		year_ = year;
		month_ = month;
		date_ = date;

	}

}
