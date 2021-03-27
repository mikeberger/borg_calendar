package net.sf.borg.test;

import java.util.Collection;

import org.junit.Assert;

import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.ical.EntityIcalAdapter;

public class IcalAdapterTest {

	public static void main(String args[]) throws Exception {
		System.setProperty("net.fortuna.ical4j.timezone.cache.impl", "net.fortuna.ical4j.util.MapTimeZoneCache");
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);

		// open default db
		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());
		DBHelper.getController().connect("jdbc:h2:file:~/.borg_db/borgdb;USER=sa");

		Collection<Appointment> appts = AppointmentModel.getReference().getAllAppts();
		System.out.println(appts.size() + " appts");
		for (Appointment appt : appts) {

			CalendarComponent comp = EntityIcalAdapter.toIcal(appt, true);
			Appointment ap2 = EntityIcalAdapter.toBorg(comp);

			try {
				Assert.assertEquals(appt.getCategory(), ap2.getCategory());
				if (appt.getUntimed() != null && appt.getUntimed().equals("N")) {
					Assert.assertEquals(appt.getDate().toString(), ap2.getDate().toString());
					Assert.assertEquals(appt.getDuration(), ap2.getDuration());
				}
				Assert.assertEquals(appt.getText(), ap2.getText());
				Assert.assertEquals(appt.getHoliday(), ap2.getHoliday());
				Assert.assertEquals(appt.isPrivate(), ap2.isPrivate());
				Assert.assertEquals(appt.isTodo(), ap2.isTodo());
				if (appt.getColor() != null)
					Assert.assertEquals(appt.getColor(), ap2.getColor());
				Assert.assertEquals(appt.isRepeatFlag(), ap2.isRepeatFlag());
				//Assert.assertEquals(appt.getReminderTimes(), ap2.getReminderTimes());
				if (appt.getUntimed() != null)
					Assert.assertEquals(appt.getUntimed(), ap2.getUntimed());
				Assert.assertEquals(appt.getPriority(), ap2.getPriority());
				Assert.assertEquals(appt.isEncrypted(), ap2.isEncrypted());

				if (!appt.isRepeatFlag())
					continue;

				Assert.assertEquals(appt.getFrequency(), ap2.getFrequency());
				Assert.assertEquals(appt.getRepeatUntil(), ap2.getRepeatUntil());
				//if (appt.isTodo())
				//	Assert.assertEquals(appt.getNextTodo(), ap2.getNextTodo());
				Assert.assertEquals(appt.getTimes(), ap2.getTimes());
				Assert.assertEquals(appt.getSkipList(), ap2.getSkipList());
				
				//System.out.println(appt.toString());
				//System.out.println(ap2.toString());
				//System.out.println(comp.toString());

				
			} catch (Throwable e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
				System.out.println(appt.toString());
				System.out.println(ap2.toString());
				System.out.println(comp.toString());

				break;
			}

		}
	}

}
