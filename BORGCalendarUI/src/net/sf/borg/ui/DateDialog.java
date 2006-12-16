package net.sf.borg.ui;

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

import com.toedter.calendar.JDateChooser;

public class DateDialog extends JDialog
{
public DateDialog(Frame frmParent)
{
	super
	(
		frmParent,
		"Enter Date",
		true
	);
	initUI();
}

// Accessors
public final Calendar getCalendar()
{
	return calendar;
}

// Modifiers
public final void setCalendar(Calendar cal)
{
	dateComboBox.setCalendar(cal);
}

// private //
private JDateChooser dateComboBox;
private Calendar calendar;

private void initUI()
{
	initCtrls();
	pack();
	
	// Make it a little wider
	Dimension dim = getSize();
	dim.width += 40;
	setSize(dim);
	setLocationRelativeTo(null);
}

private void initCtrls()
{
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
//	pnlIcon.add(new JLabel(new ImageIcon(getClass().getResource("/resource/borg.jpg"))));
	
	JPanel pnlFields = new JPanel();
	pnlInput.add(pnlFields, BorderLayout.CENTER);
	pnlFields.setLayout(new GridLayout(0,1));
	pnlFields.add(dateComboBox = new JDateChooser());
	
	JPanel pnlLabels = new JPanel();
	pnlInput.add(pnlLabels, BorderLayout.WEST);
	pnlLabels.setLayout(new GridLayout(0,1));
	
	JLabel lblDate;
	pnlLabels.add(lblDate = new JLabel());
	
	ResourceHelper.setText(lblDate, "Date");
	lblDate.setLabelFor(dateComboBox);
	lblDate.setText(lblDate.getText()+":");
	
	JPanel pnlButtons = new JPanel();
	pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
	pnlMain.add(pnlButtons, BorderLayout.SOUTH);
	JButton bn;
	pnlButtons.add(bn = new JButton("OK"));
	getRootPane().setDefaultButton(bn);
	bn.addActionListener
	(
		new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				calendar = dateComboBox.getCalendar();
				setVisible(false);
			}
		}
	);

	pnlButtons.add(bn = new JButton("Cancel"));
	ActionListener cancelListener =
		new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				calendar = null;
				setVisible(false);
			}
		};
	bn.addActionListener(cancelListener);
	getRootPane()
		.registerKeyboardAction
		(
			cancelListener,
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW
		);
}
}
