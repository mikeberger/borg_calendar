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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
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
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.LinkModel;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.Link;

/**
 * ApptBox is used to draw timed appointments on the time-grid part of the day
 * and week UIs.
 */
class ApptBox extends Box implements Box.Draggable {
	
	final static private int inset = 2;

	// rounded rectangle radius
	final static private int radius = 2;

	/**
	 * Layout all boxes for a particular day by determining how many overlap and
	 * then setting the horizontal position and width to fit them all together
	 * in the day grid.
	 * 
	 * @param boxlist the list of all ApptBox objects for a day
	 * @param starthr first hour shown on the grid
	 * @param endhr last hour shown on the grid
	 */
	static public void layoutBoxes(List<ApptBox> boxlist, int starthr, int endhr) {

		// convert to minutes
		double startmin = starthr * 60;
		double endmin = endhr * 60;

		// initialize the boxes
		for (ApptBox box : boxlist) {

			Appointment ap = box.appt;
			Date d = ap.getDate();

			// shouldn't ever happen - but if appt has no date
			// we can't show it
			if (d == null)
				continue;

			// determine appt start and end minutes
			GregorianCalendar acal = new GregorianCalendar();
			acal.setTime(d);
			double apstartmin = 60 * acal.get(Calendar.HOUR_OF_DAY)
					+ acal.get(Calendar.MINUTE);
			int dur = 0;
			Integer duri = ap.getDuration();
			if (duri != null) {
				dur = duri.intValue();
			}
			double apendmin = apstartmin + dur;

			// if appointment starts before shown hours or ends after shown
			// hours
			// adjust it to draw up to the grid boundary
			if (apstartmin < startmin)
				apstartmin = startmin;
			if (apendmin > endmin)
				apendmin = endmin;

			// fraction of the grid that the top of the box is below the grid
			// top
			// lots of items are calculated as fractions of the grid size so
			// that
			// they stay the same if the grid size changes
			box.setTopAdjustment((apstartmin - startmin) / (endmin - startmin));

			// adjust the bottom ever so slightly that appts that touch top
			// to bottom do not get detected as overlapping when rounding errors
			// creep in
			// in
			box
					.setBottomAdjustment(((apendmin - startmin) / (endmin - startmin)) - 1.0 / 10000);

		}

		// determine how many appointments each appointment overlaps with
		// to do this, check how many appointments occur for every 5 minute
		// interval on the time grid
		for (int t = (int) startmin; t <= (int) endmin; t += 5) {

			// make a list of appointments that exist at time t
			ArrayList<ApptBox> lst = new ArrayList<ApptBox>();
			for (ApptBox curBox : boxlist) {
				Calendar cal = new GregorianCalendar();
				cal.setTime(curBox.appt.getDate());
				int amin = cal.get(Calendar.HOUR_OF_DAY) * 60
						+ cal.get(Calendar.MINUTE);
				if (amin <= t
						&& (amin + curBox.appt.getDuration().intValue()) > t)
					lst.add(curBox);
			}

			// for all appts that exist at time t, adjust their max
			// number of overlaps if the value for time t is bigger than
			// their current value
			for (ApptBox curBox : lst) {
				curBox.setMaxAcrossAtOneTime(Math.max(curBox
						.getMaxAcrossAtOneTime(), lst.size()));
			}
		}

		// sort the list of boxes by how many overlaps a box has. The ones with
		// the most
		// need to be placed on the grid first.
		Collections.sort(boxlist, new Comparator<ApptBox>() {
			@Override
			public int compare(ApptBox obj1, ApptBox obj2) {
				int diff = obj2.getMaxAcrossAtOneTime()
						- obj1.getMaxAcrossAtOneTime();
				if (diff != 0)
					return (diff);
				return (int) (obj2.getDate().getTime() - obj1.getDate()
						.getTime());
			}
		});

		// determine left and right for each box by placing boxes on the grid
		// one at a time
		for (ApptBox curBox : boxlist) {

			// curBox is the box we are trying to place in the grid

			// farthest right edge of any placed appts that overlap the current
			double maxRightOfPlaced = 0;

			for (ApptBox otherBox : boxlist) {
				if (otherBox == curBox)
					continue;

				// detect overlap
				if (otherBox.getTopAdjustment() > curBox.getBottomAdjustment()
						|| otherBox.getBottomAdjustment() < curBox
								.getTopAdjustment()) {
					// no overlap
					continue;
				}

				// make every box that overlaps the same size as the others it
				// overlaps with
				// even if it's not involved in the spot where the max number of
				// appts overlap
				if (otherBox.getMaxAcrossAtOneTime() < curBox
						.getMaxAcrossAtOneTime())
					otherBox.setMaxAcrossAtOneTime(curBox
							.getMaxAcrossAtOneTime());

				// if the other box has been place (it's horizontal position
				// determined),
				// then make sure that the current box is to the right of it
				if (otherBox.isPlaced()
						&& otherBox.getRightAdjustment() > maxRightOfPlaced) {
					maxRightOfPlaced = otherBox.getRightAdjustment();
				}
			}

			// check if due to the order that boxes were placed and what
			// overlaps,
			// there is no more room across. if so, our prior overlap
			// calculations
			// should have ensured that there is an empty slot to place the box
			// in
			// note: any 3 place decimales are to guard against rounding errors
			// - which did happen
			if (maxRightOfPlaced >= 0.999) {

				// need to fine a "hole" in the grid, so check each horizontal
				// spot
				// looking for an empty one
				for (int slot = 0; slot < curBox.getMaxAcrossAtOneTime(); slot++) {

					// check if appt is in this slot
					boolean slotTaken = false;
					for (ApptBox otherBox : boxlist) {
						if (otherBox == curBox)
							continue;
						if (!otherBox.isPlaced())
							continue;

						if (otherBox.getTopAdjustment() > curBox
								.getBottomAdjustment()
								|| otherBox.getBottomAdjustment() < curBox
										.getTopAdjustment()) {
							// no overlap
							continue;
						}

						// determine if the appt is in this slot
						if (Math.abs(otherBox.getLeftAdjustment()
								- ((double) slot)
								/ (double) otherBox.getMaxAcrossAtOneTime()) < 0.001) {
							// yes, otherBox is in the slot, we can't place the
							// current box there
							slotTaken = true;
							break;
						}

						if (!slotTaken) {

							// empty slot found, place the box there
							curBox.setLeftAdjustment((double) slot
									/ (double) curBox.getMaxAcrossAtOneTime());
							curBox.setRightAdjustment(curBox
									.getLeftAdjustment()
									+ (1 / (double) curBox
											.getMaxAcrossAtOneTime()));
							curBox.setPlaced(true);
							break;
						}

					}

				}
			}
			
			if (curBox.isPlaced()) // if we were placed already in the
				// out-of-slot code above
				continue;

			// place the box horizontally
			// left side is to the right of the others already placed
			curBox.setLeftAdjustment(maxRightOfPlaced);
			// right side is left + width
			curBox.setRightAdjustment(curBox.getLeftAdjustment()
					+ (1 / (double) curBox.getMaxAcrossAtOneTime()));
			curBox.setPlaced(true);
		}

		// calculate the bounds of each box by multiplying the fractional
		// adjustments
		// with real coordinates of the grid box for the day
		for (ApptBox b : boxlist) {
			Rectangle r = new Rectangle();

			r.x = (int) (b.bounds.x + b.bounds.width * b.getLeftAdjustment());
			r.y = (int) (b.bounds.y + b.bounds.height * b.getTopAdjustment());
			r.height = (int) ((b.getBottomAdjustment() - b.getTopAdjustment()) * b.bounds.height);
			r.width = (int) ((b.getRightAdjustment() - b.getLeftAdjustment()) * b.bounds.width);
			b.setBounds(r);
		}
	}

