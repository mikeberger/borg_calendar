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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.CalendarEntity;
import net.sf.borg.model.entity.LabelEntity;

/**
 * ApptBoxPanel is the base class for Panels that act as containers for
 * Box and DateZone objects. It manages the layout of Boxes and the various operations
 * that can be done on them - such as dragging, resizing, clicking
 */
abstract class ApptBoxPanel extends JPanel implements ComponentListener {

	private static final long serialVersionUID = 1L;
	
	// rectangle corner radius
	private static final int radius = 2;

	/**
	 * ClickedBoxInfo contains information about where the mouse is and what
	 * Box and/or DateZone it is within
	 */
	private class ClickedBoxInfo {
		public Box box = null; //box that the mouse is in
		public boolean onBottomBorder = false; //true if mouse is on the bottom border of a box
		public boolean onTopBorder = false; // true if the mouse is on the top border of a box
		public DateZone zone = null; // datezone that the mouse is in
		public boolean boxChanged = false; // true if we've changed boxes since the last check
	}

	/**
	 * DragNewBox is a Box only used by ApptBoxPanel. It is the Box drawn as the
	 * user drags the mouse to create a new, timed appointment
	 */
	private class DragNewBox extends Box {
		
		// border thickness
		private BasicStroke thicker = new BasicStroke(4.0f);

		// zone that the box is in
		DateZone zone;

		// popup menu
		JPopupMenu pop = null;

		/**
		 * Instantiates a new drag new box.
		 * 
		 * @param bounds the bounds
		 * @param clip the clip
		 */
		public DragNewBox(DateZone zone) {
			super(null, null);
			this.zone = zone;
		}

		/**
		 * Adds an appointment based on the location as size of this Box
		 */
		private void addAppt() {
			
			// prompt for appt text
			String text = JOptionPane.showInputDialog("", Resource
					.getResourceString("Please_enter_some_appointment_text"));
			if (text == null)
				return;	

			// get default appt values, if any from prefs
			Appointment appt = AppointmentModel.getReference().getDefaultAppointment();

			// get a new appt if no defaults
			if (appt == null) {
				appt = AppointmentModel.getReference().newAppt();
			}

			// set text
			appt.setText(text);


			// determine the appt time and duration based on the size of this box
			Rectangle r = getBounds();
			int topmins = realMins((r.y - resizeYMin)
					/ (resizeYMax - resizeYMin));
			int botmins = realMins((r.y - resizeYMin + r.height)
					/ (resizeYMax - resizeYMin));
			int realtime = topmins;
			int hour = realtime / 60;
			int min = realtime % 60;
			min = (min / 5) * 5;
			Calendar startCal = new GregorianCalendar();
			startCal.setTime(zone.getDate());
			startCal.set(Calendar.HOUR_OF_DAY, hour);
			startCal.set(Calendar.MINUTE, min);
			appt.setDate(startCal.getTime());

			// duration
			int realend = botmins;
			int ehour = realend / 60;
			int emin = realend % 60;
			emin = (emin / 5) * 5;
			int dur = 60 * (ehour - hour) + emin - min;
			appt.setDuration(new Integer(dur));

			// set untimed if no duration
			if (dur > 0)
				appt.setUntimed("N");
			else
				appt.setUntimed("Y");
			
			// save appt
			AppointmentModel.getReference().saveAppt(appt);

			// remove the DragNewBox
			removeDragNewBox();
			repaint();
		}

