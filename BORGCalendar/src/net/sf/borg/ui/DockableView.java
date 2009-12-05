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

package net.sf.borg.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.Model;


/**
 * The Class DockableView is the base class for panels that can appear as stand-alone windows
 * or tabs in the main view and can be docked/undocked at runtime.
 */
public abstract class DockableView extends JPanel implements Model.Listener {
	
	private static final long serialVersionUID = 1L;
	/** The icon for the title bar  */
	static Image image = Toolkit.getDefaultToolkit().getImage(
			DockableView.class.getResource("/resource/borg32x32.jpg"));

	/**
	 * store the window size, position, and maximized status in a preference.
	 * used to have windows remember their sizes automatically.
	 * 
	 * @param c the window component
	 * @param pn the preference
	 */
	static private void recordSize(Component c, PrefName pn) {
		ViewSize vs = new ViewSize();
		vs.setX(c.getBounds().x);
		vs.setY(c.getBounds().y);
		vs.setWidth(c.getBounds().width);
		vs.setHeight(c.getBounds().height);
		JFrame v = (JFrame) c;
		vs.setMaximized(v.getExtendedState() == Frame.MAXIMIZED_BOTH);

		Prefs.putPref(pn, vs.toString());

	}

	/** The main frame. */
	private JFrame frame = null;

	/**
	 * register the view for model change callbacks
	 * 
	 * @param m the model
	 */
	protected void addModel(Model m) {
		m.addListener(this);
	}
	
	/**
	 * Shows the view as a docked tab or separate window, depending on the user
	 * options.
	 * 
	 */
	public void showView() {
		String dock = Prefs.getPref(PrefName.DOCKPANELS);
		if (dock.equals("true")) {
			this.dock();
		} else
			this.openInFrame();
	}


	/**
	 * Dock into the multiview
	 */
	private void dock() {
		MultiView.getMainView().addView(getFrameTitle(),this);
		remove();
	}

	/**
	 * Gets the window size preference.
	 * 
	 * @return the window size preference
	 */
	public abstract PrefName getFrameSizePref();

	/**
	 * Gets the frame title.
	 * 
	 * @return the frame title
	 */
	public abstract String getFrameTitle();

	/**
	 * Gets the menu for the frame.
	 * 
	 * @return the menu for the frame
	 */
	public abstract JMenuBar getMenuForFrame();
	
	/**
	 * determine if the view is docked
	 * @return true if docked
	 */
	public boolean isDocked()
	{
		return frame == null;
	}

	/**
	 * Sets the window size and position from the stored preference and then sets up
	 * listeners to store any updates to the window size and position based on
	 * user actions
	 * 
	 * @param pname the preference name
	 */
	private void manageMySize(PrefName pname) {

		// set the initial size
		String s = Prefs.getPref(pname);
		ViewSize vs = ViewSize.fromString(s);

		if (vs.isMaximized()) {
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		} else if (vs.getX() != -1) {
			frame.setBounds(new Rectangle(vs.getX(), vs.getY(), vs.getWidth(), vs
					.getHeight()));
		} else if (vs.getWidth() != -1) {
			frame.setSize(new Dimension(vs.getWidth(), vs.getHeight()));
		}

		frame.validate();

		final PrefName pn = pname;

		// add listeners to record any changes
		frame.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentMoved(java.awt.event.ComponentEvent e) {
				recordSize(e.getComponent(), pn);
			}

			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				recordSize(e.getComponent(), pn);
			}
		});
	}

	/**
	 * Open the view in a frame.
	 * 
	 * @return the  frame
	 */
	public JFrame openInFrame() {
		frame = new JFrame();
		manageMySize(getFrameSizePref());
		frame.setContentPane(this);
		JMenuBar bar = getMenuForFrame();
		
		// add a dock menu option
		if (bar != null) {
			JMenu jm = bar.getMenu(0);
			JMenuItem jmi = jm.add(Resource.getResourceString("dock"));
			jmi.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					dock();
				}

			});
			frame.setJMenuBar(bar);

		}

		frame
				.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		frame.setTitle(getFrameTitle());

		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent evt) {
				frame.dispose();
			}
		});

		frame.setIconImage(image);
		frame.getLayeredPane().registerKeyboardAction(new ActionListener() {
			public final void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		frame.setVisible(true);

		return frame;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.model.Model.Listener#refresh()
	 */
	public abstract void refresh();

	/* (non-Javadoc)
	 * @see net.sf.borg.model.Model.Listener#remove()
	 */
	public void remove() {
		if (frame != null)
			frame.dispose();
		frame = null;
	}

}
