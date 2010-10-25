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
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.Serializable;
import java.text.AttributedString;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.PrintHelper;
import net.sf.borg.common.Resource;
import net.sf.borg.model.Day;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.CalendarEntity;

/**
 * MonthPrintPanel creates the Borg month printouts
 */
public class MonthPrintPanel extends JPanel implements Printable {

	private static final long serialVersionUID = 1L;

	// scale factor used when this class draws a month printout on the UI
	// not currently used
	static private final double prev_scale = 1.5;

	/**
	 * prompt the user for a number of months to print (1-12), starting with the currently displayed one,
	 * and then print the months
	 * @param month displayed month
	 * @param year displayed year
	 * @throws Exception
	 */
	public static void printMonths(int month, int year) throws Exception {

		// use the Java print service
		// this relies on monthPanel.print to fill in a Graphic object and
		// respond to the Printable API
		MonthPrintPanel cp = new MonthPrintPanel(month, year);
		Object options[] = { new Integer(1), new Integer(2), new Integer(3),
				new Integer(4), new Integer(5), new Integer(6), new Integer(7),
				new Integer(8), new Integer(9), new Integer(10),
				new Integer(11), new Integer(12) };
		Object choice = JOptionPane.showInputDialog(null, Resource
				.getResourceString("nummonths"), Resource
				.getResourceString("Print_Chooser"),
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (choice == null)
			return;
		
		// print the months
		Integer i = (Integer) choice;
		cp.setPages(i.intValue());
		PrintHelper.printPrintable(cp);

	}

	// start month
	private int month_;

	// number of pages to print
	private int pages_ = 1;

	// start year
	private int year_;

	/**
	 * constructor
	 * @param month start month
	 * @param year start year
	 */
	public MonthPrintPanel(int month, int year) {
		year_ = year;
		month_ = month;
	}

	/**
	 * draw the month printout to a Graphics
	 */
	private int drawIt(Graphics g, double width, double height,
			double pageWidth, double pageHeight, double pagex, double pagey,
			int pageIndex) {
		
		Graphics2D g2 = (Graphics2D) g;

		int year;
		int month;

		// see if color printout option set
		String cp = "false";
		try {
			cp = Prefs.getPref(PrefName.COLORPRINT);
		} catch (Exception e) {
		  // empty
		}

		// get fonts
		Font def_font = g2.getFont();
		Font sm_font = Font.decode(Prefs.getPref(PrefName.PRINTFONT));
		Map<TextAttribute, Serializable> stmap = new HashMap<TextAttribute, Serializable>();
		stmap.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		stmap.put(TextAttribute.FONT, sm_font);

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

		// determine month based on the page we are on (pageIndex)
		GregorianCalendar cal = new GregorianCalendar(year_, month_, 1);
		cal.add(Calendar.MONTH, pageIndex);
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		
		// set first day of week
		cal.setFirstDayOfWeek(Prefs.getIntPref(PrefName.FIRSTDOW));

		// month title
		Date then = cal.getTime();
		SimpleDateFormat sd = new SimpleDateFormat("MMMM yyyy");
		String title = sd.format(then);

		// draw of title at correct height and centered
		// horizontally on page
		int titlewidth = g2.getFontMetrics().stringWidth(title);
		int caltop = fontHeight + fontDesent;
		int daytop = caltop + fontHeight + fontDesent;
		g2.drawString(title, ((int) pageWidth - titlewidth) / 2, fontHeight);

		// calculate width and height of day boxes (6x7 grid)
		int rowheight = ((int) pageHeight - daytop) / 6;
		int colwidth = (int) pageWidth / 7;

		// calculate the bottom and right edge of the grid
		int calbot = 6 * rowheight + daytop;
		int calright = 7 * colwidth;

		// draw the day of week names centered in each column - no boxes drawn yet
		SimpleDateFormat dfw = new SimpleDateFormat("EEE");
		cal.add(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek()
				- cal.get(Calendar.DAY_OF_WEEK));	
		for (int col = 0; col < 7; col++) {
			int colleft = (col * colwidth);
			String dayofweek = dfw.format(cal.getTime());
			int swidth = g2.getFontMetrics().stringWidth(dayofweek);
			g2.drawString(dayofweek, colleft + (colwidth - swidth) / 2, caltop
					+ fontHeight);
			cal.add(Calendar.DAY_OF_WEEK, 1);
		}

		// reset date
		cal.set(year, month, 1);
		int fdow = cal.get(Calendar.DAY_OF_WEEK) - cal.getFirstDayOfWeek();

		// print the days - either grayed out or containing a number and appts
		for (int box = 0; box < 42; box++) {

			// month length
			int mlen = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

			// determine row and column
			int boxcol = box % 7;
			int boxrow = box / 7;
			
			// determine top left of the current day box
			int rowtop = (boxrow * rowheight) + daytop;
			int colleft = boxcol * colwidth;
			
			// get day of week
			int dow = cal.getFirstDayOfWeek() + boxcol;
			if (dow == 8)
				dow = 1;

			// if box in grid is before first day or after last of the month, just draw a
			// gray box
			if (box < fdow || box > fdow + mlen - 1) {
				// gray
				if (cp.equals("false")) {
					// onyl draw gray box if color print option is not on
					g2.setColor(new Color(235, 235, 235));
					g2.fillRect(colleft, rowtop, colwidth, rowheight);
					g2.setColor(Color.black);
				}
			} else {
				
				// get date
				int date = box - fdow + 1;

				// set small font for appt text
				g2.setFont(sm_font);
				int smfontHeight = g2.getFontMetrics().getHeight();

				// set clip to the day box to truncate long appointment text
				g2.clipRect(colleft, rowtop, colwidth, rowheight);
				
				try {

					// get the appointment info for the given day
					GregorianCalendar gc = new GregorianCalendar(year, month,
							date);
					Day dayInfo = Day.getDay(year, month, date);
					if (dayInfo != null) {
						
						// vary the background color for color printing
						// the user colors are not used. colors that don't waste ink are used
						if (cp.equals("true")) {
							if (dayInfo.getVacation() != 0) {
								g2.setColor(new Color(225, 255, 225));
							} else if (dayInfo.getHoliday() == 1) {
								g2.setColor(new Color(255, 225, 195));
							} else if (dow == Calendar.SUNDAY
									|| dow == Calendar.SATURDAY) {
								g2.setColor(new Color(255, 225, 195));
							} else {
								g2.setColor(new Color(255, 245, 225));
							}

							g2.fillRect(colleft, rowtop, colwidth, rowheight);
							g2.setColor(Color.black);
						}
						
						
						Collection<CalendarEntity> appts = dayInfo.getItems();
						if (appts != null) {

							// determine X,Y coords of first appt text
							int apptx = colleft + 2 * fontDesent;
							int appty = rowtop + fontHeight + smfontHeight;

							for( CalendarEntity entity : appts ) {

								// change color for a single appointment based
								// on its color - only if color print option set
								// *** this uses the original borg colors - not the user colors
								// should be changed some time
								if (cp.equals("false"))
									g2.setColor(Color.black);
								else if (entity.getColor().equals("red"))
									g2.setColor(Color.red);
								else if (entity.getColor().equals("green"))
									g2.setColor(Color.green);
								else if (entity.getColor().equals("blue"))
									g2.setColor(Color.blue);
								
								String text = entity.getText();
								if( entity instanceof Appointment)
								{
									text = AppointmentTextFormat.format((Appointment)entity, gc.getTime());
								}
								
								// skip items that are strike-through if the option to skip them is set
								if (ApptBoxPanel.isStrike(entity, gc.getTime())) {
									if (Prefs
											.getBoolPref(PrefName.HIDESTRIKETHROUGH))
										continue;

									// draw strike-through text
									// need to use AttributedString to work
									// around a bug
									AttributedString as = new AttributedString(
											text, stmap);
									g2.drawString(as.getIterator(), apptx,
											appty);
								} else {
									// draw the entity text
									g2.drawString(text, apptx, appty);
								}

								// increment the Y coord
								appty += smfontHeight;

								// reset to black
								g2.setColor(Color.black);
							}
						}
					}

					g2.setClip(s);

				} catch (Exception e) {
					Errmsg.errmsg(e);
				}

				// draw date
				g2.setFont(def_font);
				g2.drawString(Integer.toString(date), colleft + fontDesent,
						rowtop + fontHeight);

			}

		}

		// draw the lines last

		// top border
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

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		try {
			Graphics2D g2 = (Graphics2D) g;
			g2.scale(prev_scale, prev_scale);
			drawIt(g, getWidth() / prev_scale, getHeight() / prev_scale,
					getWidth() / prev_scale - 20,
					getHeight() / prev_scale - 20, 10, 10, 0);

		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}

	/**
	 * print is called by the print service to print pages
	 */
	@Override
	public int print(Graphics g, PageFormat pageFormat, int pageIndex)
			throws PrinterException {

		// if we have exceeded the requested number of pages - tell the print
		// service that we are done
		if (pageIndex > pages_ - 1)
			return Printable.NO_SUCH_PAGE;

		return (drawIt(g, pageFormat.getWidth(), pageFormat.getHeight(),
				pageFormat.getImageableWidth(),
				pageFormat.getImageableHeight(), pageFormat.getImageableX(),
				pageFormat.getImageableY(), pageIndex));
	}

	/**
	 * set the number of months (pages) to print
	 * @param p number of pages
	 */
	public void setPages(int p) {
		pages_ = p;
	}
}
