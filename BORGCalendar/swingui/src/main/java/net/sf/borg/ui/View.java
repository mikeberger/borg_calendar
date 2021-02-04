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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import net.sf.borg.common.PrefName;
import net.sf.borg.model.Model;


/**
 * base class for borg views. contains common logic for borg windows. Views are JFrames that optionally listen for
 * change messages from models
 *
 */
public abstract class View extends JFrame implements Model.Listener {
	
	private static final long serialVersionUID = 1L;

	/** The icon image. */
	static Image image = Toolkit.getDefaultToolkit().getImage(
			View.class.getResource("/resource/borg32x32.jpg"));

	/**
	 * Instantiates a new view.
	 */
	public View() {
		initialize();
	}

	

	/**
	 * Destroy the view
	 */
	public abstract void destroy();

	/**
	 * Initialize the view
	 */
	private void initialize() {
		setIconImage(image);
	}

	/**
	 * registers this view for size management. once called, this view will 
	 * store its size and position via the given preference whenever the user changes the
	 * size or position
	 * 
	 * @param pname the pname
	 */
	public void manageMySize(PrefName pname) {
		ViewSize.manageMySize(this, pname);
	}

	
	/**
	 * refresh the view. called when one of the registered models changes. can
	 * be called any time a refresh is needed as well.
	 */
	public abstract void refresh();

	/**
	 * register the escape key to activate a button (presumably a dismiss button)
	 * 
	 * @param bn the button
	 */
	protected void setDismissButton(final JButton bn) {
		getLayeredPane().registerKeyboardAction(new ActionListener() {
			@Override
			public final void actionPerformed(ActionEvent e) {
				bn.getActionListeners()[0].actionPerformed(e);
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
}
