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
package net.sf.borg.model.undo;

import java.text.DateFormat;

import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.entity.Appointment;

public class AppointmentUndoItem extends UndoItem<Appointment> {

	private AppointmentUndoItem moveFrom;
	private AppointmentUndoItem moveTo;

	@Override
	public void executeUndo() {
		if (action == actionType.DELETE) {
			AppointmentModel.getReference().saveAppt(item, true, true);
		} else if (action == actionType.UPDATE) {
			AppointmentModel.getReference().saveAppt(item, false, true);
		} else if (action == actionType.ADD) {
			AppointmentModel.getReference().delAppt(item, true);
		} else if (action == actionType.MOVE) {
			moveTo.executeUndo();
			moveFrom.executeUndo();
		}
	}

	private AppointmentUndoItem() {

	}

	static private String apptString(Appointment appt) {
		String txt = (appt.getText().length() < 20) ? appt.getText() : appt
				.getText().substring(0, 19);
		return "["
				+ DateFormat.getDateInstance(DateFormat.SHORT).format(
						appt.getDate()) + "] " + txt;
	}

	public static AppointmentUndoItem recordUpdate(Appointment appt) {
		AppointmentUndoItem undoItem = new AppointmentUndoItem();
		undoItem.item = appt;
		undoItem.action = actionType.UPDATE;
		undoItem.setDescription(Resource.getPlainResourceString("Change") + " "
				+ Resource.getPlainResourceString("appointment") + " "
				+ apptString(appt));
		return undoItem;
	}

	public static AppointmentUndoItem recordAdd(Appointment appt) {
		AppointmentUndoItem undoItem = new AppointmentUndoItem();
		undoItem.item = appt;
		undoItem.action = actionType.ADD;
		undoItem.setDescription(Resource.getPlainResourceString("Add") + " "
				+ Resource.getPlainResourceString("appointment") + " "
				+ apptString(appt));
		return undoItem;
	}

	public static AppointmentUndoItem recordDelete(Appointment appt) {
		AppointmentUndoItem undoItem = new AppointmentUndoItem();
		undoItem.item = appt;
		undoItem.action = actionType.DELETE;
		undoItem.setDescription(Resource.getPlainResourceString("Delete") + " "
				+ Resource.getPlainResourceString("appointment") + " "
				+ apptString(appt));
		return undoItem;
	}

	@SuppressWarnings("unchecked")
	public static AppointmentUndoItem recordMove(Appointment appt) {
		AppointmentUndoItem undoItem = new AppointmentUndoItem();
		undoItem.item = appt;
		undoItem.action = actionType.MOVE;

		// need to find the add and deletes that make up this move
		UndoItem u1 = UndoLog.getReference().pop();
		UndoItem u2 = UndoLog.getReference().pop();
		if (u1 instanceof AppointmentUndoItem
				&& u2 instanceof AppointmentUndoItem) {
			undoItem.moveFrom = (AppointmentUndoItem) u1;
			undoItem.moveTo = (AppointmentUndoItem) u2;
		} else {
			UndoLog.getReference().addItem(u1);
			UndoLog.getReference().addItem(u2);
			return null;
		}

		undoItem.setDescription(Resource.getPlainResourceString("move") + " "
				+ Resource.getPlainResourceString("appointment") + " "
				+ apptString(appt));
		return undoItem;
	}

}
