package net.sf.borg.ui;

import java.awt.Frame;
import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.address.AddrListView;
import net.sf.borg.ui.calendar.DayPanel;
import net.sf.borg.ui.calendar.MonthPanel;
import net.sf.borg.ui.calendar.SearchView;
import net.sf.borg.ui.calendar.TodoView;
import net.sf.borg.ui.calendar.WeekPanel;
import net.sf.borg.ui.calendar.YearPanel;
import net.sf.borg.ui.memo.MemoPanel;
import net.sf.borg.ui.popup.ReminderPopupManager;
import net.sf.borg.ui.task.TaskModule;

/**
 * Class UIControl provides access to the UI from non-UI classes. UIControl provides the main UI entry point.
 * 
 *
 */
public class UIControl {
	
	
	/**
	 * Main UI initialization.  
	 * @param trayname - name for the tray icon
	 */
	public static void startUI(String trayname)
	{
		
		// tray icon
		SunTrayIconProxy.startTrayIcon(trayname);
		
		// create popups view
		ReminderPopupManager.getReference();

		// create the main window
		MultiView mv = MultiView.getMainView();
		
		// load the UI modules into the main window
		Calendar cal_ = new GregorianCalendar();
		mv.addModule(new MonthPanel(cal_.get(Calendar.MONTH), cal_
				.get(Calendar.YEAR)));
		mv.addModule(new WeekPanel(cal_.get(Calendar.MONTH), cal_
				.get(Calendar.YEAR), cal_.get(Calendar.DATE)));
		mv.addModule(new DayPanel(cal_.get(Calendar.MONTH), cal_
				.get(Calendar.YEAR), cal_.get(Calendar.DATE)));
		mv.addModule(new YearPanel(cal_.get(Calendar.YEAR)));
		mv.addModule(AddrListView.getReference());
		mv.addModule(TodoView.getReference());
		mv.addModule(new TaskModule());
		mv.addModule(new MemoPanel());
		mv.addModule(new SearchView());
		mv.addModule(new InfoView("/resource/RELEASE_NOTES.txt", Resource
				.getResourceString("rlsnotes")));
		mv.addModule(new InfoView("/resource/CHANGES.txt", Resource
				.getResourceString("viewchglog")));
		mv.addModule(new InfoView("/resource/license.htm", Resource
				.getResourceString("License")));

		// make the main window visible
		mv.setVisible(true);
		
		// show the month view
		mv.setView(ViewType.MONTH);

		// start todo view if there are todos
		if (AppointmentModel.getReference().haveTodos()) {
			mv.setView(ViewType.TODO);
		}
	}
	
	/**
	 * raise the UI to the front
	 */
	public static void toFront()
	{
		MultiView.getMainView().toFront();
		MultiView.getMainView().setState(Frame.NORMAL);
	}
	
	
	

}
