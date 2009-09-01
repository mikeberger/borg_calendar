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

import java.util.Date;

/**
 * Interface implemented by day/week/month panel Boxes that can be dragged
 */
interface Draggable extends Box{

	/**
	 * called when object has been moved
	 * @param realtime time of day in minutes to which the object has been dragged
	 * @param d date that the object was dragged to
	 * @throws Exception
	 */
    public abstract void move(int realtime, Date d) throws Exception;
}
