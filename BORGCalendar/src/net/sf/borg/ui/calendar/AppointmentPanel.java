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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.common.XTree;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.xml.AppointmentXMLAdapter;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.link.LinkPanel;
import net.sf.borg.ui.popup.PopupOptionsView;

import com.toedter.calendar.JDateChooser;

public class AppointmentPanel extends JPanel {

	static private class ColorBoxRenderer extends JLabel implements
			ListCellRenderer {

		public ColorBoxRenderer() {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			String sel = (String) value;

			if (isSelected) {
				setBackground(list.getSelectionBackground());
			} else {
				setBackground(list.getBackground());
			}

			setText(" ");
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
				setForeground(Color.BLACK);
				setText(Resource.getResourceString("strike"));
				setIcon(null);
			}

			return this;
		}

	}

	static private class SolidComboBoxIcon implements Icon {
		private Color color = Color.BLACK;

		private final int h = 10;

		private final int w = 60;

		public SolidComboBoxIcon(Color col) {
			color = col;
		}

		public int getIconHeight() {
			return h;
		}

		public int getIconWidth() {
			return w;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.BLACK);
			g2.drawRect(x, y, w, h);
			g2.setColor(color);
			g2.fillRect(x, y, w, h);
		}
	}

	static private DefaultComboBoxModel milHourModel = new DefaultComboBoxModel(
			new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
					"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
					"20", "21", "22", "23" });

	static private DefaultComboBoxModel normHourModel = new DefaultComboBoxModel(
			new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
					"11", "12" });

	static private SimpleDateFormat shortDayFmt = new SimpleDateFormat("EEE");

	// for setting labels
	static private GregorianCalendar tmpcal = new GregorianCalendar();

	private JCheckBox alarmcb = null;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JTextArea appttextarea;

	private JTextField apptTitleField = null;

	private LinkPanel attPanel = null;

	private javax.swing.JComboBox catbox;

	private javax.swing.JCheckBox chgdate;

	private javax.swing.JComboBox colorbox;

	private String colors[] = { "red", "blue", "green", "black", "white",
			"strike" };

	private char[] custRemTimes;

	private int day_;

	private JToggleButton dl1 = null;

	private JToggleButton dl2 = null;

	private JToggleButton dl3 = null;

	private JToggleButton dl4 = null;

	private JToggleButton dl5 = null;

	private JToggleButton dl6 = null;

	private JToggleButton dl7 = null;

	private JPanel dlistbuttonpanel = null;

	private javax.swing.JComboBox durhour;

	private javax.swing.JComboBox durmin;

	private javax.swing.JCheckBox foreverbox;

	private javax.swing.JComboBox freq;

	private javax.swing.JCheckBox halfdaycb;

	private javax.swing.JCheckBox holidaycb;

	private JLabel jLabel = null;

	private javax.swing.JLabel jLabel2;

	private JPanel jPanel = null;

	private javax.swing.JPanel jPanel1;

	private javax.swing.JPanel jPanel2;

	private javax.swing.JPanel jPanel3;

	private javax.swing.JPanel jPanel4;

	private JPanel jPanel5;

	private javax.swing.JScrollPane jScrollPane1;

	private int key_;

	private javax.swing.JLabel lblCategory;

	private javax.swing.JLabel lblColor;

	private javax.swing.JLabel lblDuration;

	private javax.swing.JLabel lblFrequency;

	private javax.swing.JLabel lblNewDate;

	private javax.swing.JLabel lblStartTime;

	private javax.swing.JLabel lblTimes;

	private int month_;

	private JSpinner ndays = null;

	private JDateChooser newdatefield;

	private javax.swing.JCheckBox notecb;

	private JButton popupTimesBtn;

	private JLabel popupTimesLabel;

	private javax.swing.JCheckBox privatecb;

	private JCheckBox rptnumbox = null;

	private javax.swing.JSpinner s_times;

	private javax.swing.JButton savebutton;

	private javax.swing.JButton saveclosebutton;

	private javax.swing.JButton savedefaultsbutton;

	private javax.swing.JCheckBox startap;

	private javax.swing.JComboBox starthour;

	private javax.swing.JComboBox startmin;

	private javax.swing.JCheckBox todocb;

	private javax.swing.JCheckBox vacationcb;

	private int year_;

	public AppointmentPanel(int year, int month, int day) {

		// init GUI
		initComponents();

		ColorBoxRenderer cbr = new ColorBoxRenderer();
		colorbox.setRenderer(cbr);
		colorbox.setEditable(false);

		// set up the spinner for repeat times
		SpinnerNumberModel mod = (SpinnerNumberModel) s_times.getModel();
		mod.setMinimum(new Integer(1));

		// set up hours pulldown
		String mt = Prefs.getPref(PrefName.MILTIME);
		if (mt.equals("true")) {
			starthour.setModel(milHourModel);
			startap.setVisible(false);
		} else {
			starthour.setModel(normHourModel);
			startap.setVisible(true);
		}

		try {
			Collection<String> cats = CategoryModel.getReference()
					.getCategories();
			Iterator<String> it = cats.iterator();
			while (it.hasNext()) {
				catbox.addItem(it.next());
			}
			catbox.setSelectedIndex(0);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		for (int i = 0; i < colors.length; i++) {
			colorbox.addItem(colors[i]);
		}

		for (int i = 0;; i++) {
			String fs = Repeat.getFreqString(i);
			if (fs == null)
				break;
			freq.addItem(fs);
		}

		setDate(year, month, day);
		setCustRemTimes(null);

		apptTitleField.requestFocus();
	}

	private void add_appt() throws Warning, Exception {
		// user has requested an add of a new appt

		// get a new appt from the model and set it from the user data
		AppointmentModel calmod_ = AppointmentModel.getReference();
		Appointment r = calmod_.newAppt();
		setAppt(r, true);

		calmod_.saveAppt(r);

		showapp(-1, null);
	}

	private void chg_appt() throws Warning, Exception {
		// user had selected appt change

		// get a new empty appt from the model and set it using the data the
		// user has entered
		AppointmentModel calmod_ = AppointmentModel.getReference();
		Appointment r = calmod_.newAppt();
		boolean dateChg = setAppt(r, true);
		r.setKey(key_);

		// call the model to change the appt
		if (dateChg == false) {
			// need to preserve data from original appt
			try {
				
				// date of orig might not match appt panel date - so
				// we must keep the original date
				Appointment ap = calmod_.getAppt(key_);
				Calendar cal = new GregorianCalendar();
				Calendar newCal = new GregorianCalendar();

				cal.setTime(ap.getDate());
				newCal.setTime(r.getDate());
				newCal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
				newCal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
				newCal.set(Calendar.DATE, cal.get(Calendar.DATE));
				r.setDate(newCal.getTime());

				// determine if we can keep certain fields related to repeating
				// and todos
				if (r.getTimes().intValue() == ap.getTimes().intValue()
						&& r.getFrequency() != null
						&& ap.getFrequency() != null
						&& Repeat.getFreq(r.getFrequency()).equals(
								Repeat.getFreq(ap.getFrequency()))
						&& r.getTodo() == ap.getTodo()
						&& r.getRepeatFlag() == ap.getRepeatFlag()) {
					// we can keep skip list and next todo
					r.setSkipList(ap.getSkipList());
					r.setNextTodo(ap.getNextTodo());
				}

				// should carry forward sync flags
				r.setNew(ap.getNew());
				r.setModified(ap.getModified());
				
			} catch (Exception e) {
				// Errmsg.errmsg(e);
			}
			calmod_.saveAppt(r);
		} else {
			r.setKey(key_);
			try {
				calmod_.saveAppt(r);
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}

		}

		showapp(-1, null);
	}

	private void chgdateActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_chgdateActionPerformed
		newdatefield.setEnabled(chgdate.isSelected());

	}// GEN-LAST:event_chgdateActionPerformed

	private void foreverboxActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_foreverboxActionPerformed
	{// GEN-HEADEREND:event_foreverboxActionPerformed
		if (foreverbox.isSelected()) {
			s_times.setValue(new Integer(0));
			s_times.setEnabled(false);

		} else {
			s_times.setValue(new Integer(1));
			s_times.setEnabled(true);
		}
	}// GEN-LAST:event_foreverboxActionPerformed

	/**
	 * This method initializes apptTitleField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getApptTitleField() {
		if (apptTitleField == null) {
			apptTitleField = new JTextField();
		}
		return apptTitleField;
	}

	/**
	 * This method initializes dl1
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getDl1() {
		if (dl1 == null) {
			dl1 = new JToggleButton();
			tmpcal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			dl1.setText(shortDayFmt.format(tmpcal.getTime()));
		}
		return dl1;
	}

	/**
	 * This method initializes dl2
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getDl2() {
		if (dl2 == null) {
			dl2 = new JToggleButton();
			tmpcal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			dl2.setText(shortDayFmt.format(tmpcal.getTime()));
		}
		return dl2;
	}

	/**
	 * This method initializes dl3
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getDl3() {
		if (dl3 == null) {
			dl3 = new JToggleButton();
			tmpcal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
			dl3.setText(shortDayFmt.format(tmpcal.getTime()));
		}
		return dl3;
	}

	/**
	 * This method initializes dl4
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getDl4() {
		if (dl4 == null) {
			dl4 = new JToggleButton();
			tmpcal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
			dl4.setText(shortDayFmt.format(tmpcal.getTime()));
		}
		return dl4;
	}

	/**
	 * This method initializes dl5
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getDl5() {
		if (dl5 == null) {
			dl5 = new JToggleButton();
			tmpcal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
			dl5.setText(shortDayFmt.format(tmpcal.getTime()));
		}
		return dl5;
	}

	/**
	 * This method initializes dl6
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getDl6() {
		if (dl6 == null) {
			dl6 = new JToggleButton();
			tmpcal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
			dl6.setText(shortDayFmt.format(tmpcal.getTime()));
		}
		return dl6;
	}

	/**
	 * This method initializes dl7
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getDl7() {
		if (dl7 == null) {
			dl7 = new JToggleButton();
			tmpcal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			dl7.setText(shortDayFmt.format(tmpcal.getTime()));
		}
		return dl7;
	}

	/**
	 * This method initializes dlistbuttonpanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getDlistbuttonpanel() {
		if (dlistbuttonpanel == null) {
			dlistbuttonpanel = new JPanel();
			dlistbuttonpanel.setLayout(new BoxLayout(getDlistbuttonpanel(),
					BoxLayout.X_AXIS)); // Generated
			dlistbuttonpanel.add(getDl1(), null); // Generated
			dlistbuttonpanel.add(getDl2(), null); // Generated
			dlistbuttonpanel.add(getDl3(), null); // Generated
			dlistbuttonpanel.add(getDl4(), null); // Generated
			dlistbuttonpanel.add(getDl5(), null); // Generated
			dlistbuttonpanel.add(getDl6(), null); // Generated
			dlistbuttonpanel.add(getDl7(), null); // Generated
		}
		return dlistbuttonpanel;
	}

	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints71 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setBorder(new javax.swing.border.TitledBorder(null, Resource
					.getResourceString("Recurrence"),
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, null,
					null));
			gridBagConstraints31.gridx = 0;
			gridBagConstraints31.gridy = 0;
			gridBagConstraints31.insets = new java.awt.Insets(0, 0, 0, 0);
			gridBagConstraints41.gridx = 1;
			gridBagConstraints41.gridy = 0;
			gridBagConstraints41.anchor = java.awt.GridBagConstraints.SOUTHEAST;
			gridBagConstraints41.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints41.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints41.weightx = 0.0D;
			gridBagConstraints51.gridx = 0;
			gridBagConstraints51.gridy = 1;
			gridBagConstraints51.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints51.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints51.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints61.gridx = 1;
			gridBagConstraints61.gridy = 1;
			gridBagConstraints61.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints61.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints61.weightx = 0.0D;
			gridBagConstraints71.gridx = 2;
			gridBagConstraints71.gridy = 1;
			gridBagConstraints71.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints13.gridx = 4;
			gridBagConstraints13.gridy = 0;
			gridBagConstraints28.gridx = 2;
			gridBagConstraints28.gridy = 0;
			gridBagConstraints28.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints28.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints19.gridx = 2;
			gridBagConstraints19.gridy = 2;
			// gridBagConstraints19.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints19.fill = java.awt.GridBagConstraints.BOTH;
			jPanel.add(lblFrequency, gridBagConstraints31);
			jPanel.add(freq, gridBagConstraints41);
			jPanel.add(lblTimes, gridBagConstraints51);
			jPanel.add(s_times, gridBagConstraints61);
			jPanel.add(foreverbox, gridBagConstraints71);
			jPanel.add(ndays, gridBagConstraints28);

			jPanel.add(getRptnumbox(), gridBagConstraints19);

			gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH; // Generated
			gridBagConstraints1.gridy = 0; // Generated
			gridBagConstraints1.gridx = 2; // Generated
			gridBagConstraints1.gridwidth = 2;
			jPanel.add(getDlistbuttonpanel(), gridBagConstraints1); // Generated
		}
		return jPanel;
	}

	private JCheckBox getRemBox() {
		if (alarmcb == null) {
			alarmcb = new JCheckBox();
			ResourceHelper.setText(alarmcb, "Alarm");
		}
		return alarmcb;
	}

	/**
	 * This method initializes rptnumbox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getRptnumbox() {
		if (rptnumbox == null) {
			rptnumbox = new JCheckBox();
			ResourceHelper.setText(rptnumbox, "show_rpt_num");
		}
		return rptnumbox;
	}

	public String getText() {
		String labelstring = appttextarea.getText();
		if (labelstring.equals("")) {
			return Resource.getResourceString("*****_NEW_APPT_*****");
		}

		return labelstring;

	}

	/**
	 * This method is called from within the constructor to init
	 * gridBagConstraints28.gridx = 0; gridBagConstraints28.gridy = 4;
	 * gridBagConstraints28.fill = java.awt.GridBagConstraints.BOTH;
	 * this.add(getJPanel(), gridBagConstraints28); ialize the form. WARNING: Do
	 * NOT modify this code. The content of this method is always regenerated by
	 * the Form Editor.
	 */
	private void initComponents()// GEN-BEGIN:initComponents
	{

		GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
		gridBagConstraints10.fill = GridBagConstraints.BOTH;
		gridBagConstraints10.gridy = 0;
		gridBagConstraints10.weightx = 1.0;
		gridBagConstraints10.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints10.gridx = 1;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		jLabel = new JLabel();
		jLabel.setText(Resource.getPlainResourceString("subject"));
		GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
		jLabel2 = new javax.swing.JLabel();
		jPanel1 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		appttextarea = new javax.swing.JTextArea();
		jPanel2 = new javax.swing.JPanel();
		starthour = new javax.swing.JComboBox();
		startmin = new javax.swing.JComboBox();
		startap = new javax.swing.JCheckBox();
		durhour = new javax.swing.JComboBox();
		durmin = new javax.swing.JComboBox();
		lblStartTime = new javax.swing.JLabel();
		lblDuration = new javax.swing.JLabel();
		notecb = new javax.swing.JCheckBox();
		lblNewDate = new javax.swing.JLabel();
		newdatefield = new JDateChooser();
		newdatefield.setDateFormatString("MMM dd, yyyy");
		chgdate = new javax.swing.JCheckBox();
		jPanel3 = new javax.swing.JPanel();
		todocb = new javax.swing.JCheckBox();
		vacationcb = new javax.swing.JCheckBox();
		halfdaycb = new javax.swing.JCheckBox();
		holidaycb = new javax.swing.JCheckBox();
		privatecb = new javax.swing.JCheckBox();
		lblColor = new javax.swing.JLabel();
		colorbox = new javax.swing.JComboBox();
		lblFrequency = new javax.swing.JLabel();
		freq = new javax.swing.JComboBox();
		s_times = new javax.swing.JSpinner();
		lblTimes = new javax.swing.JLabel();
		catbox = new javax.swing.JComboBox();
		lblCategory = new javax.swing.JLabel();
		foreverbox = new javax.swing.JCheckBox();
		jPanel4 = new javax.swing.JPanel();
		GridBagConstraints gridBagConstraints81 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints91 = new GridBagConstraints();
		savebutton = new javax.swing.JButton();
		saveclosebutton = new javax.swing.JButton();
		savedefaultsbutton = new javax.swing.JButton();
		ndays = new JSpinner();
		ndays.setModel(new SpinnerNumberModel(2, 2, 3000, 1));

		lblStartTime.setLabelFor(starthour);
		lblDuration.setLabelFor(durhour);
		lblCategory.setLabelFor(catbox);
		lblColor.setLabelFor(colorbox);
		lblFrequency.setLabelFor(freq);
		lblNewDate.setLabelFor(newdatefield);
		lblTimes.setLabelFor(s_times);

		setLayout(new java.awt.GridBagLayout());

		jLabel2.setForeground(java.awt.Color.red);
		// jLabel2.setText("jLabel2");
		GridBagConstraints gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
		add(jLabel2, gridBagConstraints2);

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setBorder(new javax.swing.border.TitledBorder(Resource
				.getResourceString("appttext")));
		jScrollPane1.setPreferredSize(new Dimension(200, 140));
		appttextarea.setColumns(40);
		appttextarea.setSize(new Dimension(300, 84));
		appttextarea.setPreferredSize(new Dimension(300, 84));
		appttextarea.setLineWrap(true);
		appttextarea.setRows(5);
		appttextarea.setWrapStyleWord(true);
		appttextarea.setBorder(new javax.swing.border.BevelBorder(
				javax.swing.border.BevelBorder.LOWERED));
		appttextarea.setMinimumSize(new java.awt.Dimension(284, 140));
		jScrollPane1.setViewportView(appttextarea);

		GridBagConstraints gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints1.weightx = 0.5D;
		gridBagConstraints1.gridy = 2;
		gridBagConstraints1.gridwidth = 1;
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints1.weighty = 0.5D;
		jPanel1.add(jScrollPane1, gridBagConstraints1);
		jPanel1.add(jLabel, gridBagConstraints);
		jPanel1.add(getApptTitleField(), gridBagConstraints10);
		GridBagConstraints gridBagConstraints3 = new java.awt.GridBagConstraints();
		gridBagConstraints3.gridx = 0;
		gridBagConstraints3.gridy = 1;
		gridBagConstraints3.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints3.weightx = 1.5;
		gridBagConstraints3.weighty = 2.0;
		gridBagConstraints3.insets = new java.awt.Insets(5, 5, 5, 5);
		add(jPanel1, gridBagConstraints3);

		jPanel2.setLayout(new java.awt.GridBagLayout());

		jPanel2.setBorder(new javax.swing.border.TitledBorder(Resource
				.getResourceString("appttime")));
		starthour.setMaximumRowCount(24);
		starthour.setMinimumSize(new java.awt.Dimension(42, 36));
		starthour.setOpaque(false);
		GridBagConstraints gridBagConstraints4 = new java.awt.GridBagConstraints();
		gridBagConstraints4.gridx = 1;
		gridBagConstraints4.gridy = 0;
		gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
		startmin.setMaximumRowCount(12);
		startmin.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
				"00", "05", "10", "15", "20", "25", "30", "35", "40", "45",
				"50", "55" }));
		startmin.setOpaque(false);
		GridBagConstraints gridBagConstraints5 = new java.awt.GridBagConstraints();
		gridBagConstraints5.gridx = 2;
		gridBagConstraints5.gridy = 0;
		gridBagConstraints5.fill = java.awt.GridBagConstraints.VERTICAL;
		startap.setText("PM");
		startap.setOpaque(false);
		GridBagConstraints gridBagConstraints6 = new java.awt.GridBagConstraints();
		gridBagConstraints6.gridx = 3;
		gridBagConstraints6.gridy = 0;
		gridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
		jPanel2.add(startap, gridBagConstraints6);

		durhour.setMaximumRowCount(24);
		durhour.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
				"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
				"12", "13", "14", "15", "16", "17", "18", "19", "20", "21",
				"22", "23", "24" }));
		GridBagConstraints gridBagConstraints7 = new java.awt.GridBagConstraints();
		gridBagConstraints7.gridx = 1;
		gridBagConstraints7.gridy = 1;
		gridBagConstraints7.fill = java.awt.GridBagConstraints.BOTH;
		durmin.setMaximumRowCount(12);
		durmin.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
				"00", "05", "10", "15", "20", "25", "30", "35", "40", "45",
				"50", "55" }));
		durmin.setOpaque(false);
		GridBagConstraints gridBagConstraints8 = new java.awt.GridBagConstraints();
		gridBagConstraints8.gridx = 2;
		gridBagConstraints8.gridy = 1;
		gridBagConstraints8.fill = java.awt.GridBagConstraints.VERTICAL;
		ResourceHelper.setText(lblStartTime, "Start_Time:");
		GridBagConstraints gridBagConstraints9 = new java.awt.GridBagConstraints();
		gridBagConstraints9.gridx = 0;
		gridBagConstraints9.gridy = 0;
		gridBagConstraints9.fill = java.awt.GridBagConstraints.VERTICAL;
		gridBagConstraints9.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints9.insets = new java.awt.Insets(0, 7, 0, 7);
		jPanel2.add(lblStartTime, gridBagConstraints9);

		ResourceHelper.setText(lblDuration, "Duration:");
		GridBagConstraints gridBagConstraints11 = new java.awt.GridBagConstraints();
		gridBagConstraints11.gridx = 0;
		gridBagConstraints11.gridy = 1;
		gridBagConstraints11.fill = java.awt.GridBagConstraints.VERTICAL;
		gridBagConstraints11.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints11.insets = new java.awt.Insets(0, 8, 0, 8);
		jPanel2.add(lblDuration, gridBagConstraints11);

		ResourceHelper.setText(notecb, "No_Specific_Time");
		freq.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				timesEnable();
			}

		});
		notecb.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				notecbActionPerformed(evt);
			}
		});

		GridBagConstraints gridBagConstraints12 = new java.awt.GridBagConstraints();
		gridBagConstraints12.gridx = 4;
		gridBagConstraints12.gridy = 0;
		gridBagConstraints12.gridwidth = 1;
		gridBagConstraints12.fill = java.awt.GridBagConstraints.BOTH;
		ResourceHelper.setText(lblNewDate, "newDate:");
		GridBagConstraints gridBagConstraints14 = new java.awt.GridBagConstraints();
		gridBagConstraints14.gridx = 5;
		gridBagConstraints14.gridy = 1;
		gridBagConstraints14.fill = java.awt.GridBagConstraints.BOTH;

		GridBagConstraints gridBagConstraints15 = new java.awt.GridBagConstraints();
		gridBagConstraints15.gridx = 6;
		gridBagConstraints15.gridy = 1;
		gridBagConstraints15.fill = java.awt.GridBagConstraints.BOTH;

		ResourceHelper.setText(chgdate, "changedate");
		chgdate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				chgdateActionPerformed(evt);
			}
		});

		GridBagConstraints gridBagConstraints16 = new java.awt.GridBagConstraints();
		gridBagConstraints16.gridx = 4;
		gridBagConstraints16.gridy = 1;
		GridBagConstraints gridBagConstraints17 = new java.awt.GridBagConstraints();
		gridBagConstraints17.gridx = 0;
		gridBagConstraints17.gridy = 2;
		gridBagConstraints17.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		gridBagConstraints17.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints17.weightx = 1.0;
		gridBagConstraints17.weighty = 1.0;
		gridBagConstraints17.insets = new java.awt.Insets(5, 5, 5, 5);
		jPanel3.setLayout(new java.awt.GridBagLayout());

		jPanel3.setBorder(new javax.swing.border.TitledBorder(Resource
				.getResourceString("Properties")));
		jPanel3.setMinimumSize(new java.awt.Dimension(539, 128));
		ResourceHelper.setText(todocb, "To_Do");
		todocb.setOpaque(false);
		GridBagConstraints gridBagConstraints21 = new java.awt.GridBagConstraints();
		gridBagConstraints21.weightx = 1.0;
		gridBagConstraints21.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(todocb, gridBagConstraints21);

		vacationcb.setForeground(new java.awt.Color(0, 102, 0));
		ResourceHelper.setText(vacationcb, "Vacation");
		vacationcb.setOpaque(false);
		GridBagConstraints gridBagConstraints22 = new java.awt.GridBagConstraints();
		gridBagConstraints22.weightx = 1.0;
		gridBagConstraints22.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(vacationcb, gridBagConstraints22);

		halfdaycb.setForeground(new java.awt.Color(0, 102, 102));
		ResourceHelper.setText(halfdaycb, "Half_Day");
		halfdaycb.setOpaque(false);
		GridBagConstraints gridBagConstraints23 = new java.awt.GridBagConstraints();
		gridBagConstraints23.weightx = 1.0;
		gridBagConstraints23.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(halfdaycb, gridBagConstraints23);

		ResourceHelper.setText(holidaycb, "Holiday");
		holidaycb.setOpaque(false);
		GridBagConstraints gridBagConstraints24 = new java.awt.GridBagConstraints();
		gridBagConstraints24.weightx = 1.0;
		gridBagConstraints24.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(holidaycb, gridBagConstraints24);

		ResourceHelper.setText(privatecb, "Private");
		privatecb.setOpaque(false);
		GridBagConstraints gridBagConstraints25 = new java.awt.GridBagConstraints();
		gridBagConstraints25.weightx = 1.0;
		gridBagConstraints25.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(privatecb, gridBagConstraints25);

		lblColor.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		ResourceHelper.setText(lblColor, "Color");
		GridBagConstraints gridBagConstraints26 = new java.awt.GridBagConstraints();
		gridBagConstraints26.gridx = 0;
		gridBagConstraints26.gridy = 1;
		gridBagConstraints26.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints26.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints26.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(lblColor, gridBagConstraints26);

		colorbox.setOpaque(false);
		GridBagConstraints gridBagConstraints27 = new java.awt.GridBagConstraints();
		gridBagConstraints27.gridx = 1;
		gridBagConstraints27.gridy = 1;
		gridBagConstraints27.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints27.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints27.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(colorbox, gridBagConstraints27);

		lblFrequency.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		ResourceHelper.setText(lblFrequency, "Frequency");
		freq.setOpaque(false);
		s_times.setBorder(new javax.swing.border.EtchedBorder());
		lblTimes.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		ResourceHelper.setText(lblTimes, "Times");
		GridBagConstraints gridBagConstraints35 = new java.awt.GridBagConstraints();
		gridBagConstraints35.gridx = 3;
		gridBagConstraints35.gridy = 1;
		gridBagConstraints35.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints35.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(catbox, gridBagConstraints35);

		lblCategory.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		ResourceHelper.setText(lblCategory, "Category");
		GridBagConstraints gridBagConstraints36 = new java.awt.GridBagConstraints();
		gridBagConstraints36.gridx = 2;
		gridBagConstraints36.gridy = 1;
		gridBagConstraints36.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints36.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints36.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(lblCategory, gridBagConstraints36);

		ResourceHelper.setText(foreverbox, "forever");
		foreverbox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				foreverboxActionPerformed(evt);
			}
		});

		GridBagConstraints gridBagConstraints38 = new java.awt.GridBagConstraints();
		gridBagConstraints38.gridx = 0;
		gridBagConstraints38.gridy = 3;
		gridBagConstraints38.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints38.weightx = 0.0D;
		gridBagConstraints38.weighty = 0.0D;
		gridBagConstraints38.gridwidth = 2;
		gridBagConstraints38.insets = new java.awt.Insets(5, 5, 5, 5);
		savebutton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Save16.gif")));
		ResourceHelper.setText(savebutton, "Save");
		savebutton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				savebuttonActionPerformed(evt);
			}
		});

		jPanel4.add(savebutton);

		saveclosebutton.setIcon(new javax.swing.ImageIcon(getClass()
				.getResource("/resource/Save16.gif")));
		ResourceHelper.setText(saveclosebutton, "Save_&_Close");
		saveclosebutton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveclosebuttonActionPerformed(evt);
			}
		});

		jPanel4.add(saveclosebutton);

		savedefaultsbutton.setIcon(new javax.swing.ImageIcon(getClass()
				.getResource("/resource/SaveAs16.gif")));
		ResourceHelper.setText(savedefaultsbutton, "save_Def");
		savedefaultsbutton.setToolTipText(Resource.getResourceString("sd_tip"));
		savedefaultsbutton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						saveDefaults(evt);
					}
				});

		jPanel4.add(savedefaultsbutton);

		gridBagConstraints12.insets = new java.awt.Insets(0, 30, 0, 0);
		gridBagConstraints16.insets = new java.awt.Insets(0, 30, 0, 0);
		this.add(jPanel2, gridBagConstraints17);
		this.setSize(648, 590);
		jPanel2.add(notecb, gridBagConstraints12);
		jPanel2.add(lblNewDate, gridBagConstraints14);
		gridBagConstraints16.fill = java.awt.GridBagConstraints.HORIZONTAL;
		jPanel2.add(chgdate, gridBagConstraints16);
		gridBagConstraints4.insets = new java.awt.Insets(2, 2, 2, 2);
		jPanel2.add(starthour, gridBagConstraints4);
		gridBagConstraints7.insets = new java.awt.Insets(2, 2, 2, 2);
		jPanel2.add(durhour, gridBagConstraints7);
		gridBagConstraints5.insets = new java.awt.Insets(2, 2, 2, 2);
		jPanel2.add(startmin, gridBagConstraints5);
		gridBagConstraints8.insets = new java.awt.Insets(2, 2, 2, 2);
		gridBagConstraints81.gridx = 0;
		gridBagConstraints81.gridy = 5;
		gridBagConstraints81.weightx = 0.0;
		gridBagConstraints81.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints81.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints91.gridx = 0;
		gridBagConstraints91.gridy = 6;
		gridBagConstraints91.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints91.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints91.gridwidth = 2;
		gridBagConstraints15.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints18.gridx = 4;
		gridBagConstraints18.gridy = 1;
		gridBagConstraints18.fill = java.awt.GridBagConstraints.NONE;
		gridBagConstraints18.insets = new java.awt.Insets(4, 4, 4, 4);
		this.add(jPanel3, gridBagConstraints38);
		this.add(getJPanel(), gridBagConstraints81);
		this.add(jPanel4, gridBagConstraints91);
		jPanel2.add(durmin, gridBagConstraints8);

		jPanel3.add(getRemBox(), gridBagConstraints18);
		jPanel2.add(newdatefield, gridBagConstraints15);

		popupTimesLabel = new javax.swing.JLabel();
		popupTimesLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		popupTimesBtn = new javax.swing.JButton();
		ResourceHelper.setText(popupTimesBtn, "Change");
		popupTimesBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				popupbuttonActionPerformed(evt);
			}
		});

		jPanel5 = new javax.swing.JPanel();
		jPanel5.setBorder(new javax.swing.border.TitledBorder(Resource
				.getResourceString("popup_reminders")));
		jPanel5.add(popupTimesLabel);
		jPanel5.add(popupTimesBtn);

		GridBagConstraints gridBagConstraints87 = new java.awt.GridBagConstraints();
		gridBagConstraints87.gridx = 0;
		gridBagConstraints87.gridy = 4;
		gridBagConstraints87.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints87.weightx = 9.0;
		gridBagConstraints87.weighty = 1.0D;
		gridBagConstraints87.gridwidth = 2;
		gridBagConstraints87.insets = new java.awt.Insets(4, 4, 4, 4);
		this.add(jPanel5, gridBagConstraints87); // Generated

		gridBagConstraints87.gridx = 1;
		gridBagConstraints87.gridy = 5;
		gridBagConstraints87.gridwidth = 1;
		gridBagConstraints87.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints87.weightx = 0.0;
		gridBagConstraints87.insets = new java.awt.Insets(4, 4, 4, 4);

		attPanel = new LinkPanel();
		attPanel.setBorder(new javax.swing.border.TitledBorder(Resource
				.getResourceString("links")));
		this.add(attPanel, gridBagConstraints87);

	}// GEN-END:initComponents

	private void notecbActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_notecbActionPerformed
	{// GEN-HEADEREND:event_notecbActionPerformed
		if (notecb.isSelected()) {
			String mt = Prefs.getPref(PrefName.MILTIME);
			if (mt.equals("true")) {
				starthour.setSelectedIndex(0);
			} else {
				starthour.setSelectedIndex(11); // hour = 12
			}
			startmin.setSelectedIndex(0);
			durmin.setSelectedIndex(0);
			durhour.setSelectedIndex(0);
			startap.setSelected(false);
			startmin.setEnabled(false);
			starthour.setEnabled(false);
			durmin.setEnabled(false);
			durhour.setEnabled(false);
			startap.setEnabled(false);
		} else {
			startmin.setEnabled(true);
			starthour.setEnabled(true);
			durmin.setEnabled(true);
			durhour.setEnabled(true);
			startap.setEnabled(true);
		}
	}// GEN-LAST:event_notecbActionPerformed

	private void popupbuttonActionPerformed(java.awt.event.ActionEvent evt) {

		PopupOptionsView pv = new PopupOptionsView(custRemTimes, this);
		pv.setVisible(true);
	}

	private void savebuttonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_savebuttonActionPerformed
		try {
			if (key_ == -1) {
				add_appt();
			} else {
				chg_appt();
			}
		} catch (Warning w) {
			Errmsg.notice(w.getMessage());
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}// GEN-LAST:event_savebuttonActionPerformed

	private void saveclosebuttonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveclosebuttonActionPerformed
		try {
			if (key_ == -1) {
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

	}// GEN-LAST:event_saveclosebuttonActionPerformed

	private void saveDefaults(java.awt.event.ActionEvent evt)// GEN-FIRST:event_saveDefaults
	{// GEN-HEADEREND:event_saveDefaults

		Appointment r = new Appointment();
		try {
			setAppt(r, false);
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}

		AppointmentXMLAdapter axa = new AppointmentXMLAdapter();
		XTree xt = axa.toXml(r);
		String s = xt.toString();
		Prefs.putPref(PrefName.DEFAULT_APPT, s);

	}// GEN-LAST:event_saveDefaults

	
	private boolean setAppt(Appointment r, boolean validate) throws Warning,
			Exception {

		// get the hour and minute
		int hr = starthour.getSelectedIndex();
		String mt = Prefs.getPref(PrefName.MILTIME);
		if (mt.equals("false")) {
			hr = hr + 1;
			if (hr == 12)
				hr = 0;
			if (startap.isSelected())
				hr += 12;
		}

		if (notecb.isSelected())
			r.setUntimed("Y");

		Date nd = null;
		boolean dateChg = false;
		if (chgdate.isSelected()) {
			nd = newdatefield.getDate();
			dateChg = true;
		}

		int min = startmin.getSelectedIndex() * 5;
		GregorianCalendar g = new GregorianCalendar();

		if (nd == null) {
			g.set(year_, month_, day_, hr, min, 0);
		} else {
			g.setTime(nd);
			g.set(Calendar.HOUR_OF_DAY, hr);
			g.set(Calendar.MINUTE, min);
			g.set(Calendar.SECOND, 0);
		}

		// set the appt date/time
		r.setDate(g.getTime());

		int du = (durhour.getSelectedIndex() * 60)
				+ (durmin.getSelectedIndex() * 5);
		if (du != 0)
			r.setDuration(new Integer(du));

		// appointment text of some sort is required
		if (apptTitleField.getText().equals("") && validate) {
			apptTitleField.requestFocus();
			apptTitleField.setBackground(new Color(255, 255, 204));
			throw new Warning(Resource
					.getResourceString("Please_enter_some_appointment_text"));
		}

		// set text
		String t = apptTitleField.getText();
		if (appttextarea.getText() != null
				&& !appttextarea.getText().equals(""))
			t += "\n" + appttextarea.getText();
		r.setText(t);

		// to do
		r.setTodo(todocb.isSelected());

		// vacation, half-day, and private checkboxes
		if (vacationcb.isSelected())
			r.setVacation(new Integer(1));
		if (halfdaycb.isSelected())
			r.setVacation(new Integer(2));
		if (holidaycb.isSelected())
			r.setHoliday(new Integer(1));
		if (alarmcb.isSelected())
			r.setAlarm("Y");

		r.setPrivate(privatecb.isSelected());

		// color
		r.setColor((String) colorbox.getSelectedItem());

		// repeat frequency
		if (freq.getSelectedIndex() != 0) {
			ArrayList<Integer> daylist = new ArrayList<Integer>();
			if (dl1.isSelected())
				daylist.add(new Integer(Calendar.SUNDAY));
			if (dl2.isSelected())
				daylist.add(new Integer(Calendar.MONDAY));
			if (dl3.isSelected())
				daylist.add(new Integer(Calendar.TUESDAY));
			if (dl4.isSelected())
				daylist.add(new Integer(Calendar.WEDNESDAY));
			if (dl5.isSelected())
				daylist.add(new Integer(Calendar.THURSDAY));
			if (dl6.isSelected())
				daylist.add(new Integer(Calendar.FRIDAY));
			if (dl7.isSelected())
				daylist.add(new Integer(Calendar.SATURDAY));
			if (!Repeat.isCompatible(g, (String) freq.getSelectedItem(),
					daylist)) {
				throw new Warning(Resource.getResourceString("recur_compat"));
			}
			r.setFrequency(Repeat
					.freqString((String) freq.getSelectedItem(),
							(Integer) ndays.getValue(), rptnumbox.isSelected(),
							daylist));
		}

		// repeat times
		Integer tm = null;
		if (foreverbox.isSelected()) {
			tm = new Integer(9999);
		} else {
			tm = (Integer) s_times.getValue();
		}

		if (tm.intValue() > 1 && freq.getSelectedIndex() != 0) {
			try {
				r.setTimes(tm);

				if (tm.intValue() > 1)
					r.setRepeatFlag(true);
				else
					r.setRepeatFlag(false);
			} catch (Exception e) {
				throw new Exception(Resource
						.getResourceString("Could_not_parse_times:_")
						+ tm);
			}

		} else {
			r.setTimes(new Integer(1));
		}

		String cat = (String) catbox.getSelectedItem();
		if (cat.equals("") || cat.equals(CategoryModel.UNCATEGORIZED)) {
			r.setCategory(null);
		} else {
			r.setCategory(cat);
		}

		r.setReminderTimes(new String(custRemTimes));

		return (dateChg);
	}

	private void setCustRemTimes(Appointment r) {
		if (r == null) {
			custRemTimes = new char[ReminderTimes.getNum()];
			for (int i = 0; i < ReminderTimes.getNum(); ++i) {
				custRemTimes[i] = 'N';
			}
		} else {

			try {
				custRemTimes = (r.getReminderTimes()).toCharArray();

			} catch (Exception e) {
				for (int i = 0; i < ReminderTimes.getNum(); ++i) {
					custRemTimes[i] = 'N';
				}
			}
		}
	}

	public void setDate(int year, int month, int day) {
		year_ = year;
		month_ = month;
		day_ = day;

	}

	// display a summary of the times selected for popup reminders
	public void setPopupTimesString() {
		StringBuffer time1 = new StringBuffer(ReminderTimes.getNum() * 5 + 15);
		StringBuffer time2 = new StringBuffer(ReminderTimes.getNum() * 5 + 15);
		if (custRemTimes != null) {
			int i = 0;
			while (ReminderTimes.getTimes(i) < 0 && i < ReminderTimes.getNum()) {
				if (custRemTimes[i] == 'Y') {
					int abs = -ReminderTimes.getTimes(i);
					if (time1.length() > 0) {
						time1 = time1.append(", ").append(abs);
					} else {
						time1 = time1.append(abs);
					}
				}
				++i;
			}
			if (time1.length() > 0) {
				time1 = time1.append("   ").append(
						Resource.getResourceString("min_aft_app"));
			}

			while (i < ReminderTimes.getNum()) {
				if (custRemTimes[i] == 'Y') {
					if (time2.length() > 0) {
						time2 = time2.append(", ").append(
								ReminderTimes.getTimes(i));
					} else {
						time2 = time2.append(ReminderTimes.getTimes(i));
					}
				}
				++i;
			}
			if (time2.length() > 0) {
				time2 = time2.append("   ").append(
						Resource.getResourceString("min_bef_app"));
			}

		}
		popupTimesLabel.setText("<html><p align=RIGHT>" + time1.toString()
				+ "<br>" + time2.toString());
	}

	// set the view to a single appt (or a new blank)
	public void showapp(int key, Appointment defaultAppt) {
		key_ = key;
		String mt = Prefs.getPref(PrefName.MILTIME);

		startap.setSelected(false);
		startmin.setEnabled(false);
		starthour.setEnabled(false);
		durmin.setEnabled(false);
		durhour.setEnabled(false);
		startap.setEnabled(false);
		notecb.setSelected(true);
		chgdate.setSelected(false);
		dl1.setSelected(false);
		dl2.setSelected(false);
		dl3.setSelected(false);
		dl4.setSelected(false);
		dl5.setSelected(false);
		dl6.setSelected(false);
		dl7.setSelected(false);

		// get default appt values, if any
		String defApptXml = Prefs.getPref(PrefName.DEFAULT_APPT);
		if (defaultAppt == null && !defApptXml.equals("")) {
			try {
				XTree xt = XTree.readFromBuffer(defApptXml);
				AppointmentXMLAdapter axa = new AppointmentXMLAdapter();
				defaultAppt = axa.fromXml(xt);
			} catch (Exception e) {
				Errmsg.errmsg(e);
			}
		}

		// a key of -1 means to show a new blank appointment
		if (key_ == -1 && defaultAppt == null) {

			if (mt.equals("true")) {
				starthour.setSelectedIndex(0);
			} else {
				starthour.setSelectedIndex(11); // hour = 12
			}

			catbox.setSelectedIndex(0);
			startmin.setSelectedIndex(0);
			durmin.setSelectedIndex(0);
			durhour.setSelectedIndex(0);

			todocb.setSelected(false); // todo unchecked
			colorbox.setSelectedIndex(3); // color = black
			vacationcb.setSelected(false); // vacation unchecked
			halfdaycb.setSelected(false); // half-day unchecked
			holidaycb.setSelected(false); // holiday unchecked
			privatecb.setSelected(false); // private unchecked
			alarmcb.setSelected(false);
			appttextarea.setText(""); // clear appt text
			freq.setSelectedIndex(0); // freq = once
			s_times.setEnabled(true);
			s_times.setValue(new Integer(1)); // times = 1
			foreverbox.setSelected(false);
			rptnumbox.setSelected(false);
			rptnumbox.setEnabled(false);
			ResourceHelper.setText(jLabel2, "*****_NEW_APPT_*****");

			// only add menu choice active for a new appt

			chgdate.setEnabled(false);
			newdatefield.setEnabled(false);

			setCustRemTimes(null);
			setPopupTimesString();

		} else {

			try {

				// get the appt Appointment from the calmodel
				Appointment r = null;
				if (key_ == -1) {
					ResourceHelper.setText(jLabel2, "*****_NEW_APPT_*****");
					r = defaultAppt;
					attPanel.setOwner(null);
				} else {
					// erase New Appt indicator
					jLabel2.setText("    ");
					r = AppointmentModel.getReference().getAppt(key_);
					attPanel.setOwner(r);
				}

				// set hour and minute
				Date d = r.getDate();
				GregorianCalendar g = new GregorianCalendar();
				g.setTime(d);
				if (mt.equals("true")) {
					int hour = g.get(Calendar.HOUR_OF_DAY);
					if (hour != 0)
						starthour.setSelectedIndex(hour);
				} else {
					int hour = g.get(Calendar.HOUR);
					if (hour == 0)
						hour = 12;
					starthour.setSelectedIndex(hour - 1);
				}

				int min = g.get(Calendar.MINUTE);
				startmin.setSelectedIndex(min / 5);

				// duration
				Integer duration = r.getDuration();
				int dur = 0;
				if (duration != null)
					dur = duration.intValue();
				durhour.setSelectedIndex(dur / 60);
				durmin.setSelectedIndex((dur % 60) / 5);

				boolean note = AppointmentModel.isNote(r);

				// check if we just have a "note" (non-timed appt)
				if (!note) {
					notecb.setSelected(false);
					startmin.setEnabled(true);
					starthour.setEnabled(true);
					durmin.setEnabled(true);
					durhour.setEnabled(true);
					startap.setEnabled(true);
				}

				// set ToDo checkbox
				todocb.setSelected(r.getTodo());

				// set vacation checkbox
				vacationcb.setSelected(false);
				Integer ii = r.getVacation();
				if (ii != null && ii.intValue() == 1)
					vacationcb.setSelected(true);

				// set half-day checkbox
				halfdaycb.setSelected(false);
				if (ii != null && ii.intValue() == 2)
					halfdaycb.setSelected(true);

				// holiday checkbox
				holidaycb.setSelected(false);
				ii = r.getHoliday();
				if (ii != null && ii.intValue() == 1)
					holidaycb.setSelected(true);

				String alm = r.getAlarm();
				if (alm != null && alm.equals("Y")) {
					alarmcb.setSelected(true);
				} else {
					alarmcb.setSelected(false);
				}

				// private checkbox
				privatecb.setSelected(r.getPrivate());

				// PM checkbox
				boolean pm = true;
				if (g.get(Calendar.AM_PM) == Calendar.AM)
					pm = false;
				startap.setSelected(pm);

				// set appt text
				String t = r.getText();
				String subj = "";
				String det = "";
				if (t == null)
					t = "";
				int nli = t.indexOf('\n');
				if (nli != -1) {
					subj = t.substring(0, nli);
					det = t.substring(nli + 1);
				} else {
					subj = t;
				}
				appttextarea.setText(det);
				apptTitleField.setText(subj);

				// color
				String sel = r.getColor();
				if (sel != null) {

					if (sel.equals("black")) {
						colorbox.setSelectedIndex(3);
					} else if (sel.equals("red")) {
						colorbox.setSelectedIndex(0);
					} else if (sel.equals("blue")) {
						colorbox.setSelectedIndex(1);
					} else if (sel.equals("green")) {
						colorbox.setSelectedIndex(2);
					} else if (sel.equals("white")) {
						colorbox.setSelectedIndex(4);
					} else {
						colorbox.setSelectedIndex(5);
					}

				} else {
					colorbox.setSelectedIndex(3);
				}

				chgdate.setEnabled(true);
				newdatefield.setEnabled(false);

				// repeat frequency
				String rpt = Repeat.getFreq(r.getFrequency());

				if (rpt != null && rpt.equals(Repeat.NDAYS)) {
					ndays.setValue(new Integer(Repeat
							.getNDays(r.getFrequency())));
				}

				if (rpt != null && rpt.equals(Repeat.DAYLIST)) {
					Collection<Integer> daylist = Repeat.getDaylist(r
							.getFrequency());
					if (daylist != null) {
						if (daylist.contains(new Integer(Calendar.SUNDAY)))
							dl1.setSelected(true);
						if (daylist.contains(new Integer(Calendar.MONDAY)))
							dl2.setSelected(true);
						if (daylist.contains(new Integer(Calendar.TUESDAY)))
							dl3.setSelected(true);
						if (daylist.contains(new Integer(Calendar.WEDNESDAY)))
							dl4.setSelected(true);
						if (daylist.contains(new Integer(Calendar.THURSDAY)))
							dl5.setSelected(true);
						if (daylist.contains(new Integer(Calendar.FRIDAY)))
							dl6.setSelected(true);
						if (daylist.contains(new Integer(Calendar.SATURDAY)))
							dl7.setSelected(true);
					}
				}

				rptnumbox.setSelected(Repeat.getRptNum(r.getFrequency()));

				freq.setSelectedItem(Repeat.getFreqString(rpt));

				// repeat times
				Integer tm = r.getTimes();
				if (tm != null) {
					if (tm.intValue() == 9999) {
						foreverbox.setSelected(true);
						s_times.setValue(new Integer(0));
						s_times.setEnabled(false);
					} else {
						s_times.setEnabled(true);
						s_times.setValue(tm);
						foreverbox.setSelected(false);
					}

				} else {
					s_times.setEnabled(true);
					s_times.setValue(new Integer(1));
					foreverbox.setSelected(false);
				}

				String cat = r.getCategory();
				if (cat != null && !cat.equals("")) {
					catbox.setSelectedItem(cat);
				} else {
					catbox.setSelectedIndex(0);
				}

				// set custRemTimes
				setCustRemTimes(r);
				setPopupTimesString();

			} catch (Exception e) {
				Errmsg.errmsg(e);
				Exception ne = new Exception(Resource
						.getResourceString("appt_error"));
				Errmsg.errmsg(ne);

			}
		}

		timesEnable();
		apptTitleField.setBackground(new Color(255, 255, 255));
		apptTitleField.requestFocus();

	}

	// set the visibility and enabling of components that depend on the
	// frequency pulldown
	private void timesEnable() {
		if (freq.getSelectedIndex() == 0) {
			s_times.setEnabled(false);
			foreverbox.setEnabled(false);
			rptnumbox.setEnabled(false);
		} else {
			s_times.setEnabled(true);
			foreverbox.setEnabled(true);
			rptnumbox.setEnabled(true);
		}

		if (Repeat.freqToEnglish((String) freq.getSelectedItem()).equals(
				Repeat.NDAYS)) {
			ndays.setVisible(true);
			dlistbuttonpanel.setVisible(false);
		} else if (Repeat.freqToEnglish((String) freq.getSelectedItem())
				.equals(Repeat.DAYLIST)) {
			dlistbuttonpanel.setVisible(true);
			ndays.setVisible(false);
		} else {
			dlistbuttonpanel.setVisible(false);
			ndays.setVisible(false);
		}
	}
} // @jve:decl-index=0:visual-constraint="10,10"