		/**
		 * draw the DragNewBox - it is just a rounded rectangle outline with
		 * start and end time indicators
		 */
		@Override
		public void draw(Graphics2D g2, Component comp) {
			
			// border
			Stroke stroke = g2.getStroke();
			g2.setStroke(thicker);
			g2.setColor(Color.GREEN);
			if (isSelected == true) {
				g2.setColor(Color.CYAN);
			}
			Rectangle r = getBounds();
			g2.drawRoundRect(r.x+2, r.y, r.width-2, r.height, radius * radius,
					radius * radius);
			
			// start and end time indicators
			g2.setStroke(stroke);
			double top = (r.y - resizeYMin) / (resizeYMax - resizeYMin);
			double bot = (r.y - resizeYMin + r.height)
					/ (resizeYMax - resizeYMin);
			g2.setColor(new Color(50, 50, 50));
			Rectangle2D bb = g2.getFont().getStringBounds("00:00",
					g2.getFontRenderContext());
			g2.fillRect(r.x + 2, r.y - (int) bb.getHeight(), (int) bb
					.getWidth(), (int) bb.getHeight());
			g2.fillRect(r.x + 2, r.y + r.height - (int) bb.getHeight(),
					(int) bb.getWidth(), (int) bb.getHeight());
			g2.setColor(Color.WHITE);
			g2.drawString(getTimeString(top), r.x + 2, r.y - 2);
			g2.drawString(getTimeString(bot), r.x + 2, r.y + r.height - 2);

		}

