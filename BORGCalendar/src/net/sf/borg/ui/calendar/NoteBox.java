package net.sf.borg.ui.calendar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.beans.Appointment;
import net.sf.borg.ui.MultiView;

// ApptDayBox holds the logical information needs to determine
// how an appointment box should be drawn in a day grid
public class NoteBox {
    
   
    final static private float hlthickness = 2.0f;
    final static private BasicStroke highlight = new BasicStroke(hlthickness);

    private Date date; // date being displayed - not necessarily date of

    private Appointment appt = null;

    private boolean isSelected = false;

    private Rectangle bounds, clip;

    public NoteBox(Date d, Appointment ap, Rectangle bounds, Rectangle clip) {
	appt = ap;
	date = d;
	this.bounds = bounds;
	this.clip = clip;
    }

    public Appointment getAppt() {
	return appt;
    }

    public void setAppt(Appointment appt) {
	this.appt = appt;
    }

    public void setSelected(boolean isSelected) {
	this.isSelected = isSelected;
    }

    public String getText() {
	return appt.getText();
    }

    public String getTextColor() {
	if (appt == null)
	    return null;

	if ((appt.getColor() != null && appt.getColor().equals("strike"))
		|| (appt.getTodo() && !(appt.getNextTodo() == null || !appt.getNextTodo().after(date)))) {
	    return ("strike");
	}
	return appt.getColor();
    }

    public void edit() {
	GregorianCalendar cal = new GregorianCalendar();
	cal.setTime(appt.getDate());
	AppointmentListView ag = new AppointmentListView(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
	MultiView.getMainView().addView(ag);
	// AppointmentListView ag = new
	// AppointmentListView(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
	// cal
	// .get(Calendar.DATE));
	ag.showApp(appt.getKey());

    }

    public void delete() {
	AppointmentModel.getReference().delAppt(appt.getKey());
    }

    public boolean isTodo() {
	return getAppt().getTodo();
    }

    public void draw(Graphics2D g2) {
	
	Stroke stroke = g2.getStroke();
	Shape s = g2.getClip();
	if (clip != null)
	    g2.setClip(clip);

	// appt is note or is outside timespan shown
	g2.clipRect(bounds.x, 0, bounds.width + 1, 1000);

	
	Font sm_font = g2.getFont();
	int smfontHeight = g2.getFontMetrics().getHeight();
	Map stmap = new HashMap();
	stmap.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
	stmap.put(TextAttribute.FONT, sm_font);
	ImageIcon todoIcon = null;
	String iconname = Prefs.getPref(PrefName.UCS_MARKER);
	String use_marker = Prefs.getPref(PrefName.UCS_MARKTODO);
	if (use_marker.equals("true") && (iconname.endsWith(".gif") || iconname.endsWith(".jpg"))) {
	    todoIcon = new javax.swing.ImageIcon(getClass().getResource("/resource/" + iconname));
	}
	
	
	if (getTextColor().equals("strike")) {

	    AttributedString as = new AttributedString(getText(), stmap);
	    g2.drawString(as.getIterator(), bounds.x + 2, bounds.y + smfontHeight);
	} else {
	    // change color for a single appointment based on
	    // its color - only if color print option set
	    g2.setColor(Color.black);
	    if (getTextColor().equals("red"))
		g2.setColor(new Color(204, 0, 51));
	    else if (getTextColor().equals("green"))
		g2.setColor(new Color(0, 153, 0));
	    else if (getTextColor().equals("blue"))
		g2.setColor(new Color(102, 0, 204));
	    // g2.setFont(sm_font);
	    if (isTodo() && todoIcon != null) {
		todoIcon.paintIcon(null, g2, bounds.x, bounds.y + 8);
		g2.drawString(getText(), bounds.x + todoIcon.getIconWidth(), bounds.y + smfontHeight);
	    } else {
		g2.drawString(getText(), bounds.x + 2, bounds.y + smfontHeight);
	    }
	    g2.setColor(Color.black);
	}

	if (isSelected == true) {
	    g2.setStroke(highlight);
	    g2.setColor(Color.BLUE);
	    g2.drawRect(bounds.x, bounds.y + 2, bounds.width, bounds.height);
	    g2.setStroke(stroke);
	}
	g2.setColor(Color.BLACK);

	g2.setClip(s);
	g2.setColor(Color.black);
    }


    public void showMenu(Component c, int x, int y) {
	// TODO Auto-generated method stub

    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }
 

}
