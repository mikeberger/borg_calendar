package net.sf.borg.plugin.sync;

import net.sf.borg.model.entity.Appointment;

public interface AppointmentAdapter<T> {

	public T fromBorg(Appointment appt) throws Exception;
	public Appointment toBorg(T extAppt) throws Exception;
	
}
