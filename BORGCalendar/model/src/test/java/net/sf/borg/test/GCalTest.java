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
        log.setLevel(Level.INFO);
        DBHelper.setFactory(new JdbcDBHelper());
        DBHelper.setController(new JdbcDBHelper());
        DBHelper.getController().connect("jdbc:h2:file:C:/Users/deskp/borgut;USER=sa");


        Prefs.setPrefRootNode("net/sf/borg/test");

        Prefs.putPref(PrefName.GOOGLE_SYNC, "true");
        Prefs.putPref(PrefName.GCAL_CAL_ID, "borgtest");
        Prefs.putPref(PrefName.GCAL_TASKLIST_ID, "testtasks");
        SyncLog.getReference();

    }

    @Test
    public void sync() throws Exception{
        GCal.getReference().sync(10,false, false);
    }

    @Test
    public void load_db() throws Exception {
        Date now = new Date();
        for( int i = 0; i < 200; i++){
            Appointment appt = AppointmentModel.getReference().newAppt();
            appt.setDate(now);
            appt.setText("appt" + i);
            AppointmentModel.getReference().saveAppt(appt);
        }
        for( int i = 0; i < 200; i++){
            Appointment appt = AppointmentModel.getReference().newAppt();
            appt.setDate(now);
            appt.setText("task" + i);
            appt.setTodo(true);
            AppointmentModel.getReference().saveAppt(appt);
        }
    }

    @Test
    public void overwrite() throws Exception {
        GCal.getReference().sync(10,true, false);
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

        GCal.getReference().sync(1, false, false);
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
        GCal.getReference().sync(1, false, false);

        e = GCal.getReference().getEvent(gid);
        Assert.assertNotNull(e);
        Assert.assertEquals("test timed 1a", e.getSummary());

        AppointmentModel.getReference().delAppt(ap.getKey());
        GCal.getReference().sync(1, false, false);

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

        GCal.getReference().sync(1, false, false);
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
        GCal.getReference().sync(1, false, false);

        e = GCal.getReference().getEvent(gid);
        Assert.assertNotNull(e);
        Assert.assertEquals("test untimed 1a", e.getSummary());

        AppointmentModel.getReference().delAppt(ap.getKey());
        GCal.getReference().sync(1, false, false);

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

        GCal.getReference().sync(1, false, false);

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

        GCal.getReference().sync(1, false, false);

    }
}
