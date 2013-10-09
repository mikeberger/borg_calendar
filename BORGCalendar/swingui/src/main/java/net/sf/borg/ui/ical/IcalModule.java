package net.sf.borg.ui.ical;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
import net.sf.borg.common.Resource;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.model.ical.AppointmentIcalAdapter;
import net.sf.borg.model.ical.IcalFTP;
import net.sf.borg.model.ical.IcalFileServer;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;
import net.sf.borg.ui.options.IcalOptionsPanel;
import net.sf.borg.ui.options.OptionsView;

public class IcalModule implements Module {

	private static PrefName url_pref = new PrefName("saved_import_url", "");
	

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
	
	public static JMenu getIcalMenu()
	{
		JMenu m = new JMenu();
		m.setText("Ical");

		m.setIcon(new javax.swing.ImageIcon(IcalModule.class.getResource(
				"/resource/Export16.gif")));
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

					String warning = AppointmentIcalAdapter.importIcalFromFile(s,
							(String) o);
					if (warning != null && !warning.isEmpty())
						Errmsg.getErrorHandler().notice(warning);
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}

			}
		});

		m.add(imp);

		JMenuItem impUrl = new JMenuItem();
		impUrl.setText(Resource.getResourceString("ImportUrl"));
		impUrl.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				// prompt for a file
				String urlString = JOptionPane.showInputDialog(null, Resource.getResourceString("enturl"), Prefs.getPref(url_pref));
				if( urlString == null ) return;
				
				Prefs.putPref(url_pref, urlString);
				try {

					String warning = AppointmentIcalAdapter.importIcalFromUrl(urlString);
					if (warning != null && !warning.isEmpty())
						Errmsg.getErrorHandler().notice(warning);
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}

			}
		});

		m.add(impUrl);
		
		JMenuItem exp = new JMenuItem();
		exp.setText(Resource.getResourceString("exportToFile"));
		exp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				export(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS));
			}
		});

		m.add(exp);

		JMenuItem expftp = new JMenuItem();
		expftp.setText(Resource.getResourceString("exportToFTP"));
		expftp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					IcalFTP.exportftp(Prefs.getIntPref(PrefName.ICAL_EXPORTYEARS));
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}
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


		return m;
	}

	@Override
	public void initialize(MultiView parent) {

		OptionsView.getReference().addPanel(new IcalOptionsPanel());

		new FileDrop(parent, new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				for (File f : files) {
					String warning;
					try {
						warning = AppointmentIcalAdapter.importIcalFromFile(
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


	/**
	 * export appts
	 * 
	 * @param years
	 *            - number of years to export or null
	 */
	private static void export(Integer years) {

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
