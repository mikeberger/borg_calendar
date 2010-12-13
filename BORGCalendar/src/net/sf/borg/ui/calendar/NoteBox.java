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
package net.sf.borg.ui.calendar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.text.AttributedString;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.sf.borg.common.DateUtil;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.LinkModel;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.CalendarEntity;
import net.sf.borg.model.entity.KeyedEntity;
import net.sf.borg.model.entity.Link;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.Task;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.task.ProjectView;
import net.sf.borg.ui.task.TaskView;

/**
 * 
 * A Note Box is used to draw an untimed event on the day/week/month panels
 * 
 */
public class NoteBox extends Box implements Box.Draggable {

	private CalendarEntity bean = null; // entity associated with this box

	private Date date; // date being displayed - not necessarily date of the
	// entity in the case of repeats

	private boolean hasLink = false; // does entity have a link

	private int oldFontHeight = -1; // involved with reszie of todo icon - see
	// draw()

	private JPopupMenu popmenu = null; // popup menu

	private Icon todoIcon = null; // icon to mark todos

	private String todoMarker = null; // textual todo marker
	
	private String noteText = null; // the text of this note box

	/**
	 * constructor
	 * 
	 * @param d
	 *            date that the box is on
	 * @param ap
	 *            the calendar entity
	 * @param bounds
	 *            bounds
	 * @param clip
	 *            clip
	 */
	public NoteBox(Date d, CalendarEntity ap, Rectangle bounds, Rectangle clip) {
		super(bounds, clip);
		bean = ap;
		date = d;

		// set the todo marker from the prefs - whether it is an icon or text
		String iconname = Prefs.getPref(PrefName.UCS_MARKER);
		String use_marker = Prefs.getPref(PrefName.UCS_MARKTODO);
		if (use_marker.equals("true")) {
			if (iconname.endsWith(".gif") || iconname.endsWith(".jpg")) {
				todoIcon = new javax.swing.ImageIcon(getClass().getResource(
						"/resource/" + iconname));
			} else {
				todoMarker = iconname;
			}
		}

		// set the link flag
		Collection<Link> atts;
		try {
			atts = LinkModel.getReference().getLinks((KeyedEntity<?>) bean);
			if (atts != null && atts.size() > 0)
				hasLink = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// appointments need a special format
		if( ap instanceof Appointment )
			noteText = AppointmentTextFormat.format((Appointment)ap, d);
		else 
			noteText = ap.getText();

	}

	@Override
	public void delete() {
		if (bean instanceof Appointment)
			AppointmentModel.getReference().delAppt(
					((Appointment) bean).getKey());
	}

	/**
	 * draw the box
	 */
	@Override
	public void draw(Graphics2D g2, Component comp) {

		Shape s = g2.getClip();
		if (clip != null)
			g2.setClip(clip);

		Font sm_font = g2.getFont();
		int smfontHeight = g2.getFontMetrics().getHeight();

		// resize todoIcon if needed to match the text size
		if (oldFontHeight != smfontHeight) {
			if (todoIcon != null) {
				try {
					// get image
					BufferedImage image1 = ImageIO.read(getClass().getResource(
							"/resource/" + Prefs.getPref(PrefName.UCS_MARKER)));
					double size = image1.getHeight();

					// scale to 1/2 font height
					double scale = smfontHeight / (2 * size);
					AffineTransform tx = AffineTransform.getScaleInstance(
							scale, scale);
					AffineTransformOp op = new AffineTransformOp(tx,
							AffineTransformOp.TYPE_BICUBIC);
					BufferedImage rImage = op.filter(image1, null);
					todoIcon = new ImageIcon(rImage);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			oldFontHeight = smfontHeight;
		}

		// create the strike-through text map
		Map<TextAttribute, Serializable> stmap = new HashMap<TextAttribute, Serializable>();
		stmap.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		stmap.put(TextAttribute.FONT, sm_font);

		// use white background to highlight selected box
		if (isSelected == true) {
			g2.setColor(Color.WHITE);
			g2.fillRect(bounds.x, bounds.y + 2, bounds.width, bounds.height);
		}

		// default is black text
		g2.setColor(Color.BLACK);

		// set alternate text color if needed
		if (getTextColor().equals("strike")) {

			AttributedString as = new AttributedString(getText(), stmap);
			g2.drawString(as.getIterator(), bounds.x + 2, bounds.y
					+ smfontHeight);
		} else {
			// change color for a single appointment based on
			// its color - only if color print option set

			// map "logical" color names to user defined colors
			// "logical" color names used to be the hard-coded colors. Now they
			// mean nothing. If it weren't for legacy databases, then red could
			// be color1
			// UCS_RED might as well be UCS_COLOR1
			if (getTextColor().equals("red"))
				g2.setColor(new Color(Integer.parseInt(Prefs
						.getPref(PrefName.UCS_RED))));
			else if (getTextColor().equals("green"))
				g2.setColor(new Color(Integer.parseInt(Prefs
						.getPref(PrefName.UCS_GREEN))));
			else if (getTextColor().equals("blue"))
				g2.setColor(new Color(Integer.parseInt(Prefs
						.getPref(PrefName.UCS_BLUE))));
			else if (getTextColor().equals("black"))
				g2.setColor(new Color(Integer.parseInt(Prefs
						.getPref(PrefName.UCS_BLACK))));
			else if (getTextColor().equals("white"))
				g2.setColor(new Color(Integer.parseInt(Prefs
						.getPref(PrefName.UCS_WHITE))));
			else if (getTextColor().equals("navy"))
				g2.setColor(new Color(Integer.parseInt(Prefs
						.getPref(PrefName.UCS_NAVY))));
			else if (getTextColor().equals("purple"))
				g2.setColor(new Color(Integer.parseInt(Prefs
						.getPref(PrefName.UCS_PURPLE))));
			else if (getTextColor().equals("brick"))
				g2.setColor(new Color(Integer.parseInt(Prefs
						.getPref(PrefName.UCS_BRICK))));

			// preprend link indicator if needed
			int offset = 2;
			String text = getText();
			if (hasLink) {
				text = "@ " + text;
			}

			// preprend todo marker if needed and draw the box text
			if (isTodo() && todoIcon != null) {
				todoIcon.paintIcon(comp, g2, bounds.x + offset, bounds.y
						+ bounds.height / 2);
				offset = todoIcon.getIconWidth();
			} else if (isTodo() && todoMarker != null) {
				text = todoMarker + " " + text;
			}
			g2.drawString(text, bounds.x + offset, bounds.y + smfontHeight);
			g2.setColor(Color.black);
		}

		g2.setClip(s);
		g2.setColor(Color.black);
	}

	/**
	 * get the popup menu
	 */
	@Override
	public JPopupMenu getMenu() {

		// don't show a popup for non-appointments (i.e. tasks)
		if (!(bean instanceof Appointment))
			return null;

		JMenuItem mnuitm;
		if (popmenu == null) {
			popmenu = new JPopupMenu();
			popmenu.add(mnuitm = new JMenuItem(Resource
					.getResourceString("Edit")));
			mnuitm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					onClick();
				}
			});
			popmenu.add(mnuitm = new JMenuItem(Resource
					.getResourceString("Delete")));
			mnuitm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					delete();
				}
			});

			if (isTodo()) {
				popmenu.add(mnuitm = new JMenuItem(Resource
						.getResourceString("Done_(No_Delete)")));
				mnuitm.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						try {
							AppointmentModel.getReference().do_todo(
									((Appointment) bean).getKey(), false, date);
						} catch (Exception e) {
							Errmsg.errmsg(e);
						}
					}
				});

				popmenu.add(mnuitm = new JMenuItem(Resource
						.getResourceString("Done_(Delete)")));
				mnuitm.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						try {
							AppointmentModel.getReference().do_todo(
									((Appointment) bean).getKey(), true, date);
						} catch (Exception e) {
							Errmsg.errmsg(e);
						}
					}
				});
			}

			if (bean instanceof Appointment
					&& Repeat.isRepeating((Appointment) bean)) {
				popmenu.add(mnuitm = new JMenuItem(Resource
						.getResourceString("Delete_One_Only")));
				mnuitm.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						try {
							AppointmentModel.getReference().delOneOnly(
									((Appointment) bean).getKey(), date);
						} catch (Exception e) {
							Errmsg.errmsg(e);
						}
					}
				});
			}
		}
		return popmenu;
	}

	@Override
	public String getText() {
		return noteText;
	}

	/**
	 * get the text color
	 * 
	 * @return the text color
	 */
	private String getTextColor() {
		if (bean == null)
			return null;

		if (ApptBoxPanel.isStrike(bean, date)) {
			return ("strike");
		}

		return bean.getColor();
	}

	/**
	 * check if is todo
	 * 
	 * @return true if this box is for a todo
	 */
	private boolean isTodo() {
		return bean.getTodo();
	}

	/**
	 * react to a drag of this box on the UI
	 */
	@Override
	public void move(int realtime, Date d) throws Exception {

		if (bean instanceof Appointment) {
			// change appointment date based on move
			Appointment ap = AppointmentModel.getReference().getAppt(
					((Appointment) bean).getKey());
			
			// if staying untimed then keep start at 12AM
			if( realtime == -1) realtime = 0;

			int hour = realtime / 60;
			int min = realtime % 60;

			int olddate = DateUtil.dayOfEpoch(ap.getDate());

			GregorianCalendar newCal = new GregorianCalendar();
			newCal.setTime(d);

			if (hour != 0 || min != 0) {

				// we are moving to be timed - set duration
				ap.setDuration(new Integer(15));
				ap.setUntimed("N");
				newCal.set(Calendar.HOUR_OF_DAY, hour);
				int roundMin = (min / 5) * 5;
				newCal.set(Calendar.MINUTE, roundMin);

			} else {

				// keep time and duration the same
				Calendar oldCal = new GregorianCalendar();
				oldCal.setTime(ap.getDate());
				newCal.set(Calendar.HOUR_OF_DAY, oldCal
						.get(Calendar.HOUR_OF_DAY));
				newCal.set(Calendar.MINUTE, oldCal.get(Calendar.MINUTE));
				newCal.set(Calendar.SECOND, 0);
			}

			Date newTime = newCal.getTime();
			int newdate = DateUtil.dayOfEpoch(newTime);
			ap.setDate(newTime);

			// check for illegal change of repeating appt
			if (olddate != newdate && Repeat.isRepeating(ap)) {
				// cannot date chg unless it is
				// on the first in a series
				int k2 = DateUtil.dayOfEpoch(date);
				if (olddate != k2) {
					Errmsg.notice(Resource.getResourceString("rpt_drag_err"));
					return;
				}
			}

			AppointmentModel.getReference().saveAppt(ap);

		} else if (bean instanceof Task) {

			// when a task is dragged, change its due date

			Task task = TaskModel.getReference()
					.getTask(((Task) bean).getKey());
			task.setDueDate(d);

			// reject change if it was dragged before its start date
			if (task.getDueDate() != null
					&& DateUtil.isAfter(task.getStartDate(), task.getDueDate())) {
				throw new Warning(Resource.getResourceString("sd_dd_warn"));
			}

			TaskModel.getReference().savetask(task);

		} else if (bean instanceof Subtask) {

			// when a subtask is dragged, change its due date

			Subtask subtask = TaskModel.getReference().getSubTask(
					((Subtask) bean).getKey());
			subtask.setDueDate(d);

			// reject change if it was dragged before its start date
			if (subtask.getDueDate() != null
					&& DateUtil.isAfter(subtask.getStartDate(), subtask
							.getDueDate())) {
				throw new Warning(Resource.getResourceString("sd_dd_warn"));
			}

			TaskModel.getReference().saveSubTask(subtask);

		} else if (bean instanceof Project) {

			// when a project is dragged, change its due date

			Project project = TaskModel.getReference().getProject(
					((Project) bean).getKey());
			project.setDueDate(d);

			// reject change if it was dragged before its start date
			if (project.getDueDate() != null
					&& DateUtil.isAfter(project.getStartDate(), project
							.getDueDate())) {
				throw new Warning(Resource.getResourceString("sd_dd_warn"));
			}

			TaskModel.getReference().saveProject(project);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.calendar.Box#edit()
	 */
	@Override
	public void onClick() {
		if (bean instanceof Appointment) {
			// appointment clicked - bring up the appt editor
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			// create new appt list view for the day of the appt
			AppointmentListView ag = new AppointmentListView(cal
					.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
					.get(Calendar.DATE));
			// add appt list tab to main view
			ag.showView();
			// set appt list to be editing the clicked appt
			ag.showApp(((Appointment) bean).getKey());

		} else if (bean instanceof Project) {
			MultiView.getMainView().setView(ViewType.TASK);
			new ProjectView((Project) bean, ProjectView.Action.CHANGE, null)
					.showView();
		} else if (bean instanceof Task) {
			// task clicked - show it
			try {
				MultiView.getMainView().setView(ViewType.TASK);
				new TaskView((Task) bean, TaskView.Action.CHANGE, null)
						.showView();
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}
		} else if (bean instanceof Subtask) {
			// subtask clicked - show its task
			MultiView.getMainView().setView(ViewType.TASK);
			int taskid = ((Subtask) bean).getTask().intValue();
			Task t;
			try {
				t = TaskModel.getReference().getTask(taskid);
				new TaskView(t, TaskView.Action.CHANGE, null).showView();
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}

		}

	}

	@Override
	public String getToolTipText() {
		return getText();
	}
}
