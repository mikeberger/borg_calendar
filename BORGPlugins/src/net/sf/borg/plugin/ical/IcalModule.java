package net.sf.borg.plugin.ical;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.iharder.dnd.FileDrop;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.plugin.common.Resource;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.options.OptionsView;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class IcalModule implements Module {

	public static PrefName PORT = new PrefName("ical-server-port", new Integer(
			8844));
	public static PrefName EXPORTYEARS = new PrefName("ical-export-years",
			new Integer(2));

	// option to prevent import of appts that were previously exported from borg
	// used when the goal is to only import appointments created outside of
	// borg, but
	// to not import appts that were exported from borg to another calendar and
	// then
	// sent back to borg as part of the export from the other calendar
	public static PrefName SKIP_BORG = new PrefName("ical-skip_borg", "true");

	// FTP
	public static PrefName FTPSERVER = new PrefName("ical-ftp-server",
			"localhost");
	public static PrefName FTPPATH = new PrefName("ical-ftp-path", "borg.ics");
	public static PrefName FTPUSER = new PrefName("ical-ftp-user", "");
	public static PrefName FTPPW = new PrefName("ical-ftp-pw", "");
	public static PrefName FTPPW2 = new PrefName("ical-ftp-pw2", "");

	@Override
	public Component getComponent() {
		return null;
	}

	@Override
	public String getModuleName() {
		return "ICAL";
	}

	@Override
	public ViewType getViewType() {
		return null;
	}

	@Override
	public void initialize(MultiView parent) {

		JMenu m = new JMenu();
		m.setText(getModuleName());

		JMenuItem imp = new JMenuItem();
		imp.setText(Resource.getResourceString("Import"));
		imp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				// prompt for a file
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						Resource.getResourceString("ical_files"), "ics", "ICS",
						"ical", "ICAL", "icalendar");
				chooser.setFileFilter(filter);
				chooser.setCurrentDirectory(new File("."));
				chooser.setDialogTitle(Resource
						.getResourceString("choose_file"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				int returnVal = chooser.showOpenDialog(null);
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				String s = chooser.getSelectedFile().getAbsolutePath();

				try {

					CategoryModel catmod = CategoryModel.getReference();
					Collection<String> allcats = catmod.getCategories();
					Object[] cats = allcats.toArray();

					Object o = JOptionPane.showInputDialog(null,
							Resource.getResourceString("import_cat_choose"),
							"", JOptionPane.QUESTION_MESSAGE, null, cats,
							cats[0]);
					if (o == null)
						return;

					String warning = AppointmentIcalAdapter.importIcal(s,
							(String) o);
					if (warning != null && !warning.isEmpty())
						Errmsg.getErrorHandler().notice(warning);
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}

			}
		});

		m.add(imp);

		JMenuItem exp = new JMenuItem();
		exp.setText(Resource.getResourceString("exportToFile"));
		exp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				export(Prefs.getIntPref(EXPORTYEARS));
			}
		});

		m.add(exp);

		JMenuItem expftp = new JMenuItem();
		expftp.setText(Resource.getResourceString("exportToFTP"));
		expftp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				exportftp(Prefs.getIntPref(EXPORTYEARS));
			}
		});

		m.add(expftp);

		JMenuItem exp3 = new JMenuItem();
		exp3.setText(Resource.getResourceString("start_server"));
		exp3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					IcalFileServer.start();
					// Errmsg.getErrorHandler().notice(Resource.getResourceString("server_started"));
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}
			}
		});

		m.add(exp3);

		JMenuItem exp4 = new JMenuItem();
		exp4.setText(Resource.getResourceString("stop_server"));
		exp4.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				IcalFileServer.stop();
				Errmsg.getErrorHandler().notice(
						Resource.getResourceString("server_stopped"));
			}
		});

		m.add(exp4);

		OptionsView.getReference().addPanel(new IcalOptionsPanel());

		parent.addPluginSubMenu(m);

		new FileDrop(parent, new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				for (File f : files) {
					String warning;
					try {
						warning = AppointmentIcalAdapter.importIcal(
								f.getAbsolutePath(), "");
						if (warning != null && !warning.isEmpty())
							Errmsg.getErrorHandler().notice(warning);
					} catch (Exception e) {
						Errmsg.getErrorHandler().errmsg(e);
					}

				}
			}
		});
	}

	@Override
	public void print() {
		// do nothing
	}

	private void checkReply(FTPClient c) throws Exception {
		int reply = c.getReplyCode();

		if (!FTPReply.isPositiveCompletion(reply)) {
			throw new Exception("FTP Error: " + reply);
		}
	}

	private void exportftp(Integer years) {

		try {
			String icalString = "";
			if (years != null) {
				GregorianCalendar cal = new GregorianCalendar();
				cal.add(Calendar.YEAR, -1 * years.intValue());
				icalString = AppointmentIcalAdapter.exportIcalToString(cal
						.getTime());
			} else {
				icalString = AppointmentIcalAdapter.exportIcalToString(null);
			}

			System.out.println(icalString);
			FTPClient client = new FTPClient();

			try {
				client.connect(Prefs.getPref(IcalModule.FTPSERVER));
				checkReply(client);

				client.login(Prefs.getPref(IcalModule.FTPUSER),
						IcalOptionsPanel.gep());
				checkReply(client);
				client.enterLocalPassiveMode();
				checkReply(client);
				InputStream is = new ByteArrayInputStream(icalString.getBytes());

				//
				// Store file to server
				//
				client.storeFile(Prefs.getPref(IcalModule.FTPPATH), is);
				checkReply(client);

				client.logout();
			} finally {
				client.disconnect();
			}

		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}

	/**
	 * export appts
	 * 
	 * @param years
	 *            - number of years to export or null
	 */
	private void export(Integer years) {

		// prompt for a file
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				Resource.getResourceString("ical_files"), "ics", "ICS", "ical",
				"ICAL", "icalendar");
		chooser.setFileFilter(filter);
		chooser.setDialogTitle(Resource.getResourceString("choose_file"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int returnVal = chooser.showSaveDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		String s = chooser.getSelectedFile().getAbsolutePath();

		// auto append extension
		if (chooser.getFileFilter() != chooser.getAcceptAllFileFilter()) {
			if (!s.contains(".")) {
				s += ".ics";
			}
		}

		try {
			if (years != null) {
				GregorianCalendar cal = new GregorianCalendar();
				cal.add(Calendar.YEAR, -1 * years.intValue());
				AppointmentIcalAdapter.exportIcalToFile(s, cal.getTime());
			} else {
				AppointmentIcalAdapter.exportIcalToFile(s, null);
			}

		} catch (Exception e) {
			Errmsg.getErrorHandler().errmsg(e);
		}
	}

}
