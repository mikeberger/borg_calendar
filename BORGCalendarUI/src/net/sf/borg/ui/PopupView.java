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
/*
 * popups.java
 *
 * Created on January 16, 2004, 3:08 PM
 */

package net.sf.borg.ui;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;
import java.util.Map.Entry;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Version;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
/**
 *
 * @author  mberger
 */
public class PopupView extends View {
    static
    {
        Version.addVersion("$Id$");
    }
         
    /** Creates a new instance of popups */
    public PopupView() {
        addModel(AppointmentModel.getReference());
        timer = new java.util.Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                EventQueue.invokeLater(doPopupChk);
            }
        }, 5*1000, Prefs.getPref("reminder_check_mins",5) * 60 * 1000);
        
    }
    
    // map that maps appointment keys to the associated popup reminder windows
    private HashMap pops = new HashMap();
    
    private java.util.Timer timer = null;
    
    public void destroy() {
        
        timer.cancel();
        
        // get rid of any open popups
        Collection s = pops.values();
        Iterator i = s.iterator();
        while( i.hasNext() ) {
            
            JDialog pop = (JDialog) i.next();
            
            // if frame is gone (killed already), then skip it
            if( pop == null )
                continue;
            pop.dispose();
            
        }
    }
    
    // check if any new popup windows are needed and pop them up
    // also beep and bring imminent popups to the front
    private void popup_chk() {
        
        String enable = Prefs.getPref("reminders", "true");
        if( enable.equals("false"))
            return;
        
        // determine if we are popping up public/private appts
        boolean showpub = false;
        boolean showpriv = false;
        String sp = Prefs.getPref("showpublic", "true" );
        if( sp.equals("true") )
            showpub = true;
        sp = Prefs.getPref("showprivate", "false" );
        if( sp.equals("true") )
            showpriv = true;
        
        // get the current day/month/year
        GregorianCalendar cal = new GregorianCalendar();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DATE);
        
        // get the key for today in the data model
        int key = AppointmentModel.dkey( year, month, day );
        
        // get the list of the today's appts
        Collection l = AppointmentModel.getReference().getAppts( key );
        if( l != null ) {
            Iterator it = l.iterator();
            Appointment appt;
            
            // iterate through the day's appts
            while( it.hasNext() ) {
                
                Integer ik = (Integer) it.next();
                
                try {
                    // read the appt record from the data model
                    appt = AppointmentModel.getReference().getAppt(ik.intValue());
                    
                    // check if we should show it based on public/private flags
                    if( appt.getPrivate() ) {
                        if( !showpriv )
                            continue;
                    }
                    else {
                        if( !showpub )
                            continue;
                    }
                    
                    // don't popup "notes"
                    if( AppointmentModel.isNote(appt) )
                        continue;
                    
                    Date d = appt.getDate();
                    
                    SimpleDateFormat df = AppointmentModel.getTimeFormat();
                    String tx = df.format(d);
                    
                    // set appt time for computation
                    GregorianCalendar now = new GregorianCalendar();
                    GregorianCalendar acal = new GregorianCalendar();
                    acal.setTime(d);
                    
                    // need to set appt time to today in case it is a repeating appt. if it is a repeat,
                    // the time will be right, but the day will be the day of the first repeat
                    acal.set( now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get( Calendar.DATE) );
                    
                    // skip the appt if it is more than 3 hrs away or more than 30 mins in the past
                    long mins_to_go = (acal.getTimeInMillis() - now.getTimeInMillis()) / (1000 * 60);
                    if( mins_to_go > Prefs.getPref("pop_before_mins", 180) || mins_to_go < -1 * Prefs.getPref("pop_after_mins", 30) )
                        continue;
                    
                    // skip appt if it is already in the pops list
                    // this means that it is already showing - or was shown and killed already
                    if( pops.containsKey( ik ) )
                        continue;
                    
                    // get appt text - should never really be null
                    String xx = appt.getText();
                    if( xx == null ) {
                        continue;
                    }
                    
                    // create a new frame for a popup and add it to the popup map
                    // along with the appt key
                    JDialog jd = new JDialog();
                    pops.put(ik, jd);
                    
                    // add text to date
                    tx += " " + xx;
                    JLabel label = new JLabel(tx);
                    label.setHorizontalAlignment(JLabel.CENTER);
                    Container contentPane = jd.getContentPane();
                    contentPane.add(label, BorderLayout.CENTER);
                    jd.setSize(new Dimension(200, 100));
                    jd.setTitle("Appointment Reminder");
                    jd.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
                    jd.setVisible(true);
                    jd.toFront();
                    
                }
                catch( Exception e) {
                    Errmsg.errmsg(e);
                }
            }
        }
        
        enable = Prefs.getPref("beeping_reminders", "true");
        if( enable.equals("false"))
            return;
        
        // if any popups that are already displayed are less than 15 min away - make a sound
        // and raise the popup
        
        // iterate through existing popups
        Set s = pops.entrySet();
        Iterator i = s.iterator();
        while( i.hasNext() ) {
            // get popup frame
            Entry me = (Entry) i.next();
            Integer apptkey = (Integer) me.getKey();
            JDialog fr = (JDialog) me.getValue();
            
            // if frame is gone (killed already), then skip it
            if( fr == null )
                continue;
            
            // skip if popup not being shown - but still in map
            if( !fr.isDisplayable() ) {
                // free resources from JFrame and remove from map
                // map should be last reference to the frame so garbage
                // collection should now be free to clean it up
                me.setValue(null);
                continue;
            }
            
            
            // if appt is < 15 min away
            try {
                // read the appt and get the date
                Appointment appt = AppointmentModel.getReference().getAppt(apptkey.intValue());
                Date d = appt.getDate();
                if( d == null ) continue;
                
                // determine how far away the appt is
                GregorianCalendar acal = new GregorianCalendar();
                acal.setTime(d);
                GregorianCalendar now = new GregorianCalendar();
                
                // need to set appt time to today in case it is a repeating appt. if it is a repeat,
                // the time will be right, but the day will be the day of the first repeat
                acal.set( now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get( Calendar.DATE) );
                
                long mins_to_go = (acal.getTimeInMillis() - now.getTimeInMillis()) / (1000 * 60 );
                
                // if less than 15 mins (includes overdue appts) then make sound
                // and bring to front. stop beeping 30 mins after the appt.
                if( mins_to_go < Prefs.getPref("beeping_mins", 15) && mins_to_go > -1 * Prefs.getPref("pop_after_mins", 30) ) {
                    fr.toFront();
                    
                    // play sound
                    URL snd = getClass().getResource("/resource/blip.wav");
                    AudioClip theSound;
                    
                    theSound = Applet.newAudioClip(snd);
                    
                    if (theSound != null) {
                        theSound.play();
                    }
                }
            }
            catch( Exception e) {
                // ignore errors here
            }
        }
        
        
        
    }
    
    final Runnable doPopupChk = new Runnable() {
        public void run() {
            popup_chk();
        }
    };
    
    public void refresh() {
    }
    
}
