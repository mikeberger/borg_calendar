package net.sf.borg.test;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.sync.SyncLog;
import net.sf.borg.model.sync.google.GCal;

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GCalTest {

    static private final Logger log = Logger.getLogger("net.sf.borg");

    public static void main(String args[])  throws Exception {

        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.FINE);
        log.addHandler(ch);
        log.setUseParentHandlers(false);
        log.setLevel(Level.ALL);
        DBHelper.setFactory(new JdbcDBHelper());
        DBHelper.setController(new JdbcDBHelper());
        DBHelper.getController().connect("jdbc:h2:file:C:/Users/i_fle/.gcaltest/borgdb;USER=sa");

        Prefs.setPrefRootNode("net/sf/borg/test");

        Prefs.putPref(PrefName.GOOGLE_SYNC, "true");
        Prefs.putPref(PrefName.GCAL_CAL_ID, "8b6erha7qmkjasa1u1nk7848g8@group.calendar.google.com");
        SyncLog.getReference();

        Appointment ap = AppointmentModel.getDefaultAppointment();
        ap.setText("test untimed 1");
        ap.setUntimed("N");
        ap.setDate(new Date());
        AppointmentModel.getReference().saveAppt(ap);

        GCal.sync(1, false);



    }
}
