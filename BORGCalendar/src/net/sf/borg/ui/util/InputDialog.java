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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import net.sf.borg.common.Resource;

/**
 * modal dialog that prompts for input and can limit the input length
 */
public class InputDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	/**
	 * document that limits length
	 */
	private class LimitDocument extends PlainDocument {
		private static final long serialVersionUID = 1L;
		int maxLength; // max characters allowed

		/**
		 * constructor
		 * @param maxLen max characters allowed
		 */
		public LimitDocument(int max) {
			this.maxLength = max;
		}

		@Override
		public void insertString(int offs, String str, AttributeSet attr)
				throws BadLocationException {
			if (getLength() >= this.maxLength) {
				Toolkit.getDefaultToolkit().beep();
			} else {
				super.insertString(offs, str, attr);
			}
		}
	}

	/**
	 * result string returned by the dialog
	 */
	static private String returnString = null;

	/**
	 * Show an input dialog that prompts for input of limited length
	 * @param message the prompt message
	 * @param maxLength the max input length allowed
	 * @return the input or null if cancelled
	 */
	static public String show(String message, int maxLength) {
		returnString = null;
		new InputDialog(message, maxLength).setVisible(true);
		return returnString;
	}

	/** The input field. */
	private JTextField inputText;

	/**
	 * Instantiates a new Input Dialog
	 * 
	 * @param s
	 *            the message
	 * @param maxLength
	 *            maximum input length
	 */
	private InputDialog(String s, int maxLength) {
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setTitle("BORG");
		
		this.setSize(165, 300);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());

		// the prompt
		JLabel message = new JLabel(s);
		topPanel.add(message, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));

		// the input field
		inputText = new JTextField(new LimitDocument(maxLength), null, maxLength);
		topPanel.add(inputText, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH, 1.0, 1.0));

		// the ok and cancel buttons
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton();
		okButton.setText(Resource.getResourceString("OK"));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				returnString = inputText.getText();
				dispose();
			}
		});
		buttonPanel.add(okButton, null);
		JButton cancelButton = new JButton();
		cancelButton.setText(Resource.getResourceString("Cancel"));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				dispose();
			}
		});
		buttonPanel.add(cancelButton, null);
		topPanel.add(buttonPanel, GridBagConstraintsFactory.create(0, 2,
				GridBagConstraints.BOTH, 1.0, 0.0));

		this.setContentPane(topPanel);

		pack();

		// position in center of screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = this.getSize();
		setLocation(screenSize.width / 2 - (labelSize.width / 2),
				screenSize.height / 2 - (labelSize.height / 2));
		
		setModal(true);
	}

}
