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
import net.sf.borg.ui.task.TaskView;

// ApptDayBox holds the logical information needs to determine
// how an appointment box should be drawn in a day grid
public class NoteBox implements Draggable {

	private Icon todoIcon = null;

	private String todoMarker = null;

	private CalendarEntity bean = null;

	private Rectangle bounds, clip;

	private Date date; // date being displayed - not necessarily date of

	private boolean isSelected = false;

	private boolean hasLink = false;

	@SuppressWarnings("unchecked")
	public NoteBox(Date d, CalendarEntity ap, Rectangle bounds, Rectangle clip) {
		bean = ap;
		date = d;
		this.bounds = bounds;
		this.clip = clip;

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

		Collection<Link> atts;
		try {
			atts = LinkModel.getReference().getLinks((KeyedEntity) bean);
			if (atts != null && atts.size() > 0)
				hasLink = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void delete() {
		if (bean instanceof Appointment)
			AppointmentModel.getReference().delAppt(
					((Appointment) bean).getKey());
	}

	private int oldFontHeight = -1;

	public void draw(Graphics2D g2, Component comp) {

		Shape s = g2.getClip();
		if (clip != null)
			g2.setClip(clip);

		Font sm_font = g2.getFont();
		int smfontHeight = g2.getFontMetrics().getHeight();

		// resize todoIcon if needed
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

		Map<TextAttribute, Serializable> stmap = new HashMap<TextAttribute, Serializable>();
		stmap.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		stmap.put(TextAttribute.FONT, sm_font);

		if (isSelected == true) {
			g2.setColor(Color.WHITE);
			g2.fillRect(bounds.x, bounds.y + 2, bounds.width, bounds.height);
		}
		g2.setColor(Color.BLACK);

		if (getTextColor().equals("strike")) {

			AttributedString as = new AttributedString(getText(), stmap);
			g2.drawString(as.getIterator(), bounds.x + 2, bounds.y
					+ smfontHeight);
		} else {
			// change color for a single appointment based on
			// its color - only if color print option set
			g2.setColor(Color.black);

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

			int offset = 2;
			String text = getText();
			if (hasLink) {
				text = "@ " + text;
			}
			if (isTodo() && todoIcon != null) {

				todoIcon.paintIcon(comp, g2, bounds.x + offset, bounds.y
						+ bounds.height / 2);
				offset = todoIcon.getIconWidth();
				g2.drawString(text, bounds.x + offset, bounds.y + smfontHeight);
			} else if (isTodo() && todoMarker != null) {
				g2.drawString(todoMarker + " " + text, bounds.x + offset,
						bounds.y + smfontHeight);
			} else {
				g2.drawString(text, bounds.x + offset, bounds.y + smfontHeight);
			}
			g2.setColor(Color.black);
		}

		g2.setClip(s);
		g2.setColor(Color.black);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.calendar.Box#edit()
	 */
	public void edit() {
		if (bean instanceof Appointment) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			AppointmentListView ag = new AppointmentListView(cal
					.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
					.get(Calendar.DATE));
			MultiView.getMainView().addView(ag);
			ag.showApp(((Appointment) bean).getKey());

		} else if (bean instanceof Project) {
			MultiView cv = MultiView.getMainView();
			if (cv != null)
				cv.showTasksForProject((Project) bean);
		} else if (bean instanceof Task) {
			try {
				MultiView.getMainView().showTasks();
				MultiView.getMainView().addView(
						new TaskView((Task) bean, TaskView.T_CHANGE, null));
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}
		} else if (bean instanceof Subtask) {
			MultiView.getMainView().showTasks();
			int taskid = ((Subtask) bean).getTask().intValue();
			Task t;
			try {
				t = TaskModel.getReference().getTask(taskid);
				MultiView.getMainView().addView(
						new TaskView(t, TaskView.T_CHANGE, null));
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.calendar.Box#getBounds()
	 */
	public Rectangle getBounds() {
		return bounds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.calendar.Box#getText()
	 */
	public String getText() {
		return bean.getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.calendar.Box#setBounds(java.awt.Rectangle)
	 */
	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.calendar.Box#setSelected(boolean)
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	private String getTextColor() {
		if (bean == null)
			return null;

		if (ApptBoxPanel.isStrike(bean, date)) {
			return ("strike");
		}

		return bean.getColor();
	}

	private boolean isTodo() {
		return bean.getTodo();
	}

	private JPopupMenu popmenu = null;

	public JPopupMenu getMenu() {

		if (!(bean instanceof Appointment))
			return null;

		JMenuItem mnuitm;
		if (popmenu == null) {
			popmenu = new JPopupMenu();
			popmenu.add(mnuitm = new JMenuItem(Resource
					.getPlainResourceString("Edit")));
			mnuitm.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					edit();
				}
			});
			popmenu.add(mnuitm = new JMenuItem(Resource
					.getPlainResourceString("Delete")));
			mnuitm.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					delete();
				}
			});

			if (isTodo()) {
				popmenu.add(mnuitm = new JMenuItem(Resource
						.getPlainResourceString("Done_(No_Delete)")));
				mnuitm.addActionListener(new ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						try {
							AppointmentModel.getReference().do_todo(
									((Appointment) bean).getKey(), false);
						} catch (Exception e) {
							Errmsg.errmsg(e);
						}
					}
				});

				popmenu.add(mnuitm = new JMenuItem(Resource
						.getPlainResourceString("Done_(Delete)")));
				mnuitm.addActionListener(new ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						try {
							AppointmentModel.getReference().do_todo(
									((Appointment) bean).getKey(), true);
						} catch (Exception e) {
							Errmsg.errmsg(e);
						}
					}
				});
			}

			if (bean instanceof Appointment
					&& Repeat.isRepeating((Appointment) bean)) {
				popmenu.add(mnuitm = new JMenuItem(Resource
						.getPlainResourceString("Delete_One_Only")));
				mnuitm.addActionListener(new ActionListener() {
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

	public void move(int realtime, Date d) throws Exception {

		if (bean instanceof Appointment) {
			Appointment ap = AppointmentModel.getReference().getAppt(
					((Appointment) bean).getKey());

			int hour = realtime / 60;
			int min = realtime % 60;

			int oldkey = (ap.getKey() / 100) * 100;

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
			int newkey = DateUtil.dayOfEpoch(newTime);

			ap.setDate(newTime);

			// only do something if date changed
			if (oldkey != newkey) { // date chg
				if (Repeat.isRepeating(ap)) { // cannot date chg unless it is
												// on
					// the first in a series
					int k2 = DateUtil.dayOfEpoch(date);
					if (oldkey != k2) {
						Errmsg.notice(Resource
								.getPlainResourceString("rpt_drag_err"));
						return;
					}
				}
				AppointmentModel.getReference().changeDate(ap);

			} else {
				AppointmentModel.getReference().saveAppt(ap, false);
			}
		}
		else if( bean instanceof Task )
		{
			Task task = TaskModel.getReference().getTask(
					((Task) bean).getKey());	
			task.setDueDate(d);	
			if( task.getDueDate() != null && DateUtil.isAfter(task.getStartDate(), task.getDueDate()) )
			{
				throw new Warning(Resource
					.getPlainResourceString("sd_dd_warn"));
			}
			TaskModel.getReference().savetask(task);
		}
		else if( bean instanceof Subtask )
		{
			Subtask task = TaskModel.getReference().getSubTask(
					((Subtask) bean).getKey());	
			task.setDueDate(d);
			if( task.getDueDate() != null && DateUtil.isAfter(task.getStartDate(), task.getDueDate()) )
			{
				throw new Warning(Resource
					.getPlainResourceString("sd_dd_warn"));
			}
			TaskModel.getReference().saveSubTask(task);
		}
		else if( bean instanceof Project )
		{
			Project project = TaskModel.getReference().getProject(
					((Project) bean).getKey());	
			project.setDueDate(d);	
			if( project.getDueDate() != null && DateUtil.isAfter(project.getStartDate(), project.getDueDate()) )
			{
				throw new Warning(Resource
					.getPlainResourceString("sd_dd_warn"));
			}
			TaskModel.getReference().saveProject(project);
		}
	}
}