	// the appointment associated with this box
	private Appointment appt = null;

	// fraction of the available grid height at which the bottom should be drawn
	private double bottom; 

	private Date date; // date being displayed - not necessarily date of the appt
	
	// flag to indicate if the appt has links
	private boolean hasLink = false;

	private boolean isPlaced = false; // whether or not this box has been placed (position calculated by the layout code)

	private double left; // fraction of the available grid width at which the left side is drawn

	private int maxAcrossAtOneTime = 0; // max number of appts overlapping any others that overlap this one

	private int oldFontHeight = -1; // used for sizing the todo marker image

	private JPopupMenu popmenu = null; // popup menu

	private double right; // fraction of the available grid width at which the right side should be drawn

	private Icon todoIcon = null; // todo icon

	private String todoMarker = null; // textual todo marker

	private double top; // fraction of the available grid height at which the top should be drawn

	/**
	 * constructor.
	 * 
	 * @param d date that the box is in - not always the appt date
	 * @param ap the appointment represented by this box
	 * @param bounds box bounds
	 * @param clip box clip
	 */
	public ApptBox(Date d, Appointment ap, Rectangle bounds, Rectangle clip) {

		super(bounds, clip);

		appt = ap;
		date = d;

		// determine the todo marker - image or textual - based on prefs
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

		// determine links flag
		Collection<Link> atts;
		try {
			atts = LinkModel.getReference().getLinks(appt);
			if (atts != null && atts.size() > 0)
				hasLink = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// format the appt text
		appt.setText(AppointmentTextFormat.format(appt, d));

	}

	/**
	 * react to a delete request on the box.
	 */
	@Override
	public void delete() {
		AppointmentModel.getReference().delAppt(appt.getKey());
	}

	/**
	 * draw the box.
	 * 
	 * @param g2 the graphics to draw in
	 * @param comp the component that the graphics is in
	 */
	@Override
	public void draw(Graphics2D g2, Component comp) {
		Shape s = g2.getClip();
		if (clip != null)
			g2.setClip(clip);

		Font sm_font = g2.getFont();
		int smfontHeight = g2.getFontMetrics().getHeight();
		
		// set strike-through text map
		Map<TextAttribute, Serializable> stmap = new HashMap<TextAttribute, Serializable>();
		stmap.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		stmap.put(TextAttribute.FONT, sm_font);
		
		// determine appt text color based on "logical" color
		// which maps the legacy color names to the new user color prefs
		// the legacy names have no special meaning
		Color textColor = Color.BLACK;
		textColor = Color.BLACK;
		if (getTextColor().equals("red"))
			textColor = new Color(Integer.parseInt(Prefs
					.getPref(PrefName.UCS_RED)));
		else if (getTextColor().equals("green"))
			textColor = new Color(Integer.parseInt(Prefs
					.getPref(PrefName.UCS_GREEN)));
		else if (getTextColor().equals("blue"))
			textColor = new Color(Integer.parseInt(Prefs
					.getPref(PrefName.UCS_BLUE)));
		else if (getTextColor().equals("black"))
			textColor = new Color(Integer.parseInt(Prefs
					.getPref(PrefName.UCS_BLACK)));
		else if (getTextColor().equals("white"))
			textColor = new Color(Integer.parseInt(Prefs
					.getPref(PrefName.UCS_WHITE)));
		else if (getTextColor().equals("navy"))
			textColor = new Color(Integer.parseInt(Prefs
					.getPref(PrefName.UCS_NAVY)));
		else if (getTextColor().equals("purple"))
			textColor = new Color(Integer.parseInt(Prefs
					.getPref(PrefName.UCS_PURPLE)));
		else if (getTextColor().equals("brick"))
			textColor = new Color(Integer.parseInt(Prefs
					.getPref(PrefName.UCS_BRICK)));


		// resize todoIcon if needed to match the text size
		if (oldFontHeight != smfontHeight) {
			if (todoIcon != null) {
				try {
					// get todo marker image
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

		// draw the box
		Paint paint = g2.getPaint();
		if( Prefs.getBoolPref(PrefName.GRADIENT_APPTS)){
			GradientPaint gp = new GradientPaint(bounds.x, bounds.y + inset + smfontHeight, getBoxColor(),
					bounds.x, bounds.y + 2*bounds.height, textColor);
			g2.setPaint(gp);
		}
		else
		{
			g2.setColor(getBoxColor());
		}
		g2.fillRoundRect(bounds.x + inset, bounds.y
				+ inset, bounds.width - inset,
				bounds.height - inset, radius * radius, radius
				* radius);
		
		g2.setPaint(paint);
		// add a border around the box
		g2.setColor(textColor);
		if (isSelected) {
			// set the border to a different color if it is selected
			g2.setColor(Color.CYAN);
		}
		
		// draw the border
		g2.drawRoundRect(bounds.x + inset, bounds.y
				+ inset, bounds.width - inset,
				bounds.height - inset, radius * radius, radius
						* radius);
		

		// set the clip for the appt text
		g2.clipRect(bounds.x, bounds.y, bounds.width, bounds.height);

		g2.setColor(textColor);
		
		// appt text
		String text = getText();
		
		// prepend link marker
		if (hasLink) {
			text = "@ " + text;
		}
		

		// prepend todo marker and draw the string
		if (isTodo() && todoIcon != null) {
			todoIcon.paintIcon(comp, g2, bounds.x + radius, bounds.y + radius
					+ smfontHeight / 2);
			drawWrappedString(g2, text, bounds.x + radius
					+ todoIcon.getIconWidth(), bounds.y + radius, bounds.width
					- radius, getTextColor().equals("strike"));
		} else if (isTodo() && todoMarker != null) {
			drawWrappedString(g2, todoMarker + " " + text, bounds.x + radius,
					bounds.y + radius, bounds.width - radius, getTextColor().equals("strike"));
		} else {
			drawWrappedString(g2, text, bounds.x + radius, bounds.y + radius,
					bounds.width - radius, getTextColor().equals("strike"));
		}

		g2.setClip(s);
		g2.setColor(Color.black);
	}

	/**
	 * draw a string with word wrap.
	 */
	private void drawWrappedString(Graphics2D g2, String tx, int x, int y, int w, boolean strike) {
		int fontDesent = g2.getFontMetrics().getDescent();
		HashMap<TextAttribute, Serializable> hm = new HashMap<TextAttribute, Serializable>();
		hm.put(TextAttribute.FONT, g2.getFont());
		if( strike )
			hm.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		AttributedString as = new AttributedString(tx, hm);
		AttributedCharacterIterator para = as.getIterator();
		int start = para.getBeginIndex();
		int endi = para.getEndIndex();
		LineBreakMeasurer lbm = new LineBreakMeasurer(para,
				new FontRenderContext(null, true, false));
		lbm.setPosition(start);
		int tt = y + 2;
		while (lbm.getPosition() < endi) {
			TextLayout tlayout = lbm.nextLayout(w - (2 * fontDesent));
			tt += tlayout.getAscent();
			tlayout.draw(g2, x + 2, tt);
			tt += tlayout.getDescent() + tlayout.getLeading();
		}
	}

	/**
	 * Gets the bottom adjustment.
	 * 
	 * @return the bottom adjustment
	 */
	private double getBottomAdjustment() {
		return bottom;
	}

	/**
	 * Gets the box color 
	 * 	
	 * @return the box color
	 */
	private Color getBoxColor() {
		return new Color(Prefs
				.getIntPref(PrefName.UCS_DEFAULT));
	}

	/**
	 * Gets the date.
	 * 
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Gets the left adjustment.
	 * 
	 * @return the left adjustment
	 */
	private double getLeftAdjustment() {
		return left;
	}

	/**
	 * Gets the max across at one time.
	 * 
	 * @return the max across at one time
	 */
	private int getMaxAcrossAtOneTime() {
		return maxAcrossAtOneTime;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.ui.calendar.Box#getMenu()
	 */
	@Override
	public JPopupMenu getMenu() {
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
									appt.getKey(), false, date);
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
									appt.getKey(), true, date);
						} catch (Exception e) {
							Errmsg.errmsg(e);
						}
					}
				});
			}

			if (Repeat.isRepeating(appt)) {
				popmenu.add(mnuitm = new JMenuItem(Resource
						.getResourceString("Delete_One_Only")));
				mnuitm.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						try {
							AppointmentModel.getReference().delOneOnly(
									appt.getKey(), date);
						} catch (Exception e) {
							Errmsg.errmsg(e);
						}
					}
				});
			}
		}
		return popmenu;
	}

	/**
	 * Gets the right adjustment.
	 * 
	 * @return the right adjustment
	 */
	private double getRightAdjustment() {
		return right;
	}

	/* (non-Javadoc)
	 * @see net.sf.borg.ui.calendar.Box#getText()
	 */
	@Override
	public String getText() {
		return appt.getText();
	}

	/**
	 * Gets the text color.
	 * 
	 * @return the text color
	 */
	private String getTextColor() {
		if (appt == null)
			return null;

		if (ApptBoxPanel.isStrike(appt, date)) {
			return ("strike");
		}
		return appt.getColor();
	}

	/**
	 * Gets the top adjustment.
	 * 
	 * @return the top adjustment
	 */
	private double getTopAdjustment() {
		return top;
	}

	/**
	 * Checks if is placed.
	 * 
	 * @return true, if is placed
	 */
	private boolean isPlaced() {
		return isPlaced;
	}

	/**
	 * Checks if is todo.
	 * 
	 * @return true, if is todo
	 */
	private boolean isTodo() {
		return appt.getTodo();
	}

	/**
	 * move an appointment when the box is dragged
	 */
	@Override
	public void move(int realtime, Date d) throws Exception {

		Appointment ap = AppointmentModel.getReference().getAppt(appt.getKey());
		int oldday = DateUtil.dayOfEpoch(ap.getDate());

		int hour = realtime / 60;
		int min = realtime % 60;

		if (realtime == -1) {
			// we are moving to be untimed - clear the duration
			ap.setDuration(null);
			ap.setUntimed("Y");
			hour = 0;
			min = 0;
		}

		// set the new time
		GregorianCalendar newCal = new GregorianCalendar();
		newCal.setTime(d);
		newCal.set(Calendar.HOUR_OF_DAY, hour);
		int roundMin = (min / 5) * 5;
		newCal.set(Calendar.MINUTE, roundMin);
		Date newTime = newCal.getTime();
		ap.setDate(newTime);

		int newday = DateUtil.dayOfEpoch(newTime);
		
		// check if the user is trying to change the date of a repeating appt
		// and error if so
		if (oldday != newday && Repeat.isRepeating(ap)) {
			// cannot date chg unless it is on
			// the first in a series
			int k2 = DateUtil.dayOfEpoch(date);
			if (oldday != k2) {
				Errmsg.notice(Resource.getResourceString("rpt_drag_err"));
				return;
			}

		}
		
		AppointmentModel.getReference().saveAppt(ap);
	}

	/**
	 * react to a mouse click on the box
	 */
	@Override
	public void onClick() {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		
		// create an appt list view, add it as a new tab, and set it
		// to show the box's appt
		AppointmentListView ag = new AppointmentListView(
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
						.get(Calendar.DATE));
		ag.showView();
		ag.showApp(appt.getKey());

	}

	/**
	 * react to a box resize - change the appt duration
	 * 
	 * @param isTop true if the top edge was dragged
	 * @param realtime the time in minutes after midnight that the dragged edge was dragged to 
	 * 
	 * @throws Exception the exception
	 */
	public void resize(boolean isTop, int realtime) throws Exception {
		// calculate new start hour or duration and update appt

		int hour = realtime / 60;
		int min = realtime % 60;

		if (isTop) {
			
			// get appt from DB - one cached here has time prepended to text by
			// Day.getDayInfo()
			Appointment ap = AppointmentModel.getReference().getAppt(
					appt.getKey());
			
			Date oldTime = ap.getDate();
			
			// top moved - this changes the appt start time
			// so determine the new time
			GregorianCalendar newCal = new GregorianCalendar();
			newCal.setTime(oldTime);
			newCal.set(Calendar.HOUR_OF_DAY, hour);
			int roundMin = (min / 5) * 5;
			newCal.set(Calendar.MINUTE, roundMin);
			Date newTime = newCal.getTime();
			
			// determine the new duration
			int newDur = ap.getDuration().intValue()
					+ ((int) (oldTime.getTime() - newTime.getTime()) / (1000 * 60));
			
			// don't allow top to be dragged onto bottom - or below
			if (newDur < 5)
				return;
			
			// update appt
			ap.setDate(newTime);
			ap.setDuration(new Integer(newDur));
			AppointmentModel.getReference().saveAppt(ap);
			
		} else {
			// get appt from DB - one cached here has time prepended to text by
			// Day.getDayInfo()
			Appointment ap = AppointmentModel.getReference().getAppt(
					appt.getKey());
			Date start = ap.getDate();
			
			// remember original end time
			long endtime = start.getTime() + (60 * 1000)
					* ap.getDuration().intValue();
			Date oldEnd = new Date(endtime);
			
			// bottom is being dragged - end time will change
			Calendar newEnd = new GregorianCalendar();
			newEnd.setTime(oldEnd);
			newEnd.set(Calendar.HOUR_OF_DAY, hour);
			int roundMin = (min / 5) * 5;
			newEnd.set(Calendar.MINUTE, roundMin);
			
			
			// calculate new duration
			int newDur = (int) (newEnd.getTime().getTime() - start.getTime())
					/ (1000 * 60);
			
			// don't let user drag bottom over the top or above
			if (newDur < 5)
				return;
			
			// update appt
			ap.setDuration(new Integer(newDur));
			AppointmentModel.getReference().saveAppt(ap);
		}
	}

	/**
	 * Sets the bottom adjustment.
	 * 
	 * @param bottom the new bottom adjustment
	 */
	private void setBottomAdjustment(double bottom) {
		this.bottom = bottom;
	}

	/**
	 * Sets the left adjustment.
	 * 
	 * @param left the new left adjustment
	 */
	private void setLeftAdjustment(double left) {
		this.left = left;
	}

	/**
	 * Sets the max across at one time.
	 * 
	 * @param maxAcrossAtOneTime the new max across at one time
	 */
	private void setMaxAcrossAtOneTime(int maxAcrossAtOneTime) {
		this.maxAcrossAtOneTime = maxAcrossAtOneTime;
	}

	/**
	 * Sets the placed flag
	 * 
	 * @param isPlaced the new placed flag value
	 */
	private void setPlaced(boolean isPlaced) {
		this.isPlaced = isPlaced;
	}

	/**
	 * Sets the right adjustment.
	 * 
	 * @param right the new right adjustment
	 */
	private void setRightAdjustment(double right) {
		this.right = right;
	}

	/**
	 * Sets the top adjustment.
	 * 
	 * @param top the new top adjustment
	 */
	private void setTopAdjustment(double top) {
		this.top = top;
	}

	@Override
	public String getToolTipText() {
		return getText();
	}

}
