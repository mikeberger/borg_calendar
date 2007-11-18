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

    private String getTextColor() {
	if (appt == null)
	    return null;

	return appt.getColor();
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
