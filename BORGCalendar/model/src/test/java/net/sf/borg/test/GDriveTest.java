package net.sf.borg.test;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.sync.google.GDrive;

@Ignore
public class GDriveTest {

    static private final Logger log = Logger.getLogger("net.sf.borg");

    @BeforeClass
    public static void setUp() throws Exception {

        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.FINE);
        log.addHandler(ch);
        log.setUseParentHandlers(false);
        log.setLevel(Level.FINE);
       

        Prefs.setPrefRootNode("net/sf/borg/test");
        Prefs.putPref(PrefName.GOOGLE_DB_FILE_PATH, "");
        Prefs.putPref(PrefName.GOOGLE_TOKEN_DIR, "/home/mike/borgcred/");
        Prefs.putPref(PrefName.GOOGLE_CRED_FILE, "/home/mike/borgcred/xxx.json");
        //Prefs.putPref(PrefName.GOOGLE_SYNC, "true");
        //Prefs.putPref(PrefName.GCAL_CAL_ID, "borgtest");
        //Prefs.putPref(PrefName.GCAL_TASKLIST_ID, "testtasks");
        //SyncLog.getReference();

    }

   @Test
   public void listCals() throws Exception {
	   GDrive g = GDrive.getReference();
	   g.connect();
	   g.checkModTimes();
	   
	
	   
   }
}
