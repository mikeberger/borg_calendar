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
package net.sf.borg.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.Warning;
import net.sf.borg.common.util.XTree;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.AppointmentXMLAdapter;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Repeat;

class AppointmentPanel extends JPanel {

	private int key_;

	private int year_;

	private int month_;

	private int day_;

	static private DefaultComboBoxModel normHourModel = new DefaultComboBoxModel(
			new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
					"11", "12" });

	static private DefaultComboBoxModel milHourModel = new DefaultComboBoxModel(
			new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
					"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
					"20", "21", "22", "23" });
	
	//private String colors[] = { "black", "red", "blue", "green", "white", "strike" };
	private String colors[] = { "red", "blue", "green", "black", "white", "strike" };
	
	static private class SolidComboBoxIcon implements Icon
	{
		private  Color color = Color.BLACK;
		private  final int h = 10;
		private  final int w = 60;
		public SolidComboBoxIcon( Color col ){ color = col; }
		public int getIconHeight(){ return h; }
		public int getIconWidth(){ return w; }
		public void paintIcon(Component c,Graphics g,int x,int y)
		{
			Graphics2D g2 = (Graphics2D)g;
			g2.setColor( Color.BLACK);
			g2.drawRect(x,y,w,h);
			g2.setColor(color);
			g2.fillRect(x, y, w,h);
		}
	}
	
	static private class ColorBoxRenderer extends JLabel implements ListCellRenderer {

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
			if( sel.equals("black") )
			{
				setIcon( new SolidComboBoxIcon(new Color(Integer.parseInt(Prefs.getPref(PrefName.UCS_BLACK)))));
			}
			else if( sel.equals("red") )
			{
				setIcon( new SolidComboBoxIcon(new Color(Integer.parseInt(Prefs.getPref(PrefName.UCS_RED)))));
			}
			else if( sel.equals("blue") )
			{
				setIcon( new SolidComboBoxIcon(new Color(Integer.parseInt(Prefs.getPref(PrefName.UCS_BLUE)))));
			}
			else if( sel.equals("green") )
			{
				setIcon( new SolidComboBoxIcon(new Color(Integer.parseInt(Prefs.getPref(PrefName.UCS_GREEN)))));
			}			
			else if( sel.equals("white") )
			{
				setIcon( new SolidComboBoxIcon(new Color(Integer.parseInt(Prefs.getPref(PrefName.UCS_WHITE)))));
			}
			else
			{
				setForeground(Color.BLACK);
				setText(Resource.getResourceString("strike"));
				setIcon(null);
			}

			return this;
		}

	}

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
			Collection cats = CategoryModel.getReference().getCategories();
			Iterator it = cats.iterator();
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

		String palm = Prefs.getPref(PrefName.PALM_SYNC);
		if (!palm.equals("true")) {
			alarmcb.setEnabled(false);
		}
		setCustRemTimes(null);
	}

	public void setDate(int year, int month, int day) {
		year_ = year;
		month_ = month;
		day_ = day;

	}

	// set the view to a single appt (or a new blank)
	public void showapp(int key) {
		key_ = key;
		String mt = Prefs.getPref(PrefName.MILTIME);

		// assume "note" as default
		boolean note = true;
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
		Appointment defaultAppt = null;
		String defApptXml = Prefs.getPref(PrefName.DEFAULT_APPT);
		if (!defApptXml.equals("")) {
			try {
				XTree xt = XTree.readFromBuffer(defApptXml);
				AppointmentXMLAdapter axa = new AppointmentXMLAdapter();
				defaultAppt = (Appointment) axa.fromXml(xt);
			} catch (Exception e) {
				Errmsg.errmsg(e);
				defaultAppt = null;
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
			jLabel2
					.setText(java.util.ResourceBundle.getBundle(
							"resource/borg_resource").getString(
							"*****_NEW_APPT_*****"));

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
					jLabel2.setText(java.util.ResourceBundle.getBundle(
							"resource/borg_resource").getString(
							"*****_NEW_APPT_*****"));
					r = defaultAppt;
				} else {
					// erase New Appt indicator
					jLabel2.setText("    ");
					r = AppointmentModel.getReference().getAppt(key_);
				}

				// set hour and minute
				Date d = r.getDate();
				GregorianCalendar g = new GregorianCalendar();
				g.setTime(d);
				if (mt.equals("true")) {
					int hour = g.get(Calendar.HOUR_OF_DAY);
					if (hour != 0)
						note = false;
					starthour.setSelectedIndex(hour);
				} else {
					int hour = g.get(Calendar.HOUR);
					if (hour != 0)
						note = false;
					if (hour == 0)
						hour = 12;
					starthour.setSelectedIndex(hour - 1);
				}

				int min = g.get(Calendar.MINUTE);
				if (min != 0)
					note = false;
				startmin.setSelectedIndex(min / 5);

				// duration
				Integer duration = r.getDuration();
				int dur = 0;
				if (duration != null)
					dur = duration.intValue();
				durhour.setSelectedIndex(dur / 60);
				durmin.setSelectedIndex((dur % 60) / 5);
				if (dur != 0)
					note = false;

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
				appttextarea.setText(t);

				// color
				String sel = r.getColor();
				if (sel != null) {
					
						if( sel.equals("black") )
						{
							colorbox.setSelectedIndex(3);
						}
						else if( sel.equals("red") )
						{
							colorbox.setSelectedIndex(0);
						}
						else if( sel.equals("blue") )
						{
							colorbox.setSelectedIndex(1);
						}
						else if( sel.equals("green") )
						{
							colorbox.setSelectedIndex(2);
						}			
						else if( sel.equals("white") )
						{
							colorbox.setSelectedIndex(4);
						}
						else
						{
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
					Collection daylist = Repeat.getDaylist(r.getFrequency());
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
		jLabel3 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		notecb = new javax.swing.JCheckBox();
		jLabel8 = new javax.swing.JLabel();
		newdatefield = new de.wannawork.jcalendar.JCalendarComboBox();
		chgdate = new javax.swing.JCheckBox();
		jPanel3 = new javax.swing.JPanel();
		todocb = new javax.swing.JCheckBox();
		vacationcb = new javax.swing.JCheckBox();
		halfdaycb = new javax.swing.JCheckBox();
		holidaycb = new javax.swing.JCheckBox();
		privatecb = new javax.swing.JCheckBox();
		jLabel5 = new javax.swing.JLabel();
		colorbox = new javax.swing.JComboBox();
		jLabel4 = new javax.swing.JLabel();
		freq = new javax.swing.JComboBox();
		s_times = new javax.swing.JSpinner();
		jLabel1 = new javax.swing.JLabel();
		catbox = new javax.swing.JComboBox();
		jLabel7 = new javax.swing.JLabel();
		foreverbox = new javax.swing.JCheckBox();
		jPanel4 = new javax.swing.JPanel();
		GridBagConstraints gridBagConstraints81 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints91 = new GridBagConstraints();
		savebutton = new javax.swing.JButton();
		savedefaultsbutton = new javax.swing.JButton();
		ndays = new JSpinner();
		ndays.setModel(new SpinnerNumberModel(2, 2, 3000, 1));

		setLayout(new java.awt.GridBagLayout());

		jLabel2.setForeground(java.awt.Color.red);
		// jLabel2.setText("jLabel2");
		GridBagConstraints gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
		add(jLabel2, gridBagConstraints2);

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setBorder(new javax.swing.border.TitledBorder(
				java.util.ResourceBundle.getBundle("resource/borg_resource")
						.getString("appttext")));
		jScrollPane1.setPreferredSize(new java.awt.Dimension(320, 140));
		appttextarea.setColumns(40);
		appttextarea.setLineWrap(true);
		appttextarea.setRows(8);
		appttextarea.setWrapStyleWord(true);
		appttextarea.setBorder(new javax.swing.border.BevelBorder(
				javax.swing.border.BevelBorder.LOWERED));
		appttextarea.setMinimumSize(new java.awt.Dimension(284, 140));
		jScrollPane1.setViewportView(appttextarea);

		GridBagConstraints gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.weighty = 1.0;
		jPanel1.add(jScrollPane1, gridBagConstraints1);

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

		jPanel2.setBorder(new javax.swing.border.TitledBorder(
				java.util.ResourceBundle.getBundle("resource/borg_resource")
						.getString("appttime")));
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
				"22", "23" }));
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
		jLabel3.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Start_Time:"));
		GridBagConstraints gridBagConstraints9 = new java.awt.GridBagConstraints();
		gridBagConstraints9.gridx = 0;
		gridBagConstraints9.gridy = 0;
		gridBagConstraints9.fill = java.awt.GridBagConstraints.VERTICAL;
		gridBagConstraints9.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints9.insets = new java.awt.Insets(0, 7, 0, 7);
		jPanel2.add(jLabel3, gridBagConstraints9);

		jLabel6.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Duration:"));
		GridBagConstraints gridBagConstraints11 = new java.awt.GridBagConstraints();
		gridBagConstraints11.gridx = 0;
		gridBagConstraints11.gridy = 1;
		gridBagConstraints11.fill = java.awt.GridBagConstraints.VERTICAL;
		gridBagConstraints11.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints11.insets = new java.awt.Insets(0, 8, 0, 8);
		jPanel2.add(jLabel6, gridBagConstraints11);

		notecb.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("No_Specific_Time"));
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
		jLabel8.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("newDate:"));
		GridBagConstraints gridBagConstraints14 = new java.awt.GridBagConstraints();
		gridBagConstraints14.gridx = 5;
		gridBagConstraints14.gridy = 1;
		gridBagConstraints14.fill = java.awt.GridBagConstraints.BOTH;

		GridBagConstraints gridBagConstraints15 = new java.awt.GridBagConstraints();
		gridBagConstraints15.gridx = 6;
		gridBagConstraints15.gridy = 1;
		gridBagConstraints15.fill = java.awt.GridBagConstraints.BOTH;
		chgdate.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("changedate"));
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

		jPanel3.setBorder(new javax.swing.border.TitledBorder(
				java.util.ResourceBundle.getBundle("resource/borg_resource")
						.getString("Properties")));
		jPanel3.setMinimumSize(new java.awt.Dimension(539, 128));
		todocb.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("To_Do"));
		todocb.setOpaque(false);
		GridBagConstraints gridBagConstraints21 = new java.awt.GridBagConstraints();
		gridBagConstraints21.weightx = 1.0;
		gridBagConstraints21.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(todocb, gridBagConstraints21);

		vacationcb.setForeground(new java.awt.Color(0, 102, 0));
		vacationcb.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Vacation"));
		vacationcb.setOpaque(false);
		GridBagConstraints gridBagConstraints22 = new java.awt.GridBagConstraints();
		gridBagConstraints22.weightx = 1.0;
		gridBagConstraints22.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(vacationcb, gridBagConstraints22);

		halfdaycb.setForeground(new java.awt.Color(0, 102, 102));
		halfdaycb.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Half_Day"));
		halfdaycb.setOpaque(false);
		GridBagConstraints gridBagConstraints23 = new java.awt.GridBagConstraints();
		gridBagConstraints23.weightx = 1.0;
		gridBagConstraints23.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(halfdaycb, gridBagConstraints23);

		holidaycb.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Holiday"));
		holidaycb.setOpaque(false);
		GridBagConstraints gridBagConstraints24 = new java.awt.GridBagConstraints();
		gridBagConstraints24.weightx = 1.0;
		gridBagConstraints24.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(holidaycb, gridBagConstraints24);

		privatecb.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Private"));
		privatecb.setOpaque(false);
		GridBagConstraints gridBagConstraints25 = new java.awt.GridBagConstraints();
		gridBagConstraints25.weightx = 1.0;
		gridBagConstraints25.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(privatecb, gridBagConstraints25);

		jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel5.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Color"));
		GridBagConstraints gridBagConstraints26 = new java.awt.GridBagConstraints();
		gridBagConstraints26.gridx = 0;
		gridBagConstraints26.gridy = 1;
		gridBagConstraints26.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints26.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints26.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(jLabel5, gridBagConstraints26);

		colorbox.setOpaque(false);
		GridBagConstraints gridBagConstraints27 = new java.awt.GridBagConstraints();
		gridBagConstraints27.gridx = 1;
		gridBagConstraints27.gridy = 1;
		gridBagConstraints27.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints27.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints27.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(colorbox, gridBagConstraints27);

		jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel4.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Frequency"));
		freq.setOpaque(false);
		s_times.setBorder(new javax.swing.border.EtchedBorder());
		jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel1.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Times"));
		GridBagConstraints gridBagConstraints35 = new java.awt.GridBagConstraints();
		gridBagConstraints35.gridx = 3;
		gridBagConstraints35.gridy = 1;
		gridBagConstraints35.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints35.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(catbox, gridBagConstraints35);

		jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel7.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Category"));
		GridBagConstraints gridBagConstraints36 = new java.awt.GridBagConstraints();
		gridBagConstraints36.gridx = 2;
		gridBagConstraints36.gridy = 1;
		gridBagConstraints36.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints36.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints36.insets = new java.awt.Insets(4, 4, 4, 4);
		jPanel3.add(jLabel7, gridBagConstraints36);

		foreverbox.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("forever"));
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
		gridBagConstraints38.insets = new java.awt.Insets(5, 5, 5, 5);
		savebutton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Save16.gif")));
		savebutton.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Save"));
		savebutton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				savebuttonActionPerformed(evt);
			}
		});

		jPanel4.add(savebutton);

		savedefaultsbutton.setIcon(new javax.swing.ImageIcon(getClass()
				.getResource("/resource/SaveAs16.gif")));
		savedefaultsbutton.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("save_Def"));
		savedefaultsbutton.setToolTipText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("sd_tip"));
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
		this.setSize(531, 487);
		jPanel2.add(notecb, gridBagConstraints12);
		jPanel2.add(jLabel8, gridBagConstraints14);
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
		gridBagConstraints81.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints81.insets = new java.awt.Insets(4, 4, 4, 4);
		gridBagConstraints91.gridx = 0;
		gridBagConstraints91.gridy = 6;
		gridBagConstraints91.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints91.insets = new java.awt.Insets(4, 4, 4, 4);
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
		popupTimesBtn.setText(java.util.ResourceBundle.getBundle(
				"resource/borg_resource").getString("Change"));
		popupTimesBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				popupbuttonActionPerformed(evt);
			}
		});

		jPanel5 = new javax.swing.JPanel();
		jPanel5.setBorder(new javax.swing.border.TitledBorder(
				java.util.ResourceBundle.getBundle("resource/borg_resource")
						.getString("popup_reminders")));
		jPanel5.add(popupTimesLabel);
		jPanel5.add(popupTimesBtn);

		GridBagConstraints gridBagConstraints88 = new java.awt.GridBagConstraints();
		gridBagConstraints88.gridx = 0;
		gridBagConstraints88.gridy = 4;
		gridBagConstraints88.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints88.weightx = 0.0;
		gridBagConstraints88.weighty = 0.0;
		gridBagConstraints88.insets = new java.awt.Insets(4, 4, 4, 4);

		// this.add(jPanel, gridBagConstraints88);

		GridBagConstraints gridBagConstraints87 = new java.awt.GridBagConstraints();
		gridBagConstraints87.gridx = 0;
		gridBagConstraints87.gridy = 4;
		gridBagConstraints87.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints87.weightx = 0.0;
		gridBagConstraints87.weighty = 0.0;
		gridBagConstraints87.insets = new java.awt.Insets(4, 4, 4, 4);
		this.add(jPanel5, gridBagConstraints87);
	}// GEN-END:initComponents

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

	private void savebuttonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_savebuttonActionPerformed
		if (key_ == -1) {
			add_appt();
		} else {
			chg_appt();
		}

		showapp(-1);

	}// GEN-LAST:event_savebuttonActionPerformed

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

	private void chgdateActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_chgdateActionPerformed
		newdatefield.setEnabled(chgdate.isSelected());

	}// GEN-LAST:event_chgdateActionPerformed

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

	// fill in an appt from the user data
	// returns changed key if any
	private int setAppt(Appointment r, boolean validate) throws Warning,
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

		Date nd = null;
		int newkey = 0;
		if (chgdate.isSelected()) {
			nd = newdatefield.getCalendar().getTime();
		}

		int min = startmin.getSelectedIndex() * 5;
		GregorianCalendar g = new GregorianCalendar();

		if (nd == null) {
			g.set(year_, month_, day_, hr, min);
		} else {
			g.setTime(nd);
			g.set(Calendar.HOUR_OF_DAY, hr);
			g.set(Calendar.MINUTE, min);
			newkey = AppointmentModel.dkey(g.get(Calendar.YEAR), g
					.get(Calendar.MONTH), g.get(Calendar.DATE));
		}

		// set the appt date/time
		r.setDate(g.getTime());

		int du = (durhour.getSelectedIndex() * 60)
				+ (durmin.getSelectedIndex() * 5);
		if (du != 0)
			r.setDuration(new Integer(du));

		// appointment text of some sort is required
		if (appttextarea.getText().equals("") && validate) {
			throw new Warning(Resource
					.getResourceString("Please_enter_some_appointment_text"));
		}

		// set text
		r.setText(appttextarea.getText());

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
			ArrayList daylist = new ArrayList();
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

				// set a boolean flag in SMDB if the appt repeats
				// this is very important for performance. When the model
				// first indexes the appts, it will only have to read and parse
				// the textual (non-boolean) data for repeating appointments
				// to calculate when the repeats fall. For non-repeating appts
				// the boolean will not be set and the model knows the appt can
				// be indexed
				// on a single day - without needing to read the DB text.
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

		return (newkey);
	}

	

	/*private String colorToEnglish(String color) {
		for (int i = 0; i < colors.length; i++) {
			if (color.equals(Resource.getResourceString(colors[i]))) {
				return (colors[i]);
			}
		}

		return ("black");
	}*/

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

	private void add_appt() {
		// user has requested an add of a new appt

		// get a new appt from the model and set it from the user data
		AppointmentModel calmod_ = AppointmentModel.getReference();
		Appointment r = calmod_.newAppt();
		int ret = 0;
		try {
			ret = setAppt(r, true);
		} catch (Warning w) {
			Errmsg.notice(w.getMessage());
			return;
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}

		if (ret < 0)
			return;

		calmod_.saveAppt(r, true);
	}

	private void chg_appt() {
		// user had selected appt change

		// get a new empty appt from the model and set it using the data the
		// user has entered
		AppointmentModel calmod_ = AppointmentModel.getReference();
		Appointment r = calmod_.newAppt();
		int newkey = 0;
		try {
			newkey = setAppt(r, true);
		} catch (Warning w) {
			Errmsg.notice(w.getMessage());
			return;
		} catch (Exception e) {
			Errmsg.errmsg(e);
			return;
		}

		if (newkey < 0)
			return;

		// call the model to change the appt
		if (newkey == 0) {
			r.setKey(key_);
			calmod_.saveAppt(r, false);
		} else {
			calmod_.delAppt(key_);
			calmod_.saveAppt(r, true);

		}
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JTextArea appttextarea;

	private javax.swing.JComboBox catbox;

	private javax.swing.JCheckBox chgdate;

	private javax.swing.JComboBox colorbox;

	private javax.swing.JComboBox durhour;

	private javax.swing.JComboBox durmin;

	private javax.swing.JCheckBox foreverbox;

	private javax.swing.JComboBox freq;

	private javax.swing.JCheckBox halfdaycb;

	private javax.swing.JCheckBox holidaycb;

	private javax.swing.JLabel jLabel1;

	private javax.swing.JLabel jLabel2;

	private javax.swing.JLabel jLabel3;

	private javax.swing.JLabel jLabel4;

	private javax.swing.JLabel jLabel5;

	private javax.swing.JLabel jLabel6;

	private javax.swing.JLabel jLabel7;

	private javax.swing.JLabel jLabel8;

	private javax.swing.JPanel jPanel1;

	private javax.swing.JPanel jPanel2;

	private javax.swing.JPanel jPanel3;

	private javax.swing.JPanel jPanel4;

	private javax.swing.JScrollPane jScrollPane1;

	private de.wannawork.jcalendar.JCalendarComboBox newdatefield;

	private javax.swing.JCheckBox notecb;

	private JButton popupTimesBtn;

	private JLabel popupTimesLabel;

	private JPanel jPanel5;

	private javax.swing.JCheckBox privatecb;

	private javax.swing.JSpinner s_times;

	private javax.swing.JButton savebutton;

	private javax.swing.JButton savedefaultsbutton;

	private javax.swing.JCheckBox startap;

	private javax.swing.JComboBox starthour;

	private javax.swing.JComboBox startmin;

	private javax.swing.JCheckBox todocb;

	private javax.swing.JCheckBox vacationcb;

	private JPanel jPanel = null;

	private JSpinner ndays = null;

	private JCheckBox alarmcb = null;

	private char[] custRemTimes;

	private JCheckBox rptnumbox = null;

	private JPanel dlistbuttonpanel = null;

	private JToggleButton dl1 = null;

	private JToggleButton dl2 = null;

	private JToggleButton dl3 = null;

	private JToggleButton dl4 = null;

	private JToggleButton dl5 = null;

	private JToggleButton dl6 = null;

	private JToggleButton dl7 = null;

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
			jPanel.setBorder(new javax.swing.border.TitledBorder(null,
					java.util.ResourceBundle
							.getBundle("resource/borg_resource").getString(
									"Recurrence"),
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
			gridBagConstraints13.gridx = 4;
			gridBagConstraints13.gridy = 0;
			gridBagConstraints28.gridx = 2;
			gridBagConstraints28.gridy = 0;
			gridBagConstraints28.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints28.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints19.gridx = 3;
			gridBagConstraints19.gridy = 1;
			gridBagConstraints19.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints19.fill = java.awt.GridBagConstraints.BOTH;
			jPanel.add(jLabel4, gridBagConstraints31);
			jPanel.add(freq, gridBagConstraints41);
			jPanel.add(jLabel1, gridBagConstraints51);
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
			alarmcb.setText(java.util.ResourceBundle.getBundle(
					"resource/borg_resource").getString("Alarm"));
		}
		return alarmcb;
	}

	private void setCustRemTimes(Appointment r) {
		if (r == null) {
			custRemTimes = new char[PrefName.REMMINUTES.length];
			for (int i = 0; i < PrefName.REMMINUTES.length; ++i) {
				custRemTimes[i] = 'N';
			}
		} else {

			try {
				custRemTimes = (r.getReminderTimes()).toCharArray();

			} catch (Exception e) {
				for (int i = 0; i < PrefName.REMMINUTES.length; ++i) {
					custRemTimes[i] = 'N';
				}
			}
		}
	}

	private void popupbuttonActionPerformed(java.awt.event.ActionEvent evt) {

		PopupOptionsView pv = new PopupOptionsView(custRemTimes, this);
		pv.setVisible(true);
	}

	// display a summary of the times selected for popup reminders
	public void setPopupTimesString() {
		StringBuffer time1 = new StringBuffer(
				PrefName.REMMINUTES.length * 5 + 15);
		StringBuffer time2 = new StringBuffer(
				PrefName.REMMINUTES.length * 5 + 15);
		if (custRemTimes != null) {
			int i = 0;
			while (PrefName.REMMINUTES[i] < 0 && i < PrefName.REMMINUTES.length) {
				if (custRemTimes[i] == 'Y') {
					int abs = -PrefName.REMMINUTES[i];
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
						java.util.ResourceBundle.getBundle(
								"resource/borg_resource").getString(
								"min_aft_app"));
			}

			while (i < PrefName.REMMINUTES.length) {
				if (custRemTimes[i] == 'Y') {
					if (time2.length() > 0) {
						time2 = time2.append(", ").append(
								PrefName.REMMINUTES[i]);
					} else {
						time2 = time2.append(PrefName.REMMINUTES[i]);
					}
				}
				++i;
			}
			if (time2.length() > 0) {
				time2 = time2.append("   ").append(
						java.util.ResourceBundle.getBundle(
								"resource/borg_resource").getString(
								"min_bef_app"));
			}

		}
		popupTimesLabel.setText("<html><p align=RIGHT>" + time1.toString()
				+ "<br>" + time2.toString());
	}

	public String getText() {
		String labelstring = appttextarea.getText();
		if (labelstring.equals("")) {
			return java.util.ResourceBundle.getBundle("resource/borg_resource")
					.getString("*****_NEW_APPT_*****");
		}

		return labelstring;

	}

	/**
	 * This method initializes rptnumbox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getRptnumbox() {
		if (rptnumbox == null) {
			rptnumbox = new JCheckBox();
			rptnumbox.setText(java.util.ResourceBundle.getBundle(
					"resource/borg_resource").getString("show_rpt_num"));
		}
		return rptnumbox;
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

	// for setting labels
	static private GregorianCalendar tmpcal = new GregorianCalendar();

	static private SimpleDateFormat shortDayFmt = new SimpleDateFormat("EEE");

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
} // @jve:decl-index=0:visual-constraint="10,10"

