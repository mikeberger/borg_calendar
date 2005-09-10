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

package net.sf.borg.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import net.sf.borg.common.app.AppHelper;
import net.sf.borg.common.io.IOHelper;
import net.sf.borg.common.io.OSServicesHome;
import net.sf.borg.common.ui.OverwriteConfirm;
import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Resource;
import net.sf.borg.common.util.XTree;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentIcalAdapter;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.AppointmentVcalAdapter;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.Day;
import net.sf.borg.model.Task;
import net.sf.borg.model.TaskModel;
// This is the month view GUI
// it is the main borg window
// like most of the other borg window, you really need to check the netbeans form
// editor to get the graphical picture of the whole window

public class CalendarView extends View implements Prefs.Listener {

    // current year/month being viewed
    private int year_;
    private int month_;
    private boolean trayIcon_;

    // the button we used to dismiss any child dialog
    private MemDialog dlgMemFiles;

    // the file we chose in our memory file chooser dialog
    private String memFile;


    private static CalendarView singleton = null;
    public static CalendarView getReference(boolean trayIcon) {
        if( singleton == null || !singleton.isShowing())
            singleton = new CalendarView(trayIcon);
        return( singleton );
    }

    public static CalendarView getReference()
    {
        return( singleton );
    }

    private CalendarView(boolean trayIcon) {
        super();
        trayIcon_ = trayIcon;

        addModel(AppointmentModel.getReference());
        addModel(TaskModel.getReference());
        addModel(AddressModel.getReference());

        // register this view as a Prefs Listener to
        // be notified of Prefs changes
        Prefs.addListener(this);
        init();
        manageMySize(PrefName.CALVIEWSIZE);
        
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    }

    private static class DayMouseListener implements MouseListener
	{
    	int year;
    	int month;
    	int date;
    	public DayMouseListener(int year, int month, int date){
    		this.date = date;
    		this.year = year;
    		this.month = month;
    	}

        public void mouseClicked(MouseEvent evt) {
            String reverseActions = Prefs.getPref(PrefName.REVERSEDAYEDIT);
            if( reverseActions.equals("true"))
            {
                // start the appt editor view
            	DayView dv = new DayView(month, year, date);
            	dv.setVisible(true);
            }
            else
            {
            	AppointmentListView ag = new AppointmentListView(year, month, date);
                ag.setVisible(true);

            }


        }

		public void mouseEntered(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}
	}


    private void init() {

        initComponents();

        GridBagConstraints cons;

        // the day boxes - which will contain a date button and day text
        days = new JPanel[37];

        // the day text areas
        daytext = new JTextPane[37];

        // the date buttons
        daynum = new JButton[37];
        dayOfYear = new JLabel[37];

        // initialize the days
        for( int i = 0; i < 37; i++ ) {

            // allocate a panel for each day
            // and add a date button and non wrapping text pane
            // in each
            days[i] = new JPanel();
            days[i].setLayout(new GridBagLayout());
            // as per the experts, this subclass of JTextPane is the only way to
            // stop word-wrap
            JTextPane jep = null;
            String wrap = Prefs.getPref(PrefName.WRAP);
            if( wrap.equals("true") ) {
                jep = new JTextPane();
            }
            else {
                jep =  new JTextPane() {
                    public boolean getScrollableTracksViewportWidth() {
                        return false;
                    }
                    public void setSize(Dimension d) {
                        if(d.width < getParent().getSize().width) {
                            d.width = getParent().getSize().width;
                        }
                        super.setSize(d);
                    }
                };
            }
            daytext[i] = jep;
            daytext[i].setEditable(false);
            daynum[i] = new JButton("N");

            // when the date button is pressed, popup an appointment editor for the day
            daynum[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    JButton but = (JButton) evt.getSource();
                    int day = Integer.parseInt(but.getText());
                    String reverseActions = Prefs.getPref(PrefName.REVERSEDAYEDIT);
                    if( reverseActions.equals("true"))
                    {
                    	AppointmentListView ag = new AppointmentListView(year_, month_, day);
                    	ag.setVisible(true);
                    }
                    else
                    {
                    // start the appt editor view
                    	DayView dv = new DayView(month_, year_, day);
                    	dv.setVisible(true);
                    }
                }
            });

            dayOfYear[i] = new JLabel();

            // continue laying out the day panel. want the date button in upper right
            // and want the text pane top to be lower than the bottom of the
            // button.
            Insets is = new Insets(1,4,1,4);
            daynum[i].setMargin(is);
            days[i].setBorder(new BevelBorder(BevelBorder.RAISED));
            days[i].add(daynum[i]);
            cons = new GridBagConstraints();
            cons.gridx = 1;
            cons.gridy = 0;
            cons.gridwidth = 1;
            cons.fill = GridBagConstraints.NONE;
            cons.anchor = GridBagConstraints.NORTHEAST;
            days[i].add(daynum[i], cons);

            cons = new GridBagConstraints();
            cons.gridx = 0;
            cons.gridy = 0;
            cons.gridwidth = 1;
            cons.fill = GridBagConstraints.NONE;
            cons.anchor = GridBagConstraints.NORTHWEST;
            days[i].add(dayOfYear[i], cons);

            cons.gridx = 0;
            cons.gridy = 1;
            cons.gridwidth = 2;
            cons.weightx = 1.0;
            cons.weighty = 1.0;
            cons.fill = GridBagConstraints.BOTH;
            cons.anchor = GridBagConstraints.NORTHWEST;

