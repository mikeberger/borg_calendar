package net.sf.borg.plugin.sync;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.plugin.sync.google.GoogleAppointmentAdapter;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.options.OptionsView;
import net.sf.borg.ui.util.ModalMessage;

import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.calendar.CalendarEventEntry;

public class SyncModule implements Module {

	static public PrefName SYNCUSER = new PrefName("syncmodule-user", "");
	static public PrefName SYNCPW = new PrefName("syncmodule-pw", "");
	static public PrefName SYNCPW2 = new PrefName("syncmodule-pw2", "");

	@Override
	public Component getComponent() {
		return null;
	}

	@Override
	public String getModuleName() {
		return "Sync";
	}

	@Override
	public ViewType getViewType() {
		return null;
	}

	@Override
	public void initialize(MultiView parent) {

		System.out.println("Loading Sync Module");

		JMenu m = new JMenu();
		m.setText(getModuleName());

		JMenuItem googleMI = new JMenuItem();
		googleMI.setText("Google");
		googleMI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					googleSync();
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		m.add(googleMI);

		parent.addPluginSubMenu(m);

		OptionsView.getReference().addPanel(new SyncOptionsPanel());

	}

	@Override
	public void print() {
		// do nothing
	}

	private void googleSync() throws Exception {

		int years_to_sync = 2;
		int syncFromYear = new GregorianCalendar().get(Calendar.YEAR)
				- years_to_sync;

		CalendarService myService = new CalendarService("BORG");
		String user = Prefs.getPref(SYNCUSER);
		String pw = SyncOptionsPanel.gep();
		myService.setUserCredentials(user, pw);

		URL eventUrl = new URL("http://www.google.com/calendar/feeds/" + user
				+ "/private/full");

		GregorianCalendar cal = new GregorianCalendar();
		GoogleAppointmentAdapter ad = new GoogleAppointmentAdapter();
		Collection<Appointment> appts = AppointmentModel.getReference()
				.getAllAppts();

		//ModalMessage msg = new ModalMessage("Sync in progress. Please wait...",
		//		false);
		//msg.setVisible(true);
		try {
			for (Appointment appt : appts) {
				cal.setTime(appt.getDate());
				if (cal.get(Calendar.YEAR) >= syncFromYear) {
					CalendarEventEntry ev = ad.fromBorg(appt);
					myService.insert(eventUrl, ev);
				}
			}
		} 
		finally{
			//msg.dispose();
		}

	}

}
