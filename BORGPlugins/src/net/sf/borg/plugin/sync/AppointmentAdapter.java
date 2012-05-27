package net.sf.borg.plugin.sync;

import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.CalendarEntity;

public interface AppointmentAdapter<T> {

	public T fromBorg(CalendarEntity appt) throws Exception;
	public Appointment toBorg(T extAppt) throws Exception;
	
}
