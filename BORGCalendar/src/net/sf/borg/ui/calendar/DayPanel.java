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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextAttribute;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JPanel;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.Day;
import net.sf.borg.ui.NavPanel;
import net.sf.borg.ui.Navigator;
import net.sf.borg.ui.calendar.ApptDayBoxLayout.ApptDayBox;
import net.sf.borg.ui.calendar.ApptDayBoxLayout.DateZone;

// weekPanel handles the printing of a single week
public class DayPanel extends JPanel implements Printable {
    private class DaySubPanel extends ApptBoxPanel implements Navigator, Prefs.Listener, Printable {

	private int year_;

	private int month_;

	private int date_;

	// set up dash line stroke
	private float dash1[] = { 1.0f, 3.0f };

	private BasicStroke dashed = new BasicStroke(0.02f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3.0f, dash1,
		0.0f);

	// print does the actual formatting of the printout
	public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
	    // only print 1 page
	    if (pageIndex > 0)
		return Printable.NO_SUCH_PAGE;
	    Font sm_font = Font.decode(Prefs.getPref(PrefName.DAYVIEWFONT));
	    return (drawIt(g, pageFormat.getWidth(), pageFormat.getHeight(), pageFormat.getImageableWidth(), pageFormat
		    .getImageableHeight(), pageFormat.getImageableX(), pageFormat.getImageableY(), sm_font));
	}

	private int drawIt(Graphics g, double width, double height, double pageWidth, double pageHeight, double pagex,
		double pagey, Font sm_font) {

	    clearBoxes();
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
	    Map stmap = new HashMap();
	    stmap.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
	    stmap.put(TextAttribute.FONT, sm_font);

	    g2.setColor(Color.white);
	    g2.fillRect(0, 0, (int) width, (int) height);
	    g2.setColor(Color.black);

	    // get font sizes
	    int fontHeight = g2.getFontMetrics().getHeight();
	    int fontDesent = g2.getFontMetrics().getDescent();

	    g2.translate(pagex, pagey);

	    // save original clip
	    Shape s = g2.getClip();

	    // save begin/end date and build title
	    GregorianCalendar cal = new GregorianCalendar(year_, month_, date_, 23, 59);

	    int caltop = fontHeight + fontDesent; // cal starts under title

	    // height of box containing day's appts
	    double rowheight = pageHeight - caltop;

	    // width of column with time scale (Y axis)
	    double timecolwidth = pageWidth / 15;

	    // top of schedules appts. non-schedules appts appear above this
	    // and get 1/6 of the day box
	    double aptop = caltop + rowheight / 6;

	    // width of each day column - related to timecolwidth
	    double colwidth = (pageWidth - timecolwidth);

	    // calculate the bottom and right edge of the grid
	    int calbot = (int) rowheight + caltop;

	    setResizeBounds((int) aptop, calbot);

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

	    g2.setColor(new Color(234, 234, 255));
	    g2.fillRect(0, caltop, (int) timecolwidth, calbot - caltop);
	    g2.setColor(Color.BLACK);

	    // draw background for appt area
	    g2.setColor(new Color(255, 250, 245));
	    g2.fillRect((int) timecolwidth, caltop, (int) (pageWidth - timecolwidth), (int) pageHeight - caltop);
	    g2.setColor(Color.BLACK);

	    // draw dashed lines for 1/2 hour intervals
	    Stroke defstroke = g2.getStroke();
	    g2.setStroke(dashed);
	    for (int row = 0; row < numhalfhours; row++) {
		int rowtop = (int) ((row * tickheight) + aptop);
		g2.drawLine((int) timecolwidth, rowtop, (int) pageWidth, rowtop);
	    }
	    g2.setStroke(defstroke);

	    // set small font for appt text
	    g2.setFont(sm_font);
	    int smfontHeight = g2.getFontMetrics().getHeight();

	    int colleft = (int) (timecolwidth);

	    addDateZone(new DateZone(cal.getTime(), starthr * 60, endhr * 60), colleft, 0, colwidth, calbot);

	    try {

		if (needLoad) {
		    // get the appointment info for the given day
		    Day di = Day.getDay(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), showpub, showpriv,
			    true);

		    if (di != null) {

			Collection appts = di.getAppts();
			if (appts != null) {
			    layout = new ApptDayBoxLayout(cal.getTime(), appts, starthr, endhr);
			}
		    }
		}

		if (layout != null) {

		    // Iterator it = appts.iterator();
		    Iterator it = layout.getBoxes().iterator();

		    // determine x coord for all appt text
		    // int apptx = colleft + 2 * fontDesent;

		    // determine Y coord for non-scheduled appts (notes)
		    // they will be above the timed appt area
		    int notey = caltop;// + smfontHeight;

		    // count of appts used to vary color
		    int apptnum = 0;

		    // loop through appts
		    while (it.hasNext()) {
			ApptDayBox box = (ApptDayBox) it.next();
			// Appointment ai = box.getAppt();

			// add a single appt text
			// if the appt falls outside the grid - leave it as a
			// note on top
			if (box.isOutsideGrid()) {

			    addBox(box, colleft + 2, notey, colwidth - 4, smfontHeight, apptnum);
			    // increment Y coord for next note text
			    notey += smfontHeight;
			} else {

			    addBox(box, colleft + 4, aptop, colwidth - 8, calbot - aptop, apptnum);
			    apptnum++;
			}

			// reset to black
			g2.setColor(Color.black);

			// reset the clip
			g2.setClip(s);

		    }
		}

		drawBoxes(g2);

		// reset the clip or bad things happen
		g2.setClip(s);

	    } catch (Exception e) {
		Errmsg.errmsg(e);
	    }

