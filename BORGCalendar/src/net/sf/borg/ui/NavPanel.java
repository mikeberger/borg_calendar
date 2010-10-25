/*
 * This file is part of BORG.
 *
 * BORG is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * BORG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * BORG; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright 2003 by Mike Berger
 */
package net.sf.borg.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.sf.borg.common.Resource;
import net.sf.borg.ui.util.DateDialog;

/**
 * A NavPanel provides common navigation buttons to other classes that navigate among dates
 *
 */
public class NavPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Interface to be implemented by any class that allows the attachment
	 * of a NavPanel navigator
	 *
	 */
	static public interface Navigator {

		/**
		 * get the navigator label - i.e. a string indicating the current date
		 * @return the text to show on the navigator main label
		 */
		public String getNavLabel();

		/** navigate to a particular date */
		public void goTo(Calendar cal);

		/** go to the next item */
		public void next();

		/** go to the previous item */
		public void prev();

		/** go to today */
		public void today();
	}

	/** the main navigator label - showing the current date */
	private JButton label = new JButton();

	/** the attached navigator object */
	private Navigator nav_ = null;

	/** 
	 * constructor 
	 * @param nav the Navigator object that this NavPanel will be controlling
	 */
	public NavPanel(Navigator nav) {

		nav_ = nav;

		setLayout(new GridBagLayout());

		// create the various navigator buttons
		JButton Prev = new JButton();
		Prev.setMaximumSize(new Dimension(16, 16));
		Prev.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Back16.gif")));
		Prev.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				nav_.prev();
				label.setText(nav_.getNavLabel());
			}
		});

		JButton Next = new JButton();
		Next.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Forward16.gif")));
		Next.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
		Next.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				nav_.next();
				label.setText(nav_.getNavLabel());
			}
		});

		JButton Today = new JButton();
		Today.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Home16.gif")));
		Today.setToolTipText(Resource.getResourceString("Today"));
		Today.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				nav_.today();
				label.setText(nav_.getNavLabel());
			}
		});

		JButton Goto = new JButton();
		Goto.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Undo16.gif")));
		Goto.setToolTipText(Resource.getResourceString("Go_To"));
		Goto.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				DateDialog dlg = new DateDialog(null);
				dlg.setCalendar(new GregorianCalendar());
				dlg.setVisible(true);
				Calendar dlgcal = dlg.getCalendar();
				if (dlgcal == null)
					return;
				nav_.goTo(dlgcal);
				label.setText(nav_.getNavLabel());
			}
		});

		// the main label
		label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		label.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		label.setText(nav_.getNavLabel());

		// add components
		GridBagConstraints cons = new GridBagConstraints();
		cons.fill = GridBagConstraints.NONE;
		cons.gridx = 0;
		cons.gridy = 0;
		add(Today, cons);

		cons.gridx = 1;
		add(Prev, cons);

		cons.gridx = 2;
		cons.weightx = 1.0;
		cons.fill = GridBagConstraints.HORIZONTAL;
		add(label, cons);

		cons.fill = GridBagConstraints.NONE;
		cons.weightx = 0.0;
		cons.gridx = 3;
		add(Next, cons);

		cons.gridx = 4;
		add(Goto, cons);

	}

	/** set the navigator label */
	public void setLabel(String l) {
		label.setText(l);
	}

}
