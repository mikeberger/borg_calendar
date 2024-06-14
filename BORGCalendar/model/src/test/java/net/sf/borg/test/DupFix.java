package net.sf.borg.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.Appointment;

public class DupFix {
	
	static public void main(String[] args)  throws Exception {
		
		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());
		DBHelper.getController().connect("jdbc:sqlite:C:\\Users\\deskp\\OneDrive\\borg_h2/borg_sqlite.db");
		
		Collection<Appointment> appts = AppointmentModel.getReference().getAllAppts();
		
		Map<String,Integer> keycount = new HashMap<String,Integer>();
		Set<String> dupkeys = new HashSet<String>();
		
		for( Appointment appt : appts) {
			
			Integer count = keycount.get(apptKey(appt));
			if( count == null)
				keycount.put(apptKey(appt), 1);
			else
				keycount.put(apptKey(appt), count + 1);
		}
		
		for( Entry<String,Integer> entry : keycount.entrySet() ) {
			if( entry.getValue() > 1)
			{
				//System.out.println(entry);
				dupkeys.add(entry.getKey());
				
			}
			else
			{
				//System.out.println( "xxx" + entry);
			}
		}
		
		for( Appointment appt : appts ) {
			if( dupkeys.contains(apptKey(appt))) {
				if( appt.getUid() != null )
				{
					//System.out.println("Keeper-" + appt.toString());
				}
				else {
					//System.out.println("Delete-" + appt.toString());
					System.out.println("Delete from appointments where appt_num == " + appt.getKey() + ";");

				}
			}
		}
		
		DBHelper.getController().close();
		
	}
	
	private static String apptKey(Appointment appt) {
		return appt.getDate() + "-" + appt.getText();
	}

}
