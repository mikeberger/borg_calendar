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
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import net.sf.borg.common.util.Errmsg;
import net.sf.borg.common.util.PrefName;
import net.sf.borg.common.util.Prefs;
import net.sf.borg.common.util.Resource;
import net.sf.borg.control.Borg;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Day;
import net.sf.borg.model.Task;
import net.sf.borg.model.TaskModel;

// This is the month view GUI
// it is the main borg window
// like most of the other borg window, you really need to check the netbeans
// form
// editor to get the graphical picture of the whole window

public class CalendarView extends View implements Prefs.Listener, Navigator {

	// current year/month being viewed
	private int year_;

	private int month_;

	private boolean trayIcon_;

	static final private int NUM_DAY_BOXES = 40;

	private static CalendarView singleton = null;

	public static CalendarView getReference(boolean trayIcon) {
		if (singleton == null || !singleton.isShowing())
			singleton = new CalendarView(trayIcon);
		return (singleton);
	}

	public static CalendarView getReference() {
		return (singleton);
	}

	public int getMonth() {
		return (month_);
	}

	public int getYear() {
		return (year_);
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
		
		getLayeredPane()
		.registerKeyboardAction
		(
			new ActionListener()
			{
				public final void actionPerformed(ActionEvent e) {
					exitForm(null);
				}
			},
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW
		);
	}

	private static class DayMouseListener implements MouseListener {
		int year;

		int month;

		int date;

		boolean isDateButton_;

		public DayMouseListener(int year, int month, int date,
				boolean isDateButton) {
			this.date = date;
			this.year = year;
			this.month = month;
			this.isDateButton_ = isDateButton;
		}

		public void mouseClicked(MouseEvent evt) {
			String reverseActions = Prefs.getPref(PrefName.REVERSEDAYEDIT);
			boolean reverse = reverseActions.equals("true");
			if ((isDateButton_ && !reverse) || (!isDateButton_ && reverse)) {
				// start the appt editor view
				DayView dv = new DayView(month, year, date);
				dv.setVisible(true);
			} else {
				AppointmentListView ag = new AppointmentListView(year, month,
						date);
				ag.setVisible(true);

			}

		}

		public void mouseEntered(MouseEvent arg0) {
		}

		public void mouseExited(MouseEvent arg0) {
		}

		public void mousePressed(MouseEvent arg0) {
		}

		public void mouseReleased(MouseEvent arg0) {
		}
	}

	private void init() {

		initComponents();

		GridBagConstraints cons;

		// the day boxes - which will contain a date button and day text
		days = new JPanel[NUM_DAY_BOXES];

		// the day text areas
		daytext = new JTextPane[NUM_DAY_BOXES];

		// the date buttons
		daynum = new JButton[NUM_DAY_BOXES];
		dayOfYear = new JLabel[NUM_DAY_BOXES];

		// initialize the days
		for (int i = 0; i < NUM_DAY_BOXES; i++) {

			// allocate a panel for each day
			// and add a date button and non wrapping text pane
			// in each
			days[i] = new JPanel();
			days[i].setLayout(new GridBagLayout());
			// as per the experts, this subclass of JTextPane is the only way to
			// stop word-wrap
			JTextPane jep = null;
			String wrap = Prefs.getPref(PrefName.WRAP);
			if (wrap.equals("true")) {
				jep = new JTextPane();
			} else {
				jep = new JTextPane() {
					public boolean getScrollableTracksViewportWidth() {
						return false;
					}

					public void setSize(Dimension d) {
						if (d.width < getParent().getSize().width) {
							d.width = getParent().getSize().width;
						}
						super.setSize(d);
					}
				};
			}
			daytext[i] = jep;
			daytext[i].setEditable(false);
			daynum[i] = new JButton("N");

			dayOfYear[i] = new JLabel();

			// continue laying out the day panel. want the date button in upper
			// right
			// and want the text pane top to be lower than the bottom of the
			// button.
			Insets is = new Insets(1, 4, 1, 4);
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
			sp
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			sp
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			sp.setBorder(new EmptyBorder(0, 0, 0, 0));
			sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
			sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
			days[i].add(sp, cons);

			jPanel1.add(days[i]);
		}

		// add filler to the Grid
		// jPanel1.add(new JPanel());
		// jPanel1.add(new JPanel());
		// jPanel1.add( new JPanel() );

		setDayLabels();

		//
		// ToDo PREVIEW BOX
		//
		todoPreview = new JTextPane() {
			public boolean getScrollableTracksViewportWidth() {
				return false;
			}

			public void setSize(Dimension d) {
				if (d.width < getParent().getSize().width) {
					d.width = getParent().getSize().width;
				}
				super.setSize(d);
			}
		};
		todoPreview.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent evt) {
				TodoView.getReference().setVisible(true);
			}

