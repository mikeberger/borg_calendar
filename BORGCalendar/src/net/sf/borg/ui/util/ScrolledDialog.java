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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;

/**
 * Common dialog class with a scrolled text area.
 */
public class ScrolledDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	public static final int CANCEL = 1;
	/** exception - if we are showing one */
	private static Exception e_;

	/* return codes for option dialog */
	public static final int OK = 0;

	/* result for option dialog */
	private static int result;

	/**
	 * Show an error dialog with optional stack trace button.
	 * 
	 * @param e
	 *            the Exception
	 * 
	 */
	public static void showError(Exception e) {
		e_ = e;
		boolean ss = false;
		String showstack = Prefs.getPref(PrefName.STACKTRACE);
		if (showstack.equals("true")) {
			ss = true;
		}

		new ScrolledDialog(e.toString(), ss, false).setVisible(true);
	}

	/**
	 * Show a notice.
	 * 
	 * @param text
	 *            the notice text
	 * 
	 */
	public static void showNotice(String text) {
		new ScrolledDialog(text, false, false).setVisible(true);
	}

	/**
	 * show a scrolled option dialog
	 * 
	 * @param message
	 *            the message to show
	 * @return the result, OK or CANCEL
	 */
	public static int showOptionDialog(String message) {
		new ScrolledDialog(message, false, true).setVisible(true);
		return result;
	}

	/**
	 * Show a table.
	 * 
	 * @param tbl
	 *            the table
	 */
	public static void showTable(JTable tbl) {
		new ScrolledDialog(tbl).setVisible(true);
	}

	/** JTable - if we are showing one */
	private JTable tbl_ = null;

	/**
	 * Instantiates a new scrolled dialog to show a table.
	 * 
	 * @param tbl
	 *            the tbl
	 */
	private ScrolledDialog(JTable tbl) {
		tbl_ = tbl;
		initComponents(null, false, false);
		setModal(false);
	}

	/**
	 * Instantiates a new scrolled dialog to show text.
	 * 
	 * @param s
	 *            the text to show
	 * @param stack
	 *            if true, show the stack request button
	 * @param option
	 *            if true, show a cancel button and set the result
	 */
	private ScrolledDialog(String s, boolean stack, boolean option) {
		initComponents(s, option, stack);
		setModal(true);
	}

	/**
	 * Inits the swing components.
	 */
	private void initComponents(String text, boolean isOption, boolean isStack) {
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("BORG");
		this.setSize(165, 300);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new java.awt.Dimension(600, 200));
		
		/* set scroll context to message or table */
		if (tbl_ != null) {
			scrollPane.setViewportView(tbl_);
		} else {
			JTextArea textArea = new JTextArea();
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			textArea.setText(text);
			scrollPane.setViewportView(textArea);
		}
		
		mainPanel.add(scrollPane, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH, 1.0, 1.0));

		/*
		 * add the buttons
		 */
		JPanel buttonPanel = new JPanel();

		/* always add ok button */
		JButton okButton = new JButton();
		okButton.setText(Resource.getResourceString("OK"));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				result = OK;
				dispose();
			}
		});
		buttonPanel.add(okButton, null);

		/* only add stack button if needed */
		if (isStack) {
			JButton stackButton = new JButton();
			stackButton.setText(Resource.getResourceString("Show_Stack_Trace"));
			stackButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// show the stack trace
					java.io.ByteArrayOutputStream bao = new java.io.ByteArrayOutputStream();
					java.io.PrintStream ps = new java.io.PrintStream(bao);
					e_.printStackTrace(ps);
					ScrolledDialog.showNotice(bao.toString());
					// dump to console too for cut & paste
					System.out.println(bao.toString());
				}
			});
			buttonPanel.add(stackButton, null);
		}

		/* only add cancel button if this is an option dialog */
		if (isOption) {
			JButton cancelButton = new JButton();
			cancelButton.setText(Resource.getResourceString("Cancel"));
			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					result = CANCEL;
					dispose();
				}
			});
			buttonPanel.add(cancelButton, null);

		}
		
		mainPanel.add(buttonPanel, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH));
		this.setContentPane(mainPanel);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent evt) {
				windowClose();
			}
		});

		pack();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = scrollPane.getPreferredSize();
		setLocation(screenSize.width / 2 - (labelSize.width / 2),
				screenSize.height / 2 - (labelSize.height / 2));
	}

	/**
	 * dispose of the window
	 * 
	 */
	private void windowClose() {
		this.dispose();
	}


}