	    // draw the box lines last so they show on top of other stuff
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

		String tmlabel = Integer.toString(hr) + ":00";
		g2.drawString(tmlabel, 2, y + smfontHeight / 2);
	    }

	    g2.drawLine(colleft, caltop, colleft, calbot);
	    g2.drawLine((int) pageWidth, caltop, (int) pageWidth, calbot);

	    needLoad = false;
	    return Printable.PAGE_EXISTS;
	}

	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    try {
		Font sm_font = Font.decode(Prefs.getPref(PrefName.DAYVIEWFONT));
		drawIt(g, getWidth(), getHeight(), getWidth() - 20, getHeight() - 20, 10, 10, sm_font);

	    } catch (Exception e) {
		Errmsg.errmsg(e);
	    }
	}

	public DaySubPanel(int month, int year, int date) {
	    year_ = year;
	    month_ = month;
	    date_ = date;
	    clearData();
	    Prefs.addListener(this);
	}

	private ApptDayBoxLayout layout;

	boolean needLoad = true;

	public void clearData() {
	    layout = null;
	    needLoad = true;
	    setToolTipText(null);
	}

	public void next() {
	    GregorianCalendar cal = new GregorianCalendar(year_, month_, date_, 23, 59);
	    cal.add(Calendar.DATE, 1);
	    year_ = cal.get(Calendar.YEAR);
	    month_ = cal.get(Calendar.MONTH);
	    date_ = cal.get(Calendar.DATE);
	    clearData();
	    repaint();
	}

	public void prev() {
	    GregorianCalendar cal = new GregorianCalendar(year_, month_, date_, 23, 59);
	    cal.add(Calendar.DATE, -1);
	    year_ = cal.get(Calendar.YEAR);
	    month_ = cal.get(Calendar.MONTH);
	    date_ = cal.get(Calendar.DATE);
	    clearData();
	    repaint();
	}

	public void today() {
	    GregorianCalendar cal = new GregorianCalendar();
	    year_ = cal.get(Calendar.YEAR);
	    month_ = cal.get(Calendar.MONTH);
	    date_ = cal.get(Calendar.DATE);
	    clearData();
	    repaint();
	}

	public void goTo(Calendar cal) {
	    year_ = cal.get(Calendar.YEAR);
	    month_ = cal.get(Calendar.MONTH);
	    date_ = cal.get(Calendar.DATE);
	    clearData();
	    repaint();
	}

	public void prefsChanged() {
	    clearData();
	    repaint();

	}

	Date getDateForX(double x) {
	    GregorianCalendar cal = new GregorianCalendar(year_, month_, date_, 23, 59);
	    return cal.getTime();
	}

	public String getNavLabel() {
	    GregorianCalendar cal = new GregorianCalendar(year_, month_, date_, 23, 59);
	    Date dt = cal.getTime();
	    DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
	    return df.format(dt);
	}

    }
    
    private NavPanel nav = null;
    private DaySubPanel dp_ = null;
    public DayPanel(int month, int year, int date) {
	   
	    dp_ = new DaySubPanel(month, year, date);
	    nav = new NavPanel(dp_);

	    setLayout(new java.awt.GridBagLayout());

	    GridBagConstraints cons = new GridBagConstraints();

	    cons.gridx = 0;
	    cons.gridy = 0;
	    cons.fill = java.awt.GridBagConstraints.BOTH;
	    add(nav, cons);

	    cons.gridy = 1;
	    cons.weightx = 1.0;
	    cons.weighty = 1.0;
	    add(dp_, cons);
	
    }
    
    public void goTo(Calendar cal) {
	   dp_.goTo(cal);
	   nav.setLabel(dp_.getNavLabel());
	}

    public int print(Graphics arg0, PageFormat arg1, int arg2) throws PrinterException {
	return dp_.print(arg0,arg1, arg2);
    }
}
