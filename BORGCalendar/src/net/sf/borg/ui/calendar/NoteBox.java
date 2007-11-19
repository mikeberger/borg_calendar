package net.sf.borg.ui.calendar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.beans.Appointment;
import net.sf.borg.ui.MultiView;

// ApptDayBox holds the logical information needs to determine
// how an appointment box should be drawn in a day grid
public class NoteBox implements Draggable {

    private Icon todoIcon = null;

    private Appointment appt = null;

    private Rectangle bounds, clip;

    private Date date; // date being displayed - not necessarily date of

    private boolean isSelected = false;

    public NoteBox(Date d, Appointment ap, Rectangle bounds, Rectangle clip) {
	appt = ap;
	date = d;
	this.bounds = bounds;
	this.clip = clip;

	String iconname = Prefs.getPref(PrefName.UCS_MARKER);
	String use_marker = Prefs.getPref(PrefName.UCS_MARKTODO);
	if (use_marker.equals("true") && (iconname.endsWith(".gif") || iconname.endsWith(".jpg"))) {
	    todoIcon = new javax.swing.ImageIcon(getClass().getResource("/resource/" + iconname));
	}
    }

    /* (non-Javadoc)
     * @see net.sf.borg.ui.calendar.Box#delete()
     */
    public void delete() {
	AppointmentModel.getReference().delAppt(appt.getKey());
    }

    /* (non-Javadoc)
     * @see net.sf.borg.ui.calendar.Box#draw(java.awt.Graphics2D, java.awt.Component)
     */
    public void draw(Graphics2D g2, Component comp) {

	Shape s = g2.getClip();
	if (clip != null)
	    g2.setClip(clip);

	Font sm_font = g2.getFont();
	int smfontHeight = g2.getFontMetrics().getHeight();
	Map stmap = new HashMap();
	stmap.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
	stmap.put(TextAttribute.FONT, sm_font);

	if (isSelected == true) {
	    g2.setColor(Color.WHITE);
	    g2.fillRect(bounds.x, bounds.y + 2, bounds.width, bounds.height);
	}
	g2.setColor(Color.BLACK);

	if (getTextColor().equals("strike")) {

	    AttributedString as = new AttributedString(getText(), stmap);
	    g2.drawString(as.getIterator(), bounds.x + 2, bounds.y + smfontHeight);
	} else {
	    // change color for a single appointment based on
	    // its color - only if color print option set
	    g2.setColor(Color.black);
	    
	    if (getTextColor().equals("red"))
		g2.setColor(new Color(Integer.parseInt(Prefs.getPref(PrefName.UCS_RED))));
	    else if (getTextColor().equals("green"))
		g2.setColor(new Color(Integer.parseInt(Prefs.getPref(PrefName.UCS_GREEN))));
	    else if (getTextColor().equals("blue"))
		g2.setColor(new Color(Integer.parseInt(Prefs.getPref(PrefName.UCS_BLUE))));
	    else if (getTextColor().equals("black"))
		g2.setColor(new Color(Integer.parseInt(Prefs.getPref(PrefName.UCS_BLACK))));
	    else if (getTextColor().equals("white"))
		g2.setColor(new Color(Integer.parseInt(Prefs.getPref(PrefName.UCS_WHITE))));
	    else if (getTextColor().equals("navy"))
		g2.setColor(new Color(Integer.parseInt(Prefs.getPref(PrefName.UCS_NAVY))));
	    else if (getTextColor().equals("purple"))
		g2.setColor(new Color(Integer.parseInt(Prefs.getPref(PrefName.UCS_PURPLE))));
	    else if (getTextColor().equals("brick"))
		g2.setColor(new Color(Integer.parseInt(Prefs.getPref(PrefName.UCS_BRICK))));

	    if (isTodo() && todoIcon != null) {
		todoIcon.paintIcon(comp, g2, bounds.x, bounds.y + 8);
		g2.drawString(getText(), bounds.x + todoIcon.getIconWidth(), bounds.y + smfontHeight);
	    } else {
		g2.drawString(getText(), bounds.x + 2, bounds.y + smfontHeight);
	    }
	    g2.setColor(Color.black);
	}

	g2.setClip(s);
	g2.setColor(Color.black);
    }

