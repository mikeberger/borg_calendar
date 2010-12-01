/*
 * This file is part of BORG. BORG is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. BORG is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of
 * the GNU General Public License along with BORG; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA Copyright 2003 by Mike Berger
 */
package net.sf.borg.ui.calendar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.link.LinkPanel;
import net.sf.borg.ui.popup.PopupOptionsView;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.PasswordHelper;

import com.toedter.calendar.JDateChooser;

/**
 * AppointmentPanel is the UI for editing an Appointment.
 */
public class AppointmentPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	// magic repeat times value that means repeat forever
	private final static int MAGIC_RPT_FOREVER_VALUE = 9999;

	/**
	 * renders the colro selection pull-down with colored boxes as the choices.
	 */
	static private class ColorBoxRenderer extends JLabel implements
			ListCellRenderer {

		private static final long serialVersionUID = 1L;

		/**
		 * constructor.
		 */
		public ColorBoxRenderer() {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}

		/**
		 * get the color choice label for a given color value.
		 */
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			String sel = (String) value;

			if (isSelected) {
				setBackground(list.getSelectionBackground());
			} else {
				setBackground(list.getBackground());
			}

			// no text - the label will be icon only
			setText(" ");

			// set the icon based on the user defined colors
			// map the "logical" color names to the user defined color
			if (sel.equals("black")) {
				setIcon(new SolidComboBoxIcon(new Color(Integer.parseInt(Prefs
						.getPref(PrefName.UCS_BLACK)))));
			} else if (sel.equals("red")) {
				setIcon(new SolidComboBoxIcon(new Color(Integer.parseInt(Prefs
						.getPref(PrefName.UCS_RED)))));
			} else if (sel.equals("blue")) {
				setIcon(new SolidComboBoxIcon(new Color(Integer.parseInt(Prefs
						.getPref(PrefName.UCS_BLUE)))));
			} else if (sel.equals("green")) {
				setIcon(new SolidComboBoxIcon(new Color(Integer.parseInt(Prefs
						.getPref(PrefName.UCS_GREEN)))));
			} else if (sel.equals("white")) {
				setIcon(new SolidComboBoxIcon(new Color(Integer.parseInt(Prefs
						.getPref(PrefName.UCS_WHITE)))));
			} else {
				// just for strike-through, we use text
				setForeground(Color.BLACK);
				setText(Resource.getResourceString("strike"));
				setIcon(null);
			}

			return this;
		}

	}

	/**
	 * Long, thin, rectanglular icon that goes in the color chooser pulldown
	 * list items.
	 */
	static private class SolidComboBoxIcon implements Icon {

		private Color color = Color.BLACK; // color
		private final int h = 10; // height
		private final int w = 60; // width

		/**
		 * Instantiates a new solid combo box icon.
		 * 
		 * @param col
		 *            the color
		 */
		public SolidComboBoxIcon(Color col) {
			color = col;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see Icon#getIconHeight()
		 */
		@Override
		public int getIconHeight() {
			return h;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see Icon#getIconWidth()
		 */
		@Override
		public int getIconWidth() {
			return w;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
		 */
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.BLACK);
			g2.drawRect(x, y, w, h);
			g2.setColor(color);
			g2.fillRect(x, y, w, h);
		}
	}

	/**
	 * combo box choices for setting the hour if we are using 24-hour time
	 */
	static private DefaultComboBoxModel milHourModel = new DefaultComboBoxModel(
			new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
					"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
					"20", "21", "22", "23" });

	/**
	 * combo box choices for setting 12-hr time
	 */
	static private DefaultComboBoxModel normHourModel = new DefaultComboBoxModel(
			new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
					"11", "12" });

	static private SpinnerNumberModel prioritySpinnerModel = new SpinnerNumberModel(5, 1, 10, 1);
	
	// appt text area
	private JTextArea appointmentBodyTextArea;

	// appt title field
	private JTextField apptTitleField = null;

	// link panel
	private LinkPanel linkPanel = null;

	// category combo box
	private JComboBox categoryBox;
	
	// priority chooser
	private JSpinner prioritySpinner;

	// date change checkbox
	private JCheckBox dateChangeCheckBox;

	// color select combo box
	private JComboBox colorComboBox;

	// names of the borg "logical" colors. the names no longer imply a
	// particular color
	// but remain to support old databases. eahc name maps to a user defined
	// color
	private String colors[] = { "red", "blue", "green", "black", "white",
			"strike" };

	// reminder times
	private char[] custRemTimes;

	// date being shown
	private int day_;
	private int month_;
	private int year_;

	// the seven toggle buttons for selecting repeat days for a repeat
	// type of select days
	private JToggleButton dayToggles[] = new JToggleButton[7];

	// the panel containing the day toggle buttons
	private JPanel selectDayButtonPanel = null;

	// hour combo for the duration value
	private JComboBox durationHourComboBox;

	// minute combo for the duration value
	private JComboBox durationMinuteComboBox;

	// repeat frequency combo box
	private JComboBox repeatFrequencyComboBox;

	// half-day (vacation) check box
	private JCheckBox halfDayVacationCheckBox;

	// holiday check box
	private JCheckBox holidayCheckBox;

	// currently shown appt key
	private int currentlyShownAppointmentKey;

	// number of days spinner for N days repeat
	private JSpinner numberOfDaysSpinner = null;

	// indicator that a new appt is being edited
	private JLabel newAppointmentIndicatorLabel;

	// new date chooser
	private JDateChooser newdatefield;

	// untimed appt checkbox
	private JCheckBox untimedCheckBox;

	// label that shows current popup times
	private JLabel popupTimesLabel;

	// private check box
	private JCheckBox privateCheckBox;

	// show repeat number check box
	private JCheckBox showRepeatNumberCheckBox = null;

	// number fo repeats spinner
	private JSpinner numberOfRepeatsSpinner;

	// AM/PM indicator
	private JCheckBox amOrPmComboBox;

	// hour combo
	private JComboBox startHourComboBox;

	// minute combo
	private JComboBox startMinuteComboBox;

	// todo check box
	private JCheckBox todoCheckBox;

	// vacation check box
	private JCheckBox vacationCheckBox;

	/** decrypt button */
	private JButton decryptButton = null;

	/**
	 * encryption checkbox
	 */
	private JCheckBox encryptBox = null;
	
	/**
	 * repeat until date chooser
	 */
	private JDateChooser untilDate = null;
	
	/**
	 * radio buttons to choose between repeat forever, times, and until date
	 */
	private JRadioButton repeatForeverRadio = null;
	private JRadioButton repeatTimesRadio = null;
	private JRadioButton repeatUntilRadio = null;

	/*
	 * save buttons
	 */
	private JButton saveCloseButton = null;
	private JButton saveButton = null;

	/**
	 * Instantiates a new appointment panel.
	 * 
	 * @param year
	 *            the year
	 * @param month
	 *            the month
	 * @param day
	 *            the day
	 */
	public AppointmentPanel(int year, int month, int day) {

		// init GUI
		initComponents();

		decryptButton.setEnabled(false);

		// set up hours pulldown
		String mt = Prefs.getPref(PrefName.MILTIME);
		if (mt.equals("true")) {
			startHourComboBox.setModel(milHourModel);
			amOrPmComboBox.setVisible(false);
		} else {
			startHourComboBox.setModel(normHourModel);
			amOrPmComboBox.setVisible(true);
		}

		// set up priority pull down
		prioritySpinner.setModel(prioritySpinnerModel);
		prioritySpinner.setVisible(true);
		
		// load categories
		try {
			Collection<String> cats = CategoryModel.getReference()
					.getCategories();
			Iterator<String> it = cats.iterator();
			while (it.hasNext()) {
				categoryBox.addItem(it.next());
			}
			categoryBox.setSelectedIndex(0);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		setDate(year, month, day);
		setCustRemTimes(null);
		apptTitleField.requestFocus();
	}

	/**
	 * encrypt the text of a given appointment - and prompt for password too
	 * 
	 * @throws Exception
	 */
	private void encryptAppt(Appointment appt) throws Exception {
		String pw = PasswordHelper.getReference().getPassword();
		if( pw == null )
			return;
		appt.encrypt(pw);

	}

	/**
	 * add an appointment to the model based on the UI settings.
	 * 
	 * @throws Warning
	 * @throws Exception
	 */
	private void add_appt() throws Warning, Exception {

		// get a new appt from the model and set it from the user data
		AppointmentModel calmod_ = AppointmentModel.getReference();
		Appointment r = calmod_.newAppt();
		setAppt(r, true);
		
		// encrypt it
		if (encryptBox.isSelected()) {
			encryptAppt(r);
		}

		// save it
		calmod_.saveAppt(r);

		// clear UI and show a new appt
		showapp(-1, null);
	}

	/**
	 * update an appointment in the model based on UI settings
	 * 
	 * @throws Warning
	 * @throws Exception
	 */
	private void chg_appt() throws Warning, Exception {

		// get a new empty appt from the model and set it using the data the
		// user has entered
		AppointmentModel calmod_ = AppointmentModel.getReference();
		Appointment appt = calmod_.newAppt();
		boolean dateChg = setAppt(appt, true);
		appt.setKey(currentlyShownAppointmentKey);

		// call the model to change the appt
		if (dateChg == false) {

			// need to selectively preserve some data from original appt
			try {

				// date of orig might not match appt panel date - so
				// we must keep the original date - we may be editing a repeat
				Appointment originalAppt = calmod_
						.getAppt(currentlyShownAppointmentKey);

				Calendar origDate = new GregorianCalendar();
				origDate.setTime(originalAppt.getDate());

				// preserve the day, but change the time in case we are editing
				// one of the repeat
				// occurrences and not the first occurrence
				Calendar newDate = new GregorianCalendar();
				newDate.setTime(appt.getDate());
				newDate.set(Calendar.YEAR, origDate.get(Calendar.YEAR));
				newDate.set(Calendar.MONTH, origDate.get(Calendar.MONTH));
				newDate.set(Calendar.DATE, origDate.get(Calendar.DATE));
				appt.setDate(newDate.getTime());

				// determine if we can keep certain fields related to repeating
				// and todos
				if (appt.getTimes().intValue() == originalAppt.getTimes()
						.intValue()
						&& appt.getRepeatUntil() == originalAppt.getRepeatUntil()
						&& appt.getFrequency() != null
						&& originalAppt.getFrequency() != null
						&& Repeat.getFreq(appt.getFrequency()).equals(
								Repeat.getFreq(originalAppt.getFrequency()))
						&& appt.getTodo() == originalAppt.getTodo()
						&& appt.getRepeatFlag() == originalAppt.getRepeatFlag()) {

					// we can keep skip list and next todo since the repeat/todo
					// info is not changing
					// otherwise, we'd reset these
					appt.setSkipList(originalAppt.getSkipList());
					appt.setNextTodo(originalAppt.getNextTodo());
				}

			} catch (Exception e) {
				// empty
			}
			
			// encrypt it
			if (encryptBox.isSelected()) {
				encryptAppt(appt);
			}
			calmod_.saveAppt(appt);
		} else {
			// for a date change - everything comes from the screen
			// the old appt info is tossed out
			appt.setKey(currentlyShownAppointmentKey);
			try {
				// encrypt it
				if (encryptBox.isSelected()) {
					encryptAppt(appt);
				}
				calmod_.saveAppt(appt);
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}

		}

		// show a new appt
		showapp(-1, null);
	}

	/**
	 * This method initializes the day select toggle button panel
	 * 
	 * @return the day select toggle button panel
	 */
	private JPanel createDaySelectPanel() {

		selectDayButtonPanel = new JPanel();

		// set up toggle buttons
		SimpleDateFormat shortDayFmt = new SimpleDateFormat("EEE");
		GregorianCalendar tmpcal = new GregorianCalendar();
		tmpcal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		for (int i = 0; i < dayToggles.length; i++) {
			dayToggles[i] = new JToggleButton();
			dayToggles[i].setText(shortDayFmt.format(tmpcal.getTime()));
			selectDayButtonPanel.add(dayToggles[i]);
			tmpcal.add(Calendar.DAY_OF_WEEK, 1);
		}
		return selectDayButtonPanel;
	}

	/**
	 * create the repeat panel
	 * 
	 * @return the repear panel
	 */
	private JPanel createRepeatPanel() {

		JPanel theRepeatPanel = new JPanel();
		theRepeatPanel.setLayout(new GridBagLayout());

		theRepeatPanel.setBorder(new TitledBorder(null, Resource
				.getResourceString("Recurrence"),
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, null, null));

		JLabel frequencyLabel = new JLabel();
		frequencyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		ResourceHelper.setText(frequencyLabel, "Frequency");
		frequencyLabel.setLabelFor(repeatFrequencyComboBox);
		theRepeatPanel.add(frequencyLabel, GridBagConstraintsFactory.create(0,
				0));

		// load repeat frequency strings
		repeatFrequencyComboBox = new JComboBox();
		repeatFrequencyComboBox.setOpaque(false);
		for (int i = 0;; i++) {
			String fs = Repeat.getFreqString(i);
			if (fs == null)
				break;
			repeatFrequencyComboBox.addItem(fs);
		}
		repeatFrequencyComboBox
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent e) {
						// show/hide the various repeat widgets based on the
						// chosen
						// frequency
						timesEnable();
					}

				});
		theRepeatPanel.add(repeatFrequencyComboBox, GridBagConstraintsFactory
				.create(1, 0, GridBagConstraints.HORIZONTAL));

		ButtonGroup buttonGroup = new ButtonGroup();

		repeatTimesRadio = new JRadioButton();
		ResourceHelper.setText(repeatTimesRadio, "Times");
		theRepeatPanel.add(repeatTimesRadio, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH));
		buttonGroup.add(repeatTimesRadio);

		numberOfRepeatsSpinner = new JSpinner();
		theRepeatPanel.add(numberOfRepeatsSpinner, GridBagConstraintsFactory
				.create(1, 1, GridBagConstraints.BOTH));
		SpinnerNumberModel mod = (SpinnerNumberModel) numberOfRepeatsSpinner
				.getModel();
		mod.setMinimum(new Integer(1));
		
		repeatUntilRadio = new JRadioButton();
		ResourceHelper.setText(repeatUntilRadio, "Until");
		theRepeatPanel.add(repeatUntilRadio, GridBagConstraintsFactory.create(0, 2,
				GridBagConstraints.BOTH));
		buttonGroup.add(repeatUntilRadio);
		
		untilDate = new JDateChooser();
		theRepeatPanel.add(untilDate, GridBagConstraintsFactory.create(1, 2,
				GridBagConstraints.BOTH));

		numberOfDaysSpinner = new JSpinner();
		numberOfDaysSpinner.setModel(new SpinnerNumberModel(2, 2, 3000, 1));
		theRepeatPanel.add(numberOfDaysSpinner, GridBagConstraintsFactory
				.create(2, 0, GridBagConstraints.HORIZONTAL));

		
		repeatForeverRadio = new JRadioButton();
		ResourceHelper.setText(repeatForeverRadio, "forever");

		theRepeatPanel.add(repeatForeverRadio, GridBagConstraintsFactory
				.create(2, 1, GridBagConstraints.BOTH));

		buttonGroup.add(repeatForeverRadio);
		
		showRepeatNumberCheckBox = new JCheckBox();
		ResourceHelper.setText(showRepeatNumberCheckBox, "show_rpt_num");
		theRepeatPanel.add(showRepeatNumberCheckBox, GridBagConstraintsFactory
				.create(2, 2, GridBagConstraints.BOTH));

		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.BOTH;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.gridx = 2;
		gridBagConstraints1.gridwidth = 2;
		theRepeatPanel.add(createDaySelectPanel(), gridBagConstraints1);

		return theRepeatPanel;
	}

	/**
	 * initialize many of the UI components. This started as very very bad
	 * generated code. It's been cleaned up somewhat
	 */
	private void initComponents() {

		// lots of components are created here. This is how the code-gen lumped
		// them.
		// not worth it to move them all where they are used.
		JLabel subjectLabel = new JLabel();
		subjectLabel.setText(Resource.getResourceString("subject"));
		newAppointmentIndicatorLabel = new JLabel();
		JScrollPane apptTextScroll = new JScrollPane();
		appointmentBodyTextArea = new JTextArea();
		startHourComboBox = new JComboBox();
		startMinuteComboBox = new JComboBox();
		prioritySpinner = new JSpinner();
		amOrPmComboBox = new JCheckBox();
		durationHourComboBox = new JComboBox();
		durationMinuteComboBox = new JComboBox();
		JLabel starttimeLabel = new JLabel();
		JLabel durationLabel = new JLabel();
		untimedCheckBox = new JCheckBox();
		JLabel newDateLabel = new JLabel();
		newdatefield = new JDateChooser();
		newdatefield.setDateFormatString("MMM dd, yyyy");
		dateChangeCheckBox = new JCheckBox();
		JPanel appointmentPropetiesPanel = new JPanel();
		todoCheckBox = new JCheckBox();
		vacationCheckBox = new JCheckBox();
		halfDayVacationCheckBox = new JCheckBox();
		holidayCheckBox = new JCheckBox();
		privateCheckBox = new JCheckBox();
		JLabel lblColor = new JLabel();
		colorComboBox = new JComboBox();
		categoryBox = new JComboBox();
		JLabel lblCategory = new JLabel();
		JLabel lblPriority = new JLabel();
		JPanel buttonPanel = new JPanel();
		saveButton = new JButton();
		saveCloseButton = new JButton();
		JButton savedefaultsbutton = new JButton();
		apptTitleField = new JTextField();

		starttimeLabel.setLabelFor(startHourComboBox);
		durationLabel.setLabelFor(durationHourComboBox);
		lblCategory.setLabelFor(categoryBox);
		lblColor.setLabelFor(colorComboBox);
		newDateLabel.setLabelFor(newdatefield);

		setLayout(new GridBagLayout());

		newAppointmentIndicatorLabel.setForeground(java.awt.Color.red);

		add(newAppointmentIndicatorLabel, GridBagConstraintsFactory.create(0,
				0, GridBagConstraints.BOTH));

		// ********************************************************************
		// appt text panel
		// ********************************************************************
		JPanel appointmentTextPanel = new JPanel();
		appointmentTextPanel.setLayout(new GridBagLayout());
		appointmentTextPanel.setBorder(new TitledBorder(Resource
				.getResourceString("appttext")));
		appointmentBodyTextArea.setColumns(40);
		appointmentBodyTextArea.setLineWrap(true);
		appointmentBodyTextArea.setRows(5);
		appointmentBodyTextArea.setWrapStyleWord(true);
		appointmentBodyTextArea.setBorder(new BevelBorder(BevelBorder.LOWERED));
		appointmentBodyTextArea
				.setMinimumSize(new java.awt.Dimension(284, 140));
		apptTextScroll.setViewportView(appointmentBodyTextArea);

		appointmentTextPanel.add(subjectLabel, GridBagConstraintsFactory
				.create(0, 0));
		appointmentTextPanel.add(apptTitleField, GridBagConstraintsFactory
				.create(1, 0, GridBagConstraints.BOTH, 1.0, 0.0));
		appointmentTextPanel.add(apptTextScroll, GridBagConstraintsFactory
				.create(1, 1, GridBagConstraints.BOTH, 0.5, 0.5));

		GridBagConstraints gridBagConstraints3 = GridBagConstraintsFactory
				.create(0, 1, GridBagConstraints.BOTH);
		gridBagConstraints3.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		gridBagConstraints3.weightx = 1.5;
		gridBagConstraints3.weighty = 2.0;
		add(appointmentTextPanel, gridBagConstraints3);

		// ********************************************************************
		// appointment time panel
		// ********************************************************************
		JPanel appointmentTimePanel = new JPanel();
		appointmentTimePanel.setLayout(new java.awt.GridBagLayout());
		appointmentTimePanel.setBorder(new TitledBorder(Resource
				.getResourceString("appttime")));

		ResourceHelper.setText(starttimeLabel, "Start_Time:");
		appointmentTimePanel.add(starttimeLabel, GridBagConstraintsFactory
				.create(0, 0, GridBagConstraints.BOTH));

		startHourComboBox.setMaximumRowCount(24);
		startHourComboBox.setMinimumSize(new java.awt.Dimension(42, 36));
		startHourComboBox.setOpaque(false);
		appointmentTimePanel.add(startHourComboBox, GridBagConstraintsFactory
				.create(1, 0, GridBagConstraints.BOTH));

		startMinuteComboBox.setMaximumRowCount(12);
		startMinuteComboBox.setModel(new DefaultComboBoxModel(new String[] {
				"00", "05", "10", "15", "20", "25", "30", "35", "40", "45",
				"50", "55" }));
		startMinuteComboBox.setOpaque(false);
		appointmentTimePanel.add(startMinuteComboBox, GridBagConstraintsFactory
				.create(2, 0, GridBagConstraints.VERTICAL));

		amOrPmComboBox.setText("PM");
		amOrPmComboBox.setOpaque(false);
		appointmentTimePanel.add(amOrPmComboBox, GridBagConstraintsFactory
				.create(3, 0, GridBagConstraints.BOTH));

		ResourceHelper.setText(untimedCheckBox, "No_Specific_Time");
		untimedCheckBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				untimedCheckBoxActionPerformed();
			}
		});
		GridBagConstraints gridBagConstraints12 = GridBagConstraintsFactory
				.create(4, 0, GridBagConstraints.BOTH);
		gridBagConstraints12.insets = new java.awt.Insets(0, 30, 0, 0);
		appointmentTimePanel.add(untimedCheckBox, gridBagConstraints12);

		ResourceHelper.setText(durationLabel, "Duration:");
		appointmentTimePanel.add(durationLabel, GridBagConstraintsFactory
				.create(0, 1, GridBagConstraints.BOTH));

		durationHourComboBox.setMaximumRowCount(24);
		durationHourComboBox.setModel(new DefaultComboBoxModel(new String[] {
				"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
				"12", "13", "14", "15", "16", "17", "18", "19", "20", "21",
				"22", "23", "24" }));
		appointmentTimePanel
				.add(durationHourComboBox, GridBagConstraintsFactory.create(1,
						1, GridBagConstraints.BOTH));

		durationMinuteComboBox.setMaximumRowCount(12);
		durationMinuteComboBox.setModel(new DefaultComboBoxModel(new String[] {
				"00", "05", "10", "15", "20", "25", "30", "35", "40", "45",
				"50", "55" }));
		durationMinuteComboBox.setOpaque(false);
		appointmentTimePanel.add(durationMinuteComboBox,
				GridBagConstraintsFactory.create(2, 1,
						GridBagConstraints.VERTICAL));

		ResourceHelper.setText(dateChangeCheckBox, "changedate");
		dateChangeCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				newdatefield.setEnabled(dateChangeCheckBox.isSelected());
			}
		});
		appointmentTimePanel.add(dateChangeCheckBox, GridBagConstraintsFactory
				.create(4, 1, GridBagConstraints.HORIZONTAL));

		ResourceHelper.setText(newDateLabel, "newDate:");
		appointmentTimePanel.add(newDateLabel, GridBagConstraintsFactory
				.create(5, 1, GridBagConstraints.BOTH));

		appointmentTimePanel.add(newdatefield, GridBagConstraintsFactory
				.create(6, 1, GridBagConstraints.BOTH));

		// ********************************************************************
		// appt properties panel
		// ********************************************************************
		appointmentPropetiesPanel.setLayout(new GridBagLayout());

		appointmentPropetiesPanel.setBorder(new TitledBorder(Resource
				.getResourceString("Properties")));
		appointmentPropetiesPanel.setMinimumSize(new java.awt.Dimension(539,
				128));

		ResourceHelper.setText(todoCheckBox, "To_Do");
		todoCheckBox.setOpaque(false);

		appointmentPropetiesPanel.add(todoCheckBox, GridBagConstraintsFactory
				.create(0, 0, GridBagConstraints.BOTH, 1.0, 0.0));

		vacationCheckBox.setForeground(new java.awt.Color(0, 102, 0));
		ResourceHelper.setText(vacationCheckBox, "Vacation");
		vacationCheckBox.setOpaque(false);

		appointmentPropetiesPanel.add(vacationCheckBox,
				GridBagConstraintsFactory.create(1, 0, GridBagConstraints.BOTH,
						1.0, 0.0));

		halfDayVacationCheckBox.setForeground(new java.awt.Color(0, 102, 102));
		ResourceHelper.setText(halfDayVacationCheckBox, "Half_Day");
		halfDayVacationCheckBox.setOpaque(false);

		appointmentPropetiesPanel.add(halfDayVacationCheckBox,
				GridBagConstraintsFactory.create(2, 0, GridBagConstraints.BOTH,
						1.0, 0.0));

		ResourceHelper.setText(holidayCheckBox, "Holiday");
		holidayCheckBox.setOpaque(false);

		appointmentPropetiesPanel.add(holidayCheckBox,
				GridBagConstraintsFactory.create(3, 0, GridBagConstraints.BOTH,
						1.0, 0.0));

		ResourceHelper.setText(privateCheckBox, "Private");
		privateCheckBox.setOpaque(false);

		appointmentPropetiesPanel.add(privateCheckBox,
				GridBagConstraintsFactory.create(4, 0, GridBagConstraints.BOTH,
						1.0, 0.0));
		
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new GridBagLayout());
		
		subPanel.add(new JLabel(), GridBagConstraintsFactory
				.create(GridBagConstraints.RELATIVE, 0, GridBagConstraints.BOTH, 1.0, 0.0)); //spacer

		lblColor.setHorizontalAlignment(SwingConstants.RIGHT);
		ResourceHelper.setText(lblColor, "Color");

		subPanel.add(lblColor, GridBagConstraintsFactory
				.create(GridBagConstraints.RELATIVE, 0, GridBagConstraints.BOTH));
		

		colorComboBox.setOpaque(false);

		subPanel.add(colorComboBox, GridBagConstraintsFactory
				.create(GridBagConstraints.RELATIVE, 0, GridBagConstraints.BOTH));
		ColorBoxRenderer cbr = new ColorBoxRenderer();
		colorComboBox.setRenderer(cbr);
		colorComboBox.setEditable(false);
		for (int i = 0; i < colors.length; i++) {
			colorComboBox.addItem(colors[i]);
		}

		lblCategory.setHorizontalAlignment(SwingConstants.RIGHT);
		ResourceHelper.setText(lblCategory, "Category");
		subPanel.add(lblCategory, GridBagConstraintsFactory
				.create(GridBagConstraints.RELATIVE, 0, GridBagConstraints.BOTH, 1.0, 0.0));

		subPanel.add(categoryBox, GridBagConstraintsFactory
				.create(GridBagConstraints.RELATIVE, 0, GridBagConstraints.BOTH));
		
		lblPriority.setHorizontalAlignment(SwingConstants.RIGHT);
		ResourceHelper.setText(lblPriority, "Priority");
		subPanel.add(lblPriority, GridBagConstraintsFactory
				.create(GridBagConstraints.RELATIVE, 0, GridBagConstraints.BOTH, 1.0, 0.0));
		subPanel.add(prioritySpinner, GridBagConstraintsFactory
				.create(GridBagConstraints.RELATIVE, 0, GridBagConstraints.BOTH));
		
		subPanel.add(new JLabel(), GridBagConstraintsFactory
				.create(GridBagConstraints.RELATIVE, 0, GridBagConstraints.BOTH, 1.0, 0.0)); //spacer
		
		GridBagConstraints subPanelConstraints = GridBagConstraintsFactory
		.create(0, 1, GridBagConstraints.BOTH, 1.0, 0.0);
		subPanelConstraints.gridwidth = 5;
		appointmentPropetiesPanel.add(subPanel, subPanelConstraints);
		
		
		
		// ********************************************************************
		// button panel
		// ********************************************************************

		saveButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Save16.gif")));
		ResourceHelper.setText(saveButton, "Save");
		saveButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					if (currentlyShownAppointmentKey == -1) {
						add_appt();
					} else {
						chg_appt();
					}
				} catch (Warning w) {
					Errmsg.notice(w.getMessage());
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});
		buttonPanel.add(saveButton);

		saveCloseButton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Save16.gif")));
		ResourceHelper.setText(saveCloseButton, "Save_&_Close");
		saveCloseButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					if (currentlyShownAppointmentKey == -1) {
						add_appt();
					} else {
						chg_appt();
					}
				} catch (Warning w) {
					Errmsg.notice(w.getMessage());
					return;
				} catch (Exception e) {
					Errmsg.errmsg(e);
					return;
				}

				MultiView.getMainView().closeSelectedTab();
			}
		});
		buttonPanel.add(saveCloseButton);
		
		encryptBox = new JCheckBox();
		encryptBox.setText(Resource.getResourceString("EncryptOnSave"));
		buttonPanel.add(encryptBox, null);

		decryptButton = new JButton();
		decryptButton.setText(Resource.getResourceString("decrypt"));
		decryptButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				try {
					Appointment appt = AppointmentModel.getReference().getAppt(
							currentlyShownAppointmentKey);
					if (appt == null)
						return;
					String pw = PasswordHelper.getReference().getPassword();
					if( pw == null )
						return;
					appt.decrypt(pw);

					// set appt text, split apart title and body
					String t = appt.getText();
					String title = "";
					String body = "";
					if (t == null)
						t = "";
					int newlineIndex = t.indexOf('\n');
					if (newlineIndex != -1) {
						title = t.substring(0, newlineIndex);
						body = t.substring(newlineIndex + 1);
					} else {
						title = t;
					}
					appointmentBodyTextArea.setText(body);
					apptTitleField.setText(title);

					appointmentBodyTextArea.setEditable(true);
					apptTitleField.setEditable(true);

					decryptButton.setEnabled(false);

					saveButton.setEnabled(true);
					saveCloseButton.setEnabled(true);

				} catch (Exception e1) {
					Errmsg.errmsg(e1);
				}

			}
		});
		buttonPanel.add(decryptButton, null);

		savedefaultsbutton.setIcon(new ImageIcon(getClass().getResource(
				"/resource/SaveAs16.gif")));
		ResourceHelper.setText(savedefaultsbutton, "save_Def");
		savedefaultsbutton.setToolTipText(Resource.getResourceString("sd_tip"));
		savedefaultsbutton
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						Appointment appt = new Appointment();
						try {
							setAppt(appt, false);
						} catch (Exception e) {
							Errmsg.errmsg(e);
							return;
						}

						AppointmentModel.getReference().saveDefaultAppointment(
								appt);
					}
				});
		
		// add a spacer
		buttonPanel.add( new JLabel("          "));
		buttonPanel.add(savedefaultsbutton);

		// ********************************************************************
		// popup reminders panel
		// ********************************************************************
		popupTimesLabel = new JLabel();
		popupTimesLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		JButton popupTimesBtn = new JButton();
		ResourceHelper.setText(popupTimesBtn, "Change");
		final AppointmentPanel thisPanel = this;
		popupTimesBtn.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String apptTitle = apptTitleField.getText();
				if (apptTitle.equals("")) {
					apptTitle = Resource
							.getResourceString("*****_NEW_APPT_*****");
				}
				PopupOptionsView pv = new PopupOptionsView(new String(
						custRemTimes), apptTitle, thisPanel);
				pv.setVisible(true);
			}
		});

		JPanel popupReminderPanel = new JPanel();
		popupReminderPanel.setBorder(new TitledBorder(Resource
				.getResourceString("popup_reminders")));
		popupReminderPanel.add(popupTimesLabel);
		popupReminderPanel.add(popupTimesBtn);

		// ********************************************************************
		// add panels to the top level
		// ********************************************************************
		GridBagConstraints gridBagConstraints17 = GridBagConstraintsFactory
				.create(0, 2, GridBagConstraints.BOTH, 1.0, 1.0);
		gridBagConstraints17.gridwidth = GridBagConstraints.REMAINDER;
		this.add(appointmentTimePanel, gridBagConstraints17);
		this.setSize(648, 590);

		GridBagConstraints gridBagConstraints38 = GridBagConstraintsFactory
				.create(0, 3, GridBagConstraints.BOTH);
		gridBagConstraints38.gridwidth = 2;
		this.add(appointmentPropetiesPanel, gridBagConstraints38);

		this.add(createRepeatPanel(), GridBagConstraintsFactory.create(0, 5,
				GridBagConstraints.BOTH));

		GridBagConstraints gridBagConstraints91 = GridBagConstraintsFactory
				.create(0, 6, GridBagConstraints.BOTH);
		gridBagConstraints91.gridwidth = 2;
		this.add(buttonPanel, gridBagConstraints91);

		GridBagConstraints gridBagConstraints87 = GridBagConstraintsFactory
				.create(0, 4, GridBagConstraints.BOTH, 9.0, 1.0);
		gridBagConstraints87.gridwidth = 2;
		this.add(popupReminderPanel, gridBagConstraints87);

		linkPanel = new LinkPanel();
		linkPanel.setBorder(new TitledBorder(Resource
				.getResourceString("links")));
		this.add(linkPanel, GridBagConstraintsFactory.create(1, 5,
				GridBagConstraints.BOTH));

	}

	/**
	 * enable/disable the time selection components based on the value of the
	 * untimed checkbox if untimed is selected, the time components are disabled
	 */
	private void untimedCheckBoxActionPerformed() {
		if (untimedCheckBox.isSelected()) {
			String mt = Prefs.getPref(PrefName.MILTIME);
			if (mt.equals("true")) {
				startHourComboBox.setSelectedIndex(0);
			} else {
				startHourComboBox.setSelectedIndex(11); // hour = 12
			}
			startMinuteComboBox.setSelectedIndex(0);
			durationMinuteComboBox.setSelectedIndex(0);
			durationHourComboBox.setSelectedIndex(0);
			amOrPmComboBox.setSelected(false);
			startMinuteComboBox.setEnabled(false);
			startHourComboBox.setEnabled(false);
			durationMinuteComboBox.setEnabled(false);
			durationHourComboBox.setEnabled(false);
			amOrPmComboBox.setEnabled(false);
		} else {
			startMinuteComboBox.setEnabled(true);
			startHourComboBox.setEnabled(true);
			durationMinuteComboBox.setEnabled(true);
			durationHourComboBox.setEnabled(true);
			amOrPmComboBox.setEnabled(true);
		}
	}

	/**
	 * Set the data in an Appointment from the UI values
	 * 
	 * @param appt
	 *            the appointment
	 * @param validate
	 *            validate the data
	 * 
	 * @return true, if the date is changing
	 * 
	 * @throws Warning
	 * @throws Exception
	 */
	private boolean setAppt(Appointment appt, boolean validate) throws Warning,
			Exception {

		// see if date is changing
		Date nd = null;
		boolean dateChg = false;
		if (dateChangeCheckBox.isSelected()) {
			nd = newdatefield.getDate();
			dateChg = true;
		}

		// get the hour and minute
		int hr = startHourComboBox.getSelectedIndex();
		String mt = Prefs.getPref(PrefName.MILTIME);
		if (mt.equals("false")) {
			hr = hr + 1;
			if (hr == 12)
				hr = 0;
			if (amOrPmComboBox.isSelected())
				hr += 12;
		}
		int min = startMinuteComboBox.getSelectedIndex() * 5;

		// compute new date/time
		GregorianCalendar g = new GregorianCalendar();
		if (nd == null) {
			g.set(year_, month_, day_, hr, min, 0);
		} else {
			g.setTime(nd);
			g.set(Calendar.HOUR_OF_DAY, hr);
			g.set(Calendar.MINUTE, min);
			g.set(Calendar.SECOND, 0);
		}
		appt.setDate(g.getTime());

		// set untimed
		if (untimedCheckBox.isSelected())
			appt.setUntimed("Y");

		// duration
		int du = (durationHourComboBox.getSelectedIndex() * 60)
				+ (durationMinuteComboBox.getSelectedIndex() * 5);
		if (du != 0)
			appt.setDuration(new Integer(du));

		// appointment text of some sort is required if we are validating
		if (apptTitleField.getText().equals("") && validate) {
			apptTitleField.requestFocus();
			apptTitleField.setBackground(new Color(255, 255, 204));
			throw new Warning(Resource
					.getResourceString("Please_enter_some_appointment_text"));
		}

		// set text. add newline between title and body text
		String t = apptTitleField.getText();
		if (appointmentBodyTextArea.getText() != null
				&& !appointmentBodyTextArea.getText().equals(""))
			t += "\n" + appointmentBodyTextArea.getText();
		appt.setText(t);

		// to do
		appt.setTodo(todoCheckBox.isSelected());

		// vacation, half-day, and private checkboxes
		if (vacationCheckBox.isSelected())
			appt.setVacation(new Integer(1));
		if (halfDayVacationCheckBox.isSelected())
			appt.setVacation(new Integer(2));
		if (holidayCheckBox.isSelected())
			appt.setHoliday(new Integer(1));

		// private
		appt.setPrivate(privateCheckBox.isSelected());

		// color
		appt.setColor((String) colorComboBox.getSelectedItem());

		// repeat frequency
		if (repeatFrequencyComboBox.getSelectedIndex() != 0) {
			ArrayList<Integer> daylist = new ArrayList<Integer>();
			if (dayToggles[0].isSelected())
				daylist.add(new Integer(Calendar.SUNDAY));
			if (dayToggles[1].isSelected())
				daylist.add(new Integer(Calendar.MONDAY));
			if (dayToggles[2].isSelected())
				daylist.add(new Integer(Calendar.TUESDAY));
			if (dayToggles[3].isSelected())
				daylist.add(new Integer(Calendar.WEDNESDAY));
			if (dayToggles[4].isSelected())
				daylist.add(new Integer(Calendar.THURSDAY));
			if (dayToggles[5].isSelected())
				daylist.add(new Integer(Calendar.FRIDAY));
			if (dayToggles[6].isSelected())
				daylist.add(new Integer(Calendar.SATURDAY));
			if (!Repeat.isCompatible(g, (String) repeatFrequencyComboBox
					.getSelectedItem(), daylist)) {
				throw new Warning(Resource.getResourceString("recur_compat"));
			}
			appt.setFrequency(Repeat.freqString(
					(String) repeatFrequencyComboBox.getSelectedItem(),
					(Integer) numberOfDaysSpinner.getValue(),
					showRepeatNumberCheckBox.isSelected(), daylist));
		}

		// repeat times
		Integer tm = null;
		if (repeatForeverRadio.isSelected()) {
			tm = new Integer(MAGIC_RPT_FOREVER_VALUE); 
		} else {
			tm = (Integer) numberOfRepeatsSpinner.getValue();
		}
		
		appt.setRepeatFlag(false);
		if (tm.intValue() > 1
				&& repeatFrequencyComboBox.getSelectedIndex() != 0) {
			try {
				appt.setTimes(tm);

				if (tm.intValue() > 1)
					appt.setRepeatFlag(true);
				
			} catch (Exception e) {
				throw new Exception(Resource
						.getResourceString("Could_not_parse_times:_")
						+ tm);
			}
		} else {
			appt.setTimes(new Integer(1));
		}
		
		// until
		if( repeatUntilRadio.isSelected())
		{
			Date until = untilDate.getDate();
			appt.setRepeatUntil(until);
			appt.setRepeatFlag(true);
		}
		else
			appt.setRepeatUntil(null);

		// category
		String cat = (String) categoryBox.getSelectedItem();
		if (cat.equals("") || cat.equals(CategoryModel.UNCATEGORIZED)) {
			appt.setCategory(null);
		} else {
			appt.setCategory(cat);
		}

		// reminder times
		appt.setReminderTimes(new String(custRemTimes));

		appt.setPriority((Integer)prioritySpinner.getValue());
		return (dateChg);
	}

	/**
	 * Set the reminder times array based on the data in an Appointment
	 * 
	 * @param appt
	 *            the appointment
	 */
	private void setCustRemTimes(Appointment appt) {
		if (appt == null) {
			// if no appt - set all N
			custRemTimes = new char[ReminderTimes.getNum()];
			for (int i = 0; i < ReminderTimes.getNum(); ++i) {
				custRemTimes[i] = 'N';
			}
		} else {
			try {
				// set from appt
				custRemTimes = (appt.getReminderTimes()).toCharArray();
			} catch (Exception e) {
				// if null in appt, then set all N
				for (int i = 0; i < ReminderTimes.getNum(); ++i) {
					custRemTimes[i] = 'N';
				}
			}
		}
	}

	/**
	 * Sets the date.
	 * 
	 * @param year
	 *            the year
	 * @param month
	 *            the month
	 * @param day
	 *            the day
	 */
	public void setDate(int year, int month, int day) {
		year_ = year;
		month_ = month;
		day_ = day;

	}

	/**
	 * Sets the popup times label to show the user what the popup times are in
	 * human readbale form.
	 */
	public void setPopupTimesString(String reminderTimes) {

		StringBuffer line1 = new StringBuffer(ReminderTimes.getNum() * 5 + 15);
		StringBuffer line2 = new StringBuffer(ReminderTimes.getNum() * 5 + 15);

		custRemTimes = reminderTimes.toCharArray();

		// every Y value in custRemTimes is an active reminder time
		if (custRemTimes != null) {
			int i = 0;

			// negative times are after the appointment time
			while (ReminderTimes.getTimes(i) < 0 && i < ReminderTimes.getNum()) {
				if (custRemTimes[i] == 'Y') {
					int abs = -ReminderTimes.getTimes(i);
					// string together the times
					if (line1.length() > 0) {
						line1 = line1.append(", ").append(abs);
					} else {
						line1 = line1.append(abs);
					}
				}
				++i;
			}
			if (line1.length() > 0) {
				line1 = line1.append("   ").append(
						Resource.getResourceString("min_aft_app"));
			}

			// positive times are before the appointment
			while (i < ReminderTimes.getNum()) {
				if (custRemTimes[i] == 'Y') {
					if (line2.length() > 0) {
						// string together the times
						line2 = line2.append(", ").append(
								ReminderTimes.getTimes(i));
					} else {
						line2 = line2.append(ReminderTimes.getTimes(i));
					}
				}
				++i;
			}
			if (line2.length() > 0) {
				line2 = line2.append("   ").append(
						Resource.getResourceString("min_bef_app"));
			}

		}

		// set the label, which is html for formatting
		popupTimesLabel.setText("<html><p align=RIGHT>" + line1.toString()
				+ "<br>" + line2.toString());
	}

	/**
	 * set the UI components to show the data in an Appointment
	 * 
	 * @param key
	 *            the appointment key or -1 for a new appt
	 * @param defaultApptIn
	 *            the default appt - only passed in if we are copying an
	 *            existing appt
	 */
	public void showapp(int key, Appointment defaultApptIn) {
		Appointment defaultAppt = defaultApptIn;
		currentlyShownAppointmentKey = key;

		// military time option
		String mt = Prefs.getPref(PrefName.MILTIME);

		// default to untimed - will change later if timed
		amOrPmComboBox.setSelected(false);
		startMinuteComboBox.setEnabled(false);
		startHourComboBox.setEnabled(false);
		durationMinuteComboBox.setEnabled(false);
		durationHourComboBox.setEnabled(false);
		amOrPmComboBox.setEnabled(false);
		untimedCheckBox.setSelected(true);
		dateChangeCheckBox.setSelected(false);
		encryptBox.setSelected(false);
		appointmentBodyTextArea.setEditable(true);
		apptTitleField.setEditable(true);
		decryptButton.setEnabled(false);

		saveButton.setEnabled(true);
		saveCloseButton.setEnabled(true);

		// default to unset for toggles
		for (JToggleButton tog : dayToggles) {
			tog.setSelected(false);
		}

		// get default appt values from XML, if any
		if (defaultAppt == null) {
			defaultAppt = AppointmentModel.getReference()
					.getDefaultAppointment();
		}

		// a key of -1 means to show a new blank appointment
		if (currentlyShownAppointmentKey == -1 && defaultAppt == null) {

			//
			// set all of the blank appointment defaults if
			// there is no default appointment
			//
			if (mt.equals("true")) {
				startHourComboBox.setSelectedIndex(0);
			} else {
				startHourComboBox.setSelectedIndex(11); // hour = 12
			}

			categoryBox.setSelectedIndex(0);
			startMinuteComboBox.setSelectedIndex(0);
			durationMinuteComboBox.setSelectedIndex(0);
			durationHourComboBox.setSelectedIndex(0);

			todoCheckBox.setSelected(false); // todo unchecked
			colorComboBox.setSelectedIndex(3); // color = black
			vacationCheckBox.setSelected(false); // vacation unchecked
			halfDayVacationCheckBox.setSelected(false); // half-day unchecked
			holidayCheckBox.setSelected(false); // holiday unchecked
			privateCheckBox.setSelected(false); // private unchecked
			appointmentBodyTextArea.setText(""); // clear appt text
			repeatFrequencyComboBox.setSelectedIndex(0); // freq = once
			numberOfRepeatsSpinner.setEnabled(true);
			numberOfRepeatsSpinner.setValue(new Integer(1)); // times = 1
			repeatForeverRadio.setSelected(false);
			repeatUntilRadio.setSelected(false);
			untilDate.setEnabled(false);
			showRepeatNumberCheckBox.setSelected(false);
			showRepeatNumberCheckBox.setEnabled(false);
			ResourceHelper.setText(newAppointmentIndicatorLabel,
					"*****_NEW_APPT_*****");

			dateChangeCheckBox.setEnabled(false);
			newdatefield.setEnabled(false);

			setCustRemTimes(null);
			setPopupTimesString(new String(custRemTimes));

		} else {

			try {

				Appointment appt = null;
				if (currentlyShownAppointmentKey == -1) {
					// new appt - but load from default appt
					ResourceHelper.setText(newAppointmentIndicatorLabel,
							"*****_NEW_APPT_*****");
					appt = defaultAppt;
					linkPanel.setOwner(null);
				} else {
					// get the appt Appointment from the calmodel
					newAppointmentIndicatorLabel.setText("    ");
					appt = AppointmentModel.getReference().getAppt(
							currentlyShownAppointmentKey);
					linkPanel.setOwner(appt);
				}

				// set hour and minute widgets
				Date d = appt.getDate();
				GregorianCalendar g = new GregorianCalendar();
				g.setTime(d);
				if (mt.equals("true")) {
					int hour = g.get(Calendar.HOUR_OF_DAY);
					if (hour != 0)
						startHourComboBox.setSelectedIndex(hour);
				} else {
					int hour = g.get(Calendar.HOUR);
					if (hour == 0)
						hour = 12;
					startHourComboBox.setSelectedIndex(hour - 1);
				}

				int min = g.get(Calendar.MINUTE);
				startMinuteComboBox.setSelectedIndex(min / 5);

				// duration
				Integer duration = appt.getDuration();
				int dur = 0;
				if (duration != null)
					dur = duration.intValue();
				durationHourComboBox.setSelectedIndex(dur / 60);
				durationMinuteComboBox.setSelectedIndex((dur % 60) / 5);

				// check if we just have a "note" (non-timed appt)
				boolean untimed = AppointmentModel.isNote(appt);
				if (!untimed) {
					// enable time widgets
					untimedCheckBox.setSelected(false);
					startMinuteComboBox.setEnabled(true);
					startHourComboBox.setEnabled(true);
					durationMinuteComboBox.setEnabled(true);
					durationHourComboBox.setEnabled(true);
					amOrPmComboBox.setEnabled(true);
				}

				// set ToDo checkbox
				todoCheckBox.setSelected(appt.getTodo());

				// set vacation checkbox
				vacationCheckBox.setSelected(false);
				Integer ii = appt.getVacation();
				if (ii != null && ii.intValue() == 1)
					vacationCheckBox.setSelected(true);

				// set half-day checkbox
				halfDayVacationCheckBox.setSelected(false);
				if (ii != null && ii.intValue() == 2)
					halfDayVacationCheckBox.setSelected(true);

				// holiday checkbox
				holidayCheckBox.setSelected(false);
				ii = appt.getHoliday();
				if (ii != null && ii.intValue() == 1)
					holidayCheckBox.setSelected(true);

				// private checkbox
				privateCheckBox.setSelected(appt.getPrivate());

				// PM checkbox
				boolean pm = true;
				if (g.get(Calendar.AM_PM) == Calendar.AM)
					pm = false;
				amOrPmComboBox.setSelected(pm);

				if (appt.isEncrypted()) {
					apptTitleField.setText(Resource
							.getResourceString("EncryptedItem"));
					apptTitleField.setEditable(false);
					appointmentBodyTextArea.setText("");
					appointmentBodyTextArea.setEditable(false);
					decryptButton.setEnabled(true);
				} else {
					// set appt text, split apart title and body
					String t = appt.getText();
					String title = "";
					String body = "";
					if (t == null)
						t = "";
					int newlineIndex = t.indexOf('\n');
					if (newlineIndex != -1) {
						title = t.substring(0, newlineIndex);
						body = t.substring(newlineIndex + 1);
					} else {
						title = t;
					}
					appointmentBodyTextArea.setText(body);
					apptTitleField.setText(title);
				}

				// color
				String sel = appt.getColor();
				if (sel != null) {

					if (sel.equals("black")) {
						colorComboBox.setSelectedIndex(3);
					} else if (sel.equals("red")) {
						colorComboBox.setSelectedIndex(0);
					} else if (sel.equals("blue")) {
						colorComboBox.setSelectedIndex(1);
					} else if (sel.equals("green")) {
						colorComboBox.setSelectedIndex(2);
					} else if (sel.equals("white")) {
						colorComboBox.setSelectedIndex(4);
					} else {
						colorComboBox.setSelectedIndex(5);
					}

				} else {
					// default is black
					colorComboBox.setSelectedIndex(3);
				}

				dateChangeCheckBox.setEnabled(true);
				newdatefield.setEnabled(false);

				// repeat frequency - turn on/off widgets as needed
				String rpt = Repeat.getFreq(appt.getFrequency());
				if (rpt != null && rpt.equals(Repeat.NDAYS)) {
					numberOfDaysSpinner.setValue(new Integer(Repeat
							.getNDays(appt.getFrequency())));
				}

				if (rpt != null && rpt.equals(Repeat.DAYLIST)) {
					Collection<Integer> daylist = Repeat.getDaylist(appt
							.getFrequency());
					if (daylist != null) {
						if (daylist.contains(new Integer(Calendar.SUNDAY)))
							dayToggles[0].setSelected(true);
						if (daylist.contains(new Integer(Calendar.MONDAY)))
							dayToggles[1].setSelected(true);
						if (daylist.contains(new Integer(Calendar.TUESDAY)))
							dayToggles[2].setSelected(true);
						if (daylist.contains(new Integer(Calendar.WEDNESDAY)))
							dayToggles[3].setSelected(true);
						if (daylist.contains(new Integer(Calendar.THURSDAY)))
							dayToggles[4].setSelected(true);
						if (daylist.contains(new Integer(Calendar.FRIDAY)))
							dayToggles[5].setSelected(true);
						if (daylist.contains(new Integer(Calendar.SATURDAY)))
							dayToggles[6].setSelected(true);
					}
				}

				showRepeatNumberCheckBox.setSelected(Repeat.getRptNum(appt
						.getFrequency()));

				repeatFrequencyComboBox.setSelectedItem(Repeat
						.getFreqString(rpt));

				// repeat times
				Integer tm = appt.getTimes();
				numberOfRepeatsSpinner.setValue(new Integer(1));
				untilDate.setDate(null);

				// if until date is set, then that takes priority
				if( appt.getRepeatUntil() != null )
				{
					untilDate.setDate(appt.getRepeatUntil());
					repeatUntilRadio.setSelected(true);
				}
				else if (tm != null) {
					if (tm.intValue() == MAGIC_RPT_FOREVER_VALUE) { 
						repeatForeverRadio.setSelected(true);
					} else {
						numberOfRepeatsSpinner.setValue(tm);
						repeatTimesRadio.setSelected(true);
					}
				} else {
					repeatTimesRadio.setSelected(true);
				}

				// set category combo box
				String cat = appt.getCategory();
				if (cat != null && !cat.equals("")) {
					categoryBox.setSelectedItem(cat);
				} else {
					categoryBox.setSelectedIndex(0);
				}

				// set reminder times
				setCustRemTimes(appt);
				setPopupTimesString(new String(custRemTimes));

				// encryption
				encryptBox.setSelected(appt.isEncrypted());
				if (appt.isEncrypted()) {
					saveButton.setEnabled(false);
					saveCloseButton.setEnabled(false);
				}
				
				// set priority
				Integer p = new Integer(5);
				Integer priority = appt.getPriority();
				if (priority != null)
					p = priority;
				prioritySpinner.setValue(p);

			} catch (Exception e) {
				Errmsg.errmsg(e);
				Exception ne = new Exception(Resource
						.getResourceString("appt_error"));
				Errmsg.errmsg(ne);

			}
		}

		// set components based on repeat frequency
		timesEnable();

		// reset background to white in case we were showing a validation error
		// color
		apptTitleField.setBackground(new Color(255, 255, 255));
		apptTitleField.requestFocus();

	}

	/**
	 * enable the proper repeat widgets based on repeat frequency
	 */
	private void timesEnable() {
		if (repeatFrequencyComboBox.getSelectedIndex() == 0) {
			numberOfRepeatsSpinner.setEnabled(false);
			repeatForeverRadio.setEnabled(false);
			repeatTimesRadio.setEnabled(false);
			repeatUntilRadio.setEnabled(false);
			showRepeatNumberCheckBox.setEnabled(false);
			untilDate.setEnabled(false);

		} else {
			numberOfRepeatsSpinner.setEnabled(true);
			repeatForeverRadio.setEnabled(true);
			repeatTimesRadio.setEnabled(true);
			repeatUntilRadio.setEnabled(true);
			showRepeatNumberCheckBox.setEnabled(true);
			untilDate.setEnabled(true);
		}

		if (Repeat.freqToEnglish(
				(String) repeatFrequencyComboBox.getSelectedItem()).equals(
				Repeat.NDAYS)) {
			numberOfDaysSpinner.setVisible(true);
			selectDayButtonPanel.setVisible(false);
		} else if (Repeat.freqToEnglish(
				(String) repeatFrequencyComboBox.getSelectedItem()).equals(
				Repeat.DAYLIST)) {
			selectDayButtonPanel.setVisible(true);
			numberOfDaysSpinner.setVisible(false);
		} else {
			selectDayButtonPanel.setVisible(false);
			numberOfDaysSpinner.setVisible(false);
		}
	}
}