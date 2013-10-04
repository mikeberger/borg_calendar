package net.sf.borg.test;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Date;

import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.LinkModel;
import net.sf.borg.model.LinkModel.LinkType;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.Address;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.KeyedEntity;
import net.sf.borg.model.entity.Link;
import net.sf.borg.model.entity.Memo;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Task;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LinkTest {

	@BeforeClass
	public static void setUp() throws Exception {
		// open the borg dbs - in memory
		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());
		DBHelper.getController().connect("jdbc:hsqldb:mem:whatever");
		
	}
	
	@Test
	public void testObjectLinks() throws Exception
	{
		
		Appointment appt = new Appointment();
		appt.setText("my appointment");
		appt.setDate(new Date());
		AppointmentModel.getReference().saveAppt(appt);
		
		Task task = new Task();
		task.setDescription("task 1");
		task.setType("TASK");
		task.setStartDate(new Date());
		task.setState("OPEN");
		TaskModel.getReference().savetask(task);
		
		Memo memo = new Memo();
		memo.setMemoName("Memo 1");
		memo.setMemoText("xx");
		MemoModel.getReference().saveMemo(memo);
		
		Address addr = new Address();
		addr.setFirstName("FF");
		addr.setLastName("LL");
		AddressModel.getReference().saveAddress(addr);
		
		Project project = new Project();
		project.setDescription("Project 1");
		project.setStatus("OPEN");
		project.setStartDate(new Date());
		TaskModel.getReference().saveProject(project);
		
		LinkModel.getReference().addLink(appt, Integer.toString(task.getKey()), LinkType.TASK);
		LinkModel.getReference().addLink(addr, Integer.toString(task.getKey()), LinkType.TASK);
		LinkModel.getReference().addLink(project, Integer.toString(task.getKey()), LinkType.TASK);

		LinkModel.getReference().addLink(addr, Integer.toString(appt.getKey()), LinkType.APPOINTMENT);
		LinkModel.getReference().addLink(project, Integer.toString(appt.getKey()), LinkType.APPOINTMENT);

		LinkModel.getReference().addLink(project, Integer.toString(addr.getKey()), LinkType.ADDRESS);

		LinkModel.getReference().addLink(task, memo.getMemoName(), LinkType.MEMO);
		LinkModel.getReference().addLink(addr, memo.getMemoName(), LinkType.MEMO);
		LinkModel.getReference().addLink(appt, memo.getMemoName(), LinkType.MEMO);
		LinkModel.getReference().addLink(project, memo.getMemoName(), LinkType.MEMO);
		
		Collection<Link> links = LinkModel.getReference().getLinks();
		
		for( Link link : links)
			System.out.println(link.toString());
		
		// number of links = 10 regular plus 6 added back links
		assertEquals("Wrong number of links", 16, links.size());
		
		for( KeyedEntity<?> ent : new KeyedEntity[]{ addr, appt, project, task })
		{
			Collection<Link> objLinks = LinkModel.getReference().getLinks(ent);
				assertEquals("Wrong number of object links", 4, objLinks.size());
		}
		
		AppointmentModel.getReference().delAppt(appt);
		
		links = LinkModel.getReference().getLinks();
		assertEquals("Wrong number of links", 9, links.size());
		
		for( KeyedEntity<?> ent : new KeyedEntity[]{ addr, project, task })
		{
			Collection<Link> objLinks = LinkModel.getReference().getLinks(ent);
			assertEquals("Wrong number of object links", 3, objLinks.size());
		}
		
		TaskModel.getReference().delete(task.getKey());
		
		links = LinkModel.getReference().getLinks();
		for( Link link : links)
			System.out.println("[4] " + link.toString());
		assertEquals("Wrong number of links", 4, links.size());
		
		for( KeyedEntity<?> ent : new KeyedEntity[]{ addr, project })
		{
			Collection<Link> objLinks = LinkModel.getReference().getLinks(ent);
			assertEquals("Wrong number of object links", 2, objLinks.size());
		}
		
		MemoModel.getReference().delete(memo.getMemoName(), false);
		
		links = LinkModel.getReference().getLinks();
		assertEquals("Wrong number of links", 2, links.size());
		
		for( KeyedEntity<?> ent : new KeyedEntity[]{ addr, project })
		{
			Collection<Link> objLinks = LinkModel.getReference().getLinks(ent);
			assertEquals("Wrong number of object links", 1, objLinks.size());
		}
		
		AddressModel.getReference().delete(addr);
		

		links = LinkModel.getReference().getLinks();
		assertEquals("Wrong number of links", 0, links.size());
		

	}
	
	
	@AfterClass
	public static void tearDown()
	{
		// empty
	}

}
