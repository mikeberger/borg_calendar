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
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.Version;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.db.DBException;



class AppointmentPanel extends JPanel
{
    static
    {
        Version.addVersion("$Id$");
    }
   
    private int key_;                           
    private int dkey_;
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
          dkey_ = AppointmentModel.dkey( year, month, day );
          
          
          // init GUI
          initComponents();
          
          // set up the spinner for repeat times
          SpinnerNumberModel mod = (SpinnerNumberModel) s_times.getModel();
          mod.setMinimum( new Integer(1) );
          
          
          // set up hours pulldown
          String mt = Prefs.getPref("miltime", "false" );
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
                  catbox.addItem( (String) it.next());
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
          String mt = Prefs.getPref("miltime", "false");
          
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
          
          
          // a key of -1 means to show a new blank appointment
          if( key_ == -1 )
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
                  Appointment r = AppointmentModel.getReference().getAppt(key_);
                  
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
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

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

        setLayout(new java.awt.GridBagLayout());

        jLabel2.setForeground(java.awt.Color.red);
        jLabel2.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jLabel2, gridBagConstraints);

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.5;
        gridBagConstraints.weighty = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel2.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("appttime")));
        starthour.setMaximumRowCount(24);
        starthour.setMinimumSize(new java.awt.Dimension(42, 36));
        starthour.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(starthour, gridBagConstraints);

        startmin.setMaximumRowCount(12);
        startmin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        startmin.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        jPanel2.add(startmin, gridBagConstraints);

        startap.setText("PM");
        startap.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(startap, gridBagConstraints);

        durhour.setMaximumRowCount(24);
        durhour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(durhour, gridBagConstraints);

        durmin.setMaximumRowCount(12);
        durmin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" }));
        durmin.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        jPanel2.add(durmin, gridBagConstraints);

        jLabel3.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Start_Time:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 7);
        jPanel2.add(jLabel3, gridBagConstraints);

        jLabel6.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Duration:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        jPanel2.add(jLabel6, gridBagConstraints);

        notecb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("No_Specific_Time"));
        notecb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                notecbActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(notecb, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(13, 0, 13, 0);
        jPanel2.add(jSeparator1, gridBagConstraints);

        jLabel8.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("newDate:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jLabel8, gridBagConstraints);

        newdatefield.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(newdatefield, gridBagConstraints);

        chgdate.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("changedate"));
        chgdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chgdateActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        jPanel2.add(chgdate, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jPanel2, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanel3.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Properties")));
        jPanel3.setMinimumSize(new java.awt.Dimension(539, 128));
        jPanel3.setPreferredSize(new java.awt.Dimension(600, 120));
        todocb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("To_Do"));
        todocb.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(todocb, gridBagConstraints);

        vacationcb.setForeground(new java.awt.Color(0, 102, 0));
        vacationcb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Vacation"));
        vacationcb.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(vacationcb, gridBagConstraints);

        halfdaycb.setForeground(new java.awt.Color(0, 102, 102));
        halfdaycb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Half_Day"));
        halfdaycb.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(halfdaycb, gridBagConstraints);

        holidaycb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Holiday"));
        holidaycb.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(holidaycb, gridBagConstraints);

        privatecb.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Private"));
        privatecb.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(privatecb, gridBagConstraints);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Color"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(jLabel5, gridBagConstraints);

        colorbox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(colorbox, gridBagConstraints);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Frequency"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(jLabel4, gridBagConstraints);

        freq.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(freq, gridBagConstraints);

        s_times.setBorder(new javax.swing.border.EtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(s_times, gridBagConstraints);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Times"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(catbox, gridBagConstraints);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Category"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(jLabel7, gridBagConstraints);

        foreverbox.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("forever"));
        foreverbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                foreverboxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        jPanel3.add(foreverbox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.weighty = 1.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jPanel3, gridBagConstraints);

        savebutton.setText(java.util.ResourceBundle.getBundle("resource/borg_resource").getString("Save"));
        savebutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savebuttonActionPerformed(evt);
            }
        });

        jPanel4.add(savebutton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jPanel4, gridBagConstraints);

    }//GEN-END:initComponents

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
            String mt = Prefs.getPref("miltime", "false");
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
    private int setAppt(Appointment r)
    {
        
        // get the hour and minute
        int hr = starthour.getSelectedIndex();
        String mt = Prefs.getPref("miltime", "false" );
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
                Errmsg.notice(Resource.getResourceString("invdate"));
                return(-1);
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
            
            //System.out.println(newkey + g.toString() + nd.toGMTString());
        }
        
        
        try
        {
            
            // set the appt date/time
            r.setDate( g.getTime() );
            
            int du = (durhour.getSelectedIndex() * 60) + (durmin.getSelectedIndex() * 5 );
            if( du != 0 )
                r.setDuration( new Integer(du));
            
            // appointment text of some sort is required
            if( appttextarea.getText().equals("") )
            {
                Errmsg.notice(Resource.getResourceString("Please_enter_some_appointment_text"));
                return(-1);
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
            
            if( tm.intValue() != 1 )
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
                    Errmsg.errmsg(new Exception(Resource.getResourceString("Could_not_parse_times:_") + tm ));
                    return(-1);
                }
                
            }
            
            // check if times and frequency conflict - i.e. repeat once, 10 times
            if( (freq.getSelectedIndex() != 0 && tm.intValue() == 1)
            || (freq.getSelectedIndex() == 0 && tm.intValue() != 1))
            {
                Errmsg.notice(Resource.getResourceString("Repeat_Frequency/Times_Value_not_compatible"));
                return(-1);
            }
            
            String cat = (String) catbox.getSelectedItem();
            //System.out.println(cat);
            if( cat.equals("") || cat.equals(Resource.getResourceString("uncategorized")))
            {
                r.setCategory(null);
            }
            else
            {
                r.setCategory(cat);
            }
            
        }catch( Exception e )
        {
            Errmsg.errmsg(e);
            return(-1);
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
    { "once", "daily", "weekly", "biweekly", "monthly", "monthly_day", "yearly", "weekdays", "weekends" };
    private String freqToEnglish( String freq )
    {
        for( int i = 0; i < freqs.length; i++ )
        {
            if( freq.equals(Resource.getResourceString(freqs[i])))
            {
                return( freqs[i] );
            }
        }
        
        return( "black" );
    }
    
    private void add_appt()
    {
               // user has requested an add of a new appt
        
        // get a new appt from the model and set it from the user data
        AppointmentModel calmod_ = AppointmentModel.getReference();
        Appointment r = calmod_.newAppt();
        int ret = setAppt(r);
        if( ret < 0 )
            return;
        
        int key = dkey_;
        if( ret > 0 )
            key = ret;
        
        // get the next unused key for a given day
        // to do this, start with the "base" key for a given day.
        // then see if an appt has this key.
        // keep adding 1 until a key is found that has no appt
        try
        {
            while( true )
            {
                Appointment ap = calmod_.getAppt(key);
                if( ap == null ) break;
                key++;
            }
        }
        catch( DBException e )
        {
            if( e.getRetCode() != DBException.RET_NOT_FOUND )
            {
                Errmsg.errmsg(e);
                return;
            }
        }
        catch( Exception ee )
        {
            Errmsg.errmsg(ee);
            return;
        }
        
        // tell the model to add the appt
        try
        {
            r.setKey(key);
            calmod_.saveAppt(r, true);
        }
        catch( DBException e )
        {
            Errmsg.errmsg(e);
            return;         
        }
    }
    
    private void chg_appt()
    {
              // user had selected appt change
        
        // get a new empty appt from the model and set it using the data the user has entered
        AppointmentModel calmod_ = AppointmentModel.getReference();
        Appointment r = calmod_.newAppt();
        int newkey = setAppt(r);
        if( newkey < 0 )
            return;
        
        try
        {
            // call the model to change the appt
            if( newkey == 0)
            {
                r.setKey(key_);
                calmod_.saveAppt(r, false);
            }
            else
            {
                calmod_.delAppt(key_);
                try
                {
                    while( true )
                    {
                        Appointment ap = calmod_.getAppt(newkey);
                        if( ap == null ) break;
                        newkey++;
                    }
                }
                catch( DBException e2 )
                {
                    if( e2.getRetCode() != DBException.RET_NOT_FOUND )
                    {
                        Errmsg.errmsg(e2);
                        return;
                    }
                }
                catch( Exception ee )
                {
                    Errmsg.errmsg(ee);
                    return;
                }
                
                r.setKey(newkey);
                calmod_.saveAppt(r, true);
                
            }
           
        }
        catch( DBException e )
        {
            
            Errmsg.errmsg(e);
            return;
            
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
    private javax.swing.JCheckBox startap;
    private javax.swing.JComboBox starthour;
    private javax.swing.JComboBox startmin;
    private javax.swing.JCheckBox todocb;
    private javax.swing.JCheckBox vacationcb;
    // End of variables declaration//GEN-END:variables
    
}
