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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;

import javax.swing.JButton;

import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.model.Model;



/**
 * $Id$
 * @author  MBERGER
 */

// Views show data from a model and respond to refresh
// cand destroy allbacks from Models
abstract class View extends javax.swing.JFrame implements Model.Listener
{
 
    
    private PrefName prefName_ = null;
	private void initialize() {
 
			
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
		
    static private void recordSize( Component c )
    {
        ViewSize vs = new ViewSize();
        vs.setX(c.getBounds().x);
        vs.setY(c.getBounds().y);
        vs.setWidth(c.getBounds().width);
        vs.setHeight(c.getBounds().height);
        View v = (View) c;
        vs.setMaximized(v.getExtendedState() == Frame.MAXIMIZED_BOTH);
        
        Prefs.putPref(v.prefName_,vs.toString());
        
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
        
        if( vs.isMaximized())
        {
            setExtendedState(Frame.MAXIMIZED_BOTH);
        }
        else if( vs.getX() != -1 )
        {
            setBounds( new Rectangle(vs.getX(),vs.getY(),vs.getWidth(),vs.getHeight()));            
        }
        else if( vs.getWidth() != -1 )
        {
            setSize(new Dimension(vs.getWidth(),vs.getHeight()));
        }
           
        validate();
        
        // add listeners to record any changes
        this.addComponentListener(new java.awt.event.ComponentAdapter() { 
        	public void componentResized(java.awt.event.ComponentEvent e) {    
        		recordSize( e.getComponent());
        	}
           	public void componentMoved(java.awt.event.ComponentEvent e) {    
           	    recordSize( e.getComponent());
        	}
        });
    }
    
    // protected //
    protected void setDismissButton(final JButton bn)
    {
    	/*
    	 * I had second thoughts about this. It allows you to dismiss
    	 * the dialog boxes with [Escape], but this also happens when
    	 * a menu is open.
    	getRootPane()
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
		*/
    }
}
