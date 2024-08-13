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
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.Model;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.ViewSize.DockType;
import net.sf.borg.ui.util.IconHelper;

/**
 * The Class DockableView is the base class for panels that can appear as
 * stand-alone windows or tabs in the main view and can be docked/undocked at
 * runtime.
 */
public abstract class DockableView extends JPanel implements Model.Listener {

	/** The icon for the title bar. */
	static Image image = Toolkit.getDefaultToolkit()
			.getImage(DockableView.class.getResource("/resource/borg32x32.jpg"));

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** main menu bar */
	private MainMenu mainMenu = null;

	private final Set<Model> models = new HashSet<Model>();


	/** The main frame. */
	private JFrame frame = null;

	/**
	 * register the view for model change callbacks.
	 * 
	 * @param m the model
	 */
	protected void addModel(Model m) {
		m.addListener(this);
		models.add(m);
	}

	/**
	 * method called to check if the view can be closed.
	 * 
	 * @return true if the view can be closed, false if not
	 */
	protected boolean canClose() {
		// default is to always allow close
		return true;
	}

	/**
	 * method called when view is being closed to allow the view to do clean up or
	 * resets.
	 */
	protected void cleanUp() {

		// modules persist when closed, so don't deregister the listeners
		if (this instanceof MultiView.Module)
			return;

		for (Model m : models)
			m.removeListener(this);
	}

	/**
	 * close the view if allowed.
	 */
	public void close() {

		if (!canClose())
			return;

		cleanUp();

		if (isDocked())
			this.getParent().remove(this);
		else {
			frame.dispose();
			frame = null;
		}
	}

	/**
	 * Dock into the multiview.
	 */
	private void dock() {
		MultiView.getMainView().addView(getFrameTitle(), this);
		if (frame != null)
			frame.dispose();
		frame = null;
		String s = Prefs.getPref(getFrameSizePref());
		ViewSize vs = ViewSize.fromString(s);
		vs.setDock(DockType.DOCK);
		Prefs.putPref(getFrameSizePref(), vs.toString());
	}

	protected void updateTitle() {
		if (isDocked()) {
			MultiView.getMainView().setTabTitle(getFrameTitle(), this);
		} else if (frame != null) {
			frame.setTitle(getFrameTitle());
		}
	}

	/**
	 * Gets the window size preference.
	 * 
	 * @return the window size preference
	 */
	protected PrefName getFrameSizePref() {
		return new PrefName(getFrameTitle() + "_framesize", "-1,-1,800,600,N");
	}

	/**
	 * Gets the frame title.
	 * 
	 * @return the frame title
	 */
	public abstract String getFrameTitle();

	/**
	 * determine if the view is docked.
	 * 
	 * @return true if docked
	 */
	public boolean isDocked() {
		return frame == null;
	}


	/**
	 * Open the view in a frame.
	 * 
	 * @return the frame
	 */
	public JFrame openInFrame() {
		frame = new JFrame();
		ViewSize.manageMySize(frame, getFrameSizePref());
		frame.setContentPane(this);

		if (mainMenu == null) {
			mainMenu = new MainMenu();

			/* add a print option if available */
			if (this instanceof Module) {
				final Module mod = (Module) this;
				mainMenu.addAction(IconHelper.getIcon("/resource/Print16.gif"),
						Resource.getResourceString("Print"), new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent evt) {
								mod.print();
							}
						}, 0);
			}

			// add a dock menu option
			mainMenu.addAction(null, Resource.getResourceString("dock"), new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					dock();
				}
			}, 0);
		}

		frame.setJMenuBar(mainMenu.getMenuBar());

		frame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setTitle(getFrameTitle());

		final DockableView dv = this;
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent evt) {
				dv.close();
			}
		});

		frame.setIconImage(image);
		frame.getLayeredPane().registerKeyboardAction(new ActionListener() {
			@Override
			public final void actionPerformed(ActionEvent e) {
				dv.close();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		frame.setVisible(true);

		return frame;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.Model.Listener#refresh()
	 */
	/**
	 * Refresh.
	 */
	public abstract void refresh();

	/**
	 * Shows the view as a docked tab or separate window, depending on the user
	 * options.
	 * 
	 */
	public void showView() {

		// if showing already....
		if (frame != null && frame.isDisplayable()) {
			frame.toFront();
			return;
		}

		if (this.isDisplayable()) {
			// already showing
			return;
		}

		// check if dock/undock is overridden for this window
		// if so, use the override instead of the system preference
		PrefName p = getFrameSizePref();
		ViewSize vs = ViewSize.fromString(Prefs.getPref(p));
		if (vs.getDock() == DockType.DOCK) {
			this.dock();
		} else {
			this.openInFrame();
		}
	}

	/**
	 * start this view in the background (dock-only) - when starting to system tray
	 */
	public void bgStart() {
		PrefName p = getFrameSizePref();
		ViewSize vs = ViewSize.fromString(Prefs.getPref(p));
		if (vs.getDock() == DockType.DOCK) {
			this.dock();
		}
	}

	public static DockableView findDockableParent(Component c) {
		for (Container cont = c.getParent(); cont != null; cont = cont.getParent()) {
			if (cont instanceof DockableView) {
				return (DockableView) cont;
			}
		}

		return null;
	}

}
