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
 * helpscrn.java
 *
 * Created on October 5, 2003, 8:55 AM
 */

package net.sf.borg.ui.util;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.borg.common.Resource;

/**
 * modal dialog with a scrollable message.
 * The ok button can start as disabled so that the user must wait until it is enabled
 * by the program while the program is doing something important
 */
public class ModalMessage extends JDialog {

	private static final long serialVersionUID = 1L;

	/** The message scroll. */
	private JScrollPane messageScroll = null;

	/** The message text. */
	private JTextArea messageText = null;

	/** The ok button. */
	private JButton okButton = null;

	/**
	 * Instantiates a new modal message.
	 * 
	 * @param s the message
	 * @param enabled if true, enable ok button, otheriwse disable
	 */
	public ModalMessage(String s, boolean enabled) {
		initComponents();
		messageText.setText(s);
		okButton.setEnabled(enabled);
		setModal(true);
	}

	/**
	 * Append text to the message while. Normally used when the program is continuing to produce output
	 * for the user and the ok button is disabled. Text is appended on a new line.
	 * 
	 * @param s the string to append
	 */
	public void appendText(String s) {
		String t = messageText.getText();
		t += "\n" + s;
		messageText.setText(t);
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setTitle("BORG");
		this.setSize(165, 300);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());

		messageScroll = new JScrollPane();
		messageScroll.setPreferredSize(new java.awt.Dimension(600, 200));

		messageText = new JTextArea();
		messageText.setEditable(false);
		messageText.setLineWrap(true);

		messageScroll.setViewportView(messageText);

		topPanel.add(messageScroll, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));

		JPanel buttonPanel = new JPanel();

		okButton = new JButton();
		okButton.setText(Resource.getResourceString("OK"));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				dispose();
			}
		});

		buttonPanel.add(okButton, null);

		topPanel.add(buttonPanel, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH, 0.0, 0.0));

		this.setContentPane(topPanel);

		pack();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = messageScroll.getPreferredSize();
		setLocation(screenSize.width / 2 - (labelSize.width / 2),
				screenSize.height / 2 - (labelSize.height / 2));
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean e) {
		okButton.setEnabled(e);
	}

	/**
	 * Sets the text.
	 * 
	 * @param s the new text
	 */
	public void setText(String s) {
		messageText.setText(s);
	}
} 
