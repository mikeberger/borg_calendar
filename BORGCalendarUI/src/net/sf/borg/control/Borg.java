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

package net.sf.borg.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.sf.borg.common.app.AppHelper;
import net.sf.borg.common.io.IOHelper;
import net.sf.borg.common.ui.NwFontChooserS;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.Sendmail;

import net.sf.borg.model.AddressModel;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Task;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.BeanDataFactoryFactory;
import net.sf.borg.model.db.IBeanDataFactory;
import net.sf.borg.model.db.remote.IRemoteProxy;
import net.sf.borg.model.db.remote.IRemoteProxyProvider;
import net.sf.borg.model.db.remote.RemoteProxyHome;
import net.sf.borg.model.db.remote.http.HTTPRemoteProxy;
import net.sf.borg.ui.Banner;
import net.sf.borg.ui.CalendarView;
import net.sf.borg.ui.JDICTrayIconProxy;
import net.sf.borg.ui.LoginDialog;
import net.sf.borg.ui.OptionsView;
import net.sf.borg.ui.PopupView;
import net.sf.borg.ui.TodoView;

/*
 * borg.java
 * 
 * Created on August 15, 2001, 9:23 PM
 */

// the borg class is responsible for starting up the appropriate models and
// views.
// The views directly interact with the models to
// display data. Views register with their models to receive notifications of
// data changes.
// Views can call other views.
public class Borg extends Controller implements OptionsView.RestartListener {


    // the calendar and task data models
    private AppointmentModel calmod_ = null;

    private TaskModel taskmod_ = null;

    private AddressModel addrmod_ = null;

    static private Banner ban_ = null; // start up banner

    private java.util.Timer versionCheckTimer_ = null;
    private java.util.Timer syncTimer_ = null;
    private Timer mailTimer_ = null;

	static private Borg singleton = null;
	static public Borg getReference()
	{
		if( singleton == null )
			singleton = new Borg();
		return( singleton );
	}

    // this is the main for the borg application
    public static void main(String args[]) {
        // create a new borg object and call its init routing with the command
        // line args
        Borg b = new Borg();
        b.init(args);
    }

    private Borg()
    {
        // If we're doing remote stuff, use HTTPRemoteProxy
        RemoteProxyHome
        	.getInstance()
        	.setProxyProvider
        	(
        		new IRemoteProxyProvider()
        		{
					public final IRemoteProxy createProxy(String url)
					{
						// No synchronization needed - we're single-threaded.
						if (proxy == null)
							proxy = new HTTPRemoteProxy(url);
						return proxy;
					}

					public final Credentials getCredentials()
					{
						return Borg.this.getCredentials();
					}
					
					// private //
					private IRemoteProxy proxy = null;
        		}
        	);
    }

    public void restart() {
    	if( versionCheckTimer_ != null )
    		versionCheckTimer_.cancel();
    	if( syncTimer_ != null )
    		syncTimer_.cancel();
    	if( mailTimer_ != null )
    		mailTimer_.cancel();
        removeListeners();
        init(new String[0]);
    }

    private String buildDbDir()
    {
        // get dir for DB
    	String dbdir = "";
    	String dbtype = Prefs.getPref( PrefName.DBTYPE );
    	if( dbtype.equals("local"))
    	{
    		dbdir = Prefs.getPref(PrefName.DBDIR);
    	}
    	else if( dbtype.equals("remote"))
    	{
    		dbdir = "remote:" + Prefs.getPref(PrefName.DBURL);
    	}
    	else
    	{
    		// build a mysql URL
    		dbdir = "jdbc:mysql://" + Prefs.getPref( PrefName.DBHOST ) + ":" + Prefs.getPref( PrefName.DBPORT )
				+ "/" + Prefs.getPref( PrefName.DBNAME ) + "?user=" + Prefs.getPref( PrefName.DBUSER ) +
				"&password=" + Prefs.getPref( PrefName.DBPASS ) + "&autoReconnect=true";
    	}
    	
    	return( dbdir );
    }
    
