package net.sf.borg.model.undo;

import java.text.DateFormat;

import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.beans.Appointment;

public class AppointmentUndoItem extends UndoItem {

	private enum actionType {
		ADD, DELETE, UPDATE
	}
	
	private Appointment appt;
	private actionType action;
	
	@Override
	public void executeUndo() {
		if( action == actionType.DELETE )
		{
			AppointmentModel.getReference().saveAppt(appt, true, true);
		}
		else if( action == actionType.UPDATE )
		{
			AppointmentModel.getReference().saveAppt(appt, false, true);
		}
		else if( action == actionType.ADD )
		{
			AppointmentModel.getReference().delAppt(appt, true);
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
		AppointmentUndoItem item = new AppointmentUndoItem();
		item.appt = appt;
		item.action = actionType.UPDATE;
		item.setDescription(Resource.getPlainResourceString("Change") + " " + 
				Resource.getPlainResourceString("appointment") + " " + apptString(appt));
		return item;
	}
	
	public static AppointmentUndoItem recordAdd(Appointment appt)
	{
		AppointmentUndoItem item = new AppointmentUndoItem();
		item.appt = appt;
		item.action = actionType.ADD;
		item.setDescription(Resource.getPlainResourceString("Add") + " " + 
				Resource.getPlainResourceString("appointment") + " " + apptString(appt));
		return item;
	}
	
	public static AppointmentUndoItem recordDelete(Appointment appt)
	{
		AppointmentUndoItem item = new AppointmentUndoItem();
		item.appt = appt;
		item.action = actionType.DELETE;
		item.setDescription(Resource.getPlainResourceString("Delete") + " " +
				Resource.getPlainResourceString("appointment") + " " + apptString(appt));
		return item;
	}

}
