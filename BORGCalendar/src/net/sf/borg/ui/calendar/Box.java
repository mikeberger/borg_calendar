package net.sf.borg.ui.calendar;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPopupMenu;

public interface Box {

    public abstract void delete();

    public abstract void draw(Graphics2D g2, Component comp);

    public abstract void edit();

    public abstract Rectangle getBounds();

    public abstract String getText();

    public abstract void setBounds(Rectangle bounds);

    public abstract void setSelected(boolean isSelected);
    
    public abstract JPopupMenu getMenu();

}