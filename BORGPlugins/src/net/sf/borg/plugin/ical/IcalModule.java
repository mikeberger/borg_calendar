package net.sf.borg.plugin.ical;

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

import net.sf.borg.common.Errmsg;
import net.sf.borg.model.CategoryModel;
import net.sf.borg.plugin.common.Resource;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.MultiView.Module;
import net.sf.borg.ui.MultiView.ViewType;

public class IcalModule implements Module {

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

					Object o = JOptionPane.showInputDialog(null, Resource
							.getResourceString("import_cat_choose"), "",
							JOptionPane.QUESTION_MESSAGE, null, cats, cats[0]);
					if (o == null)
						return;

					String warning = AppointmentIcalAdapter.importIcal(s, (String) o);
					if (warning != null && !warning.isEmpty())
						Errmsg.getErrorHandler().notice(warning);
				} catch (Exception e) {
					Errmsg.getErrorHandler().errmsg(e);
				}

			}
		});

		m.add(imp);

		JMenuItem exp = new JMenuItem();
		exp.setText(Resource.getResourceString("export"));
		exp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				export(null);
			}
		});

		m.add(exp);

		JMenuItem exp2 = new JMenuItem();
		exp2.setText(Resource.getResourceString("export") + " 2 yrs");
		exp2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				export(new Integer(2));
			}
		});

		m.add(exp2);
		
		JMenuItem exp3 = new JMenuItem();
		exp3.setText(Resource.getResourceString("start_server"));
		exp3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					IcalFileServer.start();
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
			}
		});

		m.add(exp4);

		parent.addPluginSubMenu(m);
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
	private void export(Integer years) {

		// prompt for a file
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		chooser.setDialogTitle(Resource.getResourceString("choose_file"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int returnVal = chooser.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		String s = chooser.getSelectedFile().getAbsolutePath();

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
