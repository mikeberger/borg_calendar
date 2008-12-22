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
/*
 * View.java
 *
 * Created on May 23, 2003, 11:06 AM
 */

package net.sf.borg.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.Model;



/**
 * $Id: View.java 379 2005-09-10 07:12:16Z membar $
 * @author  MBERGER
 */

// Views show data from a model and respond to refresh
// cand destroy allbacks from Models
public abstract class View extends javax.swing.JFrame implements Model.Listener
{
	static Image image = Toolkit.getDefaultToolkit().getImage(
			View.class.getResource("/resource/borg32x32.jpg"));

    private PrefName prefName_ = null;
	private void initialize() {
	    setIconImage(image);
	}
    public abstract void refresh();
    public abstract void destroy();
    
    public void remove()
    {
        destroy();
    }
    
    public View()
    {
        initialize();
    }
		
    private void recordSize(boolean resize)
    { 
    	String s = Prefs.getPref(prefName_);
        ViewSize vsnew = ViewSize.fromString(s);
        
        if( !resize )
        {
        	// for a move, ignore the event if the size changes - this is
        	// part of a maximize or minimize and the resize will follow
        	if( vsnew.getHeight() != this.getBounds().height ||
        			vsnew.getWidth() != this.getBounds().width )
        		return;
        	
        	// if x or y < 0, then this is likely the move before a maximize
        	// so ignore it
        	if( this.getBounds().x < 0 || this.getBounds().y < 0)
        		return;
        	
        	vsnew.setX(this.getBounds().x);
        	vsnew.setY(this.getBounds().y);
        	vsnew.setWidth(this.getBounds().width);
        	vsnew.setHeight(this.getBounds().height);
        }
        else if( this.getExtendedState() == Frame.MAXIMIZED_BOTH )
        {
        	vsnew.setMaximized(true);
        }
        else
        {
        	// only reset bounds if we are not maximized
        	vsnew.setMaximized(false);
        	vsnew.setX(this.getBounds().x);
        	vsnew.setY(this.getBounds().y);
        	vsnew.setWidth(this.getBounds().width);
        	vsnew.setHeight(this.getBounds().height);
        	
        }
        
        //System.out.println(vsnew.toString());
        Prefs.putPref(this.prefName_,vsnew.toString());
        
    }
      
    // function to call to register a view with the model
    protected void addModel(Model m)
    {
        m.addListener(this);
    }
     
    // called from the subclass to cause the View to use preferences to 
    // persist a View's size and locaiton if the user resizes it
    public void manageMySize(PrefName pname)
    {
        prefName_ = pname;
        
        // set the initial size
        String s = Prefs.getPref(prefName_);
        ViewSize vs = ViewSize.fromString(s);
        
        
        if( vs.getX() != -1 )
        {
            setBounds( new Rectangle(vs.getX(),vs.getY(),vs.getWidth(),vs.getHeight()));            
        }
        else if( vs.getWidth() != -1 )
        {
            setSize(new Dimension(vs.getWidth(),vs.getHeight()));
        }
        if( vs.isMaximized())
        {
            setExtendedState(Frame.MAXIMIZED_BOTH);
        }
        validate();
        
        // add listeners to record any changes
        this.addComponentListener(new java.awt.event.ComponentAdapter() { 
        	public void componentResized(java.awt.event.ComponentEvent e) {
        		//System.out.println("resize");
        		recordSize(true);
        	}
           	public void componentMoved(java.awt.event.ComponentEvent e) { 
           		//System.out.println("move");
           	    recordSize(false);
        	}
        });
    }
    
    // protected //
    protected void setDismissButton(final JButton bn)
    {
    	getLayeredPane()
			.registerKeyboardAction
			(
				new ActionListener()
				{
					public final void actionPerformed(ActionEvent e) {
						bn.getActionListeners()[0].actionPerformed(e);
					}
				},
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW
			);
    }
}
