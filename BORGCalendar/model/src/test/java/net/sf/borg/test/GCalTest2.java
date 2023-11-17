package net.sf.borg.test;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.sync.google.GCal;

@Ignore
public class GCalTest2 {

    static private final Logger log = Logger.getLogger("net.sf.borg");

    @BeforeClass
    public static void setUp() throws Exception {

        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.FINE);
        log.addHandler(ch);
        log.setUseParentHandlers(false);
        log.setLevel(Level.FINE);
        //DBHelper.setFactory(new JdbcDBHelper());
        //DBHelper.setController(new JdbcDBHelper());
        //DBHelper.getController().connect("jdbc:h2:file:C:/Users/deskp/borgut;USER=sa");


        Prefs.setPrefRootNode("net/sf/borg/test");

        Prefs.putPref(PrefName.GOOGLE_SYNC, "true");
        Prefs.putPref(PrefName.GCAL_CAL_ID, "borgtest");
        Prefs.putPref(PrefName.GCAL_TASKLIST_ID, "testtasks");
        //SyncLog.getReference();

    }

   @Test
   public void listCals() throws Exception {
	   GCal g = GCal.getReference();
	   g.connect();
	   g.setIds();
	   
	   Calendar c = g.getCalendar("xx");
	   log.info(c.toString());
	   CalendarListEntry ce = g.getCalendarListEntry("xx");
	   log.info(ce.toPrettyString());
	   
   }
}
