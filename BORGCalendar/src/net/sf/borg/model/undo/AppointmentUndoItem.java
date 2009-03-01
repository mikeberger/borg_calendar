package net.sf.borg.model.undo;

import java.text.DateFormat;

import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.beans.Appointment;

public class AppointmentUndoItem extends UndoItem<Appointment> {
	
	@Override
	public void executeUndo() {
		if( action == actionType.DELETE )
		{
			AppointmentModel.getReference().saveAppt(item, true, true);
		}
		else if( action == actionType.UPDATE )
		{
			AppointmentModel.getReference().saveAppt(item, false, true);
		}
		else if( action == actionType.ADD )
		{
			AppointmentModel.getReference().delAppt(item, true);
		}
	}
	
	private AppointmentUndoItem()
	{
		
	}
	
	static private String apptString(Appointment appt)
	{
		String txt = (appt.getText().length() < 20) ? appt.getText() : appt.getText().substring(0, 19);
		return "[" + DateFormat.getDateInstance(DateFormat.SHORT).format(appt.getDate()) +
		"] " + txt; 
	}
	
	public static AppointmentUndoItem recordUpdate(Appointment appt)
	{
		AppointmentUndoItem undoItem = new AppointmentUndoItem();
		undoItem.item = appt;
		undoItem.action = actionType.UPDATE;
		undoItem.setDescription(Resource.getPlainResourceString("Change") + " " + 
				Resource.getPlainResourceString("appointment") + " " + apptString(appt));
		return undoItem;
	}
	
	public static AppointmentUndoItem recordAdd(Appointment appt)
	{
		AppointmentUndoItem undoItem = new AppointmentUndoItem();
		undoItem.item = appt;
		undoItem.action = actionType.ADD;
		undoItem.setDescription(Resource.getPlainResourceString("Add") + " " + 
				Resource.getPlainResourceString("appointment") + " " + apptString(appt));
		return undoItem;
	}
	
	public static AppointmentUndoItem recordDelete(Appointment appt)
	{
		AppointmentUndoItem undoItem = new AppointmentUndoItem();
		undoItem.item = appt;
		undoItem.action = actionType.DELETE;
		undoItem.setDescription(Resource.getPlainResourceString("Delete") + " " +
				Resource.getPlainResourceString("appointment") + " " + apptString(appt));
		return undoItem;
	}

}
