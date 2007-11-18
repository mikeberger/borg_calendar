package net.sf.borg.ui.calendar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JPopupMenu;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.beans.Appointment;

public class LabelBox implements Box{

    private Appointment appt = null;

    private Rectangle bounds, clip;

    private boolean isSelected = false;

    public LabelBox(Appointment ap, Rectangle bounds, Rectangle clip) {
	appt = ap;
	this.bounds = bounds;
	this.clip = clip;
    }

   
    public void delete() {

    }

   
    public void draw(Graphics2D g2, Component comp) {

	Shape s = g2.getClip();
	if (clip != null)
	    g2.setClip(clip);

	int smfontHeight = g2.getFontMetrics().getHeight();

	if (isSelected == true) {
	    g2.setColor(Color.WHITE);
	    g2.fillRect(bounds.x, bounds.y + 2, bounds.width, bounds.height);
	}
	g2.setColor(new Color(Integer.parseInt(Prefs.getPref(PrefName.UCS_PURPLE))));
	g2.drawString(getText(), bounds.x + 2, bounds.y + smfontHeight);
	g2.setColor(Color.black);

	g2.setClip(s);

    }

    public void edit() {
    }


    public Rectangle getBounds() {
	return bounds;
    }

 
    public String getText() {
	return appt.getText();
    }

    public void setBounds(Rectangle bounds) {
	this.bounds = bounds;
    }


    public void setSelected(boolean isSelected) {
	this.isSelected = isSelected;
    }

    public JPopupMenu getMenu() {
	
	return null;
    }

  
}
