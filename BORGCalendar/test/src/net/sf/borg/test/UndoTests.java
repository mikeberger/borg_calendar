package net.sf.borg.test;


import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;

import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.LinkModel;
import net.sf.borg.model.beans.Appointment;
import net.sf.borg.model.undo.UndoLog;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UndoTests {

	@Before
	public void setUp() throws Exception {
		// open the borg appt db - in memory
		AppointmentModel.create().open_db("jdbc:hsqldb:mem:whatever");
		LinkModel.create().open_db("jdbc:hsqldb:mem:whatever");
	}
	
	@Test
	public void testAppointmentUndo() throws Exception
	{
		int num_appts = AppointmentModel.getReference().getAllAppts().size();
		assertTrue( "Appointment DB should be empty to start", num_appts == 0);
		
		Appointment appt = new Appointment();
		appt.setText("my appointment");
		appt.setDate(new Date());
		
		AppointmentModel.getReference().saveAppt(appt, true);
		
		Collection<Appointment> coll = AppointmentModel.getReference().getAllAppts();
		assertTrue( "Appointment DB should contain 1 appt", coll.size() == 1);
		
		// update the appt
		appt = coll.iterator().next();
		appt.setText("my updated appt");
		AppointmentModel.getReference().saveAppt(appt, false);
		
		// verify that the appt is updated
		coll = AppointmentModel.getReference().getAllAppts();
		assertTrue( "Appointment DB should contain 1 appt", coll.size() == 1);
		appt = coll.iterator().next();
		assertTrue("Appointment was not updated", "my updated appt".equals(appt.getText()));
		
		// delete the appt
		AppointmentModel.getReference().delAppt(appt);
		coll = AppointmentModel.getReference().getAllAppts();
		assertTrue( "Appointment DB should contain 0 appts", coll.size() == 0);
		
		// let the undos begin
		
		// undo the delete
		UndoLog.getReference().executeUndo();
		
		// verify that the appt is the updated one
		coll = AppointmentModel.getReference().getAllAppts();
		assertTrue( "Appointment DB should contain 1 appt", coll.size() == 1);
		appt = coll.iterator().next();
		assertTrue("Appointment was not updated", "my updated appt".equals(appt.getText()));
		
		// undo the update
		UndoLog.getReference().executeUndo();
		
		// verify that the appt is the original one
		coll = AppointmentModel.getReference().getAllAppts();
		assertTrue( "Appointment DB should contain 1 appt", coll.size() == 1);
		appt = coll.iterator().next();
		assertTrue("Appointment was not undone", "my appointment".equals(appt.getText()));
		
		//undo the add
		UndoLog.getReference().executeUndo();
		
		coll = AppointmentModel.getReference().getAllAppts();
		assertTrue( "Appointment DB should contain 0 appts after add undone", coll.size() == 0);
	}
	
	@After
	public void tearDown()
	{
		AppointmentModel.getReference().remove();
	}

}
