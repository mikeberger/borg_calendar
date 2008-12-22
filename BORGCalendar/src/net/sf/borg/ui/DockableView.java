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

// a JPanel that shows a View and can be optionally placed into a stand-alone
// JFrame
public abstract class DockableView extends JPanel implements Model.Listener {
	static Image image = Toolkit.getDefaultToolkit().getImage(
			DockableView.class.getResource("/resource/borg32x32.jpg"));

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

	private PrefName prefName_ = null;

	protected JFrame fr_ = null;

	public abstract PrefName getFrameSizePref();

	public abstract String getFrameTitle();

	public abstract JMenuBar getMenuForFrame();

	private void dock() {
		MultiView.getMainView().dock(this);
		if (fr_ != null)
			fr_.dispose();
	}

	public JFrame openInFrame() {
		fr_ = new JFrame();
		manageMySize(getFrameSizePref());
		fr_.setContentPane(this);
		JMenuBar bar = getMenuForFrame();
		if (bar != null) {
			JMenu jm = bar.getMenu(0);
			JMenuItem jmi = jm.add(Resource.getPlainResourceString("dock"));
			jmi.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					dock();
				}

			});
			fr_.setJMenuBar(bar);

		}

		fr_
				.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		fr_.setTitle(getFrameTitle());

		fr_.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				fr_.dispose();
			}
		});

		fr_.setIconImage(image);
		fr_.getLayeredPane().registerKeyboardAction(new ActionListener() {
			public final void actionPerformed(ActionEvent e) {
				fr_.dispose();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		fr_.setVisible(true);

		return fr_;
	}

	public abstract void refresh();

	public void remove() {
		if (fr_ != null)
			fr_.dispose();
		fr_ = null;
	}

	// called from the subclass to cause the View to use preferences to
	// persist a View's size and locaiton if the user resizes it
	private void manageMySize(PrefName pname) {
		prefName_ = pname;

		// set the initial size
		String s = Prefs.getPref(prefName_);
		ViewSize vs = ViewSize.fromString(s);

		if (vs.isMaximized()) {
			fr_.setExtendedState(Frame.MAXIMIZED_BOTH);
		} else if (vs.getX() != -1) {
			fr_.setBounds(new Rectangle(vs.getX(), vs.getY(), vs.getWidth(), vs
					.getHeight()));
		} else if (vs.getWidth() != -1) {
			fr_.setSize(new Dimension(vs.getWidth(), vs.getHeight()));
		}

		fr_.validate();

		final PrefName pn = pname;

		// add listeners to record any changes
		fr_.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentMoved(java.awt.event.ComponentEvent e) {
				recordSize(e.getComponent(), pn);
			}

			public void componentResized(java.awt.event.ComponentEvent e) {
				recordSize(e.getComponent(), pn);
			}
		});
	}

	// function to call to register a view with the model
	protected void addModel(Model m) {
		m.addListener(this);
	}

}