		@Override
		public JPopupMenu getMenu() {
			if (pop == null) {
				JMenuItem mnuitm = null;
				pop = new JPopupMenu();
				pop.add(mnuitm = new JMenuItem(Resource
						.getResourceString("Add_New")));
				mnuitm.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						addAppt();
					}
				});
			}
			return pop;
		}

		@Override
		public void onClick() {
			addAppt();
		}

		@Override
		public String getText() {
			return null;
		}

		@Override
		public String getToolTipText() {
			return null;
		}

	}

	/**
	 * MyMouseListener reacts to all mouse events
	 */
	private class MyMouseListener implements MouseListener, MouseMotionListener {

		// during a resize, if true indicates that we are dragging the top of a
		// box, otherwise, the bottom is being dragged
		private boolean resizeTop = true;

		/**
		 * mouse clicked - either show a popup menu (right button) or 
		 * send an onclick message to a box
		 */
		@Override
		public void mouseClicked(MouseEvent evt) {

			evt.translatePoint(translation, translation);
			
			// get the box and/or zone clicked
			ClickedBoxInfo b = getClickedBoxInfo(evt);
			
			// right click
			if (evt.getButton() == MouseEvent.BUTTON3) {

				// if no box or zone - do nothing
				if (b == null)
					return;

				// show a box's menu
				if (b.box != null) {
					if (b.box.getMenu() != null) {
						b.box.getMenu().show(evt.getComponent(), evt.getX(),
								evt.getY());
					}
				// show a zone's menu
				} else if (b.zone != null) {
					b.zone.getMenu().show(evt.getComponent(), evt.getX(),
							evt.getY());
				}

				return;
			}
			
			if( !SwingUtilities.isLeftMouseButton(evt))
				return;

			evt.getComponent().repaint();

			// call onClick() if we clicked on anything
			if (b == null)
				return;
			else if (b.box != null)
			{
				if( b.box.clicksToActivate() <= evt.getClickCount())
					b.box.onClick();
			}
			else if (b.zone != null && evt.getClickCount() > 1)
				b.zone.onClick();

		}

		/**
		 * mouse dragged - but not yet released
		 */
		@Override
		public void mouseDragged(MouseEvent evt) {
			evt.translatePoint(translation, translation);

			// ignore right-click drag
			if( !SwingUtilities.isLeftMouseButton(evt))
				return;

			// in-a-drag flag
			dragStarted = true;
			
			// if we are resizing a box
			if (resizedBox != null) {
				
				// adjust the size of the displayed resize rectangle due to the
				// mouse drag
				if (resizeTop == true) {
					int top = (int) Math.max(evt.getY(), resizeYMin);
					setResizeBox(resizedBox.getBounds().x, top, resizedBox
							.getBounds().width, resizedBox.getBounds().height
							+ resizedBox.getBounds().y - top);
				} else {
					int bot = (int) Math.min(evt.getY(), resizeYMax);
					setResizeBox(resizedBox.getBounds().x, resizedBox
							.getBounds().y, resizedBox.getBounds().width, bot
							- resizedBox.getBounds().y);
				}
				evt.getComponent().repaint();
			// if we are dragging a box around (a move)
			} else if (draggedBox != null) {

				// draw a "move" rectangle to show where the box is moving
				// we just use the resize box for this since that mechanism is already there
				int top = evt.getY() - (draggedBox.getBounds().height / 2);
				if (top < dragYMin)
					top = (int) dragYMin;
				if (top + draggedBox.getBounds().height > dragYMax)
					top = (int) dragYMax - draggedBox.getBounds().height;
				int left = evt.getX() - (draggedBox.getBounds().width / 2);
				if (left < dragXMin)
					left = (int) dragXMin;
				if (left + draggedBox.getBounds().width > dragXMax)
					left = (int) dragXMax - draggedBox.getBounds().width;
				setResizeBox(left, top, draggedBox.getBounds().width,
						draggedBox.getBounds().height);

				evt.getComponent().repaint();
				
			// if we are dragging out the outline of a new appointment
			} else if (draggedAnchor != -1) {
				
				ClickedBoxInfo b = getClickedBoxInfo(evt);
				
				// create the DragNewBox if it doesn;t yet exist
				if (dragNewBox == null)
				{	
					dragNewBox = new DragNewBox(b.zone);
					setDragNewBox(b.zone.getBounds().x, evt.getY(),
							b.zone.getBounds().width, 5);
				}
				
				// drag out the DragNewBox - but don't allow a drag beyond
				// resizeYMin and resizeYMax - the bounds allowed for dragging
				double y = evt.getY();
				y = Math.max(y, resizeYMin);
				y = Math.min(y, resizeYMax);
				Rectangle r = dragNewBox.getBounds();
				
				// draw the DragNewBox above or below the starting (anchor)
				// point, depending on if we are dragging up or down
				if (y > draggedAnchor) {
					setDragNewBox(r.x, r.y, r.width, y - draggedAnchor);
				} else {
					setDragNewBox(r.x, y, r.width, draggedAnchor - y);
				}
				evt.getComponent().repaint();
			}
		}

		/** not used */
		@Override
		public void mouseEntered(MouseEvent evt) {
		  // empty
		}

		/** not used */
		@Override
		public void mouseExited(MouseEvent evt) {
		  // empty
		}

		/**
		 * mouse moved without being pressed - do mouse-over type stuff
		 */
		@Override
		public void mouseMoved(MouseEvent evt) {
			evt.translatePoint(translation, translation);

			
			JPanel panel = (JPanel) evt.getComponent();

			// get box or zone we are within
			ClickedBoxInfo b = getClickedBoxInfo(evt);
			
			// set tool tip text
			if (b != null && b.box != null  ) {
				panel.setToolTipText(b.box.getToolTipText());
			}
			else
			{
				panel.setToolTipText(null);
			}

			// set the mouse cursor depending on where we are
			// in relation to a box - border vs. inside vs outside
			if (b != null && (b.onTopBorder || b.onBottomBorder)) {
				panel.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
			} else if (b != null && b.box != null
					&& b.box instanceof Box.Draggable) {
				panel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			} else {
				panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
			
			// only repaint if we have moved to a different box or zone
			if( b != null && b.boxChanged )
				evt.getComponent().repaint();
		}

		/**
		 * react to mouse press and not yet released
		 */
		@Override
		public void mousePressed(MouseEvent evt) {

			evt.translatePoint(translation, translation);

			// ignore right click
			if( !SwingUtilities.isLeftMouseButton(evt))
				return;

			// get box or zone we are in
			ClickedBoxInfo b = getClickedBoxInfo(evt);
			
			// if we press in a box other than the DragNewBox - then
			// get rid of the DragNewBox
			if (b == null || b.box != dragNewBox)
				removeDragNewBox();

			// reset drag started (end any current drag)
			dragStarted = false;

			// if we are on the border of a box - start a resize
			if (b != null && (b.onTopBorder || b.onBottomBorder)) {
				// start resize of an appointment box
				if (b.box instanceof ApptBox) {
					resizedBox = (ApptBox) b.box;
					setResizeBox(b.box.getBounds().x, b.box.getBounds().y,
							b.box.getBounds().width, b.box.getBounds().height);
					if (b.onBottomBorder) {
						resizeTop = false;
					} else {
						resizeTop = true;
					}
				// start resize of the drag new box
				} else if (b.box == dragNewBox) {
					if (b.onBottomBorder) {
						draggedAnchor = dragNewBox.getBounds().y;
					} else {
						draggedAnchor = dragNewBox.getBounds().y
								+ dragNewBox.getBounds().height;
					}
				}
				evt.getComponent().repaint();
			// start drag (move) of a Box
			} else if (b != null && b.box != null
					&& b.box instanceof Box.Draggable) {
				draggedBox = b.box;
				setResizeBox(b.box.getBounds().x, b.box.getBounds().y, b.box
						.getBounds().width, b.box.getBounds().height);
				evt.getComponent().repaint();
			// if we have pressed inside a zone, this is the start of a 
			// DragNewBox - so set the anchor point and record the zone.
			// *** only do this if we are inside the resize bounds
		    // this is the magic spot where we enforce sweeping out a new item
			// only in certain spots
			} else if (b != null && b.zone != null && evt.getY() > resizeYMin
					&& evt.getY() < resizeYMax) {
				draggedAnchor = evt.getY();
			}

			// set the mouse cursor depending on where we are
			// in relation to a box - border vs. inside vs outside
			JPanel panel = (JPanel) evt.getComponent();
			if (b != null && (b.onTopBorder || b.onBottomBorder)) {
				panel.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
			} else if (b != null && b.box != null
					&& b.box instanceof Box.Draggable) {
				panel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			} else {
				panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
		}

		/**
		 * mouse has been released after a press and possible drag
		 */
		@Override
		public void mouseReleased(MouseEvent evt) {
			evt.translatePoint(translation, translation);

			// ignore release of right button
			if (evt.getButton() == MouseEvent.BUTTON3)
				return;
			
			// if we have been resizing, then send a resize call to
			// the resized box so it can deal with the resize
			if (resizedBox != null && dragStarted) {
				double y = evt.getY();
				y = Math.max(y, resizeYMin);
				y = Math.min(y, resizeYMax);
				try {
					resizedBox.resize(resizeTop, realMins((y - resizeYMin)
							/ (resizeYMax - resizeYMin)));
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			// if we have been moving a box, then call its move method
			// so that it can deal with it
			} else if (draggedBox != null && dragStarted) {
				double y = resizeRectangle.y;
				y = Math.max(y, resizeYMin);
				y = Math.min(y, resizeYMax);

				double centerx = evt.getX();
				double centery = evt.getY();
				Date d = getDateForCoord(centerx, centery);
				try {
					// if we moved the box inside the resize area, send it the
					// new start time info
					if (isInsideResizeArea(resizeRectangle.y, resizeRectangle.y
							+ resizeRectangle.height)) {
						((Box.Draggable) draggedBox).move(
								realMins((y - resizeYMin)
										/ (resizeYMax - resizeYMin)), d);
					} else {
						// we moved the box outside of the resize area, so 
						// send no time info (the resize area == the time grid)
						((Box.Draggable) draggedBox).move(-1, d);
					}
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}

			}

			// reset resizing and dragging
			draggedBox = null;
			resizedBox = null;
			removeResizeBox();
			evt.getComponent().repaint();

		}
	}
	
	// format for time markers on resize/drag box
	private static SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");


	// to adjust, since since Graphics2D is translated
	final static private int translation = -10; 

	/**
	 * Checks if entity should be shown as strike-through on a certain date.
	 * 
	 * @param appt the entity
	 * @param date the date
	 * 
	 * @return true, if is strike
	 */
	public static boolean isStrike(CalendarEntity appt, Date date) {
		if ((appt.getColor() != null && appt.getColor().equals("strike"))
				|| (appt.getTodo() && !(appt.getNextTodo() == null || !appt
						.getNextTodo().after(date)))) {
			return (true);
		}
		return false;
	}

	/** the Boxes managed by this container */
	protected Collection<Box> boxes = new ArrayList<Box>();
	
	/** current box we are in - to limit repaints */
	private Box currentBox = null;

	// position of drag start
	private int draggedAnchor = -1;

	// Box that is currently being dragged around
	private Box draggedBox = null;

	// Box that shows a brand new timed appt being dragged out
	private DragNewBox dragNewBox = null;

	// flag to indicate we have started dragging
	private boolean dragStarted = false;

	// bounds which limit where a box can be dragged
	private double dragXMax = 0;
	private double dragXMin = 0;
	private double dragYMax = 0;
	private double dragYMin = 0;

	// end time of the timed zone
	protected double endmin = 0;

	// box that is being resized
	private ApptBox resizedBox = null;

	// rectangle to show a resize or move in progress
	private Rectangle resizeRectangle = null;

	// bounds which limit where resizing can occur - also limits
	// where new appts can be dragged out
	private double resizeYMax = 0;
	private double resizeYMin = 0;

	// start time of the timed zone
	protected double startmin = 0;

	// DateZones managed by this container
	private Collection<DateZone> zones = new ArrayList<DateZone>();
	
	/**
	 * Instantiates a new appt box panel.
	 */
	public ApptBoxPanel() {

		// set up event listeners
		MyMouseListener myOneListener = new MyMouseListener();
		addMouseListener(myOneListener);
		addMouseMotionListener(myOneListener);
		addComponentListener(this);

	}

	/**
	 * Adds an appointment to the container. Creates an ApptBox to contain it
	 * 
	 * @param d the date of the box - not aleays the appt date
	 * @param ap the appointment
	 * @param bounds the bounds
	 * @param clip the clip
	 */
	protected void addApptBox(Date d, Appointment ap, Rectangle bounds,
			Rectangle clip) {

		if (Prefs.getBoolPref(PrefName.HIDESTRIKETHROUGH)
				&& ApptBoxPanel.isStrike(ap, d))
			return;
		ApptBox b = new ApptBox(d, ap, bounds, clip);

		boxes.add(b);
	}

	/**
	 * Adds a date zone to this container
	 * 
	 * @param d the date
	 * @param bounds the bounds of the zone
	 */
	public void addDateZone(Date d, Rectangle bounds) {
		DateZone b = new DateZone(d, bounds);
		zones.add(b);
	}

	/**
	 * Adds the note box to this container
	 * 
	 * @param d the date
	 * @param ap the calendar entity
	 * @param bounds the bounds
	 * @param clip the clip
	 * 
	 * @return the box
	 */
	public Box addNoteBox(Date d, CalendarEntity ap, Rectangle bounds,
			Rectangle clip) {

		// ignore the note box if it is strike-through and the option
		// to hide strike through is set
		if (Prefs.getBoolPref(PrefName.HIDESTRIKETHROUGH)
				&& ApptBoxPanel.isStrike(ap, d))
			return null;

		Box b;
		if (ap instanceof LabelEntity) {
			// phony holiday appt added by Day object
			b = new LabelBox((LabelEntity) ap, bounds, clip);
		} else {
			b = new NoteBox(d, ap, bounds, clip);
		}
		
		boxes.add(b);

		return b;
	}

	/**
	 * Clear boxes and zones.
	 */
	public void clearBoxes() {
		boxes.clear();
		zones.clear();
	}

	/**
	 * Draw boxes.
	 * 
	 * @param g2 the Graphics to draw in
	 */
	public void drawBoxes(Graphics2D g2) {

		// draw each box
		for(Box b : boxes) {
			b.draw(g2, this);
		}

		// draw the resize rectangle if needed
		if (resizeRectangle != null) {
			g2.setColor(Color.RED);
			g2.drawRoundRect(resizeRectangle.x, resizeRectangle.y,
					resizeRectangle.width, resizeRectangle.height, radius
							* radius, radius * radius);

			// draw time indicators
			if (isInsideResizeArea(resizeRectangle.y, resizeRectangle.y
					+ resizeRectangle.height)) {
				double top = (resizeRectangle.y - resizeYMin)
						/ (resizeYMax - resizeYMin);
				double bot = (resizeRectangle.y - resizeYMin + resizeRectangle.height)
						/ (resizeYMax - resizeYMin);
				g2.setColor(new Color(50, 50, 50));
				Rectangle2D bb = g2.getFont().getStringBounds("00:00",
						g2.getFontRenderContext());
				g2.fillRect(resizeRectangle.x + 2, resizeRectangle.y
						- (int) bb.getHeight(), (int) bb.getWidth(), (int) bb
						.getHeight());
				g2.fillRect(resizeRectangle.x + 2, resizeRectangle.y
						+ resizeRectangle.height - (int) bb.getHeight(),
						(int) bb.getWidth(), (int) bb.getHeight());
				g2.setColor(Color.WHITE);
				g2.drawString(getTimeString(top), resizeRectangle.x + 2,
						resizeRectangle.y - 2);
				g2.drawString(getTimeString(bot), resizeRectangle.x + 2,
						resizeRectangle.y + resizeRectangle.height - 2);
			}
		}
		
		// draw drag new box if needed
		if (dragNewBox != null) {
			dragNewBox.draw(g2, this);
		}
		g2.setColor(Color.black);

	}

	/**
	 * Gets info on which zone and/or box the mouse is in - plus if we are
	 * on the top or bottom border of the box
	 * 
	 * @param evt the MouseEvent
	 * 
	 * @return the ClickedBoxInfo
	 */
	private ClickedBoxInfo getClickedBoxInfo(MouseEvent evt) {
		
		boolean onTopBorder = false;
		boolean onBottomBorder = false;
		ClickedBoxInfo ret = new ClickedBoxInfo();

		// check if we are in the drag new box
		if (dragNewBox != null) {
			Rectangle r = dragNewBox.getBounds();
			if (evt.getX() > r.x && evt.getX() < (r.x + r.width)
					&& evt.getY() > r.y && evt.getY() < (r.y + r.height)) {
				ret.box = dragNewBox;
				dragNewBox.setSelected(true);

				if (Math.abs(evt.getY() - r.y) < 4) {
					onTopBorder = true;
				} else if (Math.abs(evt.getY() - (r.y + r.height)) < 4) {
					onBottomBorder = true;
				}

			} else {
				dragNewBox.setSelected(false);
			}
		}

		// check if we are in any boxes (drag new box above takes priority)
		for(Box b : boxes){
			
			if (ret.box == null && evt.getX() > b.getBounds().x
					&& evt.getX() < (b.getBounds().x + b.getBounds().width)
					&& evt.getY() > b.getBounds().y
					&& evt.getY() < (b.getBounds().y + b.getBounds().height)) {

				b.setSelected(true);
				ret.box = b;
				if (b instanceof ApptBox) {
					if (Math.abs(evt.getY() - b.getBounds().y) < 4) {
						onTopBorder = true;
					} else if (Math.abs(evt.getY()
							- (b.getBounds().y + b.getBounds().height)) < 4) {
						onBottomBorder = true;
					}
				}
			} else {
				b.setSelected(false);
			}
		}

		// checl if we are in a date zone
		for( DateZone b : zones ) {

			if (evt.getX() > b.getBounds().x
					&& evt.getX() < (b.getBounds().x + b.getBounds().width)
					&& evt.getY() > b.getBounds().y
					&& evt.getY() < (b.getBounds().y + b.getBounds().height)) {
				ret.zone = b;
				break;
			}
		}

		ret.onTopBorder = onTopBorder;
		ret.onBottomBorder = onBottomBorder;
		
		if( ret.box != currentBox )
			ret.boxChanged = true;
		currentBox = ret.box;

		return ret;

	}

	/**
	 * Gets the date for a mouse coordinate
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * 
	 * @return the date for coord
	 */
	abstract Date getDateForCoord(double x, double y);

	/**
	 * Gets the time string for a given y coordinate
	 * 
	 * @param y_fraction the y_fraction
	 * 
	 * @return the time string
	 */
	public String getTimeString(double y_fraction) {

		int realtime = realMins(y_fraction);
		int hour = realtime / 60;
		int min = realtime % 60;
		GregorianCalendar newCal = new GregorianCalendar();
		newCal.set(Calendar.HOUR_OF_DAY, hour);
		int roundMin = (min / 5) * 5;
		newCal.set(Calendar.MINUTE, roundMin);
		Date newTime = newCal.getTime();
		return sdf.format(newTime);
	}

	/**
	 * Checks if a box is completely inside the resize area.
	 * 
	 * @param top the top
	 * @param bot the bottom
	 * 
	 * @return true, if is inside resize area
	 */
	private boolean isInsideResizeArea(int top, int bot) {
		if (top >= resizeYMin && bot <= resizeYMax)
			return true;
		return false;

	}

	/**
	 * get the time as minutes past midnight for a fraction representing
	 * how far we are between start and end of the time zone
	 * 
	 * @param y_fraction the y_fraction
	 * 
	 * @return the time as minutes past midnight
	 */
	private int realMins(double y_fraction) {
		double realtime = startmin + (endmin - startmin) * y_fraction;
		// round it because the double math is causing errors when later
		// converting to int
		int min = 5 * (int) Math.round(realtime / 5);
		return min;
	}

	/**
	 * Refresh.
	 */
	public abstract void refresh();

	/**
	 * Removes the drag new box.
	 */
	protected void removeDragNewBox() {
		dragNewBox = null;
		draggedAnchor = -1;
	}

	/**
	 * Removes the resize box.
	 */
	public void removeResizeBox() {
		resizeRectangle = null;
	}

	/**
	 * Sets the drag bounds to limit where items can be dragged.
	 * 
	 * @param ymin the y minimum
	 * @param ymax the y maximum
	 * @param xmin the x minimum
	 * @param xmax the x maximum
	 */
	protected void setDragBounds(int ymin, int ymax, int xmin, int xmax) {
		dragYMin = ymin;
		dragYMax = ymax;
		dragXMin = xmin;
		dragXMax = xmax;
	}

	/**
	 * Sets the bounds for the drag new box
	 * 
	 * @param x the x coord
	 * @param y the y coord
	 * @param w the width
	 * @param h the height
	 */
	protected void setDragNewBox(double x, double y, double w, double h) {

		Rectangle bounds = new Rectangle();
		bounds.x = (int) x;
		bounds.y = (int) y;
		bounds.height = (int) h;
		bounds.width = (int) w;
		dragNewBox.setBounds(bounds);

	}

	/**
	 * Sets the resize bounds which limit where a resized object border can be dragged and
	 * where a drag new box can be started
	 * 
	 * @param ymin the y minimum
	 * @param ymax the y maximum
	 */
	protected void setResizeBounds(int ymin, int ymax) {
		resizeYMin = ymin;
		resizeYMax = ymax;
	}

	/**
	 * Sets the resize box bounds.
	 * 
	 * @param x the x coord
	 * @param y the y coord
	 * @param w the width
	 * @param h the height
	 */
	protected void setResizeBox(double x, double y, double w, double h) {
		if (resizeRectangle == null)
			resizeRectangle = new Rectangle();
		resizeRectangle.x = (int) x;
		resizeRectangle.y = (int) y;
		resizeRectangle.height = (int) h;
		resizeRectangle.width = (int) w;

	}
	
	@Override
	public void componentHidden(ComponentEvent arg0) {
	  // empty
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	  // empty
	}

	@Override
	public void componentResized(ComponentEvent e) {
		refresh();
	}

	@Override
	public void componentShown(ComponentEvent e) {
	  // empty
	}

}