    /* (non-Javadoc)
     * @see net.sf.borg.ui.calendar.Box#edit()
     */
    public void edit() {
	GregorianCalendar cal = new GregorianCalendar();
	cal.setTime(date);
	AppointmentListView ag = new AppointmentListView(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
	MultiView.getMainView().addView(ag);
	ag.showApp(appt.getKey());

    }

    /* (non-Javadoc)
     * @see net.sf.borg.ui.calendar.Box#getBounds()
     */
    public Rectangle getBounds() {
	return bounds;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.ui.calendar.Box#getText()
     */
    public String getText() {
	return appt.getText();
    }

    /* (non-Javadoc)
     * @see net.sf.borg.ui.calendar.Box#setBounds(java.awt.Rectangle)
     */
    public void setBounds(Rectangle bounds) {
	this.bounds = bounds;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.ui.calendar.Box#setSelected(boolean)
     */
    public void setSelected(boolean isSelected) {
	this.isSelected = isSelected;
    }

    private String getTextColor() {
	if (appt == null)
	    return null;

	if ((appt.getColor() != null && appt.getColor().equals("strike"))
		|| (appt.getTodo() && !(appt.getNextTodo() == null || !appt.getNextTodo().after(date)))) {
	    return ("strike");
	}
	return appt.getColor();
    }

    private boolean isTodo() {
	return appt.getTodo();
    }

    private JPopupMenu popmenu = null;

    public JPopupMenu getMenu() {
	JMenuItem mnuitm;
	if (popmenu == null) {
	    popmenu = new JPopupMenu();
	    popmenu.add(mnuitm = new JMenuItem(Resource.getPlainResourceString("Edit")));
	    mnuitm.addActionListener(new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    edit();
		}
	    });
	    popmenu.add(mnuitm = new JMenuItem(Resource.getPlainResourceString("Delete")));
	    mnuitm.addActionListener(new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    delete();
		}
	    });
	}
	return popmenu;
    }

    public void move(int realtime, Date d) throws Exception {
	
	Appointment ap = AppointmentModel.getReference().getAppt(appt.getKey());

	int hour = realtime / 60;
	int min = realtime % 60;

	int oldkey = (ap.getKey() / 100) * 100;

	GregorianCalendar newCal = new GregorianCalendar();
	newCal.setTime(d);
	if( hour != 0 || min != 0){
	    // we are moving to be timed - set duration
	    ap.setDuration(new Integer(15));
	    newCal.set(Calendar.HOUR_OF_DAY, hour);
	    int roundMin = (min / 5) * 5;
	    newCal.set(Calendar.MINUTE, roundMin);
	}
	else
	{
	    // keep time and duration the same 
	    Calendar oldCal = new GregorianCalendar();
	    oldCal.setTime(ap.getDate());
	    newCal.set(Calendar.HOUR_OF_DAY, oldCal.get(Calendar.HOUR_OF_DAY));
	    newCal.set(Calendar.MINUTE, oldCal.get(Calendar.MINUTE));
	    newCal.set(Calendar.SECOND, 0);
	}


	int newkey = AppointmentModel.dkey(newCal);
	Date newTime = newCal.getTime();
	ap.setDate(newTime);
	
	// only do something if date changed
	if (oldkey != newkey) { // date chg
	    if (Repeat.isRepeating(ap)) { // cannot date chg unless it is on the first in a series
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		int k2 = AppointmentModel.dkey(cal);
		if (oldkey != k2) {
		    Errmsg.notice(Resource.getPlainResourceString("rpt_drag_err"));
		    return;
		}
	    }
	    AppointmentModel.getReference().delAppt(ap.getKey());
	    AppointmentModel.getReference().saveAppt(ap, true);
	}
    }
}
