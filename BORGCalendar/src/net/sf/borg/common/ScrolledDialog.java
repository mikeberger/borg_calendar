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


public class ScrolledDialog extends JDialog {

	
	private static final long serialVersionUID = 3348692535328684293L;
	private static int result_;
	public final static int OK = 0;
	public final static int STACK = 1;

	private JTable tbl_ = null;

	private static Exception e_;

	private ScrolledDialog(String s, boolean stack) {
		initComponents();
		jTextArea.setText(s);
		stackButton.setVisible(stack);
		setModal(true);
	}

	private ScrolledDialog(JTable tbl) {
		tbl_ = tbl;
		initComponents();
		stackButton.setVisible(false);
		setModal(false);
	}

	public static int showError(Exception e) {
		result_ = 0;
		e_ = e;
		boolean ss = false;
		String showstack = Prefs.getPref(PrefName.STACKTRACE);
		if (showstack.equals("true")) {
			ss = true;
		}

		new ScrolledDialog(e.toString(), ss).setVisible(true);
		return result_;
	}

	public static void showTable(JTable tbl) {
		new ScrolledDialog(tbl).setVisible(true);
	}

	public static int showNotice(String text) {
		result_ = 0;
		new ScrolledDialog(text, false).setVisible(true);
		return result_;
	}

	private void initComponents()// GEN-BEGIN:initComponents
	{
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("BORG");
		this.setSize(165, 300);
		this.setContentPane(getJPanel());
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});

		pack();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = jScrollPane.getPreferredSize();
		setLocation(screenSize.width / 2 - (labelSize.width / 2),
				screenSize.height / 2 - (labelSize.height / 2));
	}

	/** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt) {
		this.dispose();
	}

	private JPanel jPanel = null;
	private JScrollPane jScrollPane = null;
	private JTextArea jTextArea = null;
	private JPanel buttonPanel = null;
	private JButton okButton = null;
	private JButton stackButton = null;

	/**
	 * This method initializes jPanel
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
	 * This method initializes jScrollPane
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
	 * This method initializes jTextArea
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
	 * This method initializes buttonPanel
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
	 * This method initializes okButton
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

	private void doOk() {
		result_ = OK;
		this.dispose();
	}

	/**
	 * This method initializes stackButton
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

	private void doStack() {
		result_ = STACK;
		// show the stack trace
		java.io.ByteArrayOutputStream bao = new java.io.ByteArrayOutputStream();
		java.io.PrintStream ps = new java.io.PrintStream(bao);
		e_.printStackTrace(ps);
		ScrolledDialog.showNotice(bao.toString());
		// dump to console too for cut & paste
		System.out.println(bao.toString());
	}

	public static void main(String args[]) {
		int ret = ScrolledDialog.showError(new Exception("duh\nduh\nduh"));
		System.out.println(ret);
	}
} // @jve:decl-index=0:visual-constraint="10,10"