            // put the appt text in an invisible scroll pane
            // scrollbars will only appear if needed due to amount of appt text
            JScrollPane sp = new JScrollPane();
            sp.setViewportView(daytext[i]);
            sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            sp.setBorder( new EmptyBorder(0,0,0,0) );
            sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0,10));
            sp.getVerticalScrollBar().setPreferredSize(new Dimension(10,0));
            days[i].add(sp,cons);


            jPanel1.add(days[i]);
        }

        // add filler to the Grid
        jPanel1.add( new JPanel() );
        jPanel1.add( new JPanel() );
        jPanel1.add( new JPanel() );


        setDayLabels();
        //
        // ToDo PREVIEW BOX
        //
        todoPreview = new JTextPane() {
            public boolean getScrollableTracksViewportWidth() {
                return false;
            }
            public void setSize(Dimension d) {
                if(d.width < getParent().getSize().width) {
                    d.width = getParent().getSize().width;
                }
                super.setSize(d);
            }
        };
        todoPreview.addMouseListener(new MouseListener()
        	{
                public void mouseClicked(MouseEvent evt) {
                    	TodoView.getReference().setVisible(true);
                }
        		public void mouseEntered(MouseEvent arg0) {}
        		public void mouseExited(MouseEvent arg0) {}
        		public void mousePressed(MouseEvent arg0) {}
        		public void mouseReleased(MouseEvent arg0) {}
        	}
        );
        todoPreview.setBackground( new Color( 204,204,204 ));
        todoPreview.setEditable(false);
        JScrollPane sp = new JScrollPane();
        sp.setViewportView(todoPreview);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        //sp.setBorder( new javax.swing.border.EmptyBorder(0,0,0,0) );
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0,5));
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(5,0));
        jPanel1.add( sp );

        //
        // TASK PREVIEW BOX
        //
        taskPreview = new JTextPane() {
            public boolean getScrollableTracksViewportWidth() {
                return false;
            }
            public void setSize(Dimension d) {
                if(d.width < getParent().getSize().width) {
                    d.width = getParent().getSize().width;
                }
                super.setSize(d);
            }
        };
        taskPreview.setBackground( new Color( 204,204,204 ));
        taskPreview.setEditable(false);
        taskPreview.addMouseListener(new MouseListener()
            	{
                    public void mouseClicked(MouseEvent evt) {
                        	TaskListView v = TaskListView.getReference();
                        	v.refresh();
                        	v.setVisible(true);
                    }
            		public void mouseEntered(MouseEvent arg0) {}
            		public void mouseExited(MouseEvent arg0) {}
            		public void mousePressed(MouseEvent arg0) {}
            		public void mouseReleased(MouseEvent arg0) {}
            	}
            );
        sp = new JScrollPane();
        sp.setViewportView(taskPreview);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        //sp.setBorder( new javax.swing.border.EmptyBorder(0,0,0,0) );
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0,5));
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(5,0));
        jPanel1.add( sp );

        // update the styles used in the appointment text panes for the various appt text
        // colors, based on the current font size set by the user
        updStyles();

        // init view to current month
        try {
            today();
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }

        String shared = Prefs.getPref(PrefName.SHARED);
        if( !shared.equals("true")) {
            syncMI.setEnabled(false);
        }
        else {
            syncMI.setEnabled(true);
        }

		dlgMemFiles = new MemDialog(this);
		dlgMemoryFilesChooser.setSize(200,350);
		listFiles.setModel(new DefaultListModel());
		importMI.setEnabled(!AppHelper.isApplet());
		exportMI.setEnabled(AppHelper.isApplication());
		impical.setEnabled(!AppHelper.isApplet());
		expical.setEnabled(!AppHelper.isApplet());

		String showmem = Prefs.getPref(PrefName.SHOWMEMFILES);
		if( !showmem.equals("true") && !AppHelper.isApplet())
		{
			impXMLMem.setVisible(false);
			expXMLMem.setVisible(false);
			viewMem.setVisible(false);
		}

        // show the window
        pack();
        setVisible(true);

        String version = Resource.getVersion();
        if( version.indexOf("beta") != -1 )
            Errmsg.notice(Resource.getResourceString("betawarning"));

    }

    public void destroy() {
        this.dispose();
    }

	// create an OutputStream from a URL string, special-casing "mem:"
	private OutputStream createOutputStreamFromURL(String urlstr)
		throws Exception
	{
		if (urlstr.startsWith("mem:"))
			return IOHelper.createOutputStream(urlstr);

		return IOHelper.createOutputStream(new URL(urlstr));
	}

    /* set borg to current month and refresh the screen */
    private void today() throws Exception {
        GregorianCalendar cal = new GregorianCalendar();
        month_ = cal.get(Calendar.MONTH);
        year_ = cal.get(Calendar.YEAR);
        refresh();
    }

    public void goTo( Calendar cal )
    {
        month_ = cal.get(Calendar.MONTH);
        year_ = cal.get(Calendar.YEAR);
        refresh();
    }

    // initialize the various text styles used for appointment
    // text for a single text pane
    private void initStyles(JTextPane textPane, Style def, Font font) {
        //Initialize some styles.
        Style bl = textPane.addStyle("black", def);
        int fontsize = font.getSize();
        String family = font.getFamily();
        boolean bold = font.isBold();
        boolean italic = font.isItalic();

        StyleConstants.setFontFamily(bl, family);
        StyleConstants.setBold(bl, bold);
        StyleConstants.setItalic( bl, italic);
        StyleConstants.setFontSize(bl, fontsize);
        String ls = Prefs.getPref(PrefName.LINESPACING);
        float f = Float.parseFloat(ls);
        StyleConstants.setLineSpacing(bl,f);

        // bsv 2004-12-21
        boolean bUseUCS = ((Prefs.getPref(PrefName.UCS_ON)).equals("true"))?true:false;
        Color ctemp;
        try {
            Style s = textPane.addStyle("blue", bl);
            // bsv 2004-12-21
            ctemp = new Color( (new Integer( Prefs.getPref(PrefName.UCS_BLUE))).intValue() );
            StyleConstants.setForeground(s, bUseUCS?(ctemp):(Color.BLUE));
            //StyleConstants.setForeground(s, Color.BLUE);

            s = textPane.addStyle("red", bl);
            // bsv 2004-12-21
            ctemp = new Color( (new Integer( Prefs.getPref(PrefName.UCS_RED))).intValue() );
            StyleConstants.setForeground(s, bUseUCS?(ctemp):(Color.RED));
            //StyleConstants.setForeground(s, Color.RED);


            s = textPane.addStyle("green", bl);
            // bsv 2004-12-21
            ctemp = new Color( (new Integer( Prefs.getPref(PrefName.UCS_GREEN))).intValue() );
            StyleConstants.setForeground(s, bUseUCS?(ctemp):(Color.GREEN));
            //StyleConstants.setForeground(s, Color.GREEN);


            s = textPane.addStyle("white", bl);
            // bsv 2004-12-21
            ctemp = new Color( (new Integer( Prefs.getPref(PrefName.UCS_WHITE))).intValue() );
            StyleConstants.setForeground(s, bUseUCS?(ctemp):(Color.WHITE));
            //StyleConstants.setForeground(s, Color.WHITE);

            // bsv 2004-12-21
            s = textPane.addStyle("navy", bl);
            ctemp = new Color( (new Integer( Prefs.getPref(PrefName.UCS_NAVY))).intValue() );
            StyleConstants.setForeground(s, bUseUCS?(ctemp):(new Color(0,0,102)));
            s = textPane.addStyle("purple", bl);
            ctemp = new Color( (new Integer( Prefs.getPref(PrefName.UCS_PURPLE))).intValue() );
            StyleConstants.setForeground(s, bUseUCS?(ctemp):(new Color(102,0,102)));
            s = textPane.addStyle("brick", bl);
            ctemp = new Color( (new Integer( Prefs.getPref(PrefName.UCS_BRICK))).intValue() );
            StyleConstants.setForeground(s, bUseUCS?(ctemp):(new Color(102,0,0)));
            // (bsv 2004-12-21)

            s = textPane.addStyle("sm", bl );
            StyleConstants.setBold(s,false);
            StyleConstants.setFontSize(s,fontsize);

            s = textPane.addStyle("smul", s );
            StyleConstants.setUnderline( s, true );

            s = textPane.addStyle("strike", bl );
            StyleConstants.setStrikeThrough( s, true );

            URL icon = getClass().getResource("/resource/" + Prefs.getPref(PrefName.UCS_MARKER));
            if( icon != null ){
            	s = textPane.addStyle("icon", bl );
            	StyleConstants.setIcon( s, new ImageIcon(icon) );
            }

        }
        catch( NoSuchFieldError e ) {
            // java 1.3 - just use black
        }


    }

    // update the text styles for all appt text panes
    // this is called when the user changes the font size
    void updStyles() {


        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        // update all of the text panes
        String s = Prefs.getPref(PrefName.APPTFONT);
        Font f = Font.decode(s);
        for( int i = 0; i < 37; i++ ) {
            initStyles(daytext[i], def, f);
        }

        s = Prefs.getPref(PrefName.PREVIEWFONT);
        f = Font.decode(s);

        initStyles( todoPreview, def, f );
        initStyles( taskPreview, def, f );

    }

    // adds a string to an appt text pane using a given style
    private void addString(JTextPane tp, String s, String style) throws Exception {
        // Add the string.
        StyledDocument doc = tp.getStyledDocument();

        if( style == null ) style = "black";

        // get the right style based on the color
        Style st = tp.getStyle(style);

        // static can be null for old BORG DBs that have
        // colors no longer supported. Only 2-3 people would encounter this.
        // default to black
        if( st == null )
            st = tp.getStyle("black");

        // add string to text pane
        doc.insertString(doc.getLength(), s, st);

    }

    void setDayLabels() {
        // determine first day and last day of the month
        GregorianCalendar cal = new GregorianCalendar();
        cal.setFirstDayOfWeek(Prefs.getIntPref(PrefName.FIRSTDOW));
        cal.set(Calendar.DATE, 1 );
        cal.add( Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() - cal.get(Calendar.DAY_OF_WEEK) );
        SimpleDateFormat dwf = new SimpleDateFormat("EEEE");
        jLabel1.setText( dwf.format(cal.getTime() ));
        cal.add( Calendar.DAY_OF_WEEK, 1 );
        jLabel3.setText( dwf.format(cal.getTime() ));
        cal.add( Calendar.DAY_OF_WEEK, 1 );
        jLabel4.setText( dwf.format(cal.getTime() ));
        cal.add( Calendar.DAY_OF_WEEK, 1 );
        jLabel5.setText( dwf.format(cal.getTime() ));
        cal.add( Calendar.DAY_OF_WEEK, 1 );
        jLabel6.setText( dwf.format(cal.getTime() ));
        cal.add( Calendar.DAY_OF_WEEK, 1 );
        jLabel7.setText( dwf.format(cal.getTime() ));
        cal.add( Calendar.DAY_OF_WEEK, 1 );
        jLabel8.setText( dwf.format(cal.getTime() ));
    }

    private void exit()
    {
    	if (IOHelper.isMemFilesDirty() && !AppHelper.isApplet())
    	{
    		setVisible(false);
			JOptionPane.showMessageDialog(
				this,
				Resource.getResourceString("Memory_Files_Changed"),
				Resource.getResourceString("Memory_Files_Changed_Title"),
				JOptionPane.INFORMATION_MESSAGE);
			dlgMemFiles.setMemento(IOHelper.getMemFilesMemento());
			dlgMemFiles.setVisible(true);
    	}
		System.exit(0);
    }

    /** refresh displays a month on the main gui window and updates the todo and task previews*/
    public void refresh() {
        try {

            // determine first day and last day of the month
            GregorianCalendar cal = new GregorianCalendar();
            int today = -1;
            if( month_ == cal.get(Calendar.MONTH) && year_ == cal.get(Calendar.YEAR)) {
                today = cal.get(Calendar.DAY_OF_MONTH);
                Today.setEnabled(false);
            }
            else {
                Today.setEnabled(true);
            }

            cal.setFirstDayOfWeek(Prefs.getIntPref(PrefName.FIRSTDOW));

            // set cal to day 1 of month
            cal.set( year_, month_, 1 );

            // set month title
            SimpleDateFormat df = new SimpleDateFormat("MMMM yyyy");
            MonthLabel.setText( df.format(cal.getTime()) );

            // get day of week of day 1
            int fd = cal.get( Calendar.DAY_OF_WEEK ) - cal.getFirstDayOfWeek();
            if( fd == -1 ) fd = 6;

            // get last day of month
            int ld = cal.getActualMaximum( Calendar.DAY_OF_MONTH );

            // set show public/private flags
            boolean showpub = false;
            boolean showpriv = false;
            String sp = Prefs.getPref(PrefName.SHOWPUBLIC );
            if( sp.equals("true") )
                showpub = true;
            sp = Prefs.getPref(PrefName.SHOWPRIVATE);
            if( sp.equals("true") )
                showpriv = true;

            boolean showDayOfYear = false;
            sp = Prefs.getPref( PrefName.DAYOFYEAR);
            if( sp.equals("true"))
            	showDayOfYear = true;

            // fill in the day boxes that correspond to days in this month
            for( int i = 0; i < 37; i++ ) {
            	MouseListener mls[] = daytext[i].getMouseListeners();
            	daytext[i].setToolTipText(null);
            	for( int mli = 0; mli < mls.length; mli++ )
            	{
            		daytext[i].removeMouseListener(mls[mli]);
            	}
            	mls = days[i].getMouseListeners();
            	for( int mli = 0; mli < mls.length; mli++ )
            	{
            		days[i].removeMouseListener(mls[mli]);
            	}
                int daynumber = i - fd + 1;


                // clear any text in the box from the last displayed month
                StyledDocument doc = daytext[i].getStyledDocument();
                if( doc.getLength() > 0 )
                    doc.remove(0,doc.getLength());

                // if the day box is not needed for the current month, make it invisible
                if( daynumber <= 0 || daynumber > ld ) {
                    // the following line fixes a bug (in Java) where any daytext not
                    // visible in the first month would show its first line of text
                    // in the wrong Style when it became visible in a different month -
                    // using a Style not even set by this program.
                    // once this happened, resizing the window would fix the Style
                    // so it probably is a swing bug
                    addString( daytext[i], "bug fix", "black" );
                    days[i].setVisible(false);
                }
                else {

                    // set value of date button
                    daynum[i].setText(Integer.toString(daynumber));
                    daytext[i].addMouseListener(new DayMouseListener( year_, month_, daynumber ));
                    days[i].addMouseListener(new DayMouseListener( year_, month_, daynumber ));

                    GregorianCalendar gc = new GregorianCalendar(year_,month_,daynumber,23,59);
                    if( showDayOfYear )
                    {
                    	dayOfYear[i].setText(Integer.toString(gc.get(Calendar.DAY_OF_YEAR)));
                    }
                    else
                    {
                    	dayOfYear[i].setText("");
                    }

                    // get appointment info for the day's appointments from the data model
                    Day di = Day.getDay( year_, month_, daynumber, showpub,showpriv,true);
                    Collection appts = di.getAppts();

                    daytext[i].setParagraphAttributes(daytext[i].getStyle("black"),true);
                    if( appts != null ) {
                        Iterator it = appts.iterator();
                        
                        StringBuffer html = new StringBuffer();

                        // iterate through the day's appts
                        while( it.hasNext() ) {

                            Appointment info = (Appointment) it.next();

                            // bsv 2004-12-23
                            boolean bullet = false;
                            Date nt = info.getNextTodo();
                            if( Prefs.getPref(PrefName.UCS_MARKTODO).equals("true")){
                            	if( info.getTodo() && (nt == null || !nt.after(gc.getTime()))){
                            		bullet = true;
                            	    if( Prefs.getPref(PrefName.UCS_MARKER).endsWith(".gif"))
                            	    {
                            	        //daytext[i].insertIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/" + Prefs.getPref(PrefName.UCS_MARKER))));
                            	    	addString( daytext[i], Prefs.getPref(PrefName.UCS_MARKER), "icon" );
                            	    }
                            	    else
                            	    {
                            	        addString( daytext[i], Prefs.getPref(PrefName.UCS_MARKER), info.getColor() );
                            	    }
                            	 }
                            }

                            String color = info.getColor();
                            // strike-through done todos
                            if( info.getTodo() && !(nt == null || !nt.after(gc.getTime())) )
                            {
                                color = "strike";
                            }
                            boolean strike = color.equals("strike");


                            // add the day's text in the right color. If the appt is the last
                            // one - don't add a trailing newline - it will make the text pane
                            // have one extra line - forcing an unecessary scrollbar at times
                            String text = info.getText();
                            
                            if( it.hasNext() )
                            	text += "\n"; // we're doing this to a String
                            
                            addString( daytext[i], text, color );
                            
                            // Compute the tooltip text.
                            if (bullet)
                            	html.append("<b>");
                            if (strike)
                            	html.append("<strike>");
                            else
                            	html.append("<font color=\""+color+"\">");
                            String apptText =
                            	di.getUntruncatedAppointmentFor(info).getText();
                            if (apptText.indexOf('\n') != -1)
                            {
                            	StringBuffer temp = new StringBuffer();
                            	for (int j=0; j<apptText.length(); ++j)
                            	{
                            		char ch = apptText.charAt(j);
                            		if (ch == '\n')
                            			temp.append("<br>");
                            		else
                            			temp.append(ch);
                            	}
                            	apptText = temp.toString();
                            }
                            html.append(apptText);
                            if (bullet)
                            	html.append("</b>");
                            if (strike)
                            	html.append("</strike>");
                            else
                            	html.append("</font>");
                            if (it.hasNext())
                            	html.append("<br>");
                        }
                        if (html.length() != 0)
                        {
                        	daytext[i].setToolTipText("<html>"+html+"</html>");
//                        	System.out.println(i+": "+html);
                        }
                    }


                    // reset the text pane to show the top left of the appt text if the text
                    // scrolls up or right
                    daytext[i].setCaretPosition(0);

                    int xcoord = i%7;
                    int dow = cal.getFirstDayOfWeek() + xcoord;
                    if( dow == 8 ) dow = 1;

                    // set the day color based on if the day is today, or if any of the
                    // appts for the day are holidays, vacation days, half-days, or weekends
                    // bsv 2004-12-21
                    boolean bUseUCS = ((Prefs.getPref(PrefName.UCS_ON)).equals("true"))?true:false;
                    if( bUseUCS==false ){
	                    if( today == daynumber ) {
	                        // today color is pink
	                        daytext[i].setBackground( new Color(225,150,150));
	                        days[i].setBackground( new Color(225,150,150));
	                    }
	                    else if( di.getHoliday() == 1 ) {
	                        // holiday color
	                        daytext[i].setBackground( new Color(245,203,162));
	                        days[i].setBackground( new Color(245,203,162));
	                    }
	                    else if( di.getVacation() == 1 ) {
	                        // vacation color
	                        daytext[i].setBackground( new Color(155,255,153));
	                        days[i].setBackground( new Color(155,255,153));
	                    }
	                    else if( di.getVacation() == 2 ) {
	                        // half day color
	                        daytext[i].setBackground( new Color(200,255,200));
	                        days[i].setBackground( new Color(200,255,200));
	                    }
	                    else if( dow != Calendar.SUNDAY && dow != Calendar.SATURDAY ) {
	                        // weekday color
	                        days[i].setBackground( new Color(255,233,192));
	                        daytext[i].setBackground( new Color(255,233,192));
	                    }
	                    else {
	                        // weekend color
	                        daytext[i].setBackground( new Color(245,203,162));
	                        days[i].setBackground( new Color(245,203,162));
	                    }
                    } else {
                    	Color ctemp = new Color( (new Integer( Prefs.getPref(PrefName.UCS_DEFAULT))).intValue() );
	                    if( today == daynumber ) {
		                    ctemp = new Color( (new Integer( Prefs.getPref(PrefName.UCS_TODAY))).intValue() );
	                        daytext[i].setBackground( ctemp );
	                        days[i].setBackground( ctemp );
	                    }
	                    else if( di.getHoliday() == 1 ) {
		                    ctemp = new Color( (new Integer( Prefs.getPref(PrefName.UCS_HOLIDAY))).intValue() );
	                        daytext[i].setBackground( ctemp );
	                        days[i].setBackground( ctemp );
	                    }
	                    else if( di.getVacation() == 1 ) {
		                    ctemp = new Color( (new Integer( Prefs.getPref(PrefName.UCS_VACATION))).intValue() );
	                        daytext[i].setBackground( ctemp );
	                        days[i].setBackground( ctemp );
	                    }
	                    else if( di.getVacation() == 2 ) {
	                        // half day color
		                    ctemp = new Color( (new Integer( Prefs.getPref(PrefName.UCS_HALFDAY))).intValue() );
	                        daytext[i].setBackground( ctemp );
	                        days[i].setBackground( ctemp );
	                    }
	                    else if( dow != Calendar.SUNDAY && dow != Calendar.SATURDAY ) {
	                        // weekday color
		                    ctemp = new Color( (new Integer( Prefs.getPref(PrefName.UCS_WEEKDAY))).intValue() );
	                        daytext[i].setBackground( ctemp );
	                        days[i].setBackground( ctemp );
	                    }
	                    else {
	                        // weekend color
		                    ctemp = new Color( (new Integer( Prefs.getPref(PrefName.UCS_WEEKEND))).intValue() );
	                        daytext[i].setBackground( ctemp );
	                        days[i].setBackground( ctemp );
	                    }
                    }

                    // (bsv 2004-12-21)
                    days[i].setVisible(true);

                }



            }

            // label the week buttons
            // the buttons have bad names due to my lazy use of NetBeans
            cal.setMinimalDaysInFirstWeek(4);
            cal.set( year_, month_, 1 );
            int wk = cal.get( Calendar.WEEK_OF_YEAR );
            jButton1.setText( Integer.toString(wk));
            cal.set( year_, month_, 8 );
            wk = cal.get( Calendar.WEEK_OF_YEAR );
            jButton2.setText( Integer.toString(wk));
            cal.set( year_, month_, 15 );
            wk = cal.get( Calendar.WEEK_OF_YEAR );
            jButton3.setText( Integer.toString(wk));
            cal.set( year_, month_, 22 );
            wk = cal.get( Calendar.WEEK_OF_YEAR );
            jButton4.setText( Integer.toString(wk));
            cal.set( year_, month_, 29 );
            wk = cal.get( Calendar.WEEK_OF_YEAR );
            jButton5.setText( Integer.toString(wk));


            // update todoPreview Box
            StyledDocument tdoc = todoPreview.getStyledDocument();
            tdoc.remove(0,tdoc.getLength());

            // sort and add the todos
            Vector tds = AppointmentModel.getReference().get_todos();
            if( tds.size() > 0 ) {
                addString( todoPreview, Resource.getResourceString("Todo_Preview") + "\n", "smul" );


                // the treeset will sort by date
                TreeSet ts = new TreeSet(new Comparator() {
                    public int compare(java.lang.Object obj, java.lang.Object obj1) {
                        try {
                            Appointment r1 = (Appointment)obj;
                            Appointment r2 = (Appointment)obj1;
                            Date dt1 = r1.getNextTodo();
                            if( dt1 == null ) {
                                dt1 = r1.getDate();
                            }
                            Date dt2 = r2.getNextTodo();
                            if( dt2 == null ) {
                                dt2 = r2.getDate();
                            }

                            if( dt1.after( dt2 ))
                                return(1);
                            return(-1);
                        }
                        catch( Exception e ) {
                            return(0);
                        }
                    }
                });

                // sort the todos by adding to the TreeSet
                for( int i = 0; i < tds.size(); i++ ) {
                    ts.add( tds.elementAt(i) );
                }

                Iterator it = ts.iterator();
                while( it.hasNext() ) {
                    try {
                        Appointment r = (Appointment) it.next();

                        // !!!!! only show first line of appointment text !!!!!!
                        String tx = "";
                        String xx = r.getText();
                        int ii = xx.indexOf('\n');
                        if( ii != -1 ) {
                            tx = xx.substring(0,ii);
                        }
                        else {
                            tx = xx;
                        }
                        addString( todoPreview, tx + "\n", "sm" );

                    }
                    catch( Exception e)
                    { Errmsg.errmsg(e); }

                }
                todoPreview.setCaretPosition(0);
            }
            else {
                addString( todoPreview, Resource.getResourceString("Todo_Preview") + "\n", "smul" );
                addString( todoPreview, Resource.getResourceString("none_pending"), "sm" );
            }


            // update taskPreview Box
            StyledDocument tkdoc = taskPreview.getStyledDocument();
            tkdoc.remove(0,tkdoc.getLength());

            // sort and add the tasks
            Vector tks = TaskModel.getReference().get_tasks();
            if( tks.size() > 0 ) {
                addString( taskPreview, Resource.getResourceString("Task_Preview") + "\n", "smul" );

                // the treeset will sort by date
                TreeSet ts = new TreeSet(new Comparator() {
                    public int compare(java.lang.Object obj, java.lang.Object obj1) {
                        try {
                            Task r1 = (Task)obj;
                            Task r2 = (Task)obj1;
                            Date dt1 = r1.getDueDate();
                            Date dt2 = r2.getDueDate();
                            if( dt1.after( dt2 ))
                                return(1);
                            return(-1);
                        }
                        catch( Exception e ) {
                            return(0);
                        }
                    }
                });

                // sort the tasks by adding to the treeset
                for( int i = 0; i < tks.size(); i++ ) {
                    ts.add( tks.elementAt(i) );
                }

                Iterator it = ts.iterator();
                while( it.hasNext() ) {
                    try {
                        Task r = (Task) it.next();

                        // !!!!! only show first line of task text !!!!!!
                        String tx = "";
                        String xx = r.getDescription();
                        int ii = xx.indexOf('\n');
                        if( ii != -1 ) {
                            tx = xx.substring(0,ii);
                        }
                        else {
                            tx = xx;
                        }
                        addString( taskPreview, "BT" + r.getTaskNumber() + ":" + tx + "\n", "sm" );

                    }
                    catch( Exception e)
                    { Errmsg.errmsg(e); }

                }
                taskPreview.setCaretPosition(0);
            }
            else {
                addString( taskPreview, Resource.getResourceString("Task_Preview") + "\n", "smul" );
                addString( taskPreview, Resource.getResourceString("none_pending"), "sm" );
            }
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }
        finally
        {
        	// not sure I like this. this window pops in front of the appt list that I was working with
        	//requestFocus();
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        dlgMemoryFilesChooser = new javax.swing.JDialog();
        pnlMainMemFilesChooser = new javax.swing.JPanel();
        lblMemChooser = new javax.swing.JLabel();
        listFiles = new javax.swing.JList();
        pnlButtonsMemFilesChooser = new javax.swing.JPanel();
        bnMemFilesChooserOK = new javax.swing.JButton();
        bnMemFilesChooserCancel = new javax.swing.JButton();
        MonthLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        Next = new javax.swing.JButton();
        Prev = new javax.swing.JButton();
        Today = new javax.swing.JButton();
        Goto = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        ActionMenu = new javax.swing.JMenu();
        TaskTrackMI = new javax.swing.JMenuItem();
        ToDoMenu = new javax.swing.JMenuItem();
        AddressMI = new javax.swing.JMenuItem();
        SearchMI = new javax.swing.JMenuItem();
        PrintMonthMI = new javax.swing.JMenuItem();
        printprev = new javax.swing.JMenuItem();
        syncMI = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        OptionMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        navmenu = new javax.swing.JMenu();
        nextmi = new javax.swing.JMenuItem();
        prevmi = new javax.swing.JMenuItem();
        todaymi = new javax.swing.JMenuItem();
        gotomi = new javax.swing.JMenuItem();
        catmenu = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        impexpMenu = new javax.swing.JMenu();
        impXML = new javax.swing.JMenu();
        importMI = new javax.swing.JMenuItem();
        impurl = new javax.swing.JMenuItem();
        impXMLMem = new javax.swing.JMenuItem();
        expXML = new javax.swing.JMenu();
        exportMI = new javax.swing.JMenuItem();
        expurl = new javax.swing.JMenuItem();
        expXMLMem = new javax.swing.JMenuItem();
        impical = new javax.swing.JMenuItem();
        expical = new javax.swing.JMenuItem();
        viewMem = new javax.swing.JMenuItem();
        helpmenu = new javax.swing.JMenu();
        helpMI = new javax.swing.JMenuItem();
        licsend = new javax.swing.JMenuItem();
        chglog = new javax.swing.JMenuItem();
        AboutMI = new javax.swing.JMenuItem();

        ResourceHelper.setTitle(dlgMemoryFilesChooser, "dlgMemoryFilesChooser");
        dlgMemoryFilesChooser.setModal(true);
        dlgMemoryFilesChooser.setName("dlgMemoryFilesChooser");
        pnlMainMemFilesChooser.setLayout(new java.awt.BorderLayout());

        pnlMainMemFilesChooser.setMinimumSize(new java.awt.Dimension(200, 315));
        pnlMainMemFilesChooser.setPreferredSize(new java.awt.Dimension(200, 315));
        ResourceHelper.setText(lblMemChooser, "lblMemChooser");
        pnlMainMemFilesChooser.add(lblMemChooser, java.awt.BorderLayout.NORTH);

        listFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listFiles.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listFilesValueChanged(evt);
            }
        });

        pnlMainMemFilesChooser.add(listFiles, java.awt.BorderLayout.CENTER);

        dlgMemoryFilesChooser.getContentPane().add(pnlMainMemFilesChooser, java.awt.BorderLayout.CENTER);

        ResourceHelper.setText(bnMemFilesChooserOK, "OK");
        bnMemFilesChooserOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bnMemFilesChooserOKActionPerformed(evt);
            }
        });

        pnlButtonsMemFilesChooser.add(bnMemFilesChooserOK);

        ResourceHelper.setText(bnMemFilesChooserCancel, "Cancel");
        bnMemFilesChooserCancel.setDefaultCapable(false);
        bnMemFilesChooserCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bnMemFilesChooserCancelActionPerformed(evt);
            }
        });

        pnlButtonsMemFilesChooser.add(bnMemFilesChooserCancel);

        dlgMemoryFilesChooser.getContentPane().add(pnlButtonsMemFilesChooser, java.awt.BorderLayout.SOUTH);

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("Borg");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        MonthLabel.setBackground(new java.awt.Color(137, 137, 137));
        MonthLabel.setFont(new java.awt.Font("Dialog", 0, 24));
        MonthLabel.setForeground(new java.awt.Color(51, 0, 51));
        MonthLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ResourceHelper.setText(MonthLabel, "Month");
        MonthLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(MonthLabel, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridLayout(1, 7));

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
        jLabel1.setForeground(MonthLabel.getForeground());
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel2.add(jLabel1);

        jLabel3.setForeground(MonthLabel.getForeground());
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel2.add(jLabel3);

        jLabel4.setForeground(MonthLabel.getForeground());
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel2.add(jLabel4);

        jLabel5.setForeground(MonthLabel.getForeground());
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel2.add(jLabel5);

        jLabel6.setForeground(MonthLabel.getForeground());
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel2.add(jLabel6);

        jLabel7.setForeground(MonthLabel.getForeground());
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel2.add(jLabel7);

        jLabel8.setForeground(MonthLabel.getForeground());
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel2.add(jLabel8);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanel2, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridLayout(0, 7));

        jPanel1.setPreferredSize(new java.awt.Dimension(800, 600));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        Next.setForeground(MonthLabel.getForeground());
        Next.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Forward16.gif")));
        ResourceHelper.setText(Next, "Next__>>");
        Next.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        Next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NextActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(Next, gridBagConstraints);

        Prev.setForeground(MonthLabel.getForeground());
        Prev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Back16.gif")));
        ResourceHelper.setText(Prev, "<<__Prev");
        Prev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrevActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(Prev, gridBagConstraints);

        Today.setForeground(MonthLabel.getForeground());
        Today.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Home16.gif")));
        ResourceHelper.setText(Today, "curmonth");
        Today.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                today(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(Today, gridBagConstraints);

        Goto.setForeground(MonthLabel.getForeground());
        Goto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Undo16.gif")));
        ResourceHelper.setText(Goto, "Go_To");
        Goto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GotoActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(Goto, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridLayout(0, 1));

        jPanel3.setMaximumSize(new java.awt.Dimension(20, 32767));
        jPanel3.setMinimumSize(new java.awt.Dimension(30, 60));
        jPanel3.setPreferredSize(new java.awt.Dimension(30, 60));
        jButton1.setText("00");
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton1.setMaximumSize(new java.awt.Dimension(20, 10));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel3.add(jButton1);

        jButton2.setText("00");
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton2.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton2.setMaximumSize(new java.awt.Dimension(20, 10));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel3.add(jButton2);

        jButton3.setText("00");
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton3.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton3.setMaximumSize(new java.awt.Dimension(20, 10));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jPanel3.add(jButton3);

        jButton4.setText("00");
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton4.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton4.setMaximumSize(new java.awt.Dimension(20, 10));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jPanel3.add(jButton4);

        jButton5.setText("00");
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton5.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton5.setMaximumSize(new java.awt.Dimension(20, 10));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jPanel3.add(jButton5);

        jPanel3.add(jPanel4);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        getContentPane().add(jPanel3, gridBagConstraints);

        menuBar.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.RAISED));
        ActionMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Application16.gif")));
        ResourceHelper.setText(ActionMenu, "Action");
        TaskTrackMI.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Application16.gif")));
        ResourceHelper.setText(TaskTrackMI, "Task_Tracking");
        TaskTrackMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TaskTrackMIActionPerformed(evt);
            }
        });

        ActionMenu.add(TaskTrackMI);

        ToDoMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Properties16.gif")));
        ResourceHelper.setText(ToDoMenu, "To_Do");
        ToDoMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ToDoMenuActionPerformed(evt);
            }
        });

        ActionMenu.add(ToDoMenu);

        AddressMI.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/WebComponent16.gif")));
        ResourceHelper.setText(AddressMI, "Address_Book");
        AddressMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddressMIActionPerformed(evt);
            }
        });

        ActionMenu.add(AddressMI);

        SearchMI.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Find16.gif")));
        ResourceHelper.setText(SearchMI, "srch");
        SearchMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchMIActionPerformed(evt);
            }
        });

        ActionMenu.add(SearchMI);

        PrintMonthMI.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Print16.gif")));
        ResourceHelper.setText(PrintMonthMI, "pmonth");
        PrintMonthMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrintMonthMIActionPerformed(evt);
            }
        });

        ActionMenu.add(PrintMonthMI);

        printprev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/PrintPreview16.gif")));
        ResourceHelper.setText(printprev, "pprev");
        printprev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printprevActionPerformed(evt);
            }
        });

        ActionMenu.add(printprev);

        syncMI.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Refresh16.gif")));
        ResourceHelper.setText(syncMI, "Synchronize");
        syncMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncMIActionPerformed(evt);
            }
        });

        ActionMenu.add(syncMI);

        exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Stop16.gif")));
        ResourceHelper.setText(exitMenuItem, "Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        ActionMenu.add(exitMenuItem);

        menuBar.add(ActionMenu);

        OptionMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Preferences16.gif")));
        ResourceHelper.setText(OptionMenu, "Options");
        ResourceHelper.setText(jMenuItem1, "ep");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });

        OptionMenu.add(jMenuItem1);

        menuBar.add(OptionMenu);

        navmenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Refresh16.gif")));
        ResourceHelper.setText(navmenu, "navmenu");
        ResourceHelper.setText(nextmi, "Next_Month");
        nextmi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NextActionPerformed(evt);
            }
        });

        navmenu.add(nextmi);

        ResourceHelper.setText(prevmi, "Previous_Month");
        prevmi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrevActionPerformed(evt);
            }
        });

        navmenu.add(prevmi);

        ResourceHelper.setText(todaymi, "Today");
        todaymi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                today(evt);
            }
        });

        navmenu.add(todaymi);

        ResourceHelper.setText(gotomi, "Goto");
        gotomi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GotoActionPerformed(evt);
            }
        });

        navmenu.add(gotomi);

        menuBar.add(navmenu);

        catmenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Preferences16.gif")));
        ResourceHelper.setText(catmenu, "Categories");
        jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Preferences16.gif")));
        ResourceHelper.setText(jMenuItem2, "choosecat");
        jMenuItem2.setActionCommand("Choose Displayed Categories");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });

        catmenu.add(jMenuItem2);

        jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Add16.gif")));
        ResourceHelper.setText(jMenuItem3, "addcat");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });

        catmenu.add(jMenuItem3);

        jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Delete16.gif")));
        ResourceHelper.setText(jMenuItem4, "remcat");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });

        catmenu.add(jMenuItem4);

        menuBar.add(catmenu);

        impexpMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Export16.gif")));
        ResourceHelper.setText(impexpMenu, "impexpMenu");
        impXML.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Import16.gif")));
        ResourceHelper.setText(impXML, "impXML");
        ResourceHelper.setText(importMI, "impmenu");
        importMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importMIActionPerformed(evt);
            }
        });

        impXML.add(importMI);

        ResourceHelper.setText(impurl, "impurl");
        impurl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                impurlActionPerformed(evt);
            }
        });

        impXML.add(impurl);

        ResourceHelper.setText(impXMLMem, "impXMLMem");
        impXMLMem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                impXMLMemActionPerformed(evt);
            }
        });

        impXML.add(impXMLMem);

        impexpMenu.add(impXML);

        expXML.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Export16.gif")));
        ResourceHelper.setText(expXML, "expXML");
        ResourceHelper.setText(exportMI, "expmenu");
        exportMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportMIActionPerformed(evt);
            }
        });

        expXML.add(exportMI);

        ResourceHelper.setText(expurl, "expurl");
        expurl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expurlActionPerformed(evt);
            }
        });

        expXML.add(expurl);

        ResourceHelper.setText(expXMLMem, "expXMLMem");
        expXMLMem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expXMLMemActionPerformed(evt);
            }
        });

        expXML.add(expXMLMem);

        impexpMenu.add(expXML);

        impical.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Import16.gif")));
        ResourceHelper.setText(impical, "importical");
        impical.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                impicalActionPerformed(evt);
            }
        });

        impexpMenu.add(impical);

        expical.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Export16.gif")));
        ResourceHelper.setText(expical, "export_ical");
        expical.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expicalActionPerformed(evt);
            }
        });

        impexpMenu.add(expical);

        ResourceHelper.setText(viewMem, "viewMem");
        viewMem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewMemActionPerformed(evt);
            }
        });

        impexpMenu.add(viewMem);

        menuBar.add(impexpMenu);

        helpmenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Help16.gif")));
        ResourceHelper.setText(helpmenu, "Help");
        ResourceHelper.setText(helpMI, "Help");
        helpMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMIActionPerformed(evt);
            }
        });

        helpmenu.add(helpMI);

        ResourceHelper.setText(licsend, "License");
        licsend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                licsendActionPerformed(evt);
            }
        });

        helpmenu.add(licsend);

        ResourceHelper.setText(chglog, "viewchglog");
        chglog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chglogActionPerformed(evt);
            }
        });

        helpmenu.add(chglog);

        ResourceHelper.setText(AboutMI, "About");
        AboutMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AboutMIActionPerformed(evt);
            }
        });

        helpmenu.add(AboutMI);

        menuBar.add(helpmenu);

        setJMenuBar(menuBar);

        this.setContentPane(getJPanel());
        catmenu.add(getDelcatMI());
        impexpMenu.add(getExpvcal());
    }//GEN-END:initComponents

    private void impicalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_impicalActionPerformed
        File file;
        while( true ) {
            // prompt for a file
            JFileChooser chooser = new JFileChooser();

            chooser.setCurrentDirectory( new File(".") );
            chooser.setDialogTitle(Resource.getResourceString("choose_file"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int returnVal = chooser.showOpenDialog(null);
            if(returnVal != JFileChooser.APPROVE_OPTION)
                return;

            String s = chooser.getSelectedFile().getAbsolutePath();
            file = new File(s);
            String err = null;

            if( err == null )
                break;

            Errmsg.notice( err );
        }
        try {
        	CategoryModel catmod = CategoryModel.getReference();
        	Collection allcats = catmod.getCategories();
        	Object[] cats = allcats.toArray();

        	Object o = JOptionPane.showInputDialog(null,Resource.getResourceString("import_cat_choose"),
        			"",JOptionPane.QUESTION_MESSAGE,null,cats,cats[0]);
        	if( o == null)
        		return;

        	String warnings = AppointmentIcalAdapter.importIcal(file.getAbsolutePath(), (String)o);

        	if( warnings != null)
        		Errmsg.notice(warnings);
        }
    	catch (Exception e) {
			Errmsg.errmsg(e);
		}
		catch ( NoClassDefFoundError ne )
		{
			Errmsg.notice("Cannot find ICal library, import disabled");
		}

    }//GEN-LAST:event_impicalActionPerformed

    private void expicalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expicalActionPerformed
        // user wants to export the task and calendar DBs to an iCal file
        File file;
        while( true ) {
            // prompt for a file
            JFileChooser chooser = new JFileChooser();

            chooser.setCurrentDirectory( new File(".") );
            chooser.setDialogTitle(Resource.getResourceString("choose_file"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int returnVal = chooser.showOpenDialog(null);
            if(returnVal != JFileChooser.APPROVE_OPTION)
                return;

            String s = chooser.getSelectedFile().getAbsolutePath();
            file = new File(s);
            String err = null;

            if( err == null )
                break;

            Errmsg.notice( err );
        }

    	try {
			AppointmentIcalAdapter.exportIcal(file.getAbsolutePath());
		}
    	catch (Exception e) {
			Errmsg.errmsg(e);
		}
		catch ( NoClassDefFoundError ne )
		{
			Errmsg.notice("Cannot find ICal library, export disabled");
		}


    }//GEN-LAST:event_expicalActionPerformed

    private void listFilesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listFilesValueChanged
        bnMemFilesChooserOK.setEnabled(listFiles.getSelectedIndex() != -1);
    }//GEN-LAST:event_listFilesValueChanged

    private void bnMemFilesChooserCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bnMemFilesChooserCancelActionPerformed
    	memFile = null;
        dlgMemoryFilesChooser.setVisible(false);
    }//GEN-LAST:event_bnMemFilesChooserCancelActionPerformed

    private void bnMemFilesChooserOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bnMemFilesChooserOKActionPerformed
		memFile = (String) listFiles.getSelectedValue();
		dlgMemoryFilesChooser.setVisible(false);
    }//GEN-LAST:event_bnMemFilesChooserOKActionPerformed

    private void viewMemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewMemActionPerformed
		dlgMemFiles.setMemento(IOHelper.getMemFilesMemento());
		dlgMemFiles.setVisible(true);
    }//GEN-LAST:event_viewMemActionPerformed

    private void impXMLMemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_impXMLMemActionPerformed
		try{
			// Argh! This ugliness is because the dialog is not
			// a subclass. Either you can't do this with NetBeans
			// or I haven't figured it out....
			bnMemFilesChooserOK.setEnabled(false);
			String[] files = IOHelper.getMemFilesList();
			DefaultListModel model = (DefaultListModel) listFiles.getModel();
			model.removeAllElements();
			for (int i=0; i<files.length; ++i)
			{
				model.addElement(files[i]);
			}
			dlgMemoryFilesChooser.setVisible(true);
			if (memFile != null)
			{
				impURLCommon(memFile, IOHelper.openStream(memFile));
			}
		}
		catch( Exception e) {
			Errmsg.errmsg(e);
		}
    }//GEN-LAST:event_impXMLMemActionPerformed

    private void expXMLMemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expXMLMemActionPerformed
		try {
			expURLCommon("mem:");
			JOptionPane.showMessageDialog(
				this,
				Resource.getResourceString("expXMLMemConfirmation"),
				Resource.getResourceString("expXMLMemTitle"),
				JOptionPane.INFORMATION_MESSAGE);
		 }
		 catch( Exception e) {
			 Errmsg.errmsg(e);
		 }
    }//GEN-LAST:event_expXMLMemActionPerformed

    private void expurlActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_expurlActionPerformed
    {//GEN-HEADEREND:event_expurlActionPerformed
       try {
            String prevurl = Prefs.getPref(PrefName.LASTEXPURL);
            String urlst = JOptionPane.showInputDialog(Resource.getResourceString("enturl"), prevurl);
            if( urlst == null || urlst.equals("") ) return;
            Prefs.putPref(PrefName.LASTEXPURL, urlst);
            expURLCommon(urlst);
        }
        catch( Exception e) {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_expurlActionPerformed

	private void impurlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_impurlActionPerformed
        try{
            String prevurl = Prefs.getPref(PrefName.LASTIMPURL);
            String urlst = JOptionPane.showInputDialog(Resource.getResourceString("enturl"), prevurl);
            if( urlst == null || urlst.equals("") ) return;

            Prefs.putPref(PrefName.LASTIMPURL, urlst);
            URL url = new URL(urlst);
            impURLCommon(urlst, IOHelper.openStream(url));
        }
        catch( Exception e) {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_impurlActionPerformed

    private void syncMIActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_syncMIActionPerformed
    {//GEN-HEADEREND:event_syncMIActionPerformed
        try {
            AppointmentModel.getReference().sync();
            AddressModel.getReference().sync();
            TaskModel.getReference().sync();
        }
        catch( Exception e) {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_syncMIActionPerformed

    private void chglogActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_chglogActionPerformed
    {//GEN-HEADEREND:event_chglogActionPerformed
        new HelpScreen("/resource/CHANGES.txt").setVisible(true);
    }//GEN-LAST:event_chglogActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // add new category

        String inputValue = JOptionPane.showInputDialog(Resource.getResourceString("AddCat"));
        if( inputValue == null || inputValue.equals("") ) return;
        try{
            CategoryModel.getReference().addCategory(inputValue);
            CategoryModel.getReference().showCategory(inputValue);
        }
        catch( Exception e) {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        try{
            CategoryModel.getReference().syncCategories();
        }
        catch( Exception e) {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        CategoryChooser.getReference().setVisible(true);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void AddressMIActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_AddressMIActionPerformed
    {//GEN-HEADEREND:event_AddressMIActionPerformed
        AddrListView ab = AddrListView.getReference();
        ab.refresh();
        ab.setVisible(true);
    }//GEN-LAST:event_AddressMIActionPerformed

    private void impCommon( XTree xt ) throws Exception {
        String type = xt.name();
        if( !type.equals("TASKS" ) && !type.equals("APPTS") && !type.equals("ADDRESSES"))
            throw new Exception(Resource.getResourceString("Could_not_determine_if_the_import_file_was_for_TASKS,_APPTS,_or_ADDRESSES,_check_the_XML") );

        int ret = JOptionPane.showConfirmDialog(null, Resource.getResourceString("Importing_") + type + ", OK?", Resource.getResourceString("Import_WARNING"), JOptionPane.OK_CANCEL_OPTION);

        if( ret != JOptionPane.OK_OPTION )
            return;

        if( type.equals("TASKS" ) ) {
            TaskModel taskmod = TaskModel.getReference();
            taskmod.importXml(xt);
        }
        else if( type.equals("APPTS") ) {
            AppointmentModel calmod = AppointmentModel.getReference();
            calmod.importXml(xt);
        }
        else {
            AddressModel addrmod = AddressModel.getReference();
            addrmod.importXml(xt);
        }
    }

	private void impURLCommon(String url, InputStream istr) throws Exception
	{
		XTree xt = XTree.readFromStream(istr);
		if( xt == null )
			throw new Exception( Resource.getResourceString("Could_not_parse_") + url );
		//System.out.println(xt.toString());
		impCommon(xt);
	}

    private void expURLCommon(String url) throws Exception
    {
		OutputStream fos = createOutputStreamFromURL(url + "/borg.xml");
		Writer fw = new OutputStreamWriter(fos, "UTF8");
		AppointmentModel.getReference().export(fw);
		fw.close();

		fos = createOutputStreamFromURL(url + "/mrdb.xml");
		fw = new OutputStreamWriter(fos, "UTF8");
		TaskModel.getReference().export(fw);
		fw.close();

		fos = createOutputStreamFromURL(url + "/addr.xml");
		fw = new OutputStreamWriter(fos, "UTF8");
		AddressModel.getReference().export(fw);
		fw.close();
    }

    private void importMIActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_importMIActionPerformed
    {//GEN-HEADEREND:event_importMIActionPerformed
        try {
            //String msg = Resource.getResourceString("This_will_import_tasks,_addresses_or_appointments_into_an_**EMPTY**_database...continue?");
            //int ret = JOptionPane.showConfirmDialog(null, msg, Resource.getResourceString("Import_WARNING"), JOptionPane.OK_CANCEL_OPTION);

            //if( ret != JOptionPane.OK_OPTION )
            //return;
			InputStream istr =
				OSServicesHome
					.getInstance()
					.getServices()
					.fileOpen
					(
						".",
						Resource
							.getResourceString("Please_choose_File_to_Import_From")
					);

			if (istr == null)
				return;

            // parse xml file
            XTree xt = XTree.readFromStream( istr );
            istr.close();
            if( xt == null )
            	throw new Exception( Resource.getResourceString("Could_not_parse_") + "XML");

            impCommon(xt);
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_importMIActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton5ActionPerformed
    {//GEN-HEADEREND:event_jButton5ActionPerformed
        new WeekView( month_, year_, 29);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton4ActionPerformed
    {//GEN-HEADEREND:event_jButton4ActionPerformed
        new WeekView( month_, year_, 22);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton3ActionPerformed
    {//GEN-HEADEREND:event_jButton3ActionPerformed
        new WeekView( month_, year_, 15);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton2ActionPerformed
    {//GEN-HEADEREND:event_jButton2ActionPerformed
        new WeekView( month_, year_, 8);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
        new WeekView( month_, year_, 1);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void printprevActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_printprevActionPerformed
    {//GEN-HEADEREND:event_printprevActionPerformed
        new MonthPreView( month_, year_);
    }//GEN-LAST:event_printprevActionPerformed

    private void exportMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMIActionPerformed

        // user wants to export the task and calendar DBs to an XML file
        File dir;
        while( true ) {
            // prompt for a directory to store the files
            JFileChooser chooser = new JFileChooser();

            chooser.setCurrentDirectory( new File(".") );
            chooser.setDialogTitle(Resource.getResourceString("Please_choose_directory_to_place_XML_files"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setApproveButtonText(Resource.getResourceString("select_export_dir"));

            int returnVal = chooser.showOpenDialog(null);
            if(returnVal != JFileChooser.APPROVE_OPTION)
                return;

            String s = chooser.getSelectedFile().getAbsolutePath();
            dir = new File(s);
            String err = null;
            if( !dir.exists() ) {
                err = Resource.getResourceString("Directory_[") + s + Resource.getResourceString("]_does_not_exist");
            }
            else if( !dir.isDirectory() ) {
                err = "[" + s + Resource.getResourceString("]_is_not_a_directory");
            }
            else if( !dir.canWrite() ) {
                err = Resource.getResourceString("Directory_[") + s + Resource.getResourceString("]_is_not_writable");
            }

            if( err == null )
                break;

            Errmsg.notice( err );
        }

        try {

        	JOptionPane.showMessageDialog(null, Resource.getResourceString("export_notice") + dir.getAbsolutePath() );

        	String fname = dir.getAbsolutePath() + "/borg.xml";
        	if( OverwriteConfirm.checkOverwrite(fname))
        	{
        		OutputStream ostr = IOHelper.createOutputStream(fname);
        		Writer fw = new OutputStreamWriter(ostr, "UTF8");
        		AppointmentModel.getReference().export(fw);
        		fw.close();
        	}

        	fname = dir.getAbsolutePath() + "/mrdb.xml";
        	if( OverwriteConfirm.checkOverwrite(fname))
        	{
        		OutputStream ostr = IOHelper.createOutputStream(fname);
        		Writer fw = new OutputStreamWriter(ostr, "UTF8");
        		TaskModel.getReference().export(fw);
        		fw.close();
        	}

        	fname = dir.getAbsolutePath() + "/addr.xml";
        	if( OverwriteConfirm.checkOverwrite(fname))
        	{
        		OutputStream ostr = IOHelper.createOutputStream(fname);
        		Writer fw = new OutputStreamWriter(ostr, "UTF8");
        		AddressModel.getReference().export(fw);
        		fw.close();
        	}
        }
        catch( Exception e) {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_exportMIActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // bring up the options window
        OptionsView.getReference().setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void licsendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_licsendActionPerformed
        // show the open source license
        new HelpScreen("/resource/license.htm").setVisible(true);
    }//GEN-LAST:event_licsendActionPerformed

    private void helpMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMIActionPerformed
        // show the help page
        //new HelpScreen("/resource/help.htm").show();
    	try{
    		HelpProxy.launchHelp();
    	}
    	catch( Exception e)
		{
    		Errmsg.errmsg(e);
		}
    }//GEN-LAST:event_helpMIActionPerformed


    private void TaskTrackMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TaskTrackMIActionPerformed
        TaskListView bt_ = TaskListView.getReference( );
        bt_.refresh();
        bt_.setVisible(true);
    }//GEN-LAST:event_TaskTrackMIActionPerformed

    private void AboutMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AboutMIActionPerformed

        // show the About data
        String build_info = "";
        String version = "";
        try {
            // get the version and build info from a properties file in the jar file
            InputStream is = getClass().getResource("/properties").openStream();
            Properties props = new Properties();
            props.load(is);
            is.close();
            version = props.getProperty("borg.version");
            build_info = Resource.getResourceString("Build_Number:_") + props.getProperty("build.number") + Resource.getResourceString("Build_Time:_") + props.getProperty("build.time") + "\n";


        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }

        // build and show the version info.
         
        String info = Resource.getResourceString("Berger-Organizer_v") + version + "\n" +
			Resource.getResourceString("developers") + "\n" +
			Resource.getResourceString("contrib") + "\n\n" +
			"http://borg-calendar.sourceforge.net" + "\n\n" +
			Resource.getResourceString("translations") + "\n\n" +
			build_info;
        Object opts[] =
        {Resource.getPlainResourceString("Dismiss") /*,
         Resource.getResourceString("Show_Detailed_Source_Version_Info")*/ };
        JOptionPane.showOptionDialog(null, info, Resource.getResourceString("About_BORG"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(getClass().getResource("/resource/borg.jpg")), opts, opts[0]);



    }//GEN-LAST:event_AboutMIActionPerformed

    private void SearchMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchMIActionPerformed
        // user wants to do a search, so prompt for search string and request search results

    	new SearchView().setVisible(true);
        //String inputValue = JOptionPane.showInputDialog(Resource.getResourceString("Enter_search_string:"));
        ////if( inputValue == null ) return;
        // bring up srch window
        //SearchView sg = new SearchView(inputValue );
        //sg.show();

    }//GEN-LAST:event_SearchMIActionPerformed

    private void PrintMonthMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrintMonthMIActionPerformed

        // print the current month
        try {
            MonthPreView.printMonth( month_, year_);
        }
        catch( Exception e )
        { Errmsg.errmsg(e); }

    }//GEN-LAST:event_PrintMonthMIActionPerformed



    private void ToDoMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ToDoMenuActionPerformed
        // ask borg class to bring up the todo window
        try {
            TodoView.getReference().setVisible(true);
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_ToDoMenuActionPerformed

    private void GotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GotoActionPerformed

        // GOTO a particular month

        // prompt for month/year
        String in = JOptionPane.showInputDialog(Resource.getResourceString("Enter_a_Date_(mm/yyyy)"));
        if( in == null ) return;

        int index = in.indexOf('/');
        if( index == -1 ) return;

        // parse out MM/YYYY
        String mo = in.substring(0,index);
        String yr = in.substring(index+1);
        try {
            // just set the member month_ and year_ vars and call refresh to update the view
            month_ = Integer.parseInt(mo)-1;
            year_ = Integer.parseInt(yr);
            refresh();
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }


    }//GEN-LAST:event_GotoActionPerformed


    private void today(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_today
        try {
            // set view back to month containing today
            today();
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_today

    private void PrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrevActionPerformed
        // go to previous month - decrement month/year and call refresh of view

        if( month_ == 0 ) {
            month_ = 11;
            year_--;
        }
        else {
            month_--;
        }
        try {
            refresh();
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }// Add your handling code here:
    }//GEN-LAST:event_PrevActionPerformed

    private void NextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NextActionPerformed
        // go to next month - increment month/year and call refresh of view
        if( month_ == 11 ) {
            month_ = 0;
            year_++;
        }
        else {
            month_++;
        }
        try {
            refresh();
        }
        catch( Exception e ) {
            Errmsg.errmsg(e);
        }
    }//GEN-LAST:event_NextActionPerformed

    private void exitMenuItemActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed

        if(  AppHelper.isApplet() ) {
            this.dispose();
        }
        else {
			exit();
        }

    }//GEN-LAST:event_exitMenuItemActionPerformed


    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        if( trayIcon_ || AppHelper.isApplet() ) {
            this.dispose();
        }
        else {
			exit();
        }

    }//GEN-LAST:event_exitForm





    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem AboutMI;
    private javax.swing.JMenu ActionMenu;
    private javax.swing.JMenuItem AddressMI;
    private javax.swing.JButton Goto;
    private javax.swing.JLabel MonthLabel;
    private javax.swing.JButton Next;
    private javax.swing.JMenu OptionMenu;
    private javax.swing.JButton Prev;
    private javax.swing.JMenuItem PrintMonthMI;
    private javax.swing.JMenuItem SearchMI;
    private javax.swing.JMenuItem TaskTrackMI;
    private javax.swing.JMenuItem ToDoMenu;
    private javax.swing.JButton Today;
    private javax.swing.JButton bnMemFilesChooserCancel;
    private javax.swing.JButton bnMemFilesChooserOK;
    private javax.swing.JMenu catmenu;
    private javax.swing.JMenuItem chglog;
    private javax.swing.JDialog dlgMemoryFilesChooser;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu expXML;
    private javax.swing.JMenuItem expXMLMem;
    private javax.swing.JMenuItem expical;
    private javax.swing.JMenuItem exportMI;
    private javax.swing.JMenuItem expurl;
    private javax.swing.JMenuItem gotomi;
    private javax.swing.JMenuItem helpMI;
    private javax.swing.JMenu helpmenu;
    private javax.swing.JMenu impXML;
    private javax.swing.JMenuItem impXMLMem;
    private javax.swing.JMenu impexpMenu;
    private javax.swing.JMenuItem impical;
    private javax.swing.JMenuItem importMI;
    private javax.swing.JMenuItem impurl;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel lblMemChooser;
    private javax.swing.JMenuItem licsend;
    private javax.swing.JList listFiles;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu navmenu;
    private javax.swing.JMenuItem nextmi;
    private javax.swing.JPanel pnlButtonsMemFilesChooser;
    private javax.swing.JPanel pnlMainMemFilesChooser;
    private javax.swing.JMenuItem prevmi;
    private javax.swing.JMenuItem printprev;
    private javax.swing.JMenuItem syncMI;
    private javax.swing.JMenuItem todaymi;
    private javax.swing.JMenuItem viewMem;
    // End of variables declaration//GEN-END:variables


    /** array of day panels
     */
    private JPanel days[];
    private JTextPane daytext[];
    
    /** date buttons
     */
    private JButton daynum[];

    private JLabel dayOfYear[];

    private JTextPane todoPreview;
    private JTextPane taskPreview;

	private JPanel jPanel = null;
	private JPanel jPanel5 = null;
	private JMenuItem expvcal = null;
	private JMenuItem delcatMI = null;
    // called when a Prefs change notification is sent out
    // to all Prefs.Listeners
	public void prefsChanged() {
		//System.out.println("pr called");
		SwingUtilities.updateComponentTreeUI(this);
		updStyles();
        setDayLabels();
        refresh();
	}

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints60 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints59 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints58 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints57 = new GridBagConstraints();
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			gridBagConstraints57.gridx = 0;
			gridBagConstraints57.gridy = 0;
			gridBagConstraints57.insets = new java.awt.Insets(4,4,4,4);
			gridBagConstraints57.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints58.gridx = 0;
			gridBagConstraints58.gridy = 2;
			gridBagConstraints58.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints58.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints58.weightx = 1.0D;
			gridBagConstraints58.weighty = 1.0D;
			gridBagConstraints59.gridx = 1;
			gridBagConstraints59.gridy = 2;
			gridBagConstraints59.fill = java.awt.GridBagConstraints.VERTICAL;
			gridBagConstraints60.gridx = 0;
			gridBagConstraints60.gridy = 1;
			gridBagConstraints60.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints61.gridx = 0;
			gridBagConstraints61.gridy = 3;
			gridBagConstraints61.fill = java.awt.GridBagConstraints.BOTH;
			jPanel.add(MonthLabel, gridBagConstraints57);
			jPanel.add(jPanel1, gridBagConstraints58);
			jPanel.add(jPanel3, gridBagConstraints59);
			jPanel.add(jPanel2, gridBagConstraints60);
			jPanel.add(getJPanel5(), gridBagConstraints61);
		}
		return jPanel;
	}
	/**
	 * This method initializes jPanel5
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel5() {
		if (jPanel5 == null) {
			GridLayout gridLayout62 = new GridLayout();
			jPanel5 = new JPanel();
			jPanel5.setLayout(gridLayout62);
			gridLayout62.setRows(1);
			jPanel5.add(Prev, null);
			jPanel5.add(Today, null);
			jPanel5.add(Goto, null);
			jPanel5.add(Next, null);
		}
		return jPanel5;
	}
	/**
	 * This method initializes expvcal
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getExpvcal() {
		if (expvcal == null) {
			expvcal = new JMenuItem();
			ResourceHelper.setText(expvcal, "exp_vcal");
			expvcal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Export16.gif")));
			expvcal.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
			        File file;
			        while( true ) {
			            // prompt for a file
			            JFileChooser chooser = new JFileChooser();

			            chooser.setCurrentDirectory( new File(".") );
			            chooser.setDialogTitle(Resource.getResourceString("choose_file"));
			            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			            int returnVal = chooser.showOpenDialog(null);
			            if(returnVal != JFileChooser.APPROVE_OPTION)
			                return;

			            String s = chooser.getSelectedFile().getAbsolutePath();
			            file = new File(s);
			            String err = null;

			            if( err == null )
			                break;

			            Errmsg.notice( err );
			        }

			    	try {
			    		FileWriter w = new FileWriter(file);
						AppointmentVcalAdapter.exportVcal(w);
						w.close();
					}
			    	catch (Exception ex) {
						Errmsg.errmsg(ex);
					}

				}

			});
		}
		return expvcal;
	}
	/**
	 * This method initializes delcatMI
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getDelcatMI() {
		if (delcatMI == null) {
			delcatMI = new JMenuItem();
			ResourceHelper.setText(delcatMI, "delete_cat");
			delcatMI.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Delete16.gif")));
			delcatMI.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {

					try{
						CategoryModel catmod = CategoryModel.getReference();
						Collection allcats = catmod.getCategories();
						allcats.remove(CategoryModel.UNCATEGORIZED);
						if( allcats.isEmpty())
							return;
						Object[] cats = allcats.toArray();

						Object o = JOptionPane.showInputDialog(null,Resource.getResourceString("delete_cat_choose"),
								"",JOptionPane.QUESTION_MESSAGE,null,cats,cats[0]);
						if( o == null)
							return;

						int ret = JOptionPane.showConfirmDialog(null, Resource.getResourceString("delcat_warn") +
								" [" + (String)o + "]!",
								"", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
						if (ret == JOptionPane.OK_OPTION) {


							// appts
							Iterator itr = AppointmentModel.getReference().getAllAppts().iterator();
							while( itr.hasNext() )
							{
								Appointment ap = (Appointment) itr.next();
								String cat = ap.getCategory();
								if( cat != null && cat.equals(o) )
									AppointmentModel.getReference().delAppt(ap);
							}

							// tasks
							itr = TaskModel.getReference().getTasks().iterator();
							while( itr.hasNext() ) {
								Task t = (Task) itr.next();
								String cat = t.getCategory();
								if( cat != null && cat.equals(o) )
									TaskModel.getReference().delete(t.getKey());
							}


					        try{
					            CategoryModel.getReference().syncCategories();
					        }
					        catch( Exception ex) {
					            Errmsg.errmsg(ex);
					        }
						}
					}
					catch( Exception ex )
					{
						Errmsg.errmsg(ex);
					}
				}
			});
		}
		return delcatMI;
	}
  }
