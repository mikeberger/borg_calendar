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
Copyright 2003 by ==Quiet==
 */
package net.sf.borg.ui;

import java.awt.GridBagConstraints;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.Version;
import net.sf.borg.common.util.Warning;
import net.sf.borg.common.util.XTree;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.AppointmentXMLAdapter;



class AppointmentPanel extends JPanel
{
    static
    {
        Version.addVersion("$Id$");
    }
   
    private int key_;                           
    private int year_;
    private int month_;
    private int day_;
    
    static private DefaultComboBoxModel normHourModel = new DefaultComboBoxModel(new String[]
    { "1","2","3","4","5","6","7","8","9","10","11","12" });
    static private DefaultComboBoxModel milHourModel = new DefaultComboBoxModel(new String[]
    { "0","1","2","3","4","5","6","7","8","9","10","11","12",
      "13","14","15","16","17","18","19","20","21","22","23" });
      
      public AppointmentPanel(int year, int month, int day)
      {
          AppointmentModel calmod_ = AppointmentModel.getReference();
          
          year_ = year;
          month_ = month;
          day_ = day;

          
          // init GUI
          initComponents();
          
          // set up the spinner for repeat times
          SpinnerNumberModel mod = (SpinnerNumberModel) s_times.getModel();
          mod.setMinimum( new Integer(1) );
          
          
          // set up hours pulldown
          String mt = Prefs.getPref(PrefName.MILTIME);
          if( mt.equals("true"))
          {
              starthour.setModel(milHourModel);
              startap.setVisible(false);
          }
          else
          {
              starthour.setModel(normHourModel);
              startap.setVisible(true);
          }
          
          try
          {
              Collection cats = calmod_.getCategories();
              Iterator it = cats.iterator();
              while( it.hasNext())
              {
                  catbox.addItem( it.next());
              }
              catbox.setSelectedIndex(0);
          }
          catch( Exception e )
          {
              Errmsg.errmsg(e);
          }
          
          for( int i = 0; i < colors.length; i++ )
          {
            colorbox.addItem( Resource.getResourceString(colors[i]));
          }
          
          for( int i = 0; i < freqs.length; i++ )
          {
            freq.addItem( Resource.getResourceString(freqs[i]));
          }
          
         
      }
      
    
     
      
      // set the view to a single appt (or a new blank)
      public void showapp(int key ) 
      {
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
          newdatefield.setText("");
          newdatefield.setEditable(false);
          
          // get default appt values, if any
          Appointment defaultAppt = null;
          String defApptXml = Prefs.getPref(PrefName.DEFAULT_APPT);
          if( !defApptXml.equals(""))
          {
              try{
                  XTree xt = XTree.readFromBuffer(defApptXml);
                  AppointmentXMLAdapter axa = new AppointmentXMLAdapter();
                  defaultAppt = (Appointment) axa.fromXml( xt );
              }
              catch( Exception e)
              {
                  Errmsg.errmsg(e);
                  defaultAppt = null;
              }
          }
          
          
          // a key of -1 means to show a new blank appointment
          if( key_ == -1 && defaultAppt == null )
          {
              
              if( mt.equals("true"))
              {
                  starthour.setSelectedIndex(0);
              }
              else
              {
                  starthour.setSelectedIndex(11);    // hour = 12
              }
              
              catbox.setSelectedIndex(0);
              startmin.setSelectedIndex(0);
              durmin.setSelectedIndex(0);
              durhour.setSelectedIndex(0);
              
              todocb.setSelected(false);      // todo unchecked
              colorbox.setSelectedIndex(0);       // color = black
              vacationcb.setSelected(false);      // vacation unchecked
              halfdaycb.setSelected(false);      // half-day unchecked
              holidaycb.setSelected(false);      // holiday unchecked
              privatecb.setSelected(false);      // private unchecked
              appttextarea.setText("");             // clear appt text
              freq.setSelectedIndex(0);           // freq = once
              s_times.setEnabled(true);
              s_times.setValue(new Integer(1));   // times = 1
              foreverbox.setSelected(false);
              jLabel2.setText(Resource.getResourceString("*****_NEW_APPT_*****"));    // show New app indicator
              
              // only add menu choice active for a new appt
         
              
              chgdate.setEnabled(false);
              
              
          }else
          {
              
              try
              {
                  
               
                  // get the appt Appointment from the calmodel
                  Appointment r = null;
                  if( key_ == -1 )
                  {
                      r = defaultAppt;
                  }
                  else
                  {
                      r = AppointmentModel.getReference().getAppt(key_);
                  }
                  
                  // set hour and minute
                  Date d = r.getDate();
                  GregorianCalendar g = new GregorianCalendar();
                  g.setTime(d);
                  if( mt.equals("true"))
                  {
                      int hour = g.get(Calendar.HOUR_OF_DAY);
                      if( hour != 0 ) note = false;
                      starthour.setSelectedIndex(hour);
                  }
                  else
                  {
                      int hour = g.get(Calendar.HOUR);
                      if( hour != 0 ) note = false;
                      if( hour == 0 ) hour = 12;
                      starthour.setSelectedIndex(hour-1);
                  }
                  
                  int min = g.get(Calendar.MINUTE);
                  if( min != 0 ) note = false;
                  startmin.setSelectedIndex(min/5);
                  
                  // duration
                  Integer duration = r.getDuration();
                  int dur = 0;
                  if( duration != null )
                      dur = duration.intValue();
                  durhour.setSelectedIndex( dur/60 );
                  durmin.setSelectedIndex( (dur % 60) / 5 );
                  if( dur != 0 )
                      note = false;
                  
                  // check if we just have a "note" (non-timed appt)
                  if( !note )
                  {
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
                  if( ii != null && ii.intValue() == 1 )
                      vacationcb.setSelected(true);
                  
                  // set half-day checkbox
                  halfdaycb.setSelected(false);
                  if( ii != null && ii.intValue() == 2 )
                      halfdaycb.setSelected(true);
                  
                  // holiday checkbox
                  holidaycb.setSelected(false);
                  ii = r.getHoliday();
                  if( ii != null && ii.intValue() == 1 )
                      holidaycb.setSelected(true);
                  
                  // private checkbox
                  privatecb.setSelected(r.getPrivate());
                  
                  // PM checkbox
                  boolean pm = true;
                  if( g.get(Calendar.AM_PM) == Calendar.AM )
                      pm = false;
                  startap.setSelected(pm);
                  
                  // set appt text
                  String t = r.getText();
                  appttextarea.setText(t);
                  
                  // color
                  String cl = r.getColor();
                  if( cl != null )
                  {
                      try
                      {
                          colorbox.setSelectedItem(Resource.getResourceString(cl));
                      }
                      catch( Exception e)
                      {
                          colorbox.setSelectedIndex(0);
                      }
                  }
                  else
                  {
                      colorbox.setSelectedIndex(0);
                  }
                  
                  chgdate.setEnabled(true);
                  
                  // repeat frequency
                  String rpt = r.getFrequency();
                  int fin = 0;
                  if( rpt != null )
                  {
                      if( rpt.equals("weekly"))
                          fin = 2;
                      else if( rpt.equals("biweekly"))
                          fin = 3;
                      else if( rpt.equals("monthly"))
                          fin = 4;
                      else if( rpt.equals("monthly_day"))
                          fin = 5;
                      else if( rpt.equals("yearly"))
                          fin = 6;
                      else if( rpt.equals("daily"))
                          fin = 1;
                      else if( rpt.equals("weekdays"))
                          fin = 7;
                      else if( rpt.equals("weekends"))
                          fin = 8;
                      else if( rpt.equals("mwf"))
                        fin = 9;
                      else if( rpt.equals("tth"))
                        fin = 10;
                  }
                  freq.setSelectedIndex(fin);
                  
                  // repeat times
                  Integer tm = r.getTimes();
                  if( tm != null )
                  {
                      if( tm.intValue() == 9999 )
                      {
                          foreverbox.setSelected(true);
                          s_times.setValue(new Integer(0));
                          s_times.setEnabled(false);                          
                      }
                      else
                      {
                          s_times.setEnabled(true);
                          s_times.setValue(tm);
                          foreverbox.setSelected(false);
                      }
                      
                  }
                  else
                  {
                      s_times.setEnabled(true);
                      s_times.setValue(new Integer(1));
                      foreverbox.setSelected(false);
                  }
                  
                  String cat = r.getCategory();
                  if( cat != null && !cat.equals(""))
                  {
                      catbox.setSelectedItem(cat);
                  }
                  else
                  {
                      catbox.setSelectedIndex(0);
                  }
                  // erase New Appt indicator
                  jLabel2.setText("    ");
              }
              catch( Exception e )
              {
                  Errmsg.errmsg(e);
                  Exception ne = new Exception(Resource.getResourceString("appt_error") );
                  Errmsg.errmsg(ne);
                  
              }
          }
          
      }
      
      
      /** This method is called from within the constructor to
       * initialize the form.
       * WARNING: Do NOT modify this code. The content of this method is
       * always regenerated by the Form Editor.
       */
    private void initComponents()//GEN-BEGIN:initComponents
    {

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
        jSeparator1 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        newdatefield = new javax.swing.JTextField();
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
        savebutton = new javax.swing.JButton();
        savedefaultsbutton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jLabel2.setForeground(java.awt.Color.red);
        jLabel2.setText("jLabel2");
        GridBagConstraints gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        add(jLabel2, gridBagConstraints2);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("appttext")));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(320, 140));
        appttextarea.setColumns(40);
        appttextarea.setLineWrap(true);
        appttextarea.setRows(8);
        appttextarea.setWrapStyleWord(true);
        appttextarea.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
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

        jPanel2.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("appttime")));
        starthour.setMaximumRowCount(24);
        starthour.setMinimumSize(new java.awt.Dimension(42, 36));
        starthour.setOpaque(false);
        GridBagConstraints gridBagConstraints4 = new java.awt.GridBagConstraints();
        gridBagConstraints4.gridx = 1;
        gridBagConstraints4.gridy = 0;
        gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(starthour, gridBagConstraints4);

        startmin.setMaximumRowCount(12);
        startmin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        startmin.setOpaque(false);
        GridBagConstraints gridBagConstraints5 = new java.awt.GridBagConstraints();
        gridBagConstraints5.gridx = 2;
        gridBagConstraints5.gridy = 0;
        gridBagConstraints5.fill = java.awt.GridBagConstraints.VERTICAL;
        jPanel2.add(startmin, gridBagConstraints5);

        startap.setText("PM");
        startap.setOpaque(false);
        GridBagConstraints gridBagConstraints6 = new java.awt.GridBagConstraints();
        gridBagConstraints6.gridx = 3;
        gridBagConstraints6.gridy = 0;
        gridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(startap, gridBagConstraints6);

        durhour.setMaximumRowCount(24);
        durhour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        GridBagConstraints gridBagConstraints7 = new java.awt.GridBagConstraints();
        gridBagConstraints7.gridx = 1;
        gridBagConstraints7.gridy = 1;
        gridBagConstraints7.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(durhour, gridBagConstraints7);

        durmin.setMaximumRowCount(12);
        durmin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        durmin.setOpaque(false);
        GridBagConstraints gridBagConstraints8 = new java.awt.GridBagConstraints();
        gridBagConstraints8.gridx = 2;
        gridBagConstraints8.gridy = 1;
        gridBagConstraints8.fill = java.awt.GridBagConstraints.VERTICAL;
        jPanel2.add(durmin, gridBagConstraints8);

        jLabel3.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Start_Time:"));
        GridBagConstraints gridBagConstraints9 = new java.awt.GridBagConstraints();
        gridBagConstraints9.gridx = 0;
        gridBagConstraints9.gridy = 0;
        gridBagConstraints9.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints9.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints9.insets = new java.awt.Insets(0, 7, 0, 7);
        jPanel2.add(jLabel3, gridBagConstraints9);

        jLabel6.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Duration:"));
        GridBagConstraints gridBagConstraints11 = new java.awt.GridBagConstraints();
        gridBagConstraints11.gridx = 0;
        gridBagConstraints11.gridy = 1;
        gridBagConstraints11.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints11.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints11.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel2.add(jLabel6, gridBagConstraints11);

        notecb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("No_Specific_Time"));
        notecb.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                notecbActionPerformed(evt);
            }
        });

        GridBagConstraints gridBagConstraints12 = new java.awt.GridBagConstraints();
        gridBagConstraints12.gridx = 0;
        gridBagConstraints12.gridy = 3;
        gridBagConstraints12.gridwidth = 3;
        gridBagConstraints12.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(notecb, gridBagConstraints12);

        GridBagConstraints gridBagConstraints13 = new java.awt.GridBagConstraints();
        gridBagConstraints13.gridx = 0;
        gridBagConstraints13.gridy = 2;
        gridBagConstraints13.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints13.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints13.insets = new java.awt.Insets(13, 0, 13, 0);
        jPanel2.add(jSeparator1, gridBagConstraints13);

        jLabel8.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("newDate:"));
        GridBagConstraints gridBagConstraints14 = new java.awt.GridBagConstraints();
        gridBagConstraints14.gridx = 2;
        gridBagConstraints14.gridy = 4;
        gridBagConstraints14.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jLabel8, gridBagConstraints14);

        newdatefield.setColumns(10);
        GridBagConstraints gridBagConstraints15 = new java.awt.GridBagConstraints();
        gridBagConstraints15.gridx = 3;
        gridBagConstraints15.gridy = 4;
        gridBagConstraints15.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(newdatefield, gridBagConstraints15);

        chgdate.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("changedate"));
        chgdate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                chgdateActionPerformed(evt);
            }
        });

        GridBagConstraints gridBagConstraints16 = new java.awt.GridBagConstraints();
        gridBagConstraints16.gridx = 0;
        gridBagConstraints16.gridy = 4;
        jPanel2.add(chgdate, gridBagConstraints16);

        GridBagConstraints gridBagConstraints17 = new java.awt.GridBagConstraints();
        gridBagConstraints17.gridx = 0;
        gridBagConstraints17.gridy = 2;
        gridBagConstraints17.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints17.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints17.weightx = 1.0;
        gridBagConstraints17.weighty = 1.0;
        gridBagConstraints17.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jPanel2, gridBagConstraints17);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanel3.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Properties")));
        jPanel3.setMinimumSize(new java.awt.Dimension(539, 128));
        jPanel3.setPreferredSize(new java.awt.Dimension(600, 120));
        todocb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("To_Do"));
        todocb.setOpaque(false);
        GridBagConstraints gridBagConstraints21 = new java.awt.GridBagConstraints();
        gridBagConstraints21.weightx = 1.0;
        gridBagConstraints21.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(todocb, gridBagConstraints21);

        vacationcb.setForeground(new java.awt.Color(0, 102, 0));
        vacationcb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Vacation"));
        vacationcb.setOpaque(false);
        GridBagConstraints gridBagConstraints22 = new java.awt.GridBagConstraints();
        gridBagConstraints22.weightx = 1.0;
        gridBagConstraints22.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(vacationcb, gridBagConstraints22);

        halfdaycb.setForeground(new java.awt.Color(0, 102, 102));
        halfdaycb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Half_Day"));
        halfdaycb.setOpaque(false);
        GridBagConstraints gridBagConstraints23 = new java.awt.GridBagConstraints();
        gridBagConstraints23.weightx = 1.0;
        gridBagConstraints23.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(halfdaycb, gridBagConstraints23);

        holidaycb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Holiday"));
        holidaycb.setOpaque(false);
        GridBagConstraints gridBagConstraints24 = new java.awt.GridBagConstraints();
        gridBagConstraints24.weightx = 1.0;
        gridBagConstraints24.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(holidaycb, gridBagConstraints24);

        privatecb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Private"));
        privatecb.setOpaque(false);
        GridBagConstraints gridBagConstraints25 = new java.awt.GridBagConstraints();
        gridBagConstraints25.weightx = 1.0;
        gridBagConstraints25.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(privatecb, gridBagConstraints25);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Color"));
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
        jLabel4.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Frequency"));
        GridBagConstraints gridBagConstraints31 = new java.awt.GridBagConstraints();
        gridBagConstraints31.gridx = 0;
        gridBagConstraints31.gridy = 2;
        gridBagConstraints31.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints31.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints31.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(jLabel4, gridBagConstraints31);

        freq.setOpaque(false);
        GridBagConstraints gridBagConstraints32 = new java.awt.GridBagConstraints();
        gridBagConstraints32.gridx = 1;
        gridBagConstraints32.gridy = 2;
        gridBagConstraints32.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints32.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints32.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(freq, gridBagConstraints32);

        s_times.setBorder(new javax.swing.border.EtchedBorder());
        GridBagConstraints gridBagConstraints33 = new java.awt.GridBagConstraints();
        gridBagConstraints33.gridx = 3;
        gridBagConstraints33.gridy = 2;
        gridBagConstraints33.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints33.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(s_times, gridBagConstraints33);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Times"));
        GridBagConstraints gridBagConstraints34 = new java.awt.GridBagConstraints();
        gridBagConstraints34.gridx = 2;
        gridBagConstraints34.gridy = 2;
        gridBagConstraints34.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints34.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints34.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(jLabel1, gridBagConstraints34);

        GridBagConstraints gridBagConstraints35 = new java.awt.GridBagConstraints();
        gridBagConstraints35.gridx = 3;
        gridBagConstraints35.gridy = 1;
        gridBagConstraints35.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints35.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(catbox, gridBagConstraints35);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Category"));
        GridBagConstraints gridBagConstraints36 = new java.awt.GridBagConstraints();
        gridBagConstraints36.gridx = 2;
        gridBagConstraints36.gridy = 1;
        gridBagConstraints36.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints36.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints36.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(jLabel7, gridBagConstraints36);

        foreverbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("forever"));
        foreverbox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                foreverboxActionPerformed(evt);
            }
        });

        GridBagConstraints gridBagConstraints37 = new java.awt.GridBagConstraints();
        gridBagConstraints37.gridx = 4;
        gridBagConstraints37.gridy = 2;
        jPanel3.add(foreverbox, gridBagConstraints37);

        GridBagConstraints gridBagConstraints38 = new java.awt.GridBagConstraints();
        gridBagConstraints38.gridx = 0;
        gridBagConstraints38.gridy = 3;
        gridBagConstraints38.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints38.weightx = 2.0;
        gridBagConstraints38.weighty = 1.5;
        gridBagConstraints38.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jPanel3, gridBagConstraints38);

        savebutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Save16.gif")));
        savebutton.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Save"));
        savebutton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                savebuttonActionPerformed(evt);
            }
        });

        jPanel4.add(savebutton);

        savedefaultsbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/SaveAs16.gif")));
        savedefaultsbutton.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("save_Def"));
        savedefaultsbutton.setToolTipText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("sd_tip"));
        savedefaultsbutton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveDefaults(evt);
            }
        });

        jPanel4.add(savedefaultsbutton);

        GridBagConstraints gridBagConstraints44 = new java.awt.GridBagConstraints();
        gridBagConstraints44.gridx = 0;
        gridBagConstraints44.gridy = 4;
        gridBagConstraints44.fill = java.awt.GridBagConstraints.BOTH;
        add(jPanel4, gridBagConstraints44);

    }//GEN-END:initComponents

    private void saveDefaults(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveDefaults
    {//GEN-HEADEREND:event_saveDefaults
        
        Appointment r = new Appointment();
        try{
            setAppt(r, false);
        }
        catch( Exception e)
        {
            Errmsg.errmsg(e);
            return;
        }
 
        AppointmentXMLAdapter axa = new AppointmentXMLAdapter();
        XTree xt = axa.toXml(r);
        String s = xt.toString();
        Prefs.putPref( PrefName.DEFAULT_APPT, s );
        
    }//GEN-LAST:event_saveDefaults

    private void savebuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savebuttonActionPerformed
        if( key_ == -1 )
        {
            add_appt();
        }
        else
        {
            chg_appt();
        }
        
        showapp(-1);
        
    }//GEN-LAST:event_savebuttonActionPerformed

    private void foreverboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_foreverboxActionPerformed
    {//GEN-HEADEREND:event_foreverboxActionPerformed
        if( foreverbox.isSelected())
        {
            s_times.setValue( new Integer(0));
            s_times.setEnabled(false);
            
        }
        else
        {
            s_times.setValue( new Integer(1));
            s_times.setEnabled(true);
        }
    }//GEN-LAST:event_foreverboxActionPerformed
    
    private void chgdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chgdateActionPerformed
        if( !chgdate.isSelected() )
            newdatefield.setText("");
        newdatefield.setEditable( chgdate.isSelected() );
        
    }//GEN-LAST:event_chgdateActionPerformed
    
    private void notecbActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_notecbActionPerformed
    {//GEN-HEADEREND:event_notecbActionPerformed
        if( notecb.isSelected() )
        {
            String mt = Prefs.getPref(PrefName.MILTIME);
            if( mt.equals("true"))
            {
                starthour.setSelectedIndex(0);
            }
            else
            {
                starthour.setSelectedIndex(11);    // hour = 12
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
        }
        else
        {
            startmin.setEnabled(true);
            starthour.setEnabled(true);
            durmin.setEnabled(true);
            durhour.setEnabled(true);
            startap.setEnabled(true);
        }
    }//GEN-LAST:event_notecbActionPerformed
                
    // fill in an appt from the user data
    // returns changed key if any
    private int setAppt(Appointment r, boolean validate) throws Warning, Exception
    {
        
        // get the hour and minute
        int hr = starthour.getSelectedIndex();
        String mt = Prefs.getPref(PrefName.MILTIME);
        if( mt.equals("false"))
        {
            hr = hr + 1;
            if( hr == 12 ) hr = 0;
            if( startap.isSelected() )
                hr += 12;
        }
        
        Date nd = null;
        int newkey = 0;
        if( chgdate.isSelected())
        {
            String newdatetext = newdatefield.getText();
            nd = new Date();
            
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
            try
            {
                nd = df.parse(newdatetext);
            }
            catch( Exception e )
            {
            	if( validate )
            		throw new Warning(Resource.getResourceString("invdate"));
            }
            
        }
        
        int min = startmin.getSelectedIndex() * 5;
        GregorianCalendar g = new GregorianCalendar();
        
        if( nd == null )
        {
            g.set( year_, month_, day_, hr, min );
        }
        else
        {
            g.setTime(nd);
            g.set( Calendar.HOUR_OF_DAY, hr );
            g.set( Calendar.MINUTE, min );
            newkey = AppointmentModel.dkey( g.get(Calendar.YEAR), g.get(Calendar.MONTH), g.get(Calendar.DATE));
        }
          
        // set the appt date/time
        r.setDate( g.getTime() );
        
        int du = (durhour.getSelectedIndex() * 60) + (durmin.getSelectedIndex() * 5 );
        if( du != 0 )
            r.setDuration( new Integer(du));
        
        // appointment text of some sort is required
        if( appttextarea.getText().equals("") && validate )
        {
            throw new Warning(Resource.getResourceString("Please_enter_some_appointment_text"));
        }
        
        // set text
        r.setText( new String(appttextarea.getText()));
        
        // to do
        r.setTodo( todocb.isSelected() );
        
        // vacation, half-day, and private checkboxes
        if( vacationcb.isSelected() )
            r.setVacation( new Integer(1) );
        if( halfdaycb.isSelected() )
            r.setVacation( new Integer(2) );
        if( holidaycb.isSelected() )
            r.setHoliday( new Integer(1) );
        
        r.setPrivate( privatecb.isSelected() );
        
        // color
        r.setColor( colorToEnglish((String)colorbox.getSelectedItem()));
        
        // repeat frequency
        if( freq.getSelectedIndex() != 0 )
            r.setFrequency( freqToEnglish((String)freq.getSelectedItem()) );
        
        // repeat times
        Integer tm = null;
        if( foreverbox.isSelected())
        {
            tm = new Integer(9999);
        }
        else
        {
            tm = (Integer) s_times.getValue();
        }
        
        if( tm.intValue() > 1 )
        {
            try
            {
                r.setTimes( tm );
                
                // set a boolean flag in SMDB if the appt repeats
                // this is very important for performance. When the model
                // first indexes the appts, it will only have to read and parse
                // the textual (non-boolean) data for repeating appointments
                // to calculate when the repeats fall. For non-repeating appts
                // the boolean will not be set and the model knows the appt can be indexed
                // on a single day - without needing to read the DB text.
                if( tm.intValue() > 1 )
                    r.setRepeatFlag(true);
                else
                    r.setRepeatFlag(false);
            }
            catch( Exception e )
            {
            		throw new Exception(Resource.getResourceString("Could_not_parse_times:_") + tm );
            }
            
        }
        else
        {
            r.setTimes( new Integer(1));
        }
        
        // check if times and frequency conflict - i.e. repeat once, 10 times
        if( (freq.getSelectedIndex() != 0 && tm.intValue() == 1)
                || (freq.getSelectedIndex() == 0 && tm.intValue() != 1))
        {
            if( validate )
            	throw new Warning(Resource.getResourceString("Repeat_Frequency/Times_Value_not_compatible"));
        }
        
        String cat = (String) catbox.getSelectedItem();
        if( cat.equals("") || cat.equals(Resource.getResourceString("uncategorized")))
        {
            r.setCategory(null);
        }
        else
        {
            r.setCategory(cat);
        }       
        
        return(newkey);
    }
                
    private String colors[] =
    { "black", "red", "blue", "green", "white" };
    private String colorToEnglish( String color )
    {
        for( int i = 0; i < colors.length; i++ )
        {
            if( color.equals(Resource.getResourceString(colors[i])))
            {
                return( colors[i] );
            }
        }
        
        return( "black" );
    }
    
    private String freqs[] =
    { "once", "daily", "weekly", "biweekly", "monthly", "monthly_day", "yearly", "weekdays", "weekends", "mwf", "tth" };
    private String freqToEnglish( String fr )
    {
        for( int i = 0; i < freqs.length; i++ )
        {
            if( fr.equals(Resource.getResourceString(freqs[i])))
            {
                return( freqs[i] );
            }
        }
        
        return( "once" );
    }
    
    private void add_appt()
    {
               // user has requested an add of a new appt
        
        // get a new appt from the model and set it from the user data
        AppointmentModel calmod_ = AppointmentModel.getReference();
        Appointment r = calmod_.newAppt();
        int ret = 0;
        try{
            ret = setAppt(r, true);
        }
        catch( Warning w )
        {
            Errmsg.notice(w.getMessage());
            return;
        } catch (Exception e) {
            Errmsg.errmsg(e);
            return;
        }

        if( ret < 0 )
            return;
        
        calmod_.saveAppt(r, true);
    }
    
    private void chg_appt()
    {
              // user had selected appt change
        
        // get a new empty appt from the model and set it using the data the user has entered
        AppointmentModel calmod_ = AppointmentModel.getReference();
        Appointment r = calmod_.newAppt();
        int newkey = 0;
        try{
            newkey = setAppt(r, true);
        }
        catch( Warning w )
        {
            Errmsg.notice(w.getMessage());
            return;
        }
        catch (Exception e) {
            Errmsg.errmsg(e);
            return;
        }
        
        if( newkey < 0 )
            return;
        
        // call the model to change the appt
        if( newkey == 0)
        {
            r.setKey(key_);
            calmod_.saveAppt(r, false);
        }
        else
        {
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
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField newdatefield;
    private javax.swing.JCheckBox notecb;
    private javax.swing.JCheckBox privatecb;
    private javax.swing.JSpinner s_times;
    private javax.swing.JButton savebutton;
    private javax.swing.JButton savedefaultsbutton;
    private javax.swing.JCheckBox startap;
    private javax.swing.JComboBox starthour;
    private javax.swing.JComboBox startmin;
    private javax.swing.JCheckBox todocb;
    private javax.swing.JCheckBox vacationcb;
    // End of variables declaration//GEN-END:variables
    
}
