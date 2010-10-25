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
package net.sf.borg.ui.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.sf.borg.common.Resource;
import net.sf.borg.ui.ResourceHelper;

import com.toedter.calendar.JDateChooser;

/**
 * A dialog for picking a date that wraps JDateChooser
 */
public class DateDialog extends JDialog {
	
	private static final long serialVersionUID = 1L;

	/** The date. */
	private Calendar calendar;

	/** The date combo box. */
	private JDateChooser dateComboBox;

	/**
	 * constructor
	 * 
	 * @param frmParent the parent frame
	 */
	public DateDialog(Frame frmParent) {
		super(frmParent, Resource.getResourceString("Enter_Date"), true);
		initUI();
	}

	/**
	 * Gets the calendar.
	 * 
	 * @return the calendar
	 */
	public final Calendar getCalendar() {
		return calendar;
	}

	/**
	 * Inits the ui.
	 */
	private void initUI() {
		JPanel pnlMain = new JPanel();
		getContentPane().add(pnlMain);
		pnlMain.setLayout(new BorderLayout());

		JPanel pnlInputAndIcon = new JPanel();
		pnlMain.add(pnlInputAndIcon, BorderLayout.CENTER);
		pnlInputAndIcon.setLayout(new BorderLayout());

		JPanel pnlInput = new JPanel();
		pnlInputAndIcon.add(pnlInput, BorderLayout.CENTER);
		pnlInput.setLayout(new BorderLayout());

		JPanel pnlIcon = new JPanel();
		pnlInputAndIcon.add(pnlIcon, BorderLayout.WEST);
		pnlIcon.setLayout(new BorderLayout());

		JPanel pnlFields = new JPanel();
		pnlInput.add(pnlFields, BorderLayout.CENTER);
		pnlFields.setLayout(new GridLayout(0, 1));
		pnlFields.add(dateComboBox = new JDateChooser());

		JPanel pnlLabels = new JPanel();
		pnlInput.add(pnlLabels, BorderLayout.WEST);
		pnlLabels.setLayout(new GridLayout(0, 1));

		JLabel lblDate;
		pnlLabels.add(lblDate = new JLabel());

		ResourceHelper.setText(lblDate, Resource.getResourceString("Date"));
		lblDate.setLabelFor(dateComboBox);
		lblDate.setText(lblDate.getText() + ":");

		JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
		pnlMain.add(pnlButtons, BorderLayout.SOUTH);
		JButton bn;
		pnlButtons.add(bn = new JButton(Resource.getResourceString("OK")));
		getRootPane().setDefaultButton(bn);
		
		bn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				calendar = dateComboBox.getCalendar();
				setVisible(false);
			}
		});

		pnlButtons.add(bn = new JButton(Resource.getResourceString("Cancel")));
		ActionListener cancelListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				calendar = null;
				setVisible(false);
			}
		};
		bn.addActionListener(cancelListener);
		getRootPane().registerKeyboardAction(cancelListener,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);		pack();

		// Make it a little wider
		Dimension dim = getSize();
		dim.width += 40;
		setSize(dim);
		setLocationRelativeTo(null);
	}

	/**
	 * Sets the calendar.
	 * 
	 * @param cal the new calendar
	 */
	public final void setCalendar(Calendar cal) {
		dateComboBox.setCalendar(cal);
	}
}