			public void mouseEntered(MouseEvent arg0) {
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mousePressed(MouseEvent arg0) {
			}

			public void mouseReleased(MouseEvent arg0) {
			}
		});
		todoPreview.setBackground(new Color(204, 204, 204));
		todoPreview.setEditable(false);
		JScrollPane sp = new JScrollPane();
		sp.setViewportView(todoPreview);
		sp
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		// sp.setBorder( new javax.swing.border.EmptyBorder(0,0,0,0) );
		sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 5));
		sp.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0));
		jPanel1.add(sp);

		//
		// TASK PREVIEW BOX
		//
		taskPreview = new JTextPane() {
			public boolean getScrollableTracksViewportWidth() {
				return false;
			}

			public void setSize(Dimension d) {
				if (d.width < getParent().getSize().width) {
					d.width = getParent().getSize().width;
				}
				super.setSize(d);
			}
		};
		taskPreview.setBackground(new Color(204, 204, 204));
		taskPreview.setEditable(false);
		taskPreview.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent evt) {
				TaskListView v = TaskListView.getReference();
				v.refresh();
				v.setVisible(true);
			}

			public void mouseEntered(MouseEvent arg0) {
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mousePressed(MouseEvent arg0) {
			}

			public void mouseReleased(MouseEvent arg0) {
			}
		});
		sp = new JScrollPane();
		sp.setViewportView(taskPreview);
		sp
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		// sp.setBorder( new javax.swing.border.EmptyBorder(0,0,0,0) );
		sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 5));
		sp.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0));
		jPanel1.add(sp);

		// update the styles used in the appointment text panes for the various
		// appt text
		// colors, based on the current font size set by the user
		updStyles();

		// init view to current month
		try {
			today();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		// show the window
		pack();
		setVisible(true);

	}

	public void destroy() {
		this.dispose();
	}

	/* set borg to current month and refresh the screen */
	public void today() {
		GregorianCalendar cal = new GregorianCalendar();
		month_ = cal.get(Calendar.MONTH);
		year_ = cal.get(Calendar.YEAR);
		refresh();
	}

	public void goTo(Calendar cal) {
		month_ = cal.get(Calendar.MONTH);
		year_ = cal.get(Calendar.YEAR);
		refresh();
	}

	// initialize the various text styles used for appointment
	// text for a single text pane
	private void initStyles(JTextPane textPane, Style def, Font font) {
		// Initialize some styles.
		Style bl = textPane.addStyle("black", def);
		int fontsize = font.getSize();
		String family = font.getFamily();
		boolean bold = font.isBold();
		boolean italic = font.isItalic();

		StyleConstants.setFontFamily(bl, family);
		StyleConstants.setBold(bl, bold);
		StyleConstants.setItalic(bl, italic);
		StyleConstants.setFontSize(bl, fontsize);
		String ls = Prefs.getPref(PrefName.LINESPACING);
		float f = Float.parseFloat(ls);
		StyleConstants.setLineSpacing(bl, f);

		boolean bUseUCS = ((Prefs.getPref(PrefName.UCS_ON)).equals("true")) ? true
				: false;
		Color ctemp;
		try {
			Style s = textPane.addStyle("blue", bl);
			ctemp = new Color((new Integer(Prefs.getPref(PrefName.UCS_BLUE)))
					.intValue());
			StyleConstants.setForeground(s, bUseUCS ? (ctemp) : (Color.BLUE));

			s = textPane.addStyle("red", bl);
			ctemp = new Color((new Integer(Prefs.getPref(PrefName.UCS_RED)))
					.intValue());
			StyleConstants.setForeground(s, bUseUCS ? (ctemp) : (Color.RED));

			s = textPane.addStyle("green", bl);
			// bsv 2004-12-21
			ctemp = new Color((new Integer(Prefs.getPref(PrefName.UCS_GREEN)))
					.intValue());
			StyleConstants.setForeground(s, bUseUCS ? (ctemp) : (Color.GREEN));

			s = textPane.addStyle("white", bl);
			ctemp = new Color((new Integer(Prefs.getPref(PrefName.UCS_WHITE)))
					.intValue());
			StyleConstants.setForeground(s, bUseUCS ? (ctemp) : (Color.WHITE));

			s = textPane.addStyle("navy", bl);
			ctemp = new Color((new Integer(Prefs.getPref(PrefName.UCS_NAVY)))
					.intValue());
			StyleConstants.setForeground(s, bUseUCS ? (ctemp) : (new Color(0,
					0, 102)));

			s = textPane.addStyle("purple", bl);
			ctemp = new Color((new Integer(Prefs.getPref(PrefName.UCS_PURPLE)))
					.intValue());
			StyleConstants.setForeground(s, bUseUCS ? (ctemp) : (new Color(102,
					0, 102)));

			s = textPane.addStyle("brick", bl);
			ctemp = new Color((new Integer(Prefs.getPref(PrefName.UCS_BRICK)))
					.intValue());
			StyleConstants.setForeground(s, bUseUCS ? (ctemp) : (new Color(102,
					0, 0)));

			s = textPane.addStyle("ul", bl);
			StyleConstants.setUnderline(s, true);

			s = textPane.addStyle("strike", bl);
			StyleConstants.setStrikeThrough(s, true);

			URL icon = getClass().getResource(
					"/resource/" + Prefs.getPref(PrefName.UCS_MARKER));
			if (icon != null) {
				s = textPane.addStyle("icon", bl);
				StyleConstants.setIcon(s, new ImageIcon(icon));
			}

		} catch (NoSuchFieldError e) {
			// java 1.3 - just use black
		}

	}

	// update the text styles for all appt text panes
	// this is called when the user changes the font size
	void updStyles() {

		Style def = StyleContext.getDefaultStyleContext().getStyle(
				StyleContext.DEFAULT_STYLE);

		// update all of the text panes
		String s = Prefs.getPref(PrefName.APPTFONT);
		Font f = Font.decode(s);
		for (int i = 0; i < NUM_DAY_BOXES; i++) {
			initStyles(daytext[i], def, f);
		}

		s = Prefs.getPref(PrefName.PREVIEWFONT);
		f = Font.decode(s);

		initStyles(todoPreview, def, f);
		initStyles(taskPreview, def, f);
		// initStyles(dbInfo, def, f);
	}

	// adds a string to an appt text pane using a given style
	private void addString(JTextPane tp, String s, String style)
			throws Exception {
		// Add the string.
		StyledDocument doc = tp.getStyledDocument();

		if (style == null)
			style = "black";

		// get the right style based on the color
		Style st = tp.getStyle(style);

		// static can be null for old BORG DBs that have
		// colors no longer supported. Only 2-3 people would encounter this.
		// default to black
		if (st == null)
			st = tp.getStyle("black");

		// add string to text pane
		doc.insertString(doc.getLength(), s, st);

	}

	void setDayLabels() {
		// determine first day and last day of the month
		GregorianCalendar cal = new GregorianCalendar();
		cal.setFirstDayOfWeek(Prefs.getIntPref(PrefName.FIRSTDOW));
		cal.set(Calendar.DATE, 1);
		cal.add(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek()
				- cal.get(Calendar.DAY_OF_WEEK));
		SimpleDateFormat dwf = new SimpleDateFormat("EEEE");
		jLabel1.setText(dwf.format(cal.getTime()));
		cal.add(Calendar.DAY_OF_WEEK, 1);
		jLabel3.setText(dwf.format(cal.getTime()));
		cal.add(Calendar.DAY_OF_WEEK, 1);
		jLabel4.setText(dwf.format(cal.getTime()));
		cal.add(Calendar.DAY_OF_WEEK, 1);
		jLabel5.setText(dwf.format(cal.getTime()));
		cal.add(Calendar.DAY_OF_WEEK, 1);
		jLabel6.setText(dwf.format(cal.getTime()));
		cal.add(Calendar.DAY_OF_WEEK, 1);
		jLabel7.setText(dwf.format(cal.getTime()));
		cal.add(Calendar.DAY_OF_WEEK, 1);
		jLabel8.setText(dwf.format(cal.getTime()));
	}

	private void exit() {

		Borg.shutdown();
	}

	/**
	 * refresh displays a month on the main gui window and updates the todo and
	 * task previews
	 */
	public void refresh() {
		try {

			// determine first day and last day of the month
			GregorianCalendar cal = new GregorianCalendar();
			int today = -1;
			if (month_ == cal.get(Calendar.MONTH)
					&& year_ == cal.get(Calendar.YEAR)) {
				today = cal.get(Calendar.DAY_OF_MONTH);
				Today.setEnabled(false);
			} else {
				Today.setEnabled(true);
			}

			cal.setFirstDayOfWeek(Prefs.getIntPref(PrefName.FIRSTDOW));

			// set cal to day 1 of month
			cal.set(year_, month_, 1);

			// set month title
			SimpleDateFormat df = new SimpleDateFormat("MMMM yyyy");
			MonthLabel.setText(df.format(cal.getTime()));

			// get day of week of day 1
			int fd = cal.get(Calendar.DAY_OF_WEEK) - cal.getFirstDayOfWeek();
			if (fd == -1)
				fd = 6;

			// get last day of month
			int ld = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

			// set show public/private flags
			boolean showpub = false;
			boolean showpriv = false;
			String sp = Prefs.getPref(PrefName.SHOWPUBLIC);
			if (sp.equals("true"))
				showpub = true;
			sp = Prefs.getPref(PrefName.SHOWPRIVATE);
			if (sp.equals("true"))
				showpriv = true;

			boolean showDayOfYear = false;
			sp = Prefs.getPref(PrefName.DAYOFYEAR);
			if (sp.equals("true"))
				showDayOfYear = true;

			// fill in the day boxes that correspond to days in this month
			for (int i = 0; i < NUM_DAY_BOXES; i++) {
				MouseListener mls[] = daytext[i].getMouseListeners();
				daytext[i].setToolTipText(null);
				for (int mli = 0; mli < mls.length; mli++) {
					daytext[i].removeMouseListener(mls[mli]);
				}
				mls = days[i].getMouseListeners();
				for (int mli = 0; mli < mls.length; mli++) {
					days[i].removeMouseListener(mls[mli]);
				}
				mls = daynum[i].getMouseListeners();
				for (int mli = 0; mli < mls.length; mli++) {
					daynum[i].removeMouseListener(mls[mli]);
				}

				int daynumber = i - fd + 1;

				// clear any text in the box from the last displayed month
				StyledDocument doc = daytext[i].getStyledDocument();
				if (doc.getLength() > 0)
					doc.remove(0, doc.getLength());

				// if the day box is not needed for the current month, make it
				// invisible
				String show_extra = Prefs.getPref(PrefName.SHOWEXTRADAYS);
				if (!show_extra.equals("true")
						&& (daynumber <= 0 || daynumber > ld)) {
					// the following line fixes a bug (in Java) where any
					// daytext not
					// visible in the first month would show its first line of
					// text
					// in the wrong Style when it became visible in a different
					// month -
					// using a Style not even set by this program.
					// once this happened, resizing the window would fix the
					// Style
					// so it probably is a swing bug
					addString(daytext[i], "bug fix", "black");
					days[i].setVisible(false);
				} else {

					int month = month_;
					int year = year_;
					int date = daynumber;

					if (daynumber <= 0) {
						Calendar adjust = new GregorianCalendar(year, month, 1);
						adjust.add(Calendar.DATE, date - 1);
						month = adjust.get(Calendar.MONTH);
						date = adjust.get(Calendar.DATE);
						year = adjust.get(Calendar.YEAR);
					} else if (daynumber > ld) {
						Calendar adjust = new GregorianCalendar(year, month, ld);
						adjust.add(Calendar.DATE, date - ld);
						month = adjust.get(Calendar.MONTH);
						date = adjust.get(Calendar.DATE);
						year = adjust.get(Calendar.YEAR);
					}

					// set value of date button
					daynum[i].setText(Integer.toString(date));
					daynum[i].addMouseListener(new DayMouseListener(year,
							month, date, true));
					daytext[i].addMouseListener(new DayMouseListener(year,
							month, date, false));
					days[i].addMouseListener(new DayMouseListener(year, month,
							date, false));

					GregorianCalendar gc = new GregorianCalendar(year, month,
							date, 23, 59);
					if (showDayOfYear) {
						dayOfYear[i].setText(Integer.toString(gc
								.get(Calendar.DAY_OF_YEAR)));
					} else {
						dayOfYear[i].setText("");
					}
					// get appointment info for the day's appointments from the
					// data model
					Day di = Day.getDay(year, month, date, showpub, showpriv,
							true);
					Collection appts = di.getAppts();

					daytext[i].setParagraphAttributes(daytext[i]
							.getStyle("black"), true);
					if (appts != null) {
						Iterator it = appts.iterator();

						StringBuffer html = new StringBuffer();

						// iterate through the day's appts
						while (it.hasNext()) {

							Appointment info = (Appointment) it.next();

							// bsv 2004-12-23
							boolean bullet = false;
							Date nt = info.getNextTodo();
							if (Prefs.getPref(PrefName.UCS_MARKTODO).equals(
									"true")) {
								if (info.getTodo()
										&& (nt == null || !nt.after(gc
												.getTime()))) {
									bullet = true;
									if (Prefs.getPref(PrefName.UCS_MARKER)
											.endsWith(".gif")) {
										// daytext[i].insertIcon(new
										// javax.swing.ImageIcon(getClass().getResource("/resource/"
										// +
										// Prefs.getPref(PrefName.UCS_MARKER))));
										addString(daytext[i], Prefs
												.getPref(PrefName.UCS_MARKER),
												"icon");
									} else {
										addString(daytext[i], Prefs
												.getPref(PrefName.UCS_MARKER),
												info.getColor());
									}
								}
							}

							String color = info.getColor();
							// strike-through done todos
							if (info.getTodo()
									&& !(nt == null || !nt.after(gc.getTime()))) {
								color = "strike";
							}
							boolean strike = color.equals("strike");

							// add the day's text in the right color. If the
							// appt is the last
							// one - don't add a trailing newline - it will make
							// the text pane
							// have one extra line - forcing an unecessary
							// scrollbar at times
							String text = info.getText();

							if (it.hasNext())
								text += "\n"; // we're doing this to a String

							addString(daytext[i], text, color);

							// Compute the tooltip text.
							if (bullet)
								html.append("<b>");
							if (strike)
								html.append("<strike>");
							else
								html.append("<font color=\"" + color + "\">");
							String apptText = di.getUntruncatedAppointmentFor(
									info).getText();
							if (apptText.indexOf('\n') != -1) {
								StringBuffer temp = new StringBuffer();
								for (int j = 0; j < apptText.length(); ++j) {
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
						if (html.length() != 0) {
							daytext[i].setToolTipText("<html>" + html
									+ "</html>");
							// System.out.println(i+": "+html);
						}
					}

					// reset the text pane to show the top left of the appt text
					// if the text
					// scrolls up or right
					daytext[i].setCaretPosition(0);

					int xcoord = i % 7;
					int dow = cal.getFirstDayOfWeek() + xcoord;
					if (dow == 8)
						dow = 1;

					// set the day color based on if the day is today, or if any
					// of the
					// appts for the day are holidays, vacation days, half-days,
					// or weekends
					boolean bUseUCS = ((Prefs.getPref(PrefName.UCS_ON))
							.equals("true")) ? true : false;
					if (daynumber <= 0 || daynumber > ld) {
						daytext[i].setBackground(jPanel1.getBackground());
						days[i].setBackground(jPanel1.getBackground());
					} else if (bUseUCS == false) {
						if (today == daynumber) {
							// today color is pink
							daytext[i].setBackground(new Color(225, 150, 150));
							days[i].setBackground(new Color(225, 150, 150));
						} else if (di.getHoliday() == 1) {
							// holiday color
							daytext[i].setBackground(new Color(245, 203, 162));
							days[i].setBackground(new Color(245, 203, 162));
						} else if (di.getVacation() == 1) {
							// vacation color
							daytext[i].setBackground(new Color(155, 255, 153));
							days[i].setBackground(new Color(155, 255, 153));
						} else if (di.getVacation() == 2) {
							// half day color
							daytext[i].setBackground(new Color(200, 255, 200));
							days[i].setBackground(new Color(200, 255, 200));
						} else if (dow != Calendar.SUNDAY
								&& dow != Calendar.SATURDAY) {
							// weekday color
							days[i].setBackground(new Color(255, 233, 192));
							daytext[i].setBackground(new Color(255, 233, 192));
						} else {
							// weekend color
							daytext[i].setBackground(new Color(245, 203, 162));
							days[i].setBackground(new Color(245, 203, 162));
						}
					} else {
						Color ctemp = new Color((new Integer(Prefs
								.getPref(PrefName.UCS_DEFAULT))).intValue());
						if (today == daynumber) {
							ctemp = new Color((new Integer(Prefs
									.getPref(PrefName.UCS_TODAY))).intValue());
							daytext[i].setBackground(ctemp);
							days[i].setBackground(ctemp);
						} else if (di.getHoliday() == 1) {
							ctemp = new Color((new Integer(Prefs
									.getPref(PrefName.UCS_HOLIDAY))).intValue());
							daytext[i].setBackground(ctemp);
							days[i].setBackground(ctemp);
						} else if (di.getVacation() == 1) {
							ctemp = new Color((new Integer(Prefs
									.getPref(PrefName.UCS_VACATION)))
									.intValue());
							daytext[i].setBackground(ctemp);
							days[i].setBackground(ctemp);
						} else if (di.getVacation() == 2) {
							// half day color
							ctemp = new Color((new Integer(Prefs
									.getPref(PrefName.UCS_HALFDAY))).intValue());
							daytext[i].setBackground(ctemp);
							days[i].setBackground(ctemp);
						} else if (dow != Calendar.SUNDAY
								&& dow != Calendar.SATURDAY) {
							// weekday color
							ctemp = new Color((new Integer(Prefs
									.getPref(PrefName.UCS_WEEKDAY))).intValue());
							daytext[i].setBackground(ctemp);
							days[i].setBackground(ctemp);
						} else {
							// weekend color
							ctemp = new Color((new Integer(Prefs
									.getPref(PrefName.UCS_WEEKEND))).intValue());
							daytext[i].setBackground(ctemp);
							days[i].setBackground(ctemp);
						}
					}

					// (bsv 2004-12-21)
					days[i].setVisible(true);

				}

			}

			// label the week buttons
			if (Prefs.getPref(PrefName.ISOWKNUMBER).equals("true"))
				cal.setMinimalDaysInFirstWeek(4);
			else
				cal.setMinimalDaysInFirstWeek(1);
			cal.set(year_, month_, 1);
			int wk = cal.get(Calendar.WEEK_OF_YEAR);
			jButton1.setText(Integer.toString(wk));
			cal.set(year_, month_, 8);
			wk = cal.get(Calendar.WEEK_OF_YEAR);
			jButton2.setText(Integer.toString(wk));
			cal.set(year_, month_, 15);
			wk = cal.get(Calendar.WEEK_OF_YEAR);
			jButton3.setText(Integer.toString(wk));
			cal.set(year_, month_, 22);
			wk = cal.get(Calendar.WEEK_OF_YEAR);
			jButton4.setText(Integer.toString(wk));
			cal.set(year_, month_, 29);
			wk = cal.get(Calendar.WEEK_OF_YEAR);
			jButton5.setText(Integer.toString(wk));

			refreshTodoView();
			refreshTaskView();
			// refreshDbInfo();

		} catch (Exception e) {
			Errmsg.errmsg(e);
		} finally {
			// not sure I like this. this window pops in front of the appt list
			// that I was working with
			// requestFocus();
		}
	}

	private void refreshTodoView() throws Exception {

		// update todoPreview Box
		StyledDocument tdoc = todoPreview.getStyledDocument();
		tdoc.remove(0, tdoc.getLength());

		// sort and add the todos
		Vector tds = AppointmentModel.getReference().get_todos();
		if (tds.size() > 0) {
			addString(todoPreview, Resource.getResourceString("Todo_Preview")
					+ "\n", "ul");

			// the treeset will sort by date
			TreeSet ts = new TreeSet(new Comparator() {
				public int compare(java.lang.Object obj, java.lang.Object obj1) {
					try {
						Appointment r1 = (Appointment) obj;
						Appointment r2 = (Appointment) obj1;
						Date dt1 = r1.getNextTodo();
						if (dt1 == null) {
							dt1 = r1.getDate();
						}
						Date dt2 = r2.getNextTodo();
						if (dt2 == null) {
							dt2 = r2.getDate();
						}

						if (dt1.after(dt2))
							return (1);
						return (-1);
					} catch (Exception e) {
						return (0);
					}
				}
			});

			// sort the todos by adding to the TreeSet
			for (int i = 0; i < tds.size(); i++) {
				ts.add(tds.elementAt(i));
			}

			Iterator it = ts.iterator();
			while (it.hasNext()) {
				try {
					Appointment r = (Appointment) it.next();

					// !!!!! only show first line of appointment text !!!!!!
					String tx = "";
					String xx = r.getText();
					int ii = xx.indexOf('\n');
					if (ii != -1) {
						tx = xx.substring(0, ii);
					} else {
						tx = xx;
					}
					addString(todoPreview, tx + "\n", r.getColor());

				} catch (Exception e) {
					Errmsg.errmsg(e);
				}

			}
			todoPreview.setCaretPosition(0);
		} else {
			addString(todoPreview, Resource.getResourceString("Todo_Preview")
					+ "\n", "ul");
			addString(todoPreview, Resource.getResourceString("none_pending"),
					"black");
		}

	}

	private void refreshTaskView() throws Exception {
		// update taskPreview Box
		StyledDocument tkdoc = taskPreview.getStyledDocument();
		tkdoc.remove(0, tkdoc.getLength());

		// sort and add the tasks
		Vector tks = TaskModel.getReference().get_tasks();
		if (tks.size() > 0) {
			addString(taskPreview, Resource.getResourceString("Task_Preview")
					+ "\n", "ul");

			// the treeset will sort by date
			TreeSet ts = new TreeSet(new Comparator() {
				public int compare(java.lang.Object obj, java.lang.Object obj1) {
					try {
						Task r1 = (Task) obj;
						Task r2 = (Task) obj1;
						Date dt1 = r1.getDueDate();
						Date dt2 = r2.getDueDate();
						if (dt1.after(dt2))
							return (1);
						return (-1);
					} catch (Exception e) {
						return (0);
					}
				}
			});

			// sort the tasks by adding to the treeset
			for (int i = 0; i < tks.size(); i++) {
				ts.add(tks.elementAt(i));
			}

			Iterator it = ts.iterator();
			while (it.hasNext()) {
				try {
					Task r = (Task) it.next();

					// !!!!! only show first line of task text !!!!!!
					String tx = "";
					String xx = r.getDescription();
					int ii = xx.indexOf('\n');
					if (ii != -1) {
						tx = xx.substring(0, ii);
					} else {
						tx = xx;
					}
					addString(taskPreview, "BT" + r.getTaskNumber() + ":" + tx
							+ "\n", "black");

				} catch (Exception e) {
					Errmsg.errmsg(e);
				}

			}
			taskPreview.setCaretPosition(0);
		} else {
			addString(taskPreview, Resource.getResourceString("Task_Preview")
					+ "\n", "ul");
			addString(taskPreview, Resource.getResourceString("none_pending"),
					"black");
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the FormEditor.
	 */

	private void initComponents() {// GEN-BEGIN:initComponents
		java.awt.GridBagConstraints gridBagConstraints;

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

		jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0,
				0));
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
		Next.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Forward16.gif")));
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
		Prev.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Back16.gif")));
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
		Today.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Home16.gif")));
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
		Goto.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Undo16.gif")));
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

		setJMenuBar(new MainMenu(this).getMenuBar());

		this.setContentPane(getJPanel());

	}// GEN-END:initComponents

	private void jButton5ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton5ActionPerformed
	{// GEN-HEADEREND:event_jButton5ActionPerformed
		new WeekView(month_, year_, 29);
	}// GEN-LAST:event_jButton5ActionPerformed

	private void jButton4ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton4ActionPerformed
	{// GEN-HEADEREND:event_jButton4ActionPerformed
		new WeekView(month_, year_, 22);
	}// GEN-LAST:event_jButton4ActionPerformed

	private void jButton3ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton3ActionPerformed
	{// GEN-HEADEREND:event_jButton3ActionPerformed
		new WeekView(month_, year_, 15);
	}// GEN-LAST:event_jButton3ActionPerformed

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton2ActionPerformed
	{// GEN-HEADEREND:event_jButton2ActionPerformed
		new WeekView(month_, year_, 8);
	}// GEN-LAST:event_jButton2ActionPerformed

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton1ActionPerformed
	{// GEN-HEADEREND:event_jButton1ActionPerformed
		new WeekView(month_, year_, 1);
	}// GEN-LAST:event_jButton1ActionPerformed

	private void GotoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_GotoActionPerformed

		// GOTO a particular month
		DateDialog dlg = new DateDialog(this);
		dlg.setCalendar(new GregorianCalendar());
		dlg.setVisible(true);
		Calendar dlgcal = dlg.getCalendar();
		if (dlgcal == null)
			return;

		month_ = dlgcal.get(Calendar.MONTH);
		year_ = dlgcal.get(Calendar.YEAR);
		refresh();

	}

	public void today(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_today
		try {
			// set view back to month containing today
			today();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}// GEN-LAST:event_today

	private void NextActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_NextActionPerformed
		// go to next month - increment month/year and call refresh of view
		if (month_ == 11) {
			month_ = 0;
			year_++;
		} else {
			month_++;
		}
		try {
			refresh();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}
	}// GEN-LAST:event_NextActionPerformed

	/** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_exitForm
		if (trayIcon_) {
			this.dispose();
		} else {
			exit();
		}

	}// GEN-LAST:event_exitForm

	// Variables declaration - do not modify//GEN-BEGIN:variables

	private javax.swing.JButton Goto;

	private javax.swing.JLabel MonthLabel;

	private javax.swing.JButton Next;

	private javax.swing.JButton Prev;

	private javax.swing.JButton Today;

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

	private javax.swing.JPanel jPanel1;

	private javax.swing.JPanel jPanel2;

	private javax.swing.JPanel jPanel3;

	private javax.swing.JPanel jPanel4;

	// End of variables declaration//GEN-END:variables

	/**
	 * array of day panels
	 */
	private JPanel days[];

	private JTextPane daytext[];

	/**
	 * date buttons
	 */
	private JButton daynum[];

	private JLabel dayOfYear[];

	private JTextPane todoPreview;

	private JTextPane taskPreview;

	// private JTextPane dbInfo;

	private JPanel jPanel = null;

	private JPanel jPanel5 = null;

	// called when a Prefs change notification is sent out
	// to all Prefs.Listeners
	public void prefsChanged() {
		// System.out.println("pr called");
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
			gridBagConstraints57.insets = new java.awt.Insets(4, 4, 4, 4);
			gridBagConstraints57.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints58.gridx = 0;
			gridBagConstraints58.gridy = 2;
			gridBagConstraints58.insets = new java.awt.Insets(0, 0, 0, 0);
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

	private void PrevActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_PrevActionPerformed
		// go to previous month - decrement month/year and call refresh of view

		if (month_ == 0) {
			month_ = 11;
			year_--;
		} else {
			month_--;
		}
		try {
			refresh();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}// Add your handling code here:
	}// GEN-LAST:event_PrevActionPerformed

	public void next() {
		NextActionPerformed(null);
	}

	public void prev() {
		PrevActionPerformed(null);		
	}


}
