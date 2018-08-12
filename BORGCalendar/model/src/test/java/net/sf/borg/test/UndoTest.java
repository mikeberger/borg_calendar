package net.sf.borg.test;


import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.Address;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.Memo;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.Task;
import net.sf.borg.model.undo.UndoLog;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class UndoTest {

	@BeforeClass
	public static void setUp() throws Exception {
		// open the borg dbs - in memory
		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());
		DBHelper.getController().connect("jdbc:hsqldb:mem:whatever");
		
	}
	
	@Test
	public void testAppointmentUndo() throws Exception
	{
		int num_appts = AppointmentModel.getReference().getAllAppts().size();
		assertTrue( "Appointment DB should be empty to start", num_appts == 0);
		
		Appointment appt = new Appointment();
		appt.setText("my appointment");
		appt.setDate(new Date());
		
		Appointment appt2 = new Appointment();
		appt2.setText("my appointment 2");
		appt2.setDate(new Date());
		
		AppointmentModel.getReference().saveAppt(appt);
		AppointmentModel.getReference().saveAppt(appt2);
		
		Collection<Appointment> coll = AppointmentModel.getReference().getAllAppts();
		assertTrue( "Appointment DB should contain 2 appts", coll.size() == 2);
		
		// update the appt
		Iterator<Appointment> iter = coll.iterator();
		appt = iter.next();
		int id = appt.getKey(); // save the id
		
		System.out.println("Appt key = " + id);
		
		appt.setText("my updated appt");
		AppointmentModel.getReference().saveAppt(appt);
		
		// verify that the appt is updated
		coll = AppointmentModel.getReference().getAllAppts();
		assertTrue( "Appointment DB should contain 2 appts", coll.size() == 2);
		appt = AppointmentModel.getReference().getAppt(id);
		assertTrue("Appointment was not updated", "my updated appt".equals(appt.getText()));
		
		// delete the appt
		AppointmentModel.getReference().delAppt(appt);
		coll = AppointmentModel.getReference().getAllAppts();
		assertTrue( "Appointment DB should contain 1 appts", coll.size() == 1);
		
		// let the undos begin
		
		// undo the delete
		UndoLog.getReference().executeUndo();
		
		// verify that the appt is the updated one
		coll = AppointmentModel.getReference().getAllAppts();
		assertTrue( "Appointment DB should contain 2 appts", coll.size() == 2);
		appt = AppointmentModel.getReference().getAppt(id);
		assertTrue("Appointment was not updated", "my updated appt".equals(appt.getText()));
		
		// undo the update
		UndoLog.getReference().executeUndo();
		
		// verify that the appt is the original one
		coll = AppointmentModel.getReference().getAllAppts();
		assertTrue( "Appointment DB should contain 2 appt", coll.size() == 2);
		appt = AppointmentModel.getReference().getAppt(id);
		assertTrue("Appointment was not undone: " + appt.getText(), "my appointment".equals(appt.getText()));
		
		//undo the add
		UndoLog.getReference().executeUndo();
		
		coll = AppointmentModel.getReference().getAllAppts();
		assertTrue( "Appointment DB should contain 1 appts after add undone", coll.size() == 1);
	}
	
	@Test
	public void testAddressUndo() throws Exception
	{
		int num_addrs = AddressModel.getReference().getAddresses().size();
		assertTrue( "Address DB should be empty to start", num_addrs == 0);
		
		Address addr = new Address();
		addr.setLastName("Last name");
		addr.setFirstName("First name");
		
		AddressModel.getReference().saveAddress(addr);
		
		Collection<Address> coll = AddressModel.getReference().getAddresses();
		assertTrue( "Address DB should contain 1 addr", coll.size() == 1);
		
		// update the addr
		addr = coll.iterator().next();
		addr.setFirstName("Updated name");
		AddressModel.getReference().saveAddress(addr);
		
		// verify that the addr is updated
		coll = AddressModel.getReference().getAddresses();
		assertTrue( "Address DB should contain 1 addr", coll.size() == 1);
		addr = coll.iterator().next();
		assertTrue("Address was not updated", "Updated name".equals(addr.getFirstName()));
		
		// delete the addr
		AddressModel.getReference().delete(addr);
		coll = AddressModel.getReference().getAddresses();
		assertTrue( "Address DB should contain 0 appts", coll.size() == 0);
		
		// let the undos begin
		
		// undo the delete
		UndoLog.getReference().executeUndo();
		
		// verify that the addr is the updated one
		coll = AddressModel.getReference().getAddresses();
		assertTrue( "Address DB should contain 1 addr", coll.size() == 1);
		addr = coll.iterator().next();
		assertTrue("Address was not updated", "Updated name".equals(addr.getFirstName()));
		
		// undo the update
		UndoLog.getReference().executeUndo();
		
		// verify that the addr is the original one
		coll = AddressModel.getReference().getAddresses();
		assertTrue( "Address DB should contain 1 addr", coll.size() == 1);
		addr = coll.iterator().next();
		
		assertTrue("Address was not undone", "First name".equals(addr.getFirstName()));
		
		//undo the add
		UndoLog.getReference().executeUndo();
		
		coll = AddressModel.getReference().getAddresses();
		assertTrue( "Address DB should contain 0 appts after add undone", coll.size() == 0);
	}
	
	@Test
	public void testMemoUndo() throws Exception
	{
		int num_addrs = MemoModel.getReference().getMemos().size();
		assertTrue( "Address DB should be empty to start", num_addrs == 0);
		
		Memo memo = new Memo();
		memo.setMemoName("memo name");
		memo.setMemoText("memo text");
		
		MemoModel.getReference().saveMemo(memo);
		
		Collection<Memo> coll = MemoModel.getReference().getMemos();
		assertTrue( "Memo DB should contain 1 memo", coll.size() == 1);
		
		// update the memo
		memo = coll.iterator().next();
		memo.setMemoText("Updated text");
		MemoModel.getReference().saveMemo(memo);
		
		// verify that the memo is updated
		coll = MemoModel.getReference().getMemos();
		assertTrue( "Memo DB should contain 1 memo", coll.size() == 1);
		memo = coll.iterator().next();
		assertTrue("Memo was not updated", "Updated text".equals(memo.getMemoText()));
		
		// delete the memo
		MemoModel.getReference().delete("memo name", false);
		coll = MemoModel.getReference().getMemos();
		assertTrue( "Memo DB should contain 0 appts", coll.size() == 0);
		
		// let the undos begin
		
		// undo the delete
		UndoLog.getReference().executeUndo();
		
		// verify that the memo is the updated one
		coll = MemoModel.getReference().getMemos();
		assertTrue( "Memo DB should contain 1 memo", coll.size() == 1);
		memo = coll.iterator().next();
		assertTrue("Memo was not updated", "Updated text".equals(memo.getMemoText()));
		
		// undo the update
		UndoLog.getReference().executeUndo();
		
		// verify that the memo is the original one
		coll = MemoModel.getReference().getMemos();
		assertTrue( "Memo DB should contain 1 memo", coll.size() == 1);
		memo = coll.iterator().next();
		
		assertTrue("Memo was not undone: " + memo.getMemoText(), "memo text".equals(memo.getMemoText()));
		
		//undo the add
		UndoLog.getReference().executeUndo();
		
		coll = MemoModel.getReference().getMemos();
		assertTrue( "Memo DB should contain 0 appts after add undone", coll.size() == 0);
	}
	
	@Test
	public void testTaskUndo() throws Exception
	{
		int num = TaskModel.getReference().getTasks().size();
		assertTrue( "Address DB should be empty to start", num == 0);
		
		Task task = new Task();
		task.setDescription("task 1");
		task.setStartDate(new Date());
		task.setState("OPEN");
		task.setType("CODE");
		TaskModel.getReference().savetask(task);
		
		Collection<Task> coll = TaskModel.getReference().getTasks();
		assertTrue( "Task DB should contain 1 task", coll.size() == 1);
		
		Integer taskid = Integer.valueOf(coll.iterator().next().getKey());
		Subtask st1 = new Subtask();
		st1.setDescription("st1");
		st1.setStartDate(new Date());
		st1.setTask(taskid);
		TaskModel.getReference().saveSubTask(st1);
		Subtask st2 = new Subtask();
		st2.setDescription("st2");
		st2.setStartDate(new Date());
		st2.setTask(taskid);
		TaskModel.getReference().saveSubTask(st2);
		num = TaskModel.getReference().getSubTasks(taskid.intValue()).size();
		assertTrue("Task does not have 2 subtasks: " + num, num == 2);
		
		// update the task
		task = coll.iterator().next();
		task.setDescription("Updated text");
		TaskModel.getReference().savetask(task);
		
		// verify that the task is updated
		coll = TaskModel.getReference().getTasks();
		assertTrue( "Task DB should contain 1 task", coll.size() == 1);
		task = coll.iterator().next();
		assertTrue("Task was not updated", "Updated text".equals(task.getDescription()));
		
		// delete a subtask
		Collection<Subtask> scoll = TaskModel.getReference().getSubTasks(taskid.intValue());
		TaskModel.getReference().deleteSubTask(scoll.iterator().next().getKey());
		num = TaskModel.getReference().getSubTasks(taskid.intValue()).size();
		assertTrue("Task does not have 1 subtask: " + num, num == 1);
		
		
		// delete the task
		TaskModel.getReference().delete(taskid.intValue());
		coll = TaskModel.getReference().getTasks();
		assertTrue( "Task DB should contain 0 tasks", coll.size() == 0);
		
		// let the undos begin
		
		// undo the delete
		UndoLog.getReference().executeUndo();
		
		// verify that the task is the updated one
		coll = TaskModel.getReference().getTasks();
		assertTrue( "Task DB should contain 1 task", coll.size() == 1);
		task = coll.iterator().next();
		assertTrue("Task was not updated", "Updated text".equals(task.getDescription()));
		num = TaskModel.getReference().getSubTasks(taskid.intValue()).size();
		assertTrue("Task does not have 1 subtask: " + num, num == 1);
		
		// undo the update - should add back 1 subtask as well
		UndoLog.getReference().executeUndo();
		
		// verify that the task is the original one
		coll = TaskModel.getReference().getTasks();
		assertTrue( "Task DB should contain 1 task", coll.size() == 1);
		task = coll.iterator().next();
		assertTrue("Task was not undone: " + task.getDescription(), "task 1".equals(task.getDescription()));
		num = TaskModel.getReference().getSubTasks(taskid.intValue()).size();
		assertTrue("Task does not have 2 subtasks: " + num, num == 2);
		
		//undo the add
		UndoLog.getReference().executeUndo();
		
		coll = TaskModel.getReference().getTasks();
		assertTrue( "Task DB should contain 0 tasks after add undone", coll.size() == 0);
	}
	
	@AfterClass
	public static void tearDown()
	{
		// empty
	}

}
