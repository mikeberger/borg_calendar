package net.sf.borg.test;

import com.google.api.services.calendar.model.Event;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.sync.SyncLog;
import net.sf.borg.model.sync.google.EntityGCalAdapter;
import net.sf.borg.model.sync.google.GCal;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

@Ignore
public class GCalTest {

    static private final Logger log = Logger.getLogger("net.sf.borg");

    @BeforeClass
    public static void setUp() throws Exception {

        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.FINE);
        log.addHandler(ch);
        log.setUseParentHandlers(false);
        log.setLevel(Level.ALL);
        DBHelper.setFactory(new JdbcDBHelper());
        DBHelper.setController(new JdbcDBHelper());
        DBHelper.getController().connect("jdbc:h2:mem:C:/Users/i_fle/.gcaltest/borgdb;USER=sa");


        Prefs.setPrefRootNode("net/sf/borg/unittest");

        Prefs.putPref(PrefName.GOOGLE_SYNC, "true");
        Prefs.putPref(PrefName.GCAL_CAL_ID, "8b6erha7qmkjasa1u1nk7848g8@group.calendar.google.com");
        Prefs.putPref(PrefName.GCAL_TASKLIST_ID, "MTc1MjMwMzA0ODkwNjY1NDc0NzU6MDow");
        SyncLog.getReference();

    }

    @Test
    public void singleTimedAppt()  throws Exception {


        Appointment ap = AppointmentModel.getDefaultAppointment();
        if( ap == null ) ap = AppointmentModel.getReference().newAppt();
        ap.setText("test timed 1");
        ap.setUntimed("N");
        ap.setDate(new Date());
        ap.setDuration(45);
        AppointmentModel.getReference().saveAppt(ap);

        GCal.getReference().sync(1, false);
        ap = AppointmentModel.getReference().getAppointmentsByText("test timed 1").get(0);

        Assert.assertNotNull(ap);
        log.info(ap.toString());
        Assert.assertNotNull(ap.getUrl());
        String gid = EntityGCalAdapter.getIdFromJSON(ap.getUrl());
        Assert.assertNotNull(gid);

        Event e = GCal.getReference().getEvent(gid);
        Assert.assertNotNull(e);
        Assert.assertEquals("test timed 1", e.getSummary());

        ap.setText("test timed 1a");
        AppointmentModel.getReference().saveAppt(ap);
        GCal.getReference().sync(1, false);

        e = GCal.getReference().getEvent(gid);
        Assert.assertNotNull(e);
        Assert.assertEquals("test timed 1a", e.getSummary());

        AppointmentModel.getReference().delAppt(ap.getKey());
        GCal.getReference().sync(1, false);

        e = GCal.getReference().getEvent(gid);
        if( e != null )
        {
            Assert.assertEquals("cancelled", e.getStatus());
        }

        //GCal.getReference().removeEvent(e.getId());


    }

    @Test
    public void singleUnTimedAppt()  throws Exception {


        Appointment ap = AppointmentModel.getDefaultAppointment();
        if( ap == null ) ap = AppointmentModel.getReference().newAppt();
        ap.setText("test untimed 1");
        ap.setUntimed("Y");
        ap.setDate(new Date());
        AppointmentModel.getReference().saveAppt(ap);

        GCal.getReference().sync(1, false);
        ap = AppointmentModel.getReference().getAppointmentsByText("test untimed 1").get(0);

        Assert.assertNotNull(ap);
        log.info(ap.toString());
        Assert.assertNotNull(ap.getUrl());
        String gid = EntityGCalAdapter.getIdFromJSON(ap.getUrl());
        Assert.assertNotNull(gid);

        Event e = GCal.getReference().getEvent(gid);
        Assert.assertNotNull(e);
        Assert.assertEquals("test untimed 1", e.getSummary());

        ap.setText("test untimed 1a");
        AppointmentModel.getReference().saveAppt(ap);
        GCal.getReference().sync(1, false);

        e = GCal.getReference().getEvent(gid);
        Assert.assertNotNull(e);
        Assert.assertEquals("test untimed 1a", e.getSummary());

        AppointmentModel.getReference().delAppt(ap.getKey());
        GCal.getReference().sync(1, false);

        e = GCal.getReference().getEvent(gid);
        if( e != null )
        {
            Assert.assertEquals("cancelled", e.getStatus());
        }

        //GCal.getReference().removeEvent(e.getId());


    }

    @Test
    public void singleTodo() throws Exception {

        Appointment ap = AppointmentModel.getDefaultAppointment();
        if( ap == null ) ap = AppointmentModel.getReference().newAppt();
        ap.setText("test todo 2");
        ap.setUntimed("Y");
        ap.setDate(new Date());
        ap.setTodo(true);
        AppointmentModel.getReference().saveAppt(ap);

        log.info(EntityGCalAdapter.toGCalTask(ap).toPrettyString());

        GCal.getReference().sync(1, false);

    }

    @Test
    public void recurringTodo() throws Exception {

        Appointment ap = AppointmentModel.getDefaultAppointment();
        if( ap == null ) ap = AppointmentModel.getReference().newAppt();
        ap.setText("test todo 1");
        ap.setUntimed("Y");
        ap.setDate(new Date());
        ap.setTodo(true);
        ap.setFrequency("weekly");
        ap.setTimes(52);
        ap.setRepeatFlag(true);
        AppointmentModel.getReference().saveAppt(ap);

        log.info(EntityGCalAdapter.toGCalTask(ap).toPrettyString());

        GCal.getReference().sync(1, false);

    }
}
