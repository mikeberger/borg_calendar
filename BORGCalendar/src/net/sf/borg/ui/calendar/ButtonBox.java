package net.sf.borg.ui.calendar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Date;

import javax.swing.JPopupMenu;

// ApptDayBox holds the logical information needs to determine
// how an appointment box should be drawn in a day grid
public abstract class ButtonBox implements Box {

    private Rectangle bounds, clip;

    private Date date; // date being displayed - not necessarily date of

    private String text;

    private boolean isSelected = false;

    public ButtonBox(Date d, String text, Rectangle bounds, Rectangle clip) {
	date = d;
	this.text = text;
	this.bounds = bounds;
	this.clip = clip;
    }

    public void delete() {

    }

    public Date getDate() {
	return date;
    }

    public void draw(Graphics2D g2, Component comp) {

	Shape s = g2.getClip();
	if (clip != null)
	    g2.setClip(clip);

	g2.clipRect(bounds.x, 0, bounds.width + 1, 1000);
	if (isSelected == true) {
	    g2.setColor(Color.RED);
	    g2.fillRect(bounds.x, bounds.y + 2, bounds.width, bounds.height);
	}
	int smfontHeight = g2.getFontMetrics().getHeight();

	g2.setColor(Color.black);
	g2.drawString(text, bounds.x + 2, bounds.y + smfontHeight);

	g2.setClip(s);

    }

    public Rectangle getBounds() {
	return bounds;
    }

    /* (non-Javadoc)
     * @see net.sf.borg.ui.calendar.Box#getText()
     */
    public String getText() {
	return null;
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

    public JPopupMenu getMenu() {
	return null;
    }
}