    // init will process the command line args, open and load the databases, and
    // start up the
    // main month view
    private void init(String args[]) {
		AppHelper.freeze();
			// prevent subsequent tampering with our environment
		
        boolean readonly = false; // open DBs readonly
        boolean aplist = false; // do not start GUI, only generate a list of
        // appointments
        // this option is not really used anymore
        boolean autostart = false; // autostart feature - only bring up the GUI
        // if an appointment is approaching
 

        OptionsView.setRestartListener(this);
        
        // override for testing a different db
        String testdb = !AppHelper.isApplication() ? "mem:" : null;

        // override for tray icon name
        String trayname = "BORG";

        // process command line args
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("-r"))
            {
                readonly = true;
            }
            else if (args[i].equals("-aplist"))
            {
                readonly = true;
                aplist = true;

                // set Errmsg class to log all errors to stdout, not using popup
                // windows
                Errmsg.console(true);
            }
            else if (args[i].equals("-trayname"))
            {
                i++;
                if (i >= args.length)
                {
                    System.out.println("Error: missing trayname argument");
                    System.exit(1);
                }
                trayname = args[i];
            }
            else if (args[i].equals("-db"))
            {
                i++;
                if (i >= args.length)
                {
                    System.out.println(Resource
                            .getResourceString("-db_argument_is_missing"));
                    System.exit(1);
                }
                testdb = args[i];
            }
            else if (args[i].equals("-autostart"))
            {
                autostart = true;
                System.out.println(Resource
                        .getResourceString("Autostart_mode_on"));
                // set Errmsg class to log all errors to stdout, not using popup
                // windows
                Errmsg.console(true);

            }
        }

        boolean splash = true;
        String spl = Prefs.getPref(PrefName.SPLASH);
        if (spl.equals("false"))
        {
            splash = false;
        }

        String deffont = Prefs.getPref(PrefName.DEFFONT);
        if (!deffont.equals(""))
        {
            Font f = Font.decode(deffont);
            NwFontChooserS.setDefaultFont(f);
        }
        
        // set the look and feel
        String lnf = Prefs.getPref(PrefName.LNF);
        try
        {
            UIManager.setLookAndFeel(lnf);
            UIManager.getLookAndFeelDefaults().put("ClassLoader", getClass().getClassLoader());
        }
        catch (Exception e)
        {
            //System.out.println(e.toString());
        }

        String country = Prefs.getPref(PrefName.COUNTRY);
        String language = Prefs.getPref(PrefName.LANGUAGE);

        if (!language.equals(""))
        {
            Locale.setDefault(new Locale(language, country));
        }
        
        // do not show the startup banner if autostart or aplist features are on
        if (!aplist && !autostart && splash)
        {
            ban_ = new Banner();
            ban_.setText(Resource.getResourceString("Initializing"));
            ban_.setVisible(true);
        }
        
        // Which database implementation are we using?
        String dbdir = "";
        boolean shared = false;
        try
        {
            // init cal model & load data from database
            if (testdb != null)
                dbdir = testdb;
            else
            	dbdir = buildDbDir();

            if (dbdir.equals("not-set"))
            {

                // cannot run in autostart mode if no db set. user might not be
                // there
                // to respond to dialog. autostart probably run from cron
                // job/scheduled task
                if (autostart)
                {
                    System.out
                            .println(Resource
                                    .getResourceString("Cannot_run_autostart_mode_-_database_directory_has_not_been_set."));
                    System.exit(0);
                }

                JOptionPane.showMessageDialog(null, Resource.getResourceString("selectdb"), Resource.getResourceString("Notice"), JOptionPane.INFORMATION_MESSAGE);
                
                if( ban_ != null )
                    ban_.dispose();
                
                OptionsView.dbSelectOnly();
                return;
            }

            String shrd = Prefs.getPref(PrefName.SHARED);
            if (shrd.equals("true"))
            {
                shared = true;
            }
            
            // default uid - this will eventually be set by a login/passwd lookup into
            // a users table when multi-user support is added
            String uid = "$default";
            
            /*
            if( Prefs.getPref( PrefName.DBTYPE ).equals("mysql"))
            {
                // validate user id for mysql
                UserModel umod = UserModel.create();
                register(umod);
                umod.open_db(dbdir, readonly);
                uid = umod.validate("ckb", "ckb");
            }*/
           
            // skip banner stuff if autostart or aplist on
            if (!aplist && !autostart && splash)
                ban_.setText(Resource
                        .getResourceString("Loading_Appt_Database"));
                        
            // If we're working from memory, give them the opportunity
            // to populate our memory files first.
            if (dbdir.startsWith("mem:"))
            {
            	for (;;)
            	{
					int ret =
						JOptionPane.showConfirmDialog(
							ban_,
							Resource.getResourceString("Sandboxed_Mode"),
							Resource.getResourceString("Sandboxed_Mode_Title"),
							JOptionPane.YES_NO_OPTION);
					if (ret != JOptionPane.YES_OPTION)
						break;
						
					String url = "";
					URL codeBase = AppHelper.getCodeBase();
					if (codeBase != null)
						url = codeBase.toExternalForm() + "borg_mem.dat";
						
					try
					{
						String urlst =
							JOptionPane.showInputDialog(
								ResourceBundle.getBundle(
									"resource/borg_resource").getString(
									"enturl"),
								url);
								
						if (urlst == null)
							break;
								
						if(urlst.length() > 0)
						{
							IOHelper.loadMemoryFromURL(urlst);
							break;
						}
					}
					catch (Exception e)
					{
						Errmsg.errmsg(e);
					}
            	}
            }
            
            // Get our DB factory
            StringBuffer tmp = new StringBuffer(dbdir);
            IBeanDataFactory factory = BeanDataFactoryFactory.getInstance()
					.getFactory(tmp, readonly, shared);
            dbdir = tmp.toString();
            	// let the factory tweak dbdir
            
            calmod_ = AppointmentModel.create();
            register(calmod_);
            calmod_.open_db(factory, dbdir, uid);

            // aplist only needs calendar data - so just print list of
            // appointments
            // and exit. not used anymore
            if (aplist)
            {
                emailReminder(null);
                System.exit(0);
                return;
            }

            if (autostart)
            {
                // check if auto start conditions are true
                if (!should_auto_start())
                    System.exit(0);
            }

            // we are past autostart check so we must be ready to start GUI.
            // now all errors can go to popup windows
            Errmsg.console(false); // send errors to screen

            // init task model & load database
            if (!autostart && splash)
                ban_.setText(Resource
                        .getResourceString("Loading_Task_Database"));
            taskmod_ = TaskModel.create();
            register(taskmod_);
            taskmod_.open_db(factory, dbdir, uid);

            if (!autostart && splash)
                ban_.setText(Resource
                        .getResourceString("Opening_Address_Database"));
            addrmod_ = AddressModel.create();
            register(addrmod_);
            addrmod_.open_db(factory, dbdir, uid);

            if (!autostart && splash)
                ban_.setText(Resource.getResourceString("Opening_Main_Window"));

            	final String traynm = trayname;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                    	swingStart(traynm);
                    }
                }
                );
         

            if (!autostart && splash)
                ban_.dispose();
            ban_ = null;
            
            if (!AppHelper.isApplet())
            {
            	// start up version check timer
                versionCheckTimer_ = new java.util.Timer();
                versionCheckTimer_.schedule(new TimerTask() {
                    public void run() {
                        //reminder();
                        version_chk();
                    }
                }, 10 * 1000, 20 * 60 * 1000);
                
                // calculate email time in minutes from now
                Calendar cal = new GregorianCalendar();
                int emailmins = Prefs.getIntPref(PrefName.EMAILTIME);
                int curmins = 60 * cal.get(Calendar.HOUR_OF_DAY) + cal.get( Calendar.MINUTE);
                int mailtime = emailmins - curmins;
                if( mailtime < 0 )
                {
                	// we are past mailtime - send it now
                	emailReminder(null);
                	
                	// set timer for next mailtime
                	mailtime += 24*60; // 24 hours from now
                }

            	// start up email check timer - every 24 hours
                versionCheckTimer_ = new java.util.Timer();
                versionCheckTimer_.schedule(new TimerTask() {
                    public void run() {
                        emailReminder(null);
                    }
                }, mailtime * 60 * 1000, 24 * 60 * 60 * 1000);
                
                // start autosync timer
                int syncmins = Prefs.getIntPref(PrefName.SYNCMINS);
                if( shared && syncmins != 0 )
                {
                    syncTimer_ = new java.util.Timer();
                    syncTimer_.schedule(new TimerTask() {
                        public void run() {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    try {
                                        AppointmentModel.getReference().sync();
                                        AddressModel.getReference().sync();
                                        TaskModel.getReference().sync();
                                    }
                                    catch( Exception e) {
                                        Errmsg.errmsg(e);
                                    }
                                }
                            });
                        }
                    }, syncmins * 60 * 1000, syncmins * 60 * 1000);
                }
            }

        }
        catch (Exception e)
        {
            // if something goes wrong, it might be that the database directory
            // is bad. Maybe
            // it does not exist anymore or something, so give the user a chance
            // to change it
            // if it will fix the problem
        	
        	
            Errmsg.errmsg(e);

            // get rid of NESTED exceptions for SQL exceptions - they make the error window too large
            String es = e.toString();
            int i1 = es.indexOf("** BEGIN NESTED");
            int i2 = es.indexOf("** END NESTED");
            
            if( i1 != -1 && i2 != -1 )
            {
            	int i3 = es.indexOf('\n', i1);
            	String newstring = es.substring(0,i3) + "\n-- removed --\n" + es.substring(i2);
                es = newstring;
            }
            es += Resource.getResourceString("db_set_to") + dbdir;
            es += Resource.getResourceString("bad_db_2");

            // prompt for ok
            int ret = JOptionPane
                    .showConfirmDialog(null, es, Resource
                            .getResourceString("BORG_Error"),
                            JOptionPane.YES_NO_OPTION);
            if (ret == JOptionPane.YES_OPTION)
            {
                if( ban_ != null )
                    ban_.dispose();
                OptionsView.dbSelectOnly();
                return;
            }

            System.exit(1);

        }

    }

    private void swingStart(String trayname)
    {
        boolean trayIcon = true;
        if (!AppHelper.isApplication())
        {
            trayIcon = false;
        }
        else
        {
            try
            {
            	JDICTrayIconProxy tip = JDICTrayIconProxy.getReference();
            	tip.init(trayname);
            }
            catch (UnsatisfiedLinkError le)
            {
            	Errmsg.errmsg(new Exception(le));
                trayIcon = false;
            }
            catch (NoClassDefFoundError ncf)
            {
            	Errmsg.errmsg(new Exception(ncf));
                trayIcon = false;
            }
            catch (Exception e)
            {
                Errmsg.errmsg(e);
                System.exit(0);
            }
        }


        
        // create popups view
        new PopupView();
        
        // only start to systray (i.e. no month/todo views, if
        // trayicon is available and option is set
        String backgstart = Prefs.getPref(PrefName.BACKGSTART);
        if (backgstart.equals("false") || !trayIcon)
        {
        	// start main month view
        	CalendarView.getReference(trayIcon);

        	// start todo view if there are todos
        	if (AppointmentModel.getReference().haveTodos())
        	{
        		startTodoView();
        	}
        }
    }
   
 
    // check if we should auto_start
    // this function checks if an appointment is coming close
    // it does not check if BORG is already running or if the user is not logged
    // on - that will fall out elsewhere
    private boolean should_auto_start() {

        System.out.println(Resource
                .getResourceString("Commencing_auto-start_checks"));

        GregorianCalendar now = new GregorianCalendar();

        // get the base day key for today
        int key = AppointmentModel.dkey(now.get(Calendar.YEAR), now
                .get(Calendar.MONTH), now.get(Calendar.DATE));

        // get the list of appts for today
        Collection l = calmod_.getAppts(key);
        if (l != null)
        {

            Iterator it = l.iterator();
            Appointment appt;

            // iterate through the day's appts
            while (it.hasNext())
            {

                Integer ik = (Integer) it.next();

                // read the appt
                try
                {
                    appt = calmod_.getAppt(ik.intValue());

                    // an untimed appt (note) cannot force an auto start
                    if (AppointmentModel.isNote(appt))
                        continue;

                    Date d = appt.getDate();

                    // set acal to the appointments due time and calculate how
                    // many minutes
                    // there are before the appointment time
                    GregorianCalendar acal = new GregorianCalendar();
                    acal.setTime(d);
                    long mins_to_go = (acal.getTimeInMillis() - now
                            .getTimeInMillis())
                            / (1000 * 60);

                    // if the appointment is less than 30 minutes away or within
                    // the past 5 minutes,
                    // we can autostart borg. otherwise, look for the next appt
                    if (mins_to_go > 30 || mins_to_go < -5)
                        continue;

                    System.out.println("Found an appointment with time "
                            + mins_to_go + " minutes from now, starting BORG");

                    return (true);
                }
                catch (Exception e)
                {
                    System.out.println(e.toString());
                    return (false);
                }
            }

        }

        System.out.println("No appointments coming due, canceling BORG start");
        return (false);

    }

    // show the todo list view
    private void startTodoView() {

        try
        {
            // bring up todo window
            TodoView tg = TodoView.getReference();
            tg.setVisible(true);
        }
        catch (Exception e)
        {
            Errmsg.errmsg(e);
        }
    }

    static private int verToInt(String version) {
        int res = 0;
        String parts[] = version.split("[.]");
        for (int i = 0; i < parts.length; i++)
        {
            res += Integer.parseInt(parts[i]) * Math.pow(10, (5 - i));
        }

        return (res);
    }

    private void version_chk() {
        try
        {
            // check if the version check feature has been enabled
            int vcl = Prefs.getIntPref(PrefName.VERCHKLAST);
            if (vcl == -1)
                return;

            // if version check was already done today - don't do again
            GregorianCalendar cal = new GregorianCalendar();
            int doy = cal.get(Calendar.DAY_OF_YEAR);
            if (doy == vcl)
                return;

            // get version and compare
            URL webverurl = new URL(
                    "http://borg-calendar.sourceforge.net/latest_version");
            InputStream is = webverurl.openStream();
            int i;
            String webver = "";
            while (true)
            {
                i = is.read();
                if (i == -1 || i == '\n' || i == '\r')
                    break;
                webver += (char) i;
            }

            if (!webver.equals(Resource.getVersion()))
            {

                // check if webver is lower than the current version
                if (verToInt(webver) < verToInt(Resource.getVersion()))
                {
                    return;
                }
                String info = "A new version of BORG is available\nYour version = "
                        + Resource.getVersion()
                        + "\nNew version = "
                        + webver
                        + "\nCheck the BORG website at http://borg-calendar.sourceforge.net for details"
                        + "\nuse the Edit Preferences menu to shut off this automatic check";

                // Cannot use JOptionPane here since the dialog will pop up
                // without
                // the user around - use non-modal JDialog
                JDialog jd = new JDialog();
                JTextArea label = new JTextArea(info);
                //label.setHorizontalAlignment(JLabel.CENTER);
                Container contentPane = jd.getContentPane();
                label.setEditable(false);
                label.setBackground(new Color(204, 204, 204));
                contentPane.add(label, BorderLayout.CENTER);
                jd.setSize(new Dimension(450, 150));
                jd.setTitle("BORG Version Check");
                jd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                jd.setVisible(true);
                jd.toFront();
                //JOptionPane.showMessageDialog(null, info, "BORG Version
                // Check", JOptionPane.INFORMATION_MESSAGE, new
                // ImageIcon(getClass().getResource("/borg/borg.jpg")));
            }

            // set new version check day
            Prefs.putPref(PrefName.VERCHKLAST, new Integer(doy));
        }
        catch (Exception e)
        {
            Errmsg.errmsg(e);
        }

    }

    // send an email of the next day's appointments if the user has requested
    // this and
    // such an email has not been sent today yet.
    static public void emailReminder(Calendar emailday) {

        // check if the email feature has been enabled
        String email = Prefs.getPref(PrefName.EMAILENABLED);
        if (email.equals("false"))
            return;

        // get the SMTP host and address
        String host = Prefs.getPref(PrefName.EMAILSERVER);
        String addr = Prefs.getPref(PrefName.EMAILADDR);


        if (host.equals("") || addr.equals(""))
            return;
        
        Calendar cal = new GregorianCalendar();
        
        // if no date passed, the timer has gone off and we need to check if we can send
        // email now
        int doy = -1;
        if( emailday == null )
        {
        	// get the last day that email was sent
        	int lastday = Prefs.getIntPref(PrefName.EMAILLAST);

        	// if email was already sent today - don't send again
        	doy = cal.get(Calendar.DAY_OF_YEAR);
        	if (doy == lastday)
        		return;
        	
        	// create the calendar model key for tomorrow
        	cal.add(Calendar.DATE, 1);
        }
        else
        {
        	// just send email for the given day
        	cal = emailday;
        }
        
        int key = AppointmentModel.dkey(cal.get(Calendar.YEAR), cal
                .get(Calendar.MONTH), cal.get(Calendar.DATE));

        // tx is the contents of the email
        String tx = "Appointments for "
                + DateFormat.getDateInstance().format(cal.getTime()) + "\n";

        // get the list of appts for tomorrow
        Collection l = AppointmentModel.getReference().getAppts(key);
        if (l != null)
        {

            Iterator it = l.iterator();
            Appointment appt;

            // iterate through the day's appts
            while (it.hasNext())
            {

                Integer ik = (Integer) it.next();

                try
                {
                    // read the appointment from the calendar model
                    appt = AppointmentModel.getReference().getAppt(ik.intValue());

                    // get the appt flags to see if the appointment is private
                    // if so, don't include it in the email
                    if (appt.getPrivate())
                        continue;

                    if (!AppointmentModel.isNote(appt))
                    {
                        // add the appointment time to the email if it is not a
                        // note
                        Date d = appt.getDate();
                        SimpleDateFormat df = AppointmentModel.getTimeFormat();
                        tx += df.format(d) + " ";
                    }

                    // add the appointment text
                    tx += appt.getText();
                    tx += "\n";
                }
                catch (Exception e)
                {
                    System.out.println(e.toString());
                    return;
                }
            }

        }

        // load any task tracker items for the email
        // these items are cached in the calendar model
        // by date - but the taskmodel is the real owner of them
        l = TaskModel.getReference().get_tasks(key);
        if (l != null)
        {

            Iterator it = l.iterator();

            while (it.hasNext())
            {
                // add each task to the email - and remove newlines

                Task task = (Task) it.next();
                tx += "Task[" + task.getTaskNumber() + "] ";
                String de = task.getDescription();
                tx += de.replace('\n', ' ');
                tx += "\n";
            }
        }

        // send the email using SMTP
        try
        {
            StringTokenizer stk = new StringTokenizer(addr,",;");
            while (stk.hasMoreTokens())
            {
                String a = stk.nextToken();
                if( !a.equals("") )
                {
                    String s = Sendmail.sendmail(host, 25, "Borg Reminder", tx, a.trim(),
                            a.trim());
                    String ed = Prefs.getPref(PrefName.EMAILDEBUG);
                    if (ed.equals("1"))
                        Errmsg.notice(s);
                }
            }


        }
        catch (Exception e)
        {
            Errmsg.errmsg(e);
        }

        // record that we sent email today
        if( doy != -1 )
        	Prefs.putPref(PrefName.EMAILLAST, new Integer(doy));

        return;
    }

    private IRemoteProxyProvider.Credentials getCredentials()
    {
    	// Find a suitable frame parent for the login dialog.
    	JFrame parent = null;
    	if (ban_ != null)
    		parent = ban_;
    	else
    		parent = CalendarView.getReference();
    	
		final LoginDialog dlg = new LoginDialog(parent);
    	Runnable runnable =
    		new Runnable()
    		{
			public void run()
			{
				dlg.setVisible(true);
			}
    		};
    		
        // Bring up the login dialog. How we do this properly in
    	// Swing depends on whether we're the event dispatch thread
    	// or not.
    	if (SwingUtilities.isEventDispatchThread())
    		runnable.run();
    	else
    	{
    		try
    		{
    			SwingUtilities.invokeAndWait(runnable);
    		}
    		catch (InvocationTargetException e)
    		{}
    		catch (InterruptedException e)
    		{}
    	}
    		
    	return
    		new IRemoteProxyProvider.Credentials
    		(
    			dlg.getUsername(),
				dlg.getPassword()
			);
    }
}