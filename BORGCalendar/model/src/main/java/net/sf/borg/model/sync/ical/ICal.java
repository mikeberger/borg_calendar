package net.sf.borg.model.sync.ical;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.fortuna.ical4j.validate.ValidationException;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.IOHelper;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Repeat;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.Task;

public class ICal {

	static {
		System.setProperty("net.fortuna.ical4j.timezone.cache.impl", "net.fortuna.ical4j.util.MapTimeZoneCache");
	}

	static public void exportApptsToFileByYear(String dir) throws Exception {

		Collection<Appointment> appts = AppointmentModel.getReference().getAllAppts();
		if (appts.isEmpty())
			return;

		HashMap<Integer, List<Appointment>> apptMap = new HashMap<Integer, List<Appointment>>();

		// put all appts into a tree by year
		GregorianCalendar cal = new GregorianCalendar();
		for (Appointment ap : appts) {

			cal.setTime(ap.getDate());
			int year = cal.get(java.util.Calendar.YEAR);
			if (!apptMap.containsKey(year)) {
				List<Appointment> l = new ArrayList<Appointment>();
				apptMap.put(year, l);
			}

			apptMap.get(year).add(ap);
		}

		// start the output zip file
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String uniq = sdf.format(new Date());

		String backupFilename = dir + "/borg_ics" + uniq + ".zip";
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(backupFilename));
		Writer fw = new OutputStreamWriter(out, StandardCharsets.UTF_8);

		// put out the years
		for (Integer year : apptMap.keySet()) {

			out.putNextEntry(new ZipEntry(year + ".ics"));

			ComponentList<CalendarComponent> clist = new ComponentList<CalendarComponent>();

			for (Appointment ap : apptMap.get(year)) {
				ap.setUid("");
				ap.setUrl("");
				CalendarComponent ve = EntityIcalAdapter.toIcal(ap, true);
				if (ve != null)
					clist.add(ve);
			}
			
			PropertyList<Property> pl = new PropertyList<Property>();
			pl.add(new ProdId("BORG Calendar"));
			pl.add(Version.VERSION_2_0);
			net.fortuna.ical4j.model.Calendar ical = new net.fortuna.ical4j.model.Calendar(pl, clist);
			ical.validate();
			fw.write(ical.toString());
			fw.flush();
			out.closeEntry();
		}

		out.close();

		
	}

	static public void exportIcalToFile(String filename, Date after) throws Exception {
		Calendar cal = exportIcal(after);
		OutputStream oostr = IOHelper.createOutputStream(filename);
		CalendarOutputter op = new CalendarOutputter();
		op.output(cal, oostr);
		oostr.close();
	}

	static public String exportIcalToString(Date after) throws Exception {
		Calendar cal = exportIcal(after);
		CalendarOutputter op = new CalendarOutputter();
		StringWriter sw = new StringWriter();
		op.output(cal, sw);
		return sw.toString();
	}

	static public Calendar exportIcal(Date after) throws Exception {

		ComponentList<CalendarComponent> clist = new ComponentList<CalendarComponent>();

		exportAppointments(clist, after);
		exportTasks(clist);
		exportSubTasks(clist);
		exportProjects(clist);

		PropertyList<Property> pl = new PropertyList<Property>();
		pl.add(new ProdId("BORG Calendar"));
		pl.add(Version.VERSION_2_0);
		net.fortuna.ical4j.model.Calendar cal = new net.fortuna.ical4j.model.Calendar(pl, clist);

		cal.validate();

		return cal;
	}

	static private void exportAppointments(ComponentList<CalendarComponent> clist, Date after) throws Exception {
		boolean export_todos = Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO);

		for (Appointment ap : AppointmentModel.getReference().getAllAppts()) {

			// limit by date
			if (after != null) {
				Date latestInstance = Repeat.calculateLastRepeat(ap);
				if (latestInstance != null && latestInstance.before(after))
					continue;
			}

			CalendarComponent ve = EntityIcalAdapter.toIcal(ap, export_todos);
			if (ve != null)
				clist.add(ve);

		}

	}

	static private void exportTasks(ComponentList<CalendarComponent> clist) throws Exception {

		boolean export_todos = Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO);

		for (Task t : TaskModel.getReference().getTasks()) {
			CalendarComponent c = EntityIcalAdapter.toIcal(t, export_todos);
			if (c != null)
				clist.add(c);

		}

	}

	static private void exportProjects(ComponentList<CalendarComponent> clist) throws Exception {

		boolean export_todos = Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO);

		for (Project t : TaskModel.getReference().getProjects()) {
			CalendarComponent c = EntityIcalAdapter.toIcal(t, export_todos);
			if (c != null)
				clist.add(c);

		}
	}

	static private void exportSubTasks(ComponentList<CalendarComponent> clist) throws Exception {

		boolean export_todos = Prefs.getBoolPref(PrefName.ICAL_EXPORT_TODO);

		for (Subtask t : TaskModel.getReference().getSubTasks()) {
			CalendarComponent c = EntityIcalAdapter.toIcal(t, export_todos);
			if (c != null)
				clist.add(c);

		}
	}

	static private void setHints() {
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
	}

	static public String importIcalFromUrl(String urlString) throws Exception {

		setHints();

		CalendarBuilder builder = new CalendarBuilder();
		URI uri = new URI(urlString);
		URL url = uri.toURL();
		InputStream is = url.openStream();
		Calendar cal = builder.build(is);
		is.close();

		return importIcal(cal);
	}

	static public String importIcalFromFile(String file) throws Exception {

		setHints();

		CalendarBuilder builder = new CalendarBuilder();
		InputStream is = new FileInputStream(file);
		Calendar cal = builder.build(is);
		is.close();

		return importIcal(cal);
	}

	static private String importIcal(Calendar cal) throws Exception {

		int skipped = 0;
		StringBuffer dups = new StringBuffer();

		setHints();

		StringBuffer warning = new StringBuffer();

		try {
			cal.validate();
		} catch (ValidationException e) {
			Errmsg.getErrorHandler().notice("Ical4j validation error: " + e.getLocalizedMessage());
		}

		ArrayList<Appointment> aplist = new ArrayList<Appointment>();

		AppointmentModel amodel = AppointmentModel.getReference();
		ComponentList<CalendarComponent> clist = cal.getComponents();
		Iterator<CalendarComponent> it = clist.iterator();
		while (it.hasNext()) {
			Component comp = it.next();

			Appointment ap = EntityIcalAdapter.toBorg(comp);
			if (ap != null)
				aplist.add(ap);

		}

		int imported = 0;
		int dup_count = 0;

		for (Appointment ap : aplist) {
			// check for dups
			List<Appointment> appts = AppointmentModel.getReference().getAppointmentsByText(ap.getText());

			if (appts.contains(ap)) {
				dup_count++;
				dups.append("DUP: " + ap.getText() + "\n");
				continue;
			}

			imported++;
			amodel.saveAppt(ap);
		}

		warning.append("Imported: " + imported + "\n");
		warning.append("Skipped: " + skipped + "\n");
		warning.append("Duplicates: " + dup_count + "\n");
		warning.append(dups);

		if (warning.length() == 0)
			return (null);

		return (warning.toString());

	}

}
