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

package net.sf.borg.common;

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


/**
 * Common dialog class with a scrolled text area. It is not in the ui package because
 * it is one of the few UI related components that is used outside of the main UI. NOTE: this class contains 
 * generated UI code that has not been cleaned up.
 */
public class ScrolledDialog extends JDialog {

	
	private static final long serialVersionUID = 3348692535328684293L;

	/** JTable - if we are showing one */
	private JTable tbl_ = null;

	/** exception - if we are showing one */
	private static Exception e_;

	/**
	 * Instantiates a new scrolled dialog to show text.
	 * 
	 * @param s the text to show
	 * @param stack if true, show the stack request button
	 */
	private ScrolledDialog(String s, boolean stack) {
		initComponents();
		jTextArea.setText(s);
		stackButton.setVisible(stack);
		setModal(true);
	}

	/**
	 * Instantiates a new scrolled dialog to show a table.
	 * 
	 * @param tbl the tbl
	 */
	private ScrolledDialog(JTable tbl) {
		tbl_ = tbl;
		initComponents();
		stackButton.setVisible(false);
		setModal(false);
	}

	/**
	 * Show an error dialog with optional stack trace button.
	 * 
	 * @param e the Exception
	 * 
	 * @return the int
	 */
	public static void showError(Exception e) {
		e_ = e;
		boolean ss = false;
		String showstack = Prefs.getPref(PrefName.STACKTRACE);
		if (showstack.equals("true")) {
			ss = true;
		}

		new ScrolledDialog(e.toString(), ss).setVisible(true);
	}

	/**
	 * Show a table.
	 * 
	 * @param tbl the table
	 */
	public static void showTable(JTable tbl) {
		new ScrolledDialog(tbl).setVisible(true);
	}

	/**
	 * Show a notice.
	 * 
	 * @param text the notice text
	 * 
	 * @return the int
	 */
	public static void showNotice(String text) {
		new ScrolledDialog(text, false).setVisible(true);
	}

	/**
	 * Inits the swing components.
	 */
	private void initComponents()// GEN-BEGIN:initComponents
	{
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("BORG");
		this.setSize(165, 300);
		this.setContentPane(getJPanel());
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				windowClose();
			}
		});

		pack();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = jScrollPane.getPreferredSize();
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

	/** The panel. */
	private JPanel jPanel = null;
	
	/** The scroll pane. */
	private JScrollPane jScrollPane = null;
	
	/** The text area. */
	private JTextArea jTextArea = null;
	
	/** The button panel. */
	private JPanel buttonPanel = null;
	
	/** The ok button. */
	private JButton okButton = null;
	
	/** The stack button. */
	private JButton stackButton = null;

	/**
	 * This method initializes jPanel.
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0; // Generated
			gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH; // Generated
			gridBagConstraints1.gridy = 1; // Generated
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH; // Generated
			gridBagConstraints.gridy = 0; // Generated
			gridBagConstraints.weightx = 1.0; // Generated
			gridBagConstraints.weighty = 1.0; // Generated
			gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4); // Generated
			gridBagConstraints.gridx = 0; // Generated
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout()); // Generated
			jPanel.add(getJScrollPane(), gridBagConstraints); // Generated
			jPanel.add(getButtonPanel(), gridBagConstraints1); // Generated
		}
		return jPanel;
	}

	/**
	 * This method initializes jScrollPane.
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jScrollPane.setPreferredSize(new java.awt.Dimension(600, 200)); // Generated
			if (tbl_ != null) {
				jScrollPane.setViewportView(tbl_);

			} else {
				jScrollPane.setViewportView(getJTextArea()); 
			}
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTextArea.
	 * 
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new JTextArea();
			jTextArea.setEditable(false); // Generated
			jTextArea.setLineWrap(true); // Generated
		}
		return jTextArea;
	}

	/**
	 * This method initializes buttonPanel.
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getOkButton(), null); // Generated
			buttonPanel.add(getStackButton(), null); // Generated
		}
		return buttonPanel;
	}

	/**
	 * This method initializes okButton.
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText(Resource.getResourceString("OK"));
			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doOk();
				}
			});
		}
		return okButton;
	}

	/**
	 * do ok action - dispose of the dialog
	 */
	private void doOk() {
		this.dispose();
	}

	/**
	 * This method initializes stackButton.
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getStackButton() {
		if (stackButton == null) {
			stackButton = new JButton();
			stackButton.setText(Resource.getResourceString("Show_Stack_Trace"));
			stackButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doStack();
				}
			});
		}
		return stackButton;
	}

	/**
	 * Do stack actin - show the stack trace in a dialog and write the stack trace to stdout and stderr
	 */
	private void doStack() {
		// show the stack trace
		java.io.ByteArrayOutputStream bao = new java.io.ByteArrayOutputStream();
		java.io.PrintStream ps = new java.io.PrintStream(bao);
		e_.printStackTrace(ps);
		ScrolledDialog.showNotice(bao.toString());
		// dump to console too for cut & paste
		System.out.println(bao.toString());
	}

	
} 
